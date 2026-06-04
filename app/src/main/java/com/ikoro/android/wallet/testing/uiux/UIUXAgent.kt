package com.ikoro.android.wallet.testing.uiux

import androidx.compose.ui.graphics.Color
import com.ikoro.android.wallet.branding.BrandStrategistAgent
import com.ikoro.android.wallet.branding.BrandGuidelines


/**
 * UI/UX Agents - Wallet design system and screens
 */
class UIUXAgent {
    private val brandAgent = BrandStrategistAgent()

    // Get brand colors
    fun getBrandColors(): BrandColors {
        val guidelines = brandAgent.getBrandGuidelines()
        val primary = parseColor(guidelines.colorPrimary)
        val secondary = parseColor(guidelines.colorSecondary)

        return BrandColors(
            primary = primary,
            secondary = secondary,
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFE53935),
            textPrimary = Color(0xFF212121),
            textSecondary = Color(0xFF757575),
            success = Color(0xFF43A047)
        )
    }

    // Get wallet wireframe structure
    fun getWalletWireframe(): Wireframe {
        return Wireframe(
            screens = listOf(
                Screen(
                    name = "Wallet Home",
                    components = listOf(
                        Component("Balance Card", "Large balance display"),
                        Component("Quick Actions", "Send/Receive buttons"),
                        Component("Currency Tabs", "Switch between currencies"),
                        Component("Transaction List", "Recent transactions")
                    )
                ),
                Screen(
                    name = "Send Screen",
                    components = listOf(
                        Component("Recipient Input", "Address or QR code"),
                        Component("Amount Input", "Number with currency selector"),
                        Component("Fee Display", "Estimated fee"),
                        Component("Confirm Button", "PIN/biometric auth")
                    )
                ),
                Screen(
                    name = "Receive Screen",
                    components = listOf(
                        Component("QR Code Display", "Scannable QR code"),
                        Component("Address Display", "Copyable address"),
                        Component("Share Button", "Share via messaging"),
                        Component("Balance Info", "Minimum balance required")
                    )
                )
            )
        )
    }

    // Color helper
    private fun parseColor(hex: String): Color {
        val color = hex.removePrefix("#").toLong(16)
        return Color(
            red = ((color shr 16) and 0xFF) / 255f,
            green = ((color shr 8) and 0xFF) / 255f,
            blue = (color and 0xFF) / 255f,
            alpha = 1f
        )
    }
}

// Brand colors model
data class BrandColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color
)

// Wireframe model
data class Wireframe(
    val screens: List<Screen>
)

data class Screen(
    val name: String,
    val components: List<Component>
)

data class Component(
    val name: String,
    val description: String
)
