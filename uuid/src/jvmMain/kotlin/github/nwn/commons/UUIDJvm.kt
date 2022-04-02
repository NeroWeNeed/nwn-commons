package github.nwn.commons

import java.io.*
import java.net.Inet6Address
import java.net.NetworkInterface
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

internal actual fun getMACAddress(): ByteArray = Inet6Address.getLocalHost().let {
    NetworkInterface.getByInetAddress(it).hardwareAddress
}

fun UUID.toJVM(): java.util.UUID = java.util.UUID(this.high.toLong(), this.low.toLong())
fun java.util.UUID.fromJVM(): UUID = UUID(this.mostSignificantBits.toULong(), this.leastSignificantBits.toULong())

actual fun UUID.toByteArray(): ByteArray {
    return ByteArrayOutputStream(16).use { byteArrayOutputStream ->
        val dataArrayOutputStream = DataOutputStream(byteArrayOutputStream).apply {
            writeLong(low.toLong())
            writeLong(high.toLong())
        }
        val r = byteArrayOutputStream.toByteArray()
        dataArrayOutputStream.close()
        r
    }
}


actual object DefaultFileUUIDStableStoreHandler :
    FileUUIDStableStoreHandler(File(System.getProperty("user.dir"), ".uuid-data.bin"))

actual open class FileUUIDStableStoreHandler(private val stateFile: File) : UUIDStableStoreHandler {
    private val stateFileLock = File(stateFile.parentFile, "${stateFile.nameWithoutExtension}.lock.bin")
    private fun readState(): UUIDBuildState? {
        return try {
            stateFile.inputStream().use { inputStream ->
                DataInputStream(inputStream).use { dataInputStream ->
                    UUIDBuildState(
                        dataInputStream.readLong().toULong(),
                        dataInputStream.readLong().toShort(),
                        dataInputStream.readLong().toULong()
                    )
                }
            }
        } catch (exception: IOException) {
            null
        }
    }

    private fun writeState(state: UUIDBuildState) {
        stateFile.outputStream().use { outputStream ->
            DataOutputStream(outputStream).use { dataOutputStream ->
                dataOutputStream.writeLong(state.timestamp.toLong())
                dataOutputStream.writeLong(state.clockSequence.toLong())
                dataOutputStream.writeLong(state.nodeId.toLong())
            }
        }
    }

    override fun update(op: (UUIDBuildState?) -> UUIDBuildState): UUIDBuildState {
        val l = FileChannel.open(
            stateFileLock.toPath(),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        )
        val state = op(readState())
        writeState(state)
        l.close()
        return state
    }

}

actual fun UUID.Companion.fromByteArray(byteArray: ByteArray): UUID =
    ByteArrayInputStream(byteArray).use { byteArrayInputStream ->
        DataInputStream(byteArrayInputStream).use { dataInputStream ->
            dataInputStream.readLong().toULong() to dataInputStream.readLong().toULong()
        }
    }.let {
        UUID(it.second, it.first)
    }

actual object SHA1UUIDNameHasher : UUIDNameHasher {

    override fun hash(namespace: UUID, name: String, charset: String): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(namespace.toByteArray())
        messageDigest.update(name.toByteArray(Charset.forName(charset)))
        return messageDigest.digest()
    }

}

actual object MD5UUIDNameHasher : UUIDNameHasher {

    override fun hash(namespace: UUID, name: String, charset: String): ByteArray {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(namespace.toByteArray())
        messageDigest.update(name.toByteArray(Charset.forName(charset)))
        return messageDigest.digest()
    }

}


actual fun ByteArray.toULongPair(): Pair<ULong, ULong> {
    return ByteArrayInputStream(this).use { byteArrayInputStream: ByteArrayInputStream ->
        DataInputStream(byteArrayInputStream).use { dataInputStream ->
            dataInputStream.readLong().toULong() to dataInputStream.readLong().toULong()
        }
    }
}