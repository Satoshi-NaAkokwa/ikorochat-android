//
// EducationScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// Education courses and learning
//

package com.ikoro.android.ecommerce.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
fun EducationScreen(modifier: Modifier = Modifier) {
    val courses = remember {
        listOf(
            EducationCourse(
                "E001", "Android Development", "Build modern Android apps with Kotlin",
                EducationCategory.TECH, "Adaobi Okonkwo", "I001", 720,
                listOf(
                    Lesson("L001", "Introduction", 15),
                    Lesson("L002", "Setup", 20),
                    Lesson("L003", "UI Basics", 30)
                ),
                0.05, CourseLevel.BEGINNER, 4.7f, 128, 456,
                System.currentTimeMillis() - 604800000
            ),
            EducationCourse(
                "E002", "Digital Marketing", "Master online marketing strategies",
                EducationCategory.BUSINESS, "Chukwuemeka Adebayo", "I002", 900,
                emptyList(),
                0.08, CourseLevel.INTERMEDIATE, 4.8f, 89, 234,
                System.currentTimeMillis() - 1209600000
            ),
            EducationCourse(
                "E003", "Python for Data Science",
                "Learn Python fundamentals and data analysis",
                EducationCategory.TECH, "Fatima Ibrahim", "I003", 1080,
                emptyList(),
                0.1, CourseLevel.BEGINNER, 4.9f, 256, 892,
                System.currentTimeMillis() - 1814400000
            )
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF2196F3))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Education",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses) { course ->
                EducationCourseCard(course = course)
            }
        }
    }
}

@Composable
fun EducationCourseCard(course: EducationCourse) {
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
                        course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "By ${course.instructor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (course.level) {
                        CourseLevel.BEGINNER -> Color(0xFF4CAF50)
                        CourseLevel.INTERMEDIATE -> Color(0xFF2196F3)
                        CourseLevel.ADVANCED -> Color(0xFFE91E63)
                        CourseLevel.ALL_LEVELS -> Color(0xFF9C27B0)
                    }
                ) {
                    Text(
                        course.level.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(course.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${course.lessons.size} lessons", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${formatDuration(course.duration)}", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${course.rating}", style = MaterialTheme.typography.bodySmall)
                    Text(" (${course.reviewCount})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₿${course.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Button(onClick = { /* Enroll */ }) {
                    Text("Enroll Now")
                }
            }
        }
    }
}

fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return "${hours}h ${remainingMinutes}m"
}