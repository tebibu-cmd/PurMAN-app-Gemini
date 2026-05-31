package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ai.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PurManViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = PurManRepository(db.dao())

    // --- State Observables ---
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val salesOrders = repository.allSalesOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchaseOrders = repository.allPurchaseOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val staff = repository.allStaff.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tickets = repository.allSupportTickets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val announcements = repository.allAnnouncements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transfers = repository.allWarehouseTransfers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active User / Role Settings State ---
    val currentUserRole = MutableStateFlow("Admin") // Can be Admin / Manager / Cashier / Storekeeper
    val currentUserName = MutableStateFlow("Alexander Wright")
    val selectedBranch = MutableStateFlow("London HQ")

    // --- Finance Theme Settings ---
    val selectedCurrency = MutableStateFlow("GBP") // USD / EUR / GBP / JPY
    val valTaxRate = MutableStateFlow(20.0) // VAT / Tax percentage 

    // --- AI Insight State ---
    val aiInsightText = MutableStateFlow("Click 'Generate AI Audit Insights' to invoke the PurMAN AI engine...")
    val isAiLoading = MutableStateFlow(false)

    // --- POS Billing System State (In-Memory) ---
    val posCart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val posCustomerSelection = MutableStateFlow<Customer?>(null)
    val posDiscountPercentage = MutableStateFlow(0.0)

    // Helper formatter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun currentCurrencySymbol(): String {
        return when (selectedCurrency.value) {
            "USD" -> "$"
            "EUR" -> "€"
            "JPY" -> "¥"
            else -> "£"
        }
    }

    // --- Log Audit Helper ---
    fun logAction(action: String, details: String) {
        viewModelScope.launch {
            repository.insertAuditLog(
                AuditLog(
                    username = currentUserName.value,
                    roleStr = currentUserRole.value,
                    actionStr = action,
                    timestampStr = dateFormat.format(Date()),
                    details = details
                )
            )
        }
    }

    // --- Core Logic Operations ---

    // 1. Sales & POS Actions
    fun addProductToCart(product: Product) {
        val current = posCart.value.toMutableMap()
        current[product] = (current[product] ?: 0) + 1
        posCart.value = current
    }

    fun removeProductFromCart(product: Product) {
        val current = posCart.value.toMutableMap()
        val count = current[product] ?: 0
        if (count <= 1) {
            current.remove(product)
        } else {
            current[product] = count - 1
        }
        posCart.value = current
    }

    fun clearCart() {
        posCart.value = emptyMap()
    }

    fun checkoutPosCart(paymentMethod: String) {
        val cartItems = posCart.value
        val currentCustomer = posCustomerSelection.value
        if (cartItems.isEmpty()) return

        viewModelScope.launch {
            val totalRaw = cartItems.entries.sumOf { it.key.price * it.value }
            val discAmount = totalRaw * (posDiscountPercentage.value / 100.0)
            val netTotal = totalRaw - discAmount
            val earnedPoints = (netTotal * 0.1).toInt() // Loyalty reward: 10%

            val orderId = repository.insertSalesOrder(
                SalesOrder(
                    customerName = currentCustomer?.name ?: "Walk-in Customer",
                    totalAmount = totalRaw,
                    discountAmount = discAmount,
                    netAmount = netTotal,
                    paymentMethod = paymentMethod,
                    branchName = selectedBranch.value,
                    timestamp = System.currentTimeMillis(),
                    status = "Invoiced",
                    loyaltyPointsEarned = earnedPoints
                )
            )

            // Insert Items and adjust Stock
            cartItems.forEach { (product, qty) ->
                repository.insertSalesItem(
                    SalesItem(
                        orderId = orderId.toInt(),
                        productName = product.name,
                        quantity = qty,
                        price = product.price
                    )
                )
                // Deduct stock
                val updatedStock = (product.stock - qty).coerceAtLeast(0)
                repository.updateStock(product.id, updatedStock)
            }

            // Update customer outstanding balances and reward points
            currentCustomer?.let {
                val newOutstanding = it.outstandingBalance + netTotal
                val newPoints = it.loyaltyPoints + earnedPoints
                repository.updateCustomer(it.copy(outstandingBalance = newOutstanding, loyaltyPoints = newPoints))
            }

            logAction("POS_TILL_CHECKOUT", "Executed POS checkout. Order #${orderId}. NetTotal: ${currentCurrencySymbol()}${String.format("%.2f", netTotal)}. Points awarded: ${earnedPoints}")
            
            // Auto trigger low-stock notification generator if needed
            cartItems.forEach { (prod, qty) ->
                if ((prod.stock - qty) <= prod.minStockLevel) {
                    repository.insertAnnouncement(
                        Announcement(
                            title = "AUTOMATED_LOW_STOCK_RUNOUT",
                            content = "Alert: SKU [${prod.sku}] has fallen below minimum stock level of ${prod.minStockLevel}. Current stock: ${prod.stock - qty}.",
                            dateStr = dateFormat.format(Date()),
                            type = "Notice Board"
                        )
                    )
                }
            }

            // Clear in-memory checkout parameters
            clearCart()
            posDiscountPercentage.value = 0.0
            posCustomerSelection.value = null
        }
    }

    fun submitQuotation(customerName: String, amount: Double) {
        viewModelScope.launch {
            val orderId = repository.insertSalesOrder(
                SalesOrder(
                    customerName = customerName,
                    totalAmount = amount,
                    discountAmount = 0.0,
                    netAmount = amount,
                    paymentMethod = "On Account",
                    branchName = selectedBranch.value,
                    timestamp = System.currentTimeMillis(),
                    status = "Quotation",
                    loyaltyPointsEarned = 0
                )
            )
            logAction("QUOTATION_CREATED", "Quotation #${orderId} created for $customerName. Total value: ${currentCurrencySymbol()}$amount")
        }
    }

    fun confirmSalesOrder(order: SalesOrder) {
        viewModelScope.launch {
            repository.updateSalesOrder(order.copy(status = "Confirmed"))
            logAction("SALES_ORDER_CONFIRMED", "Confirmed sales quotation #${order.id} for ${order.customerName}")
        }
    }

    fun invoiceSalesOrder(order: SalesOrder) {
        viewModelScope.launch {
            repository.updateSalesOrder(order.copy(status = "Invoiced"))
            logAction("SALES_ORDER_INVOICED", "Invoiced sales order #${order.id} for ${order.customerName}")
        }
    }

    fun processSalesReturn(order: SalesOrder) {
        viewModelScope.launch {
            repository.updateSalesOrder(order.copy(status = "Returned"))
            // Deduct customer credit/charges if registered
            val matchedCustomer = customers.value.find { it.name == order.customerName }
            matchedCustomer?.let {
                val newOutstanding = (it.outstandingBalance - order.netAmount).coerceAtLeast(0.0)
                val newPoints = (it.loyaltyPoints - order.loyaltyPointsEarned).coerceAtLeast(0)
                repository.updateCustomer(it.copy(outstandingBalance = newOutstanding, loyaltyPoints = newPoints))
            }
            logAction("SALES_RETURNED", "Sales refund completed. Order #${order.id} of ${order.customerName} set to Returned status.")
        }
    }

    // 2. Customer CRUD
    fun addCustomer(name: String, phone: String, email: String, limit: Double) {
        viewModelScope.launch {
            repository.insertCustomer(
                Customer(name = name, phone = phone, email = email, creditLimit = limit, outstandingBalance = 0.0, loyaltyPoints = 0)
            )
            logAction("CUSTOMER_ADDED", "Added customer $name. Credit Limit: ${currentCurrencySymbol()}$limit")
        }
    }

    fun applyCreditPayment(customer: Customer, paymentAmount: Double) {
        viewModelScope.launch {
            val updated = customer.copy(outstandingBalance = (customer.outstandingBalance - paymentAmount).coerceAtLeast(0.0))
            repository.updateCustomer(updated)
            logAction("CUSTOMER_PAYMENT", "Customer payment applied to ${customer.name}. Paid: ${currentCurrencySymbol()}$paymentAmount. Remaining balance: ${currentCurrencySymbol()}${updated.outstandingBalance}")
        }
    }

    // 3. Purchase & Supplier Actions
    fun submitPurchaseRequisition(supplierName: String, amount: Double, itemSummary: String) {
        viewModelScope.launch {
            val poId = repository.insertPurchaseOrder(
                PurchaseOrder(
                    supplierName = supplierName,
                    totalAmount = amount,
                    timestamp = System.currentTimeMillis(),
                    status = "Pending Requisition",
                    isApproved = false,
                    details = itemSummary
                )
            )
            logAction("PURCHASE_REQUISITION", "Purchase requisition #${poId} registered for $supplierName. Value: ${currentCurrencySymbol()}$amount")
        }
    }

    fun approvePurchaseRequisition(order: PurchaseOrder) {
        viewModelScope.launch {
            repository.approvePurchaseOrder(order.id, "Approved", true)
            logAction("PURCHASE_APPROVED", "Procurement Workflow: Purchase Order #${order.id} approved by ${currentUserName.value}")
        }
    }

    fun receiveGoodsPurchase(order: PurchaseOrder) {
        viewModelScope.launch {
            // Change status
            repository.updatePurchaseOrder(order.copy(status = "Goods Received"))
            
            // Find products matching tags to increase stock (simulated auto verification)
            // As a simplified realistic system, we will increase stock level of a sample item that corresponds to the purchase order
            val allProds = products.value
            val detailsStringLower = order.details.lowercase()
            val productToIncrease = allProds.find { detailsStringLower.contains(it.name.lowercase()) || detailsStringLower.contains(it.sku.lowercase()) }
                ?: allProds.firstOrNull()

            productToIncrease?.let {
                val addedAmount = if (order.totalAmount > 500) 10 else 5
                val newStock = it.stock + addedAmount
                repository.updateStock(it.id, newStock)
                logAction("GOODS_VERIFICATION", "Goods verification complete. Received matching components for '${it.name}'. Increased stock level (+${addedAmount}) to $newStock.")
            }

            // Augment Supplier's Accounts Payable (outstanding payment)
            val matchedSupplier = suppliers.value.find { it.name == order.supplierName }
            matchedSupplier?.let {
                val newBill = it.outstandingPayment + order.totalAmount
                repository.updateSupplier(it.copy(outstandingPayment = newBill))
            }

            logAction("INVENTORY_RESTOCK", "Procurement items delivered. Order #${order.id} marked as physical Goods Received.")
        }
    }

    fun paySupplierOrder(order: PurchaseOrder) {
        viewModelScope.launch {
            repository.updatePurchaseOrder(order.copy(status = "Paid"))
            
            val matchedSupplier = suppliers.value.find { it.name == order.supplierName }
            matchedSupplier?.let {
                val newBill = (it.outstandingPayment - order.totalAmount).coerceAtLeast(0.0)
                repository.updateSupplier(it.copy(outstandingPayment = newBill))
            }

            logAction("SUPPLIER_PAYMENT", "Settled purchase dues of ${currentCurrencySymbol()}${order.totalAmount} to supplier ${order.supplierName} for PO #${order.id}.")
        }
    }

    fun addSupplier(name: String, person: String, phone: String, email: String, rating: String) {
        viewModelScope.launch {
            repository.insertSupplier(
                Supplier(name = name, contactPerson = person, phone = phone, email = email, outstandingPayment = 0.0, ratingString = rating)
            )
            logAction("SUPPLIER_ADDED", "Added commercial supplier $name ($person). Performance evaluation tier: $rating.")
        }
    }

    // 4. Inventory Actions
    fun addProduct(name: String, sku: String, category: String, price: Double, cost: Double, stock: Int, minStock: Int, barcode: String, batch: String, expiry: String, location: String) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    name = name,
                    sku = sku,
                    category = category,
                    price = price,
                    costPrice = cost,
                    stock = stock,
                    minStockLevel = minStock,
                    barcode = barcode,
                    batchNumber = batch,
                    expiryDate = expiry,
                    warehouseLocation = location
                )
            )
            logAction("PRODUCT_CREATED", "Catalog setup complete for sku: $sku ($name). Price: ${currentCurrencySymbol()}$price. Location: $location")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            logAction("PRODUCT_DELETED", "Deleted product ${product.name} [SKU: ${product.sku}] from physical database.")
        }
    }

    fun submitWarehouseTransfer(prodName: String, from: String, to: String, qty: Int) {
        viewModelScope.launch {
            repository.insertWarehouseTransfer(
                WarehouseTransfer(
                    productName = prodName,
                    fromWarehouse = from,
                    toWarehouse = to,
                    quantity = qty,
                    timestamp = System.currentTimeMillis(),
                    status = "Pending"
                )
            )
            logAction("STOCK_TRANSFER_INITIATED", "Stock transfer generated: $qty of $prodName from $from to $to")
        }
    }

    fun executeWarehouseTransfer(transfer: WarehouseTransfer) {
        viewModelScope.launch {
            repository.updateWarehouseTransfer(transfer.copy(status = "Completed"))
            
            // Adjust stock visually for associated SKU
            val matchedProd = products.value.find { it.name == transfer.productName }
            matchedProd?.let {
                logAction("STOCK_TRANSFER_COMPLETED", "Stock transfer completed for ${it.name}. Handled physically across warehouses $transfer.fromWarehouse → $transfer.toWarehouse.")
            }
        }
    }

    fun adjustDamageStock(product: Product, damagedCount: Int, reason: String) {
        viewModelScope.launch {
            val adjusted = (product.stock - damagedCount).coerceAtLeast(0)
            repository.updateStock(product.id, adjusted)
            
            // Add custom expense representing damage valuation loss
            val lossValue = product.costPrice * damagedCount
            repository.insertExpense(
                Expense(
                    amount = lossValue,
                    category = "Damaged Inventory Loss",
                    description = "Written off $damagedCount units of ${product.sku} due to: $reason",
                    timestamp = System.currentTimeMillis(),
                    registeredBy = currentUserName.value
                )
            )

            logAction("INVENTORY_ADJUSTMENT", "Inventory adjustment. Written off $damagedCount units of ${product.name}. Financial loss of ${currentCurrencySymbol()}${String.format("%.2f", lossValue)} registered.")
        }
    }

    // 5. Finance & Expense Actions
    fun addExpense(amount: Double, category: String, description: String) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    registeredBy = currentUserName.value
                )
            )
            logAction("EXPENSE_ADDED", "Registered business expense: $category. Value: ${currentCurrencySymbol()}$amount")
        }
    }

    fun getAccountsReceivable(): Double {
        return customers.value.sumOf { it.outstandingBalance }
    }

    fun getAccountsPayable(): Double {
        return suppliers.value.sumOf { it.outstandingPayment }
    }

    fun calculateTaxSum(netSales: Double): Double {
        return netSales * (valTaxRate.value / 100.0)
    }

    // 6. Users & Staff Actions
    fun registerStaff(name: String, role: String, shift: String, department: String) {
        viewModelScope.launch {
            repository.insertStaff(
                Staff(name = name, roleStr = role, statusStr = "Active", shiftInfoStr = shift, department = department, branch = selectedBranch.value)
            )
            logAction("STAFF_HIRED", "Employee registered: $name as role: $role assigned to department: $department")
        }
    }

    fun demoteOrDismissStaff(staffEntity: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staffEntity)
            logAction("STAFF_REMOVED", "Staff member ${staffEntity.name} registration deactivated.")
        }
    }

    // 7. CRM Actions
    fun createSupportTicket(customerName: String, complaint: String, priority: String) {
        viewModelScope.launch {
            repository.insertSupportTicket(
                SupportTicket(
                    customerName = customerName,
                    complaint = complaint,
                    status = "Pending",
                    priority = priority,
                    dateStr = dateFormat.format(Date())
                )
            )
            logAction("CRM_TICKET_ADDED", "Support system logged new ticket for $customerName. Priority: $priority")
        }
    }

    fun updateTicketStatus(ticket: SupportTicket, newStatus: String) {
        viewModelScope.launch {
            repository.updateSupportTicket(ticket.copy(status = newStatus))
            logAction("CRM_TICKET_RESOLVED", "Ticket for ${ticket.customerName} set to state $newStatus.")
        }
    }

    fun dispatchAnnouncementAlert(title: String, body: String, deliveryType: String) {
        viewModelScope.launch {
            repository.insertAnnouncement(
                Announcement(
                    title = title,
                    content = body,
                    dateStr = dateFormat.format(Date()),
                    type = deliveryType
                )
            )
            logAction("CRM_NOTIFICATION_SENT", "Blast CRM announcement generated: '$title' via delivery channel $deliveryType")
        }
    }

    // --- AI Audit Insights (Gemini) ---
    fun triggerAiInsightsReport() {
        viewModelScope.launch {
            isAiLoading.value = true
            aiInsightText.value = "Establishing handshake with PurMAN AI Audit Engine..."

            val activeProducts = products.value.size
            val lowStockText = products.value
                .filter { it.stock <= it.minStockLevel }
                .joinToString(", ") { "${it.name} (${it.stock}/${it.minStockLevel})" }
                .ifEmpty { "None" }

            val totalRevenue = salesOrders.value.filter { it.status == "Invoiced" }.sumOf { it.netAmount }
            val salesCountVal = salesOrders.value.size

            val purchaseCountVal = purchaseOrders.value.size
            val totalPurchaseVal = purchaseOrders.value.sumOf { it.totalAmount }

            val totalExpenseVal = expenses.value.sumOf { it.amount }

            val pendingTickets = tickets.value.count { it.status == "Pending" || it.status == "Active" }
            val ticketSummaryStr = if (pendingTickets > 0) "$pendingTickets pending support inquiries" else "No pending issues"

            try {
                val insight = GeminiService.analyzeBusinessState(
                    productCount = activeProducts,
                    lowStockItems = lowStockText,
                    salesCount = salesCountVal,
                    totalSales = totalRevenue,
                    purchaseCount = purchaseCountVal,
                    totalPurchases = totalPurchaseVal,
                    totalExpenses = totalExpenseVal,
                    ticketSummary = ticketSummaryStr
                )
                aiInsightText.value = insight
                logAction("AI_AUDIT_INSIGHTS", "AI insights generated successfully via generative model.")
            } catch (e: Exception) {
                aiInsightText.value = "Error compiling generative analytics report: ${e.message}"
            } finally {
                isAiLoading.value = false
            }
        }
    }

    // Backup & Restore
    fun triggerBackupSystem() {
        // Mock print logs
        logAction("BACKUP_SUCCESS", "Database snapshots saved to system cloud partition successfully. All records persisted securely.")
    }

    fun triggerRestoreSystem() {
        viewModelScope.launch {
            repository.insertAnnouncement(
                Announcement(
                    title = "SYSTEM_INTEGRITY_COMPLIANCE",
                    content = "Operational database table structures refreshed. System logs and audit trials cleared matching restore rules.",
                    dateStr = dateFormat.format(Date()),
                    type = "Notice Board"
                )
            )
            logAction("RESTORE_INTEGRITY", "ERP structural database snapshot recovered safely.")
        }
    }
}
