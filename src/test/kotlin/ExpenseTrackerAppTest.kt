import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class ExpenseTrackerAppTest {

    @Test
    fun testExpenseReportWorkflow() = testApplication {
        // ตั้งค่าให้ test module ของเราคือ module หลักของแอป
        application {
            module()
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        // 1. สร้าง Category
        val categoryResponse = client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody("""{"id":0, "name":"Food"}""")
        }
        assertEquals(HttpStatusCode.Created, categoryResponse.status)
        val createdCategory = Json.decodeFromString<Category>(categoryResponse.bodyAsText())

        // 2. สร้าง Transaction
        val transactionResponse = client.post("/transactions") {
            contentType(ContentType.Application.Json)
            setBody("""
            {
                "id": 0,
                "description": "Lunch",
                "amount": 15.50,
                "type": "EXPENSE",
                "date": "2024-12-10",
                "categoryId": ${createdCategory.id}
            }
            """)
        }
        assertEquals(HttpStatusCode.Created, transactionResponse.status)

        // 3. ขอ Report
        val reportResponse = client.get("/reports/monthly?year=2024&month=12")
        assertEquals(HttpStatusCode.OK, reportResponse.status)
        val report = Json.decodeFromString<MonthlyReport>(reportResponse.bodyAsText())

        // 4. ตรวจสอบความถูกต้องของ Report
        assertEquals(1, report.expensesByCategory.size)
        assertEquals("Food", report.expensesByCategory.first().categoryName)
        assertEquals(15.50, report.expensesByCategory.first().totalAmount)
    }
}