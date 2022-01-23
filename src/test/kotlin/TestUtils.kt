import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.function.Executable
import kotlin.test.assertEquals

inline fun <reified T: Throwable> assertThrows(executable: Executable) {
    Assertions.assertThrows(T::class.java, executable)
}

fun <K, V> assertEmpty(map: Map<K, V>) {
    Assertions.assertTrue(map.isEmpty(), "Map is not empty")
}