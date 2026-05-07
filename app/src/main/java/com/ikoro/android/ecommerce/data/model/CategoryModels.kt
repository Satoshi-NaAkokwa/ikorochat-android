//
// CategoryModels.kt
// Ikoro - ₿ỌFỌ Platform
//
// Extended category models for specialized e-commerce
//

package com.ikoro.android.ecommerce.data.model

/**
 * Healthcare Product Model
 */
data class HealthcareProduct(
    val id: String,
    val name: String,
    val description: String,
    val category: HealthcareCategory,
    val manufacturer: String,
    val expiryDate: Long? = null,
    val prescriptionRequired: Boolean = false,
    val requiresLicense: Boolean = false,
    val isActive: Boolean,
    val createdAt: Long
)

enum class HealthcareCategory {
    MEDICINE,
    SUPPLEMENTS,
    MEDICAL_DEVICES,
    PERSONAL_CARE,
    FIRST_AID,
    DIAGNOSTICS
}

/**
 * Healthcare Consultation Model
 */
data class HealthcareConsultation(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val specialization: String,
    val isVerified: Boolean = false,
    val consultationFee: Double, // in ₿ỌFỌ
    val availability: String = "Available",
    val rating: Float = 0.0f,
    val reviewCount: Int = 0
)

/**
 * Education Course Model
 */
data class EducationCourse(
    val id: String,
    val title: String,
    val description: String,
    val category: EducationCategory,
    val instructor: String,
    val instructorId: String,
    val duration: Long, // in minutes
    val lessons: List<Lesson>,
    val price: Double, // in ₿ỌFỌ
    val level: CourseLevel,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val enrollmentCount: Int = 0,
    val createdAt: Long
)

enum class EducationCategory {
    TECH,
    BUSINESS,
    LANGUAGE,
    ARTS,
    SCIENCE,
    HEALTH,
    TRADE,
    SKILLS
}

enum class CourseLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ALL_LEVELS
}

data class Lesson(
    val id: String,
    val title: String,
    val duration: Long, // in minutes
    val videoUrl: String? = null,
    val resources: List<String> = emptyList()
)

/**
 * Agricultural Product Model
 */
data class AgriculturalProduct(
    val id: String,
    val name: String,
    val description: String,
    val category: AgriculturalCategory,
    val farmer: String,
    val farmerId: String,
    val originLocation: String,
    val harvestDate: Long,
    val保质期: Long? = null, // shelf life in days
    val certifications: List<String> = emptyList(),
    val price: Double, // in ₿ỌFỌ per kg/unit
    val inStock: Boolean = true,
    val isOrganic: Boolean = false
)

enum class AgriculturalCategory {
    GRAINS,
    VEGETABLES,
    FRUITS,
    LIVESTOCK,
    DAIRY,
    SPICES,
    HERBS,
    SEEDS,
    FERTILIZERS,
    EQUIPMENT
}

/**
 * Real Estate Model
 */
data class RealEstateListing(
    val id: String,
    val title: String,
    val description: String,
    val type: PropertyType,
    val location: String,
    val price: Double, // Total price in ₿ỌFỌ
    val size: Double, // in square meters
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val furnished: Boolean = false,
    val isForSale: Boolean = true, // or rent
    val rentPerMonth: Double? = null, // if renting
    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val sellerId: String,
    val sellerName: String,
    val isVerified: Boolean = false,
    val createdAt: Long
)

enum class PropertyType {
    APARTMENT,
    HOUSE,
    LAND,
    COMMERCIAL,
    WAREHOUSE,
    OFFICE,
    SHOP
}

/**
 * Automotive Model
 */
data class AutomotiveListing(
    val id: String,
    val title: String,
    val description: String,
    val category: AutomotiveCategory,
    val make: String,
    val model: String,
    val year: Int,
    val mileage: Long, // in kilometers
    val fuelType: FuelType,
    val transmission: Transmission,
    val condition: VehicleCondition,
    val price: Double, // in ₿ỌFỌ
    val images: List<String> = emptyList(),
    val features: List<String> = emptyList(),
    val location: String,
    val sellerId: String,
    val sellerName: String,
    val createdAt: Long
)

enum class AutomotiveCategory {
    CAR,
    MOTORCYCLE,
    TRUCK,
    VAN,
    BUS,
    PARTS,
    ACCESSORIES
}

enum class FuelType {
    PETROL,
    DIESEL,
    ELECTRIC,
    HYBRID,
    LPG
}

enum class Transmission {
    AUTOMATIC,
    MANUAL,
    CVT,
    DCT
}

enum class VehicleCondition {
    NEW,
    GOOD,
    FAIR,
    FOR_PARTS
}

/**
 * Fashion Product Model
 */
data class FashionProduct(
    val id: String,
    val name: String,
    val description: String,
    val category: FashionCategory,
    val designer: String? = null,
    val brand: String,
    val sizes: List<String>,
    val colors: List<String>,
    val materials: List<String>,
    val careInstructions: String? = null,
    val price: Double, // in ₿ỌFỌ
    val discountPrice: Double? = null,
    val inStock: Boolean = true,
    val isNewArrival: Boolean = false,
    val isTrending: Boolean = false,
    val images: List<String>
)

enum class FashionCategory {
    CLOTHING,
    FOOTWEAR,
    ACCESSORIES,
    JEWELRY,
    WATCHES,
    BAGS,
    BEAUTY,
    HAIRCARE
}

/**
 * Food & Grocery Model
 */
data class FoodGroceryProduct(
    val id: String,
    val name: String,
    val description: String,
    val category: FoodGroceryCategory,
    val supplier: String,
    val weight: String, // e.g., "500g", "1kg"
    val unitPrice: Double, // in ₿ỌFỌ
    val expiryDate: Long? = null,
    val isPerishable: Boolean = true,
    val isOrganic: Boolean = false,
    val allergens: List<String> = emptyList(),
    val nutritionalInfo: NutritionalInfo? = null,
    val inStock: Boolean = true,
    val stockQuantity: Int = 0,
    val minimumOrder: Int = 1
)

enum class FoodGroceryCategory {
    FRUITS_VEGETABLES,
    MEAT_POULTRY,
    DAIRY_EGGS,
    BAKING,
    BEVERAGES,
    SNACKS,
    CONDIMENTS,
    CANNED_FOODS,
    GRAINS,
    OILS
}

data class NutritionalInfo(
    val calories: Int,
    val protein: Double, // in grams
    val carbohydrates: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double,
    val vitamins: List<String> = emptyList()
)

/**
 * Service Model
 */
data class ServiceListing(
    val id: String,
    val title: String,
    val description: String,
    val category: ServiceCategory,
    val providerId: String,
    val providerName: String,
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val hourlyRate: Double, // in ₿ỌFỌ
    val availability: String = "Available",
    val location: String,
    val skills: List<String>,
    val experience: String? = null,
    val portfolio: List<String> = emptyList(),
    val responseTime: String = "Within 1 hour",
    val createdAt: Long
)

enum class ServiceCategory {
    HOME_SERVICES,
    CLEANING,
    REPAIR,
    EVENT_SERVICES,
    PERSONAL_SERVICES,
    TUTORING,
    CONSULTING,
    TRANSPORTATION,
    BEAUTY_WELLNESS,
    TECH_SUPPORT
}

/**
 * Category Base Model for Unified Display
 */
data class CategoryListing(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val subcategoryId: String,
    val price: Double,
    val displayPrice: String,
    val image: String,
    val badge: String? = null,
    val rating: Float? = null,
    val sellerName: String,
    val location: String,
    val tags: List<String>
)