//
// HealthcareScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// Healthcare products and consultations
//

package com.ikoro.android.ecommerce.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikoro.android.ecommerce.data.model.*

@Composable
fun HealthcareScreen(modifier: Modifier = Modifier) {
    val products = remember {
        listOf(
            HealthcareProduct(
                "H001", "Paracetamol 500mg", "Relief from mild to moderate pain and fever",
                HealthcareCategory.MEDICINE, "PharmPlus", null, false, false,
                System.currentTimeMillis() - 259200000
            ),
            HealthcareProduct(
                "H002", "Vitamin C 1000mg", "Immune system support supplement",
                HealthcareCategory.SUPPLEMENTS, "HealthFirst", null, false, false,
                System.currentTimeMillis() - 518400000
            ),
            HealthcareProduct(
                "H003", "Digital Thermometer", "Fast and accurate temperature readings",
                HealthcareCategory.MEDICAL_DEVICES, "MedTech", null, false, false,
                System.currentTimeMillis() - 777600000
            )
        )
    }

    val consultations = remember {
        listOf(
            HealthcareConsultation(
                "C001", "D001", "Dr. Adaobi Okonkwo", "General Practitioner",
                true, 0.01, "Available Now", 4.8f, 245
            ),
            HealthcareConsultation(
                "C002", "D002", "Dr. Chukwuemeka Adebayo", "Cardiologist",
                true, 0.025, "Next Available: Today", 4.9f, 189
            ),
            HealthcareConsultation(
                "C003", "D003", "Dr. Fatima Ibrahim", "Pediatrician",
                true, 0.015, "Next Available: Tomorrow", 4.7f, 312
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        SectionHeader("Healthcare", Icons.Default.LocalHospital)

        HealthcareConsultationsRow(consultations)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Products", Icons.Default.Medication)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                HealthcareProductCard(product = product)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HealthcareConsultationsRow(consultations: List<HealthcareConsultation>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(consultations) { consultation ->
            ConsultationCard(consultation = consultation)
        }
    }
}

@Composable
fun ConsultationCard(consultation: HealthcareConsultation) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Doctor",
                    modifier = Modifier.padding(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        consultation.doctorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (consultation.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = Color(0xFF4CAF50), shape = MaterialTheme.shapes.small) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                Text(
                    consultation.specialization,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${consultation.rating}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(${consultation.reviewCount} reviews)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Button(onClick = { /* Book consultation */ }) {
                Text("₿${consultation.consultationFee}")
            }
        }
    }
}

@Composable
fun HealthcareProductCard(product: HealthcareProduct) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        product.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (product.category) {
                        HealthcareCategory.MEDICINE -> Color(0xFF2196F3)
                        HealthcareCategory.SUPPLEMENTS -> Color(0xFF4CAF50)
                        HealthcareCategory.MEDICAL_DEVICES -> Color(0xFF9C27B0)
                        else -> Color(0xFF757575)
                    }
                ) {
                    Text(
                        product.category.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(product.description, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("By ${product.manufacturer}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                if (product.prescriptionRequired) {
                    Icon(Icons.Default.Warning, contentDescription = "Prescription required", tint = Color(0xFFFF5722), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}