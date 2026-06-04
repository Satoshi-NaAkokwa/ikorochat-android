package com.ikoro.android.wallet.testing.qa


/**
 * Brand Strategist Agent - Wallet branding and positioning
 */
class BrandStrategistAgent {
    companion object {
        const val APP_NAME = "Ikoro Wallet"
        const val TAGLINE = "Secure, Offline-First Crypto Wallet for Emerging Markets"
        const val BRAND_COLOR = "#F29F05" // Gold/Orange for energy and trust
        const val BRAND_SECONDARY_COLOR = "#2EC4B6" // Teal for trust and innovation
    }

    // Brand guidelines
    fun getBrandGuidelines(): BrandGuidelines {
        return BrandGuidelines(
            name = APP_NAME,
            tagline = TAGLINE,
            colorPrimary = BRAND_COLOR,
            colorSecondary = BRAND_SECONDARY_COLOR,
            tone = "Trustworthy, Accessible, Powerful",
            targetAudience = "Emerging markets, crypto beginners, offline-first users",
            valueProposition = "Complete control over your crypto, without internet dependency"
        )
    }

    // Wallet features for marketing
    fun getFeatureHighlights(): List<String> {
        return listOf(
            "Offline-First Architecture",
            "Mesh Network Transaction Broadcasting",
            "Zero-Risk Secure Storage",
            "Multi-Currency Support (BTC, OFO, USDT, USDC, ₦)",
            "Biometric & PIN Protection",
            "Fraud Detection AI",
            "QR Code Payments",
            "Transaction Queue for Offline",
            "Mesh-Based Verification"
        )
    }

    // Brand voice guide
    fun getBrandVoice(): String {
        return """
            Voice: Professional yet approachable
            Tone: Trustworthy, secure, empowering
            Language: Simple, clear, no jargon (except for crypto terms)
            Messaging: Focus on security and control
            """
    }

    // Social media guidelines
    fun getSocialMediaGuidelines(): List<SocialMediaGuideline> {
        return listOf(
            SocialMediaGuideline(
                platform = "Twitter",
                characterLimit = 280,
                hashtags = listOf("#Crypto", "#OfflineWallet", "#IkoroWallet"),
                mention = "@IkoroWallet"
            ),
            SocialMediaGuideline(
                platform = "Telegram",
                maxMessageLength = 4096,
                hashtags = listOf("#Ikoro", "#CryptoNG"),
                announceChannel = "@IkoroWalletOfficial"
            ),
            SocialMediaGuideline(
                platform = "Discord",
                voice = "Community-driven",
                categories = listOf("Announcements", "Support", "Community")
            )
        )
    }
}

// Brand guidelines model
data class BrandGuidelines(
    val name: String,
    val tagline: String,
    val colorPrimary: String,
    val colorSecondary: String,
    val tone: String,
    val targetAudience: String,
    val valueProposition: String
)

// Social media guideline model
data class SocialMediaGuideline(
    val platform: String,
    val characterLimit: Int? = null,
    val maxMessageLength: Int? = null,
    val hashtags: List<String> = emptyList(),
    val mention: String? = null,
    val announceChannel: String? = null,
    val voice: String? = null,
    val categories: List<String>? = null
)
