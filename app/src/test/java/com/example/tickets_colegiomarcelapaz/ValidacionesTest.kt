package com.example.tickets_colegiomarcelapaz

import com.example.tickets_colegiomarcelapaz.data.siguienteEstado
import com.example.tickets_colegiomarcelapaz.validation.validateConfirmPassword
import com.example.tickets_colegiomarcelapaz.validation.validateDocument
import com.example.tickets_colegiomarcelapaz.validation.validateEmail
import com.example.tickets_colegiomarcelapaz.validation.validatePassword
import com.example.tickets_colegiomarcelapaz.validation.validatePlace
import com.example.tickets_colegiomarcelapaz.validation.validateTitle
import com.example.tickets_colegiomarcelapaz.validation.validateUrgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {
    @Test
    fun usuario() {
        assertNull(validateEmail("ana@colegio.cl"))
        assertNotNull(validateEmail("ana@colegio"))
        assertNotNull(validatePassword("abcdefgh", strict = true)) // sin números
        assertNull(validatePassword("abcd1234", strict = true))
        assertNull(validatePassword("x", strict = false))
        assertNotNull(validateConfirmPassword("abcd1234", "abcd1235"))
    }

    @Test
    fun ticket() {
        assertNotNull(validateTitle("Luz"))
        assertNull(validateTitle("Luz rota"))
        assertNotNull(validatePlace("Otro", " "))
        assertNull(validatePlace("Patio", ""))
        assertNotNull(validateUrgency(""))
        assertEquals("En Proceso", siguienteEstado("Abierto"))
        assertEquals("Abierto", siguienteEstado("Cerrado"))
    }

    @Test
    fun documento() {
        assertNull(validateDocument("application/pdf", 1_000))
        assertNotNull(validateDocument("image/png", 1_000))
        assertNotNull(validateDocument("application/pdf", 20L * 1024 * 1024))
    }
}
