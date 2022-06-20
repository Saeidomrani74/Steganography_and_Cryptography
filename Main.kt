package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.experimental.xor

const val BYTE_SIZE = 8
val endBytes = byteArrayOf(0, 0, 3)

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val task = readln()) {
            "exit" -> {
                println("Bye!")
                return
            }
            "hide" -> hide()
            "show" -> show()
            else -> println("Wrong task: $task")
        }
    }
}

fun hide() {
    println("Hiding message in image.")
    val inputFile = getInputFile()
    val outputFile = getOutputFile()
    val message = getMessage()
    var bytesMessage: ByteArray = message.encodeToByteArray()
    val password = getPassword()
    val bytesPassword = password.encodeToByteArray()
    var j: Int
    for (i in bytesMessage.indices) {
        j = i % bytesPassword.size
        bytesMessage[i] = bytesMessage[i] xor bytesPassword[j]
    }
    bytesMessage += endBytes
try {
    val image: BufferedImage = ImageIO.read(inputFile)
    if (image.width * image.height < bytesMessage.size * BYTE_SIZE) {
        println("The input image is not large enough to hold this message.")
        return
    }
    var x = 0
    var y = 0
    for (i in bytesMessage.indices) {
        repeat(BYTE_SIZE) {
            bytesMessage[i] = bytesMessage[i].rotateLeft(1)
            image.setRGB(x, y, copyMinorBit(bytesMessage[i].toInt(), image.getRGB(x, y)))
            if (++x == image.width) {
                x = 0
                y++
            }
        }
    }
    saveImage(image, outputFile)
} catch (e: IIOException) {
    println(e.message)
}
return
}

fun getInputFile(): File {
    println("Input image file:")
    val inputFileName = readln()
    return File(inputFileName)
}

fun getOutputFile(): File {
    println("Output image file:")
    val outputFileName = readln()
    return File(outputFileName)
}

fun getMessage(): String {
    println("Message to hide:")
    return readln()
}

fun getPassword(): String {
    println("Password:")
    return readln()
}

fun saveImage(image: BufferedImage, outputFile: File) {
    ImageIO.write(image, "png", outputFile)
    println("Message saved in ${outputFile.name} image.")
}

fun copyMinorBit(a: Int, b: Int): Int = (a and 1) or (b and 1.inv())

fun show() {
    val inputFile = getInputFile()
    val password = getPassword()
    val bytesPassword = password.encodeToByteArray()
    val bytesList = MutableList<Byte>(0) { 0 }
    try {
        val image: BufferedImage = ImageIO.read(inputFile)
        if (image.width * image.height < endBytes.size * BYTE_SIZE) {
            println("The input image is not large enough to hold a message.")
            return
        }
        var x = 0
        var y = 0
        while (true) {
            var byte: Byte = 0
            repeat(BYTE_SIZE) {
                byte = byte.rotateLeft(1)
                byte = copyMinorBit(image.getRGB(x, y), byte.toInt()).toByte()
                if (++x == image.width) {
                    x = 0
                    if (++y == image.height) {
                        println("The input image does not contain a message.")
                        return
                    }
                }
            }
            bytesList.add(byte)
            if (bytesList.size >= endBytes.size) {
                val currentEndBytes = bytesList.subList(bytesList.size - endBytes.size, bytesList.size).toByteArray()
                if (endBytes.contentEquals(currentEndBytes)) break
            }
        }
        val bytesEncryptedMessage = bytesList.subList(0, bytesList.size - endBytes.size).toByteArray()
        var j: Int
        for (i in bytesEncryptedMessage.indices) {
            j = i % bytesPassword.size
            bytesEncryptedMessage[i] = bytesEncryptedMessage[i] xor bytesPassword[j]
        }
        val message = bytesEncryptedMessage.decodeToString()
        println("Message: $message")
    } catch (e: IIOException) {
        println(e.message)
    }
    return
}