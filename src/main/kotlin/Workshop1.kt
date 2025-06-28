package org.example

// Workshop #1: Simple Console Application - Unit Converter

fun main() {
    // 2. ใช้ while (true) เพื่อให้โปรแกรมทำงานวนซ้ำ
    while (true) {
        // 1. แสดงเมนูให้ผู้ใช้เลือก
        println("===== Unit Converter =====")
        println("โปรดเลือกหน่วยที่ต้องการแปลง:")
        println("1. Celsius to Fahrenheit")
        println("2. Kilometers to Miles")
        println("พิมพ์ 'exit' เพื่อออกจากโปรแกรม")
        print("เลือกเมนู (1, 2, or exit): ")

        // 2. รับข้อมูลตัวเลือกจากผู้ใช้
        val choice = readln()
            when(choice){
                "1" -> convertCelsiusToFahrenheit();
                "2" -> convertKilometersToMiles();
                "exit"-> {println("ออกจากโปรแกรม");
                return
                }
                else -> println("ใส่ใหม่ให้ถูกต้อง");
            }
        // 3. ควบคุมการทำงานด้วย when expression
        // เลือก 1 เพื่อแปลง Celsius เป็น Fahrenheit: convertCelsiusToFahrenheit()
        // เลือก 2 เพื่อแปลง Kilometers เป็น Miles: convertKilometersToMiles()
        // เลือก 'exit' เพื่อออกจากโปรแกรม
        // 🚨


        println() // พิมพ์บรรทัดว่างเพื่อความสวยงาม
    }
}

// 4. สร้างฟังก์ชันแยกสำหรับการแปลงหน่วย Celsius to Fahrenheit: celsiusToFahrenheit
// สูตร celsius * 9.0 / 5.0 + 32
// 🚨
fun celsiusToFahrenheit(Celsius :Double ):Double {
        return Celsius * 9 /5.0 + 32
}




// 4. สร้างฟังก์ชันแยกสำหรับการแปลงหน่วย Kilometers to Miles: kilometersToMiles
// สูตร kilometers * 0.621371
// 🚨
fun kilometersToMiles (Kilometers :Double):Double{
    return Kilometers * 0.621371
}


// ฟังก์ชันสำหรับจัดการกระบวนการแปลง Celsius to Fahrenheit ทั้งหมด
fun convertCelsiusToFahrenheit() {
    print("ป้อนค่าองศาเซลเซียส (Celsius): ")
    val input = readln()

    // 5. จัดการ Null Safety ด้วย toDoubleOrNull() และ Elvis operator (?:)
    // ออกจากฟังก์ชัน convertCelsiusToFahrenheit() หากข้อมูลผิดพลาด: return
    // celsius
    // 🚨
    val Celsius = input.toDoubleOrNull() ?: run{
        println("ใส่ข้อมูลไม่ถูกต้อง")
        return;
    }

//🚨    val fahrenheitResult = celsiusToFahrenheit(celsius)
    val fahrenheitResult = celsiusToFahrenheit(Celsius)

    // 6. แสดงผลลัพธ์
    // ใช้ String format เพื่อแสดงทศนิยม 2 ตำแหน่ง
    //🚨    println("ผลลัพธ์: $celsius °C เท่ากับ ${"%.2f".format(fahrenheitResult)} °F")
    println("ผลลัพธ์: $Celsius °C เท่ากับ ${"%.2f".format(fahrenheitResult)} °F")

}



// ฟังก์ชันสำหรับจัดการกระบวนการแปลง Kilometers to Miles ทั้งหมด
fun convertKilometersToMiles() {
    print("ป้อนค่ากิโลเมตร (Kilometers): ")
    val input = readln()

    val kilometers = input.toDoubleOrNull()?: run{
        println("ข้อมูลไม่ถูกต้อง")
    }
    // 5. จัดการ Null Safety ด้วย toDoubleOrNull() และ Elvis operator (?:)
    // ออกจากฟังก์ชัน convertKilometersToMiles() หากข้อมูลผิดพลาด: return
    // kilometers
    // 🚨


//🚨    val milesResult = kilometersToMiles(kilometers)
    val milesResult = kilometersToMiles(kilometers as Double);

    // 6. แสดงผลลัพธ์
//🚨    println("ผลลัพธ์: $kilometers km เท่ากับ ${"%.2f".format(milesResult)} miles")
    println("ผลลัพธ์: $kilometers km เท่ากับ ${"%.2f".format(milesResult)} miles")
}