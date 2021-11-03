import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.function.Executable

inline fun <reified T: Throwable> assertThrows(executable: Executable) {
    Assertions.assertThrows(T::class.java, executable)
}