package com.equipo7.oftaapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.equipo7.oftaapp.ui.screens.LoginScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Compose UI Test exigido por la rúbrica: validaciones visibles y envío válido del formulario. */
@RunWith(AndroidJUnit4::class)
class LoginScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun muestraErroresPorCampoCuandoLaValidacionFalla() {
        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    error = null,
                    errores = mapOf(
                        "usuario" to "Ingresa un usuario válido.",
                        "contrasena" to "La contraseña debe tener al menos 4 caracteres."
                    ),
                    preparando = false,
                    onIniciarSesion = { _, _ -> },
                    onCampoModificado = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Ingresa un usuario válido.").assertIsDisplayed()
        composeTestRule.onNodeWithText("La contraseña debe tener al menos 4 caracteres.").assertIsDisplayed()
    }

    @Test
    fun formularioValidoEntregaUsuarioYContrasenaAlCallback() {
        var enviado: Pair<String, String>? = null
        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    error = null,
                    errores = emptyMap(),
                    preparando = false,
                    onIniciarSesion = { usuario, contrasena -> enviado = usuario to contrasena },
                    onCampoModificado = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Usuario").performTextInput("admin.ofta")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("1234")
        composeTestRule.onNodeWithText("Iniciar sesión").performClick()

        composeTestRule.waitForIdle()
        assertEquals("admin.ofta" to "1234", enviado)
    }
}
