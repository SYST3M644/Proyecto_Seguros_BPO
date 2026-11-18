package cl.duocuc.segurosbpo.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class SesionRepositoryTest {

    private lateinit var apiFalsa: FakeUsuarioApi
    private lateinit var repositorio: SesionRepository

    @Before
    fun preparar() {
        apiFalsa = FakeUsuarioApi()
        repositorio = SesionRepository(usuarioApi = apiFalsa, rolApi = FakeRolApi())
    }

    @Test
    fun iniciarSesion_conCredencialesCorrectas_devuelveElUsuario() = runBlocking {
        val usuario = repositorio.iniciarSesion("crojas@correo.cl", "123456")

        assertEquals("1", usuario.id)
        assertEquals("Camila Rojas Fuentes", usuario.nombre)
        assertEquals("Asegurado", usuario.rolNombre)
    }

    @Test
    fun iniciarSesion_conContrasenaIncorrecta_avisaAlUsuario() {
        val error = assertThrows(Exception::class.java) {
            runBlocking { repositorio.iniciarSesion("crojas@correo.cl", "otra") }
        }

        assertEquals("El correo o la contraseña no son correctos", error.message)
    }

    @Test
    fun sinConexion_entregaUnMensajeEntendible() {
        apiFalsa.sinConexion = true

        val error = assertThrows(Exception::class.java) {
            runBlocking { repositorio.listarUsuarios("") }
        }

        assertEquals("No se pudo conectar con el servidor", error.message)
    }
}
