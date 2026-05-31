package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Moshi Data Classes for Gemini REST ---
data class GeminiPart(
    val text: String? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

object GeminiService {
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Call the actual Gemini 3.5 Flash REST API to analyze sales data.
     */
    suspend fun analyzeBusinessState(
        productCount: Int,
        lowStockItems: String,
        salesCount: Int,
        totalSales: Double,
        purchaseCount: Int,
        totalPurchases: Double,
        totalExpenses: Double,
        ticketSummary: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Graceful Check: if safety placeholder or blank
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineMockInsight(
                productCount, lowStockItems, salesCount, totalSales, purchaseCount, totalPurchases, totalExpenses, ticketSummary
            )
        }

        val prompt = """
            You are "PurMAN AI Audit Engine", an expert AI controller for a company's ERP (PurMAN AI Sales and Purchase System).
            Analyze these current inventory, finance, and CRM aggregates:
            - Total Products Tracked: $productCount
            - Low Stock Products Alert: [$lowStockItems]
            - Total Sales Transactions: $salesCount (Value: $${String.format("%.2f", totalSales)})
            - Total Purchase Procurements: $purchaseCount (Value: $${String.format("%.2f", totalPurchases)})
            - Operational Expenses Total: $${String.format("%.2f", totalExpenses)}
            - CRM Pending Support Tickets: $ticketSummary
            
            Give a premium, highly actionable business analysis. Format with these bulleted headings:
            📊 REVENUE VS COST ANALYSIS
            📦 INVENTORY & EXPEDITING ACTIONS
            👥 CUSTOMER & CRM MITIGATION
            💡 PROACTIVE STRATEGIC RECOMMENDATIONS
            
            Keep your text concise, highly executive, professional, and do not use flowery fluff. Direct and sharp.
        """.trimIndent()

        val reqObj = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )

        val jsonRequest = requestAdapter.toJson(reqObj)
        val url = "$BASE_URL$MODEL:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: Gemini REST failure (${response.code}). Falling back to local intelligence.\n\n" +
                            getOfflineMockInsight(productCount, lowStockItems, salesCount, totalSales, purchaseCount, totalPurchases, totalExpenses, ticketSummary)
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body from Gemini."
                val geminiResp = responseAdapter.fromJson(responseBody)
                val responseText = geminiResp?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                responseText ?: "Error: Did not receive structured response text from Gemini."
            }
        } catch (e: IOException) {
            "Error: Network timeout or connection issues (${e.message}). Falling back to local offline insights:\n\n" +
                    getOfflineMockInsight(productCount, lowStockItems, salesCount, totalSales, purchaseCount, totalPurchases, totalExpenses, ticketSummary)
        }
    }

    /**
     * Local fallback to generate high-fidelity, customized report when no API key is provided
     */
    private fun getOfflineMockInsight(
        productCount: Int,
        lowStockItems: String,
        salesCount: Int,
        totalSales: Double,
        purchaseCount: Int,
        totalPurchases: Double,
        totalExpenses: Double,
        ticketSummary: String
    ): String {
        val profitMargin = totalSales - totalPurchases - totalExpenses
        val healthyStockAlert = if (lowStockItems.length > 5) "REORDER MANDATED: [$lowStockItems] is critical!" else "Stock levels look stable across active SKU lines."
        val ticketNote = if (ticketSummary.contains("No pending")) "Outstanding CRM tickets are clear." else "Careful: Customers are waiting for technical support."

        return """
            [DEMO MODE: PurMAN AI Offline Insight Suite]
            
            📊 REVENUE VS COST ANALYSIS
            • Gross Sales Revenue: $${String.format("%.2f", totalSales)} collected across $salesCount order fulfillments.
            • Trade Purchases & Logistics Costs: $${String.format("%.2f", totalPurchases + totalExpenses)} (Purchasing: $${String.format("%.2f", totalPurchases)}, Expenses: $${String.format("%.2f", totalExpenses)}).
            • Operating Profit/Loss: $${String.format("%.2f", profitMargin)} (${if (profitMargin >= 0) "SURPLUS" else "DEFICIT"}).
            
            📦 INVENTORY & EXPEDITING ACTIONS
            • $productCount active product lines tracked across main and annex warehouses.
            • $healthyStockAlert
            • Multi-branch balancing needed to offset regional stock-out risks.
            
            👥 CUSTOMER & CRM MITIGATION
            • $ticketNote
            • Recommendation: Implement high-priority ticketing triage during the current shift.
            
            💡 PROACTIVE STRATEGIC RECOMMENDATIONS
            • Negotiate 2% cash discount with Preferred Suppliers using our good credit evaluation.
            • Minimize high storage fees by liquidating overstocked items.
            • Setup automatic SMS reminders in CRM Settings to expedite accounts receivables.
        """.trimIndent()
    }
}
