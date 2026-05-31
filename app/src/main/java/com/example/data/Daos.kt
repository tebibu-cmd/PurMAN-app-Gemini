package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Products
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Int, newStock: Int)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    // Sales Orders
    @Query("SELECT * FROM sales_orders ORDER BY timestamp DESC")
    fun getAllSalesOrders(): Flow<List<SalesOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOrder(salesOrder: SalesOrder): Long

    @Update
    suspend fun updateSalesOrder(salesOrder: SalesOrder)

    @Query("UPDATE sales_orders SET status = :status WHERE id = :orderId")
    suspend fun updateSalesOrderStatus(orderId: Int, status: String)

    // Sales Items
    @Query("SELECT * FROM sales_items WHERE orderId = :orderId")
    fun getSalesItemsForOrder(orderId: Int): Flow<List<SalesItem>>

    @Query("SELECT * FROM sales_items")
    fun getAllSalesItems(): Flow<List<SalesItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesItem(item: SalesItem)

    // Purchase Orders
    @Query("SELECT * FROM purchase_orders ORDER BY timestamp DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(purchaseOrder: PurchaseOrder): Long

    @Update
    suspend fun updatePurchaseOrder(purchaseOrder: PurchaseOrder)

    @Query("UPDATE purchase_orders SET status = :status, isApproved = :approved WHERE id = :orderId")
    suspend fun approvePurchaseOrder(orderId: Int, status: String, approved: Boolean)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Staff
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // Support Tickets
    @Query("SELECT * FROM support_tickets ORDER BY id DESC")
    fun getAllSupportTickets(): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicket)

    @Update
    suspend fun updateSupportTicket(ticket: SupportTicket)

    // Announcements & Notice Board
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    // Warehouse Transfers
    @Query("SELECT * FROM warehouse_transfers ORDER BY timestamp DESC")
    fun getAllWarehouseTransfers(): Flow<List<WarehouseTransfer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseTransfer(transfer: WarehouseTransfer)

    @Update
    suspend fun updateWarehouseTransfer(transfer: WarehouseTransfer)
}
