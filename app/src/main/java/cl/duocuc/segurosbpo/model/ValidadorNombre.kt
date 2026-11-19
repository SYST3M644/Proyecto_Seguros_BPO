package cl.duocuc.segurosbpo.model

object ValidadorNombre {

    private const val MINIMO = 5
    private const val MAXIMO = 60

    private val PERMITIDO = Regex("""[\p{L}][\p{L}' ]*[\p{L}]""")

    fun normalizar(nombre: String): String =
        nombre.trim().split(" ").filter { it.isNotBlank() }.joinToString(" ")

    fun error(nombre: String): String? {
        val limpio = normalizar(nombre)
        return when {
            limpio.isBlank() -> "Indica tu nombre completo"
            limpio.length < MINIMO -> "El nombre es muy corto"
            limpio.length > MAXIMO -> "El nombre supera los $MAXIMO caracteres"
            limpio.any { it.isDigit() } -> "El nombre no puede llevar números"
            !PERMITIDO.matches(limpio) -> "Usa solo letras y espacios"
            !limpio.contains(' ') -> "Escribe tu nombre y tu apellido"
            limpio.split(" ").any { it.length < 2 } -> "Cada palabra necesita al menos dos letras"
            else -> null
        }
    }
}
