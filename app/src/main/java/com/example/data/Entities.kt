package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String,
    val category: String,
    val price: Double,
    val costPrice: Double,
    val stock: Int,
    val minStockLevel: Int,
    val barcode: String,
    val batchNumber: String = "",
    val expiryDate: String = "N/A",
    val warehouseLocation: String = "Main Warehouse"
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val creditLimit: Double,
    val outstandingBalance: Double,
    val loyaltyPoints: Int
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val outstandingPayment: Double,
    val ratingString: String = "A+" // For Supplier Performance Evaluation
)

@Entity(tableName = "sales_orders")
data class SalesOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val totalAmount: Double,
    val discountAmount: Double,
    val netAmount: Double,
    val paymentMethod: String,
    val branchName: String,
    val timestamp: Long,
    val status: String, // e.g. "Draft", "Confirmed", "Invoiced", "Returned"
    val loyaltyPointsEarned: Int
)

@Entity(tableName = "sales_items")
data class SalesItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val productName: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val supplierName: String,
    val totalAmount: Double,
    val timestamp: Long,
    val status: String, // e.g., "Pending Requisition", "Approved", "Goods Received", "Paid", "Returned"
    val isApproved: Boolean = false,
    val details: String = "" // Summary of items
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val timestamp: Long,
    val registeredBy: String
)

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val roleStr: String, // Admin / Manager / Cashier / Storekeeper
    val statusStr: String, // Active / Off-duty
    val shiftInfoStr: String, // morning / evening
    val department: String,
    val branch: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val roleStr: String,
    val actionStr: String,
    val timestampStr: String,
    val details: String
)

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val complaint: String,
    val status: String, // Pending, Resolved, Active
    val priority: String, // High, Medium, Low
    val dateStr: String
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val dateStr: String,
    val type: String // SMS, Email, Notice Board
)

@Entity(tableName = "warehouse_transfers")
data class WarehouseTransfer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val fromWarehouse: String,
    val toWarehouse: String,
    val quantity: Int,
    val timestamp: Long,
    val status: String // Pending, Completed
)
