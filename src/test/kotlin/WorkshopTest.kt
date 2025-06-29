import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.*

class WorkshopTest {

    // --- Tests for Workshop #1: Unit Converter ---

    // celsius input: 20.0
    // expected output: 68.0
    @Test
    fun `test celsiusToFahrenheit with positive value`() {
        // Arrange: ตั้งค่า input และผลลัพธ์ที่คาดหวัง
        val celsiusInput = 20.0
        val expectedFahrenheit = 68.0

        // Act: เรียกใช้ฟังก์ชันที่ต้องการทดสอบ
        val actualFahrenheit = celsiusToFahrenheit(celsiusInput)

        // Assert: ตรวจสอบว่าผลลัพธ์ที่ได้ตรงกับที่คาดหวัง
        assertEquals(expectedFahrenheit, actualFahrenheit, 0.001, "20°C should be 68°F")
    }

    // celsius input: 0.0
    // expected output: 32.0
    @Test
    fun `test celsiusToFahrenheit with zero`() {
        // Arrange
        val celsiusInput = 0.0
        val expectedFahrenheit = 32.0
        // Act
        val actualFahrenheit = celsiusToFahrenheit(celsiusInput)
        // Assert
        assertEquals(expectedFahrenheit, actualFahrenheit, 0.001, "0°C should be 32°F")
    }

    // celsius input: -10.0
    // expected output: 14.0
    @Test
    fun `test celsiusToFahrenheit with negative value`() {
        // Arrange
        val celsiusInput = -10.0
        val expectedFahrenheit = 14.0
        // Act
        val actualFahrenheit = celsiusToFahrenheit(celsiusInput)
        // Assert
        assertEquals(expectedFahrenheit, actualFahrenheit, 0.001, "-10°C should be 14°F")
    }

    // test for kilometersToMiles function
    // kilometers input: 1.0
    // expected output: 0.621371
    @Test
    fun `test kilometersToMiles with one kilometer`() {
        // Arrange
        val kilometersInput = 1.0
        val expectedMiles = 0.621371
        // Act
        val actualMiles = kilometersToMiles(kilometersInput)
        // Assert
        assertEquals(expectedMiles, actualMiles, 0.000001, "1.0 km should be 0.621371 miles")
    }

    // --- Tests for Workshop #1: Unit Converter End ---

    // --- Tests for Workshop #2: Data Analysis Pipeline ---
    // สร้าง list สินค้าสำหรับทดสอบ
    private val testProducts = listOf(
        Product("Laptop", 35000.0, "Electronics"),
        Product("Smartphone", 25000.0, "Electronics"),
        Product("T-shirt", 450.0, "Apparel"),
        Product("Monitor", 7500.0, "Electronics"),
        Product("Keyboard", 499.0, "Electronics"), // ราคาไม่ผ่านเงื่อนไข
        Product("Jeans", 1200.0, "Apparel"),
        Product("Headphones", 1800.0, "Electronics") // ผ่านเงื่อนไข
    )

    // จงเขียน test cases สำหรับฟังก์ชันนี้ โดยตรวจสอบผลรวมราคาสินค้า Electronics ที่ราคา > 500 บาท
    @Test
    fun `test total price of electronics over 500`() {
        // Arrange
        val expectedTotal = 35000.0 + 25000.0 + 7500.0 + 1800.0 // 69300.0

        // Act
        val actualTotal = calculateTotalElectronicsPriceOver500(testProducts)

        // Assert
        assertEquals(expectedTotal, actualTotal, 0.001, "Total price of electronics over 500 should be 69300.0")
    }

    // จงเขียน test cases เช็คจำนวนสินค้าที่อยู่ในหมวด 'Electronics' และมีราคามากกว่า 500 บาท
    @Test
    fun `test count of electronics over 500`() {
        // Arrange
        val expectedCount = 4

        // Act
        val actualCount = countElectronicsOver500(testProducts)

        // Assert
        assertEquals(expectedCount, actualCount, "Count of electronics over 500 should be 4")
    }

    // --- Tests for Workshop #2: Data Analysis Pipeline End ---
}