package cl.duocuc.segurosbpo.data.remote

object SesionToken {

    @Volatile
    private var valor: String? = null

    fun guardar(token: String?) {
        valor = token
    }

    fun actual(): String? = valor

    fun limpiar() {
        valor = null
    }
}
