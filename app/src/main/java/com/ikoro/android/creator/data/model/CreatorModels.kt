//
// CreatorTools.kt
// Ikoro - ₿ỌFỌ Platform
//
// Creator economy - Music, DJ tools, VFX, and AI creative features
//

package com.ikoro.android.creator.data.model

/**
 * Music Project Model for DAW (Digital Audio Workstation)
 */
data class MusicProject(
    val id: String,
    val name: String,
    val created: Long,
    val modified: Long,
    val duration: Long, // in seconds
    val tracks: List<MusicTrack>,
    val bpm: Int = 120,
    val key: String = "C",
    val isPublished: Boolean = false
)

/**
 * Music Track Model
 */
data class MusicTrack(
    val id: String,
    val name: String,
    val type: TrackType,
    val color: Int = 0xFF6200EE.toInt(),
    val volume: Float = 1.0f,
    val pan: Float = 0.0f, // -1.0 (left) to 1.0 (right)
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val waveformData: List<Float>? = null
)

enum class TrackType {
    AUDIO,
    MIDI,
    INSTUMENT,
    VOICE,
    SAMPLE
}

/**
 * DJ Mix Model
 */
data class DJMix(
    val id: String,
    val name: String,
    val created: Long,
    val duration: Long,
    val tracks: List<MixTrack>,
    val bpm: Int = 120,
    val isPublished: Boolean = false,
    val playCount: Int = 0
)

/**
 * DJ Mix Track
 */
data class MixTrack(
    val id: String,
    val name: String,
    val startTime: Double, // in seconds
    val duration: Double,
    val cuePoints: List<Double>,
    val loops: List<LoopRegion>,
    val effects: List<Effect>
)

/**
 * Loop Region
 */
data class LoopRegion(
    val startTime: Double,
    val endTime: Double,
    val isEnabled: Boolean = true
)

/**
 * Audio Effect
 */
data class Effect(
    val id: String,
    val name: String,
    val type: EffectType,
    val parameters: Map<String, Float>,
    val isEnabled: Boolean = true
)

enum class EffectType {
    EQUALIZER,
    COMPRESSOR,
    REVERB,
    DELAY,
    CHORUS,
    DISTORTION,
    FILTER
}

/**
 * VFX Project Model
 */
data class VFXProject(
    val id: String,
    val name: String,
    val created: Long,
    val modified: Long,
    val frameRate: Int = 30,
    val resolution: String = "1080p",
    val layers: List<VFXLayer>,
    val isPublished: Boolean = false
)

/**
 * VFX Layer
 */
data class VFXLayer(
    val id: String,
    val name: String,
    val type: LayerType,
    val startTime: Double, // in seconds
    val duration: Double,
    val isIn: Boolean = true,
    val effects: List<VFXEffect>
)

enum class LayerType {
    VIDEO,
    IMAGE,
    TEXT,
    PARTICLE,
    COLOR_CORRECTION,
    BLUR,
    TRANSITION
}

/**
 * VFX Effect
 */
data class VFXEffect(
    val id: String,
    val name: String,
    val type: VFXEffectType,
    val parameters: Map<String, Any>,
    val isEnabled: Boolean = true
)

enum class VFXEffectType {
    GLOW,
    BLUR,
    COLOR_ADJUSTMENT,
    PARTICLES,
    ANIMATION,
    TRANSITION
}

/**
 * AI Creative Request
 */
data class AIRequest(
    val id: String,
    val type: AIRequestType,
    val description: String,
    val parameters: Map<String, Any>,
    val status: AIRequestStatus,
    val created: Long,
    val completed: Long? = null,
    val result: String? = null
)

enum class AIRequestType {
    MUSIC_GENERATION,
    VOICE_SYNTHESIS,
    LYRICS_GENERATION,
    VIDEO_EFFECT,
    IMAGE_GENERATION,
    MIX_ASSISTANCE,
    MASTERING_ASSISTANCE
}

enum class AIRequestStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

/**
 * Creator Portfolio
 */
data class CreatorPortfolio(
    val id: String,
    val creatorId: String,
    val creations: List<Creation>,
    val stats: CreatorStats
)

/**
 * Creation Item
 */
data class Creation(
    val id: String,
    val title: String,
    val type: CreationType,
    val description: String,
    val thumbnail: String? = null,
    val mediaUrl: String,
    val createdAt: Long,
    val likes: Int = 0,
    val downloads: Int = 0,
    val earnings: Double = 0.0, // in ₿ỌFỌ
    val isPublished: Boolean = false,
    val isExclusive: Boolean = false
)

enum class CreationType {
    MUSIC_TRACK,
    DJ_MIX,
    VFX_VIDEO,
    PODCAST,
    SOUND_EFFECT,
    STOCK_AUDIO,
    STOCK_VIDEO,
    STOCK_IMAGE
}

/**
 * Creator Statistics
 */
data class CreatorStats(
    val totalCreations: Int = 0,
    val totalViews: Int = 0,
    val totalDownloads: Int = 0,
    val totalEarnings: Double = 0.0,
    val averageRating: Float = 0.0f,
    val reviewCount: Int = 0
)

/**
 * Creative License
 */
data class CreativeLicense(
    val id: String,
    val creationId: String,
    val type: LicenseType,
    val price: Double, // in ₿ỌFỌ
    val terms: String,
    val exclusive: Boolean = false,
    val usage: String // Personal, Commercial, etc.
)

enum class LicenseType {
    FREE,
    ROYALTY_FREE,
    EXCLUSIVE,
    SUBSCRIPTION,
    CUSTOM
}