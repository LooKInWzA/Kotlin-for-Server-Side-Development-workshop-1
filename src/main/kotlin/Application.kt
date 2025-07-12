package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

//================================================================
// 1. ENTRY POINT & SERVER CONFIGURATION
//================================================================

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // ติดตั้ง Plugin สำหรับแปลง JSON อัตโนมัติ
    install(ContentNegotiation) {
        json()
    }
    // เรียกใช้การตั้งค่า Routing ทั้งหมด
    configureRouting()
}

//================================================================
// 2. DATA MODELS (สำหรับทั้งสองโปรเจกต์)
//================================================================

// --- Blog Models ---
@Serializable
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: String = LocalDateTime.now().toString(),
    var updatedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class Comment(
    val id: Int,
    val postId: Int,
    val authorName: String,
    val content: String,
    val createdAt: String = LocalDateTime.now().toString()
)

@Serializable
data class NewPost(val title: String, val content: String)

@Serializable
data class NewComment(val authorName: String, val content: String)


// --- Expense Tracker Models ---
@Serializable
enum class TransactionType { INCOME, EXPENSE }

@Serializable
data class Category(val id: Int, val name: String)

@Serializable
data class Transaction(
    val id: Int,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val date: String = LocalDate.now().toString(),
    val categoryId: Int
)

@Serializable
data class NewTransaction(
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Int
)

//================================================================
// 3. REPOSITORIES (In-Memory Database)
//================================================================

// --- Blog Repositories ---
object PostRepository {
    private val posts = mutableListOf<Post>()
    private var lastId = 0

    fun create(newPost: NewPost): Post {
        val post = Post(id = ++lastId, title = newPost.title, content = newPost.content)
        posts.add(post)
        return post
    }
    fun findAll(): List<Post> = posts
    fun findById(id: Int): Post? = posts.find { it.id == id }
    // (สามารถเพิ่มเมธอด update และ delete ได้)
}

object CommentRepository {
    private val comments = mutableListOf<Comment>()
    private var lastId = 0

    fun create(postId: Int, newComment: NewComment): Comment {
        val comment = Comment(id = ++lastId, postId = postId, authorName = newComment.authorName, content = newComment.content)
        comments.add(comment)
        return comment
    }
    fun findByPostId(postId: Int): List<Comment> = comments.filter { it.postId == postId }
}

// --- Expense Tracker Repositories ---
object CategoryRepository {
    private val categories = mutableListOf(
        Category(1, "อาหาร"), Category(2, "เดินทาง"), Category(3, "เงินเดือน"), Category(4, "บันเทิง")
    )
    fun findAll(): List<Category> = categories
    fun findById(id: Int): Category? = categories.find { it.id == id }
    // (สามารถเพิ่มเมธอด create, update, delete ได้)
}

object TransactionRepository {
    private val transactions = mutableListOf<Transaction>()
    private var lastId = 0
    init {
        // เพิ่มข้อมูลตัวอย่าง
        create(NewTransaction("มื้อกลางวัน", 80.0, TransactionType.EXPENSE, 1))
        create(NewTransaction("ค่า BTS", 45.0, TransactionType.EXPENSE, 2))
        create(NewTransaction("เงินเดือนเข้า", 30000.0, TransactionType.INCOME, 3))
    }

    fun create(newTransaction: NewTransaction): Transaction {
        val transaction = Transaction(id = ++lastId, description = newTransaction.description, amount = newTransaction.amount, type = newTransaction.type, categoryId = newTransaction.categoryId)
        transactions.add(transaction)
        return transaction
    }
    fun findAll(): List<Transaction> = transactions
    fun findByMonth(year: Int, month: Int): List<Transaction> {
        return transactions.filter {
            val date = LocalDate.parse(it.date)
            date.year == year && date.monthValue == month
        }
    }
}


//================================================================
// 4. API ROUTING
//================================================================

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to Ktor APIs! Try /blog/posts or /expenses/transactions")
        }

        // --- Personal Blog API Routes ---
        route("/blog") {
            // CRUD สำหรับ Posts
            route("/posts") {
                get {
                    call.respond(PostRepository.findAll())
                }
                post {
                    val newPost = call.receive<NewPost>()
                    val createdPost = PostRepository.create(newPost)
                    call.respond(HttpStatusCode.Created, createdPost)
                }
                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    val post = PostRepository.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                    val comments = CommentRepository.findByPostId(id)
                    call.respond(mapOf("post" to post, "comments" to comments))
                }
            }
            // Endpoint สำหรับเพิ่ม Comment
            post("/posts/{postId}/comments") {
                val postId = call.parameters["postId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid Post ID")
                if (PostRepository.findById(postId) == null) return@post call.respond(HttpStatusCode.NotFound, "Post not found")

                val newComment = call.receive<NewComment>()
                val createdComment = CommentRepository.create(postId, newComment)
                call.respond(HttpStatusCode.Created, createdComment)
            }
        }

        // --- Expense Tracker API Routes ---
        route("/expenses") {
            // CRUD สำหรับ Categories
            route("/categories") {
                get {
                    call.respond(CategoryRepository.findAll())
                }
            }
            // CRUD สำหรับ Transactions
            route("/transactions") {
                get {
                    call.respond(TransactionRepository.findAll())
                }
                post {
                    val newTransaction = call.receive<NewTransaction>()
                    val createdTransaction = TransactionRepository.create(newTransaction)
                    call.respond(HttpStatusCode.Created, createdTransaction)
                }
            }
            // Endpoint สำหรับ Report
            get("/reports/monthly") {
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()

                if (year == null || month == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, "Missing 'year' or 'month' query parameters.")
                }

                val monthlyTransactions = TransactionRepository.findByMonth(year, month)
                val summary = monthlyTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .map { (categoryId, trans) ->
                        val categoryName = CategoryRepository.findById(categoryId)?.name ?: "Unknown"
                        val totalAmount = trans.sumOf { it.amount }
                        mapOf("categoryId" to categoryId, "categoryName" to categoryName, "total" to totalAmount)
                    }
                call.respond(summary)
            }
        }
    }
}

//================================================================
// 5. UNIT TESTS (จะอยู่ใน src/test/kotlin/... โดยปกติ)
//================================================================

class PostRepositoryTest {
    @Test
    fun `test create and find post`() {
        // Arrange
        val newPost = NewPost(title = "My Test Post", content = "Hello World")
        // Act
        val createdPost = PostRepository.create(newPost)
        val foundPost = PostRepository.findById(createdPost.id)
        // Assert
        assertNotNull(foundPost)
        assertEquals("My Test Post", foundPost.title)
    }
}

class ExpenseReportTest {
    @Test
    fun `test monthly report logic`() {
        // Arrange: ข้อมูลตัวอย่างถูกสร้างไว้ใน TransactionRepository.init แล้ว
        val currentYear = LocalDate.now().year
        val currentMonth = LocalDate.now().monthValue

        // Act
        val transactions = TransactionRepository.findByMonth(currentYear, currentMonth)
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

        // Assert
        assertEquals(2, expenseTransactions.size) // คาดว่ามี 2 รายการที่เป็นรายจ่ายในเดือนปัจจุบัน
        assertEquals(80.0 + 45.0, expenseTransactions.sumOf { it.amount })
    }
}