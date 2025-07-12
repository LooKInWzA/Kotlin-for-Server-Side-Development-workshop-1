import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.functions.sum
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.YearMonth

// --- 1. Data Models ---

@Serializable
data class Category(val id: Int, val name: String)

enum class TransactionType { INCOME, EXPENSE }

@Serializable
data class Transaction(
    val id: Int,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val date: String, // e.g., "2024-12-31"
    val categoryId: Int
)

@Serializable
data class CategoryExpense(val categoryName: String, val totalAmount: Double)

@Serializable
data class MonthlyReport(
    val year: Int,
    val month: Int,
    val expensesByCategory: List<CategoryExpense>
)

// --- 2. Database Table Schemas ---

object Categories : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}

object Transactions : Table() {
    val id = integer("id").autoIncrement()
    val description = varchar("description", 255)
    val amount = double("amount")
    val type = enumerationByName("type", 10, TransactionType::class)
    val date = varchar("date", 10) // YYYY-MM-DD
    val categoryId = integer("category_id").references(Categories.id)
    override val primaryKey = PrimaryKey(id)
}

// --- 3. Database Factory ---

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Categories, Transactions)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

// --- 4. Main Application ---

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize Database
    DatabaseFactory.init()

    // Install Ktor plugins
    install(ContentNegotiation) {
        json()
    }

    // --- 5. API Routes (Endpoints) ---
    routing {
        // Category Routes
        route("/categories") {
            get {
                val categories = DatabaseFactory.dbQuery {
                    Categories.selectAll().map { Category(it[Categories.id], it[Categories.name]) }
                }
                call.respond(categories)
            }
            post {
                val category = call.receive<Category>()
                val id = DatabaseFactory.dbQuery {
                    Categories.insertAndGetId { it[name] = category.name }
                }
                call.respond(HttpStatusCode.Created, Category(id.value, category.name))
            }
            get("{id?}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val category = DatabaseFactory.dbQuery {
                    Categories.select { Categories.id eq id }.map { Category(it[Categories.id], it[Categories.name]) }.singleOrNull()
                }
                if (category != null) call.respond(category) else call.respond(HttpStatusCode.NotFound)
            }
        }

        // Transaction Routes
        route("/transactions") {
            post {
                val transaction = call.receive<Transaction>()
                val id = DatabaseFactory.dbQuery {
                    Transactions.insertAndGetId {
                        it[description] = transaction.description
                        it[amount] = transaction.amount
                        it[type] = transaction.type
                        it[date] = transaction.date
                        it[categoryId] = transaction.categoryId
                    }
                }
                call.respond(HttpStatusCode.Created, transaction.copy(id = id.value))
            }
            get {
                val transactions = DatabaseFactory.dbQuery {
                    Transactions.selectAll().map {
                        Transaction(
                            id = it[Transactions.id],
                            description = it[Transactions.description],
                            amount = it[Transactions.amount],
                            type = it[Transactions.type],
                            date = it[Transactions.date],
                            categoryId = it[Transactions.categoryId]
                        )
                    }
                }
                call.respond(transactions)
            }
        }

        // Report Routes
        route("/reports") {
            get("/monthly") {
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()

                if (year == null || month == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, "Missing year or month query parameters")
                }

                val yearMonth = YearMonth.of(year, month)
                val startDate = yearMonth.atDay(1).toString()
                val endDate = yearMonth.atEndOfMonth().toString()

                val expenses = DatabaseFactory.dbQuery {
                    (Transactions innerJoin Categories)
                        .slice(Categories.name, Transactions.amount.sum())
                        .select {
                            (Transactions.type eq TransactionType.EXPENSE) and
                                    (Transactions.date greaterEq startDate) and
                                    (Transactions.date lessEq endDate)
                        }
                        .groupBy(Categories.name)
                        .map {
                            CategoryExpense(
                                categoryName = it[Categories.name],
                                totalAmount = it[Transactions.amount.sum()] ?: 0.0
                            )
                        }
                }
                call.respond(MonthlyReport(year, month, expenses))
            }
        }
    }
}