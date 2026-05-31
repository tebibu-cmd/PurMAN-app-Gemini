package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Customer::class,
        Supplier::class,
        SalesOrder::class,
        SalesItem::class,
        PurchaseOrder::class,
        Expense::class,
        Staff::class,
        AuditLog::class,
        SupportTicket::class,
        Announcement::class,
        WarehouseTransfer::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "purman_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDb(database.dao())
                }
            }
        }

        private suspend fun populateDb(dao: AppDao) {
            // Preload premium realistic sample data
            
            // 1. Products
            val sampleProducts = listOf(
                Product(name = "Xenon Quantum Processor v2", sku = "XQN-PRO-V2", category = "Hardware", price = 899.99, costPrice = 450.0, stock = 24, minStockLevel = 5, barcode = "890123456789", batchNumber = "XEN-B3-2026", expiryDate = "N/A", warehouseLocation = "Aisle A1"),
                Product(name = "Eldridge Neon Light-Grid Screen", sku = "ELD-NLG-4K", category = "Displays", price = 450.00, costPrice = 210.0, stock = 4, minStockLevel = 5, barcode = "123456789012", batchNumber = "ELD-H98", expiryDate = "N/A", warehouseLocation = "Aisle B4"), // Low Stock Alert target
                Product(name = "Titanium Heat-Sync Reactor Case", sku = "TIT-HSR-01", category = "Cooling", price = 120.00, costPrice = 55.0, stock = 15, minStockLevel = 2, barcode = "987654321098", batchNumber = "TIT-B1", expiryDate = "N/A", warehouseLocation = "Aisle C2"),
                Product(name = "Superconductive Graphite Paste", sku = "SCG-PST-10", category = "Consumables", price = 24.99, costPrice = 10.0, stock = 130, minStockLevel = 10, barcode = "456789012345", batchNumber = "CGP-0525", expiryDate = "2028-12-01", warehouseLocation = "Cold Room 1"),
                Product(name = "Apex RGB Mechanical Core (Blue)", sku = "APX-MKB-BL", category = "Peripherals", price = 159.00, costPrice = 75.0, stock = 45, minStockLevel = 6, barcode = "345678901234", batchNumber = "APX-2026", expiryDate = "N/A", warehouseLocation = "Aisle D1"),
                Product(name = "Specter Audio-Amplify Soundcard", sku = "SPC-AM-SND", category = "Hardware", price = 299.00, costPrice = 140.0, stock = 2, minStockLevel = 4, barcode = "567890123456", batchNumber = "SPC-A1", expiryDate = "OOR-N/A", warehouseLocation = "Aisle A2") // Low Stock Alert target
            )
            sampleProducts.forEach { dao.insertProduct(it) }

            // 2. Customers
            val sampleCustomers = listOf(
                Customer(name = "Omni Galactic Corp", phone = "+1 (555) 901-4433", email = "procurement@omnigalactic.io", creditLimit = 50000.0, outstandingBalance = 12450.0, loyaltyPoints = 4500),
                Customer(name = "Neo-Tokyo Esport Arena", phone = "+81 3-5555-0143", email = "contact@nt-esports.jp", creditLimit = 25000.0, outstandingBalance = 0.0, loyaltyPoints = 1250),
                Customer(name = "Sarah Connor", phone = "+1 (310) 555-0199", email = "sconnor@resistance.net", creditLimit = 2000.0, outstandingBalance = 450.00, loyaltyPoints = 800),
                Customer(name = "AeroDynamic Tech Labs", phone = "+44 20 7946 0958", email = "inventory@aerodynlab.co.uk", creditLimit = 15000.0, outstandingBalance = 3800.0, loyaltyPoints = 190)
            )
            sampleCustomers.forEach { dao.insertCustomer(it) }

            // 3. Suppliers
            val sampleSuppliers = listOf(
                Supplier(name = "Apex Micro-Core Industries Ltd", contactPerson = "Daisuke Sato", phone = "+81 6-6555-0100", email = "sato@apex-mcore.co.jp", outstandingPayment = 8500.0, ratingString = "A++ Preferred"),
                Supplier(name = "Vanguard Premium Metals LLC", contactPerson = "Marcus Aurelius", phone = "+1 (800) 555-4012", email = "leads@vanguardmetals.com", outstandingPayment = 0.00, ratingString = "A Good"),
                Supplier(name = "Krypton Cooling Solutions Co", contactPerson = "Clark Kent", phone = "+1 (212) 555-0120", email = "clark@kryptoncool.com", outstandingPayment = 1400.00, ratingString = "B Neutral")
            )
            sampleSuppliers.forEach { dao.insertSupplier(it) }

            // 4. Staff
            val sampleStaff = listOf(
                Staff(name = "Alexander Wright", roleStr = "Admin", statusStr = "Active", shiftInfoStr = "All-duty", department = "Global Operations", branch = "London HQ"),
                Staff(name = "Elena Rostova", roleStr = "Manager", statusStr = "Active", shiftInfoStr = "Morning (08:00 - 16:00)", department = "Sales", branch = "London HQ"),
                Staff(name = "Chen Wei", roleStr = "Cashier", statusStr = "Active", shiftInfoStr = "Evening (16:00 - 24:00)", department = "Storefront", branch = "London HQ"),
                Staff(name = "Marcus Finch", roleStr = "Storekeeper", statusStr = "Off-duty", shiftInfoStr = "Night (00:00 - 08:00)", department = "Logistics", branch = "London HQ")
            )
            sampleStaff.forEach { dao.insertStaff(it) }

            // 5. Support Tickets
            val sampleTickets = listOf(
                SupportTicket(customerName = "Omni Galactic Corp", complaint = "Slight outer box scratch on Xenon Quantum Delivery", status = "Pending", priority = "Medium", dateStr = "2026-05-30"),
                SupportTicket(customerName = "Sarah Connor", complaint = "Request expedited invoice backup for tax reference", status = "Resolved", priority = "Low", dateStr = "2026-05-28"),
                SupportTicket(customerName = "Neo-Tokyo Esport Arena", complaint = "Need custom firmware updates for Eldridge light grid", status = "Active", priority = "High", dateStr = "2026-05-31")
            )
            sampleTickets.forEach { dao.insertSupportTicket(it) }

            // 6. Announcements
            val sampleAnnouncements = listOf(
                Announcement(title = "Welcome to PurMAN AI v2.6", content = "System update deployed: Added direct Gemini business insight analysis module. Check 'Intelligence Dashboard'!", dateStr = "2026-05-31", type = "Notice Board"),
                Announcement(title = "Annual Inventory Valuation Completed", content = "Official physical balance reports reconciled in London HQ warehouse on May 28th.", dateStr = "2026-05-28", type = "Email Alerts"),
                Announcement(title = "Automated Accounts Reminder", content = "Outstanding invoices automatically emailed to Omni Galactic Corp and AeroDynamic Tech Labs.", dateStr = "2026-05-25", type = "SMS Alert")
            )
            sampleAnnouncements.forEach { dao.insertAnnouncement(it) }

            // 7. Warehouse Transfers
            val sampleTransfers = listOf(
                WarehouseTransfer(productName = "Titanium Heat-Sync Reactor Case", fromWarehouse = "Main Warehouse", toWarehouse = "Battersea Storage Annex", quantity = 5, timestamp = System.currentTimeMillis() - 86400000, status = "Completed"),
                WarehouseTransfer(productName = "Apex RGB Mechanical Core (Blue)", fromWarehouse = "Main Warehouse", toWarehouse = "West End Branch Store", quantity = 10, timestamp = System.currentTimeMillis(), status = "Pending")
            )
            sampleTransfers.forEach { dao.insertWarehouseTransfer(it) }

            // 8. Expense
            val sampleExpenses = listOf(
                Expense(amount = 250.00, category = "Utilities", description = "High-speed optical fiber connection", timestamp = System.currentTimeMillis() - 172800000, registeredBy = "Elena Rostova"),
                Expense(amount = 1400.00, category = "Logistics", description = "Forklift repair and hydraulic fluid refill", timestamp = System.currentTimeMillis() - 86400000, registeredBy = "Alexander Wright")
            )
            sampleExpenses.forEach { dao.insertExpense(it) }

            // 9. Initial Sales Order
            val oId = dao.insertSalesOrder(
                SalesOrder(
                    customerName = "Omni Galactic Corp",
                    totalAmount = 2699.97,
                    discountAmount = 100.00,
                    netAmount = 2599.97,
                    paymentMethod = "Bank Wire",
                    branchName = "London HQ",
                    timestamp = System.currentTimeMillis() - 3600000 * 4,
                    status = "Invoiced",
                    loyaltyPointsEarned = 260
                )
            )
            dao.insertSalesItem(SalesItem(orderId = oId.toInt(), productName = "Xenon Quantum Processor v2", quantity = 3, price = 899.99))

            // 10. Initial Purchase Order
            dao.insertPurchaseOrder(
                PurchaseOrder(
                    supplierName = "Apex Micro-Core Industries Ltd",
                    totalAmount = 5400.00,
                    timestamp = System.currentTimeMillis() - 3600000 * 24,
                    status = "Approved",
                    isApproved = true,
                    details = "12x Xenon Quantum Processor v2 components"
                )
            )

            // 11. Initial Audit Log
            dao.insertAuditLog(
                AuditLog(
                    username = "Alexander Wright",
                    roleStr = "Admin",
                    actionStr = "DB_SETUP_POPULATE",
                    timestampStr = "2026-05-31 08:44",
                    details = "Setup complete. Loaded 6 products, 4 customers, 3 suppliers, and operational sample database records."
                )
            )
        }
    }
}
