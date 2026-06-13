//
// CategoryScreens.kt
// Ikoro - ₿ỌFỌ Platform
//
// All category-specific screens: Agriculture, Real Estate, Automotive, Fashion, Food & Grocery, Services
//

package com.ikoro.android.ecommerce.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikoro.android.ecommerce.data.model.*

@Composable
fun AgricultureScreen(modifier: Modifier = Modifier) {
    val products = remember {
        listOf(
            AgriculturalProduct(
                "AG001", "Fresh Tomatoes", "Ripe organic tomatoes",
                AgriculturalCategory.VEGETABLES, "Farm Fresh", "F001",
                "Lagos Main Market", System.currentTimeMillis() - 86400000,
                10, listOf("Organic Certified"), 0.0005, true, true
            ),
            AgriculturalProduct(
                "AG002", "Rice 50kg", "Premium quality long-grain rice",
                AgriculturalCategory.GRAINS, "Agrimech", "F002",
                "Kano State", System.currentTimeMillis() - 259200000,
                365, emptyList(), 0.12, true, false
            ),
            AgriculturalProduct(
                "AG003", "Chicken (Whole)", "Fresh farm-raised chicken",
                AgriculturalCategory.LIVESTOCK, "Poultry Plus", "F003",
                "Ogun State", System.currentTimeMillis() - 43200000,
                7, emptyList(), 0.025, true, false
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Agriculture, contentDescription = null, tint = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Agriculture",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                AgriculturalProductCard(product = product)
            }
        }
    }
}

