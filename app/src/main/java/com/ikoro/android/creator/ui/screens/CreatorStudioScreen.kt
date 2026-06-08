//
// CreatorStudioScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// Creator Studio main interface with DAW, DJ mixer, and VFX options
//

package com.ikoro.android.creator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikoro.android.creator.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorStudioScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val creatorTabs = listOf("Music DAW", "DJ Mixer", "VFX Studio", "AI Creative", "Portfolio")

    // Sample projects
    val musicProjects = remember {
        mutableStateListOf(
            MusicProject(
                "MP001",
                "Afrobeat Session 1",
                System.currentTimeMillis() - 86400000,
                System.currentTimeMillis() - 3600000,
                180,
                emptyList(),
                bpm = 120,
                key = "F minor",
                isPublished = false
            ),
            MusicProject(
                "MP002",
                "Chill Vibes",
                System.currentTimeMillis() - 172800000,
                System.currentTimeMillis() - 86400000,
                240,
                emptyList(),
                bpm = 90,
                key = "C major",
                isPublished = true
            )
        )
    }

    val djMixes = remember {
        mutableStateListOf(
            DJMix(
                "DJ001",
                "Weekend Party Mix",
                System.currentTimeMillis() - 604800000,
                3600,
                emptyList(),
                bpm = 128,
                isPublished = true,
                playCount = 342
            ),
            DJMix(
                "DJ002",
                "Chill Lounge Mix",
                System.currentTimeMillis() - 1209600000,
                5400,
                emptyList(),
                bpm = 100,
                isPublished = true,
                playCount = 512
            )
        )
    }

    val vfxProjects = remember {
        mutableStateListOf(
            VFXProject(
                "VFX001",
                "Product Demo Video",
                System.currentTimeMillis() - 259200000,
                System.currentTimeMillis() - 86400000,
                frameRate = 30,
                resolution = "1080p",
                layers = emptyList()
            ),
            VFXProject(
                "VFX002",
                "Transition Pack Demo",
                System.currentTimeMillis() - 518400000,
                System.currentTimeMillis() - 172800000,
                frameRate = 60,
                resolution = "4K",
                layers = emptyList()
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Creator Studio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { /* Create new project */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Project")
            }
        }

        // Stats cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CreatorStatCard("Music", "${musicProjects.size} projects", Color(0xFF9C27B0))
            CreatorStatCard("DJ Mixes", "${djMixes.size} mixes", Color(0xFFE91E63))
            CreatorStatCard("VFX", "${vfxProjects.size} projects", Color(0xFF2196F3))
            CreatorStatCard("AI", "0 requests", Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            creatorTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab) }
                )
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> MusicDAWTab(projects = musicProjects)
            1 -> DJMixerTab(mixes = djMixes)
            2 -> VFXStudioTab(projects = vfxProjects)
            3 -> AICreativeTab()
            4 -> PortfolioTab()
        }
    }
}

@Composable
fun CreatorStatCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun MusicDAWTab(projects: List<MusicProject>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Record audio */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record")
                }

                OutlinedButton(
                    onClick = { /* Import audio */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }

                OutlinedButton(
                    onClick = { /* Use samples */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Samples")
                }
            }
        }

        items(projects) { project ->
            MusicProjectCard(project = project)
        }
    }
}

@Composable
fun MusicProjectCard(project: MusicProject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open project */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (project.isPublished) {
                        Icon(Icons.Default.Public, contentDescription = "Published", tint = Color(0xFF4CAF50))
                    }
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${project.tracks.size} tracks")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${formatDuration(project.duration)}")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${project.bpm} BPM")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Piano, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(project.key)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Modified ${formatTime(project.modified)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun DJMixerTab(mixes: List<DJMix>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Auto mix */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Mix")
                }

                OutlinedButton(
                    onClick = { /* Import tracks */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Tracks")
                }

                OutlinedButton(
                    onClick = { /* Apply effects */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Effects")
                }
            }
        }

        items(mixes) { mix ->
            DJMixCard(mix = mix)
        }
    }
}

@Composable
fun DJMixCard(mix: DJMix) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open mix */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    mix.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${mix.playCount}", style = MaterialTheme.typography.bodySmall)

                    if (mix.isPublished) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.Public, contentDescription = "Published", tint = Color(0xFF4CAF50))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${mix.tracks.size} tracks")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${formatDuration(mix.duration)}")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${mix.bpm} BPM")
            }

            Spacer(modifier = Modifier.height(8.dp))

            lazyRow {
                mix.tracks.take(4).forEach { track ->
                    Chip(modifier = Modifier.padding(horizontal = 2.dp)) {
                        Text(track.name)
                    }
                }
            }
        }
    }
}

@Composable
fun VFXStudioTab(projects: List<VFXProject>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Import video */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Video")
                }

                OutlinedButton(
                    onClick = { /* Add effects */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Effects")
                }

                OutlinedButton(
                    onClick = { /* Export */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Export, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
            }
        }

        items(projects) { project ->
            VFXProjectCard(project = project)
        }
    }
}

@Composable
fun VFXProjectCard(project: VFXProject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open project */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${project.layers.size} layers")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Tv, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(project.resolution)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Film, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${project.frameRate} fps")
            }
        }
    }
}

@Composable
fun AICreativeTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        ) {
        Text(
            "AI Creative Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        val aiTools = listOf(
            "🎵 Music Generation",
            "🎙️ Voice Synthesis",
            "✍️ Lyrics Generation",
            "🎬 Video Effects",
            "🖼️ Image Generation",
            "🎧 Mixing Assistance",
            "🎚️ Mastering Assistance"
        )

        aiTools.forEach { tool ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { /* Open tool */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tool, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Open", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AI Credits: Unlimited",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE)
                )
                Text(
                    "Generate unlimited content powered by Agbara AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PortfolioTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        ) {
        Text(
            "My Portfolio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "0 Creations",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    "Start creating to build your portfolio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
}

fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 2592000000 -> "${diff / 86400000}d ago"
        else -> "${diff / 2592000000}M ago"
    }
}