package com.example.data

import kotlinx.coroutines.flow.Flow

class PurManRepository(private val dao: AppDao) {

    // Products
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    suspend fun insertProduct(product: Product) = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)
    suspend fun updateStock(productId: Int, newStock: Int) = dao.updateStock(productId, newStock)

    // Customers
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    suspend fun insertCustomer(customer: Customer) = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)

    // Suppliers
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliers()
    suspend fun insertSupplier(supplier: Supplier) = dao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = dao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = dao.deleteSupplier(supplier)

    // Sales Orders
    val allSalesOrders: Flow<List<SalesOrder>> = dao.getAllSalesOrders()
    suspend fun insertSalesOrder(salesOrder: SalesOrder): Long = dao.insertSalesOrder(salesOrder)
    suspend fun updateSalesOrder(salesOrder: SalesOrder) = dao.updateSalesOrder(salesOrder)
    suspend fun updateSalesOrderStatus(orderId: Int, status: String) = dao.updateSalesOrderStatus(orderId, status)

    // Sales Items
    val allSalesItems: Flow<List<SalesItem>> = dao.getAllSalesItems()
    fun getSalesItemsForOrder(orderId: Int): Flow<List<SalesItem>> = dao.getSalesItemsForOrder(orderId)
    suspend fun insertSalesItem(item: SalesItem) = dao.insertSalesItem(item)

    // Purchase Orders
    val allPurchaseOrders: Flow<List<PurchaseOrder>> = dao.getAllPurchaseOrders()
    suspend fun insertPurchaseOrder(purchaseOrder: PurchaseOrder): Long = dao.insertPurchaseOrder(purchaseOrder)
    suspend fun updatePurchaseOrder(purchaseOrder: PurchaseOrder) = dao.updatePurchaseOrder(purchaseOrder)
    suspend fun approvePurchaseOrder(orderId: Int, status: String, approved: Boolean) = dao.approvePurchaseOrder(orderId, status, approved)

    // Expenses
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    suspend fun insertExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    // Staff
    val allStaff: Flow<List<Staff>> = dao.getAllStaff()
    suspend fun insertStaff(staff: Staff) = dao.insertStaff(staff)
    suspend fun deleteStaff(staff: Staff) = dao.deleteStaff(staff)

    // Audit Logs
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()
    suspend fun insertAuditLog(log: AuditLog) = dao.insertAuditLog(log)

    // Support Tickets
    val allSupportTickets: Flow<List<SupportTicket>> = dao.getAllSupportTickets()
    suspend fun insertSupportTicket(ticket: SupportTicket) = dao.insertSupportTicket(ticket)
    suspend fun updateSupportTicket(ticket: SupportTicket) = dao.updateSupportTicket(ticket)

    // Announcements
    val allAnnouncements: Flow<List<Announcement>> = dao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: Announcement) = dao.insertAnnouncement(announcement)

    // Transfers
    val allWarehouseTransfers: Flow<List<WarehouseTransfer>> = dao.getAllWarehouseTransfers()
    suspend fun insertWarehouseTransfer(transfer: WarehouseTransfer) = dao.insertWarehouseTransfer(transfer)
    suspend fun updateWarehouseTransfer(transfer: WarehouseTransfer) = dao.updateWarehouseTransfer(transfer)
}