@Composable
fun AgriculturalProductCard(product: AgriculturalProduct) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(Icons.Default.Grass, contentDescription = null, Modifier.padding(20.dp), tint = Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(product.category.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("From: ${product.farmer}", style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₿${product.price}/kg", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    if (product.isOrganic) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF4CAF50)) {
                            Text("ORGANIC", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealEstateScreen(modifier: Modifier = Modifier) {
    val listings = remember {
        listOf(
            RealEstateListing(
                "RE001", "3-Bedroom Apartment", "Modern apartment in prime location",
                PropertyType.APARTMENT, "Lekki Phase 1", 2.5,
                150.0, 3, 2, true, true, null,
                emptyList(), listOf("AC", "Parking", "Security"),
                "S001", "Top Realty", true, System.currentTimeMillis() - 604800000
            ),
            RealEstateListing(
                "RE002", "Commercial Office Space",
                "1200 sqm office building for sale",
                PropertyType.COMMERCIAL, "Victoria Island", 8.0,
                1200.0, 0, 0, false, true, null,
                emptyList(), listOf(" elevator", "Generator", "Parking"],
                "S002", "Prime Properties", true, System.currentTimeMillis() - 2592000000
            ),
            RealEstateListing(
                "RE003", "Land for Development",
                "2000 sqm plot of land",
                PropertyType.LAND, "Ibeju Lekki", 1.5,
                2000.0, 0, 0, false, true, null,
                emptyList(), emptyList(),
                "S003", "Land Masters", false, System.currentTimeMillis() - 1209600000
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF6200EE))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Real Estate",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listings) { property ->
                RealEstateCard(property = property)
            }
        }
    }
}

@Composable
fun RealEstateCard(property: RealEstateListing) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(property.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (property.isVerified) {
                    Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF03DAC6)) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(property.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF6200EE), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${property.size} sqm")
                Spacer(modifier = Modifier.width(16.dp))
                if (property.bedrooms > 0) {
                    Icon(Icons.Default.Bed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${property.bedrooms} beds")
                    Spacer(modifier = Modifier.width(16.dp))
                }
                if (property.bathrooms > 0) {
                    Icon(Icons.Default.Bathtub, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${property.bathrooms} baths")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (property.isForSale) {
                    Text("₿${property.price}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                } else {
                    Text("₿${property.rentPerMonth}/mo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                Text(property.type.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AutomotiveScreen(modifier: Modifier = Modifier) {
    val vehicles = remember {
        listOf(
            AutomotiveListing(
                "AU001", "Toyota Camry 2022",
                "Well maintained sedan, low mileage",
                AutomotiveCategory.CAR, "Toyota", "Camry", 2022,
                25000, FuelType.PETROL, Transmission.AUTOMATIC,
                VehicleCondition.GOOD, 0.8, emptyList(),
                emptyList(), "Lagos", "S001", "Car World",
                System.currentTimeMillis() - 1209600000
            ),
            AutomotiveListing(
                "AU002", "Honda PCX 150",
                "Smooth scooters, perfect for city commute",
                AutomotiveCategory.MOTORCYCLE, "Honda", "PCX150", 2021,
                8000, FuelType.PETROL, Transmission.AUTOMATIC,
                VehicleCondition.GOOD, 0.15, emptyList(),
                emptyList(), "Abuja", "S002", "Bike Masters",
                System.currentTimeMillis() - 2592000000
            ),
            AutomotiveListing(
                "AU003", "Ford Transit",
                "Reliable van for business",
                AutomotiveCategory.VAN, "Ford", "Transit", 2020,
                120000, FuelType.DIESEL, Transmission.MANUAL,
                VehicleCondition.FAIR, 2.5, emptyList(),
                emptyList(), "Kano", "S003", "Trucky",
                System.currentTimeMillis() - 1814400000
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFFE91E63))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Automotive",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vehicles) { vehicle ->
                AutomotiveCard(vehicle = vehicle)
            }
        }
    }
}

@Composable
fun AutomotiveCard(vehicle: AutomotiveListing) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(vehicle.make, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${vehicle.year}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${vehicle.mileage.toLocaleString()}km", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₿${vehicle.price}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                Surface(shape = MaterialTheme.shapes.small, color = when (vehicle.condition) {
                    VehicleCondition.NEW -> Color(0xFF4CAF50)
                    VehicleCondition.GOOD -> Color(0xFF2196F3)
                    VehicleCondition.FAIR -> Color(0xFFFF9800)
                    VehicleCondition.FOR_PARTS -> Color(0xFFF44336)
                }) {
                    Text(vehicle.condition.name, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FashionScreen(modifier: Modifier = Modifier) {
    val products = remember {
        listOf(
            FashionProduct(
                "FS001", "Ankara Lace Gown",
                "Traditional Nigerian attire with modern twist",
                FashionCategory.CLOTHING, "Da Silva", "AnkaraStyle",
                listOf("S", "M", "L", "XL"), listOf("Red", "Blue", "Green"),
                listOf("Cotton", "Lace"), "Hand wash only", 0.015, null,
                true, true, true,
                emptyList()
            ),
            FashionProduct(
                "FS002", "Men's Ankara Shirt",
                "Stylish traditional shirt",
                FashionCategory.CLOTHING, null, "Kulture Cloth",
                listOf("M", "L", "XL"), listOf("Black", "White", "Multi"),
                listOf("Cotton"), "Machine wash", 0.025, 0.02,
                true, false, true,
                emptyList()
            ),
            FashionProduct(
                "FS003", "Handwoven Bag",
                "Beautiful handcrafted leather bag",
                FashionCategory.BAGS, "Akindele", "Crafts by Ada",
                emptyList(), listOf("Brown", "Black"),
                listOf("Leather"), "Wipe clean", 0.08, null,
                true, true, false,
                emptyList()
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Checkroom, contentDescription = null, tint = Color(0xFF9C27B0))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Fashion",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                FashionProductCard(product = product)
            }
        }
    }
}

@Composable
fun FashionProductCard(product: FashionProduct) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(Icons.Default.Checkroom, contentDescription = null, Modifier.padding(20.dp), tint = Color(0xFF9C27B0))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(product.category.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.isNewArrival) {
                        Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF9C27B0)) {
                            Text("NEW", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (product.isTrending) {
                        Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFFF5722)) {
                            Text("HOT", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.discountPrice != null) {
                        Text("₿${product.price}", style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("₿${product.discountPrice}", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    } else {
                        Text("₿${product.price}", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}

@Composable
fun FoodGroceryScreen(modifier: Modifier = Modifier) {
    val products = remember {
        listOf(
            FoodGroceryProduct(
                "FG001", "Fresh Orange Juice",
                "100% natural fruit juice",
                FoodGroceryCategory.BEVERAGES, "Naija Fruits",
                "500ml", 0.001, null, false, false,
                emptyList(), null, true, 50
            ),
            FoodGroceryProduct(
                "FG002", "Garri 5kg",
                "Premium white garri",
                FoodGroceryCategory.GRAINS, "Agrimech",
                "5kg", 0.008, null, false, false,
                emptyList(), null, true, 100
            ),
            FoodGroceryProduct(
                "FG003", "Beans (Black Eyed)",
                "Local beans",
                FoodGroceryCategory.GRAINS, "Grain Mill",
                "1kg", 0.004, null, true, false,
                emptyList(), NutritionalInfo(
                    125, 8.0, 22.0, 0.5, 6.0, 0.1, 0.5
                ), true, 75
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Food & Grocery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                FoodGroceryProductCard(product = product)
            }
        }
    }
}

@Composable
fun FoodGroceryProductCard(product: FoodGroceryProduct) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, Modifier.padding(20.dp), tint = Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(product.weight, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₿${product.unitPrice}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    if (product.isOrganic) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF4CAF50)) {
                            Text("ORGANIC", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServicesScreen(modifier: Modifier = Modifier) {
    val services = remember {
        listOf(
            ServiceListing(
                "SR001", "Home Cleaning Service",
                "Professional cleaning for homes and offices",
                ServiceCategory.HOME_SERVICES, "P001", "CleanTeam Nigeria",
                true, 4.8f, 156, 0.01, "Available 9AM-6PM", "Lagos",
                listOf("Deep Cleaning", "Regular Cleaning", "Office Cleaning"),
                "5 years experience"
            ),
            ServiceListing(
                "SR002", "Plumbing Repairs",
                "Fix leaks, replace pipes, install fixtures",
                ServiceCategory.REPAIR, "P002", "Master Plumbers",
                true, 4.6f, 89, 0.015, "Emergency Available", "Abuja",
                listOf("Pipe Repair", "Fixture Installation", "Drain Cleaning"),
                "10 years experience"
            ),
            ServiceListing(
                "SR003", "Electrical Works",
                "Wiring, repairs, installations",
                ServiceCategory.REPAIR, "P003", "Spark Electrics",
                true, 4.9f, 203, 0.02, "Next available: Today", "Kano",
                listOf("Wiring", "Replacements", "Installations"),
                "7 years experience"
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Construction, contentDescription = null, tint = Color(0xFFE91E63))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Services",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services) { service ->
                ServiceCard(service = service)
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceListing) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, Modifier.padding(15.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(service.category.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${service.rating}", style = MaterialTheme.typography.bodySmall)
                    Text(" (${service.reviewCount})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("From: ${service.providerName}", style = MaterialTheme.typography.bodySmall)
                    if (service.isVerified) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₿${service.hourlyRate}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                Text("/hr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

fun Long.toLocaleString(): String = java.text.NumberFormat.getInstance().format(this)