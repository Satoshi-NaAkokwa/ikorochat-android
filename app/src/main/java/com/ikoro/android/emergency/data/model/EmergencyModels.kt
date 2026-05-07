//
// EmergencyMode.kt
// Ikoro - ₿ỌFỌ Platform
//
// Emergency Mode: SOS, location sharing, offline first aid
//

package com.ikoro.android.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Emergency broadcast for offline/mesh networks
 */
data class EmergencyBroadcast(
    val id: String,
    val type: EmergencyType,
    val location: Location,
    val reporterName: String,
    val reporterId: String,
    val message: String,
    val severity: EmergencySeverity,
    val needs: List<String>,
    val medicalCondition: String? = null, // if injury/accident
    val timestamp: Long,
    val responders: List<String> = emptyList() // IDs of people responding
)

enum class EmergencyType {
    MEDICAL,
    ACCIDENT,
    CRIME,
    FIRE,
    FLOOD,
    LOST,
    DISTRESS,
    SUPPLY_SHORTAGE
}

enum class EmergencySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val landmarks: String? = null // e.g., "Near Oshodi Market"
)

/**
 * First Aid Guide (Offline)
 */
data class FirstAidGuide(
    val id: String,
    val title: String,
    val category: FirstAidCategory,
    val symptoms: List<String>,
    val immediateActions: List<FirstAidAction>,
    val whatNotToDo: List<String> = emptyList(),
    val supplyNeeds: List<String> = emptyList()
)

enum class FirstAidCategory {
    INJURY,
    BURN,
    POISONING,
    CHOKING,
    HEART_ATTACK,
    STROKE,
    BLEEDING,
    FRACTURE,
    DROWNING,
    SNAKE_BITE
}

data class FirstAidAction(
    val step: Int,
    val action: String,
    val urgency: ActionUrgency
)

enum class ActionUrgency {
    CRITICAL,
    IMPORTANT,
    HELPFUL
}

/**
 * Offline First Aid Guides Database
 */
val firstAidGuides = listOf(
    FirstAidGuide(
        "FG001", "CPR - Cardiopulmonary Resuscitation",
        FirstAidCategory.HEART_ATTACK,
        listOf("Unconscious", "Not breathing", "No pulse"),
        listOf(
            FirstAidAction(1, "Check for responsiveness - tap and shout", ActionUrgency.CRITICAL),
            FirstAidAction(2, "Call for help - shout and ask someone to call emergency", ActionUrgency.CRITICAL),
            FirstAidAction(3, "Position person on back on firm surface", ActionUrgency.IMPORTANT),
            FirstAidAction(4, "Place heel of one hand on center of chest", ActionUrgency.CRITICAL),
            FirstAidAction(5, "Place other hand on top, interlock fingers", ActionUrgency.IMPORTANT),
            FirstAidAction(6, "Push hard and fast - 100-120 compressions per minute", ActionUrgency.CRITICAL),
            FirstAidAction(7, "Allow chest to recoil fully between compressions", ActionUrgency.IMPORTANT),
            FirstAidAction(8, "Continue until help arrives or person recovers", ActionUrgency.CRITICAL)
        ),
        listOf("Do not pause for more than 10 seconds", "Do not lean on chest between compressions"),
        listOf("First aid kit", "Phone (if signal available)")
    ),
    FirstAidGuide(
        "FG002", "Severe Bleeding",
        FirstAidCategory.BLEEDING,
        listOf("Heavy blood loss", "Blood soaking through clothing"),
        listOf(
            FirstAidAction(1, "Apply direct pressure to wound with clean cloth", ActionUrgency.CRITICAL),
            FirstAidAction(2, "Lift area above heart level if possible", ActionUrgency.IMPORTANT),
            FirstAidAction(3, "Add more cloths if first one soaks through", ActionUrgency.CRITICAL),
            FirstAidAction(4, "Do NOT remove first cloth - continue applying pressure", ActionUrgency.CRITICAL),
            FirstAidAction(5, "Apply tourniquet only if bleeding cannot be stopped", ActionUrgency.IMPORTANT),
            FirstAndAction(6, "Keep person warm and calm", ActionUrgency.HELPFUL)
        ),
        listOf("Do NOT remove bandages - add more if needed", "Do NOT wash wound"),
        listOf("Clean cloth or bandage", "Gloves if available")
    ),
    FirstAidGuide(
        "FG003", "Burns",
        FirstAidCategory.BURN,
        listOf("Red or black skin", "Blistering", "Pain"),
        listOf(
            FirstAidAction(1, "Cool burn with running cool (not cold) water for 10-20 min", ActionUrgency.CRITICAL),
            FirstAidAction(2, "Remove any clothing near burn - do NOT pull stuck clothing", ActionUrgency.IMPORTANT),
            FirstAidAction(3, "Loosely cover burn with clean bandage", ActionUrgency.IMPORTANT),
            FourthAidAction(4, "Take over-the-counter pain relievers if needed", ActionUrgency.HELPFUL),
            FifthAidAction(5, "Do NOT break blisters", ActionUrgency.IMPORTANT)
        ),
        listOf("Do NOT use ice, butter, or ointments", "Do NOT break blisters"),
        listOf("Clean water", "Clean bandage", "Pain relievers")
    ),
    FirstAidGuide(
        "FG004", "Snake Bite",
        FirstAidCategory.SNAKE_BITE,
        listOf("Puncture wounds", "Swelling", "Pain"),
        listOf(
            FirstAidAction(1, "Keep person still and calm to slow venom spread", ActionUrgency.CRITICAL),
            FirstAidAction(2, "Position bitten area below heart level", ActionUrgency.IMPORTANT),
            ThirdAidAction(3, "Remove constricting items (rings, bracelets)", ActionUrgency.IMPORTANT),
            FourthAidAction(4, "Do NOT wash wound - venom can help identify snake", ActionUrgency.IMPORTANT),
            FifthAidAction(5, "Do NOT try to suck out venom", ActionUrgency.CRITICAL)
        ),
        listOf("Do NOT apply tourniquet", "Do NOT cut wound", "Do NOT try to suck venom"),
        listOf("Phone for emergency transport if available")
    ),
    FirstAidGuide(
        "FG005", "Choking",
        FirstAidCategory.CHOKING,
        listOf("Cannot speak", "Clutching throat", "Difficulty breathing"),
        listOf(
            FirstAidAction(1, "Ask 'Are you choking?' - if person nods, proceed", ActionUrgency.CRITICAL),
            SecondAidAction(2, "Stand behind person, wrap arms around waist", ActionUrgency.IMPORTANT),
            ThirdAidAction(3, "Make fist with one hand, place above navel", ActionUrgency.IMPORTANT),
            FourthAidAction(4, "Grasp fist with other hand, pull upward and inward", ActionUrgency.CRITICAL),
            FifthAidAction(5, "Repeat until object is expelled or person becomes unconscious", ActionUrgency.CRITICAL)
        ),
        emptyList(),
        emptyList()
    ),
    FirstAidGuide(
        "FG006", "Stroke",
        FirstAidCategory.STROKE,
        listOf("Face drooping", "Arm weakness", "Speech difficulty", "Time to act"),
        listOf(
            FirstAidAction(1, "Do NOT drive yourself - call for help via Ikoro network", ActionUrgency.CRITICAL),
            SecondAidAction(2, "Lay person on side with head slightly elevated", ActionUrgency.IMPORTANT),
            ThirdAidAction(3, "Do NOT give food or water", ActionUrgency.IMPORTANT),
            FourthAidAction(4, "Loosen tight clothing", ActionUrgency.HELPFUL),
            FifthAidAction(5, "Note time symptoms started - tell responders", ActionUrgency.IMPORTANT)
        ),
        listOf("Do NOT give food, water, or medication", "Do NOT let person sleep"),
        emptyList()
    ),
    FirstAidGuide(
        "FG007", "Fracture/Broken Bones",
        FirstAidCategory.FRACTURE,
        listOf("Deformity", "Swelling", "Intense pain", "Unable to move limb"),
        listOf(
            FirstAidAction(1, "Do NOT move injured area unnecessarily", ActionUrgency.CRITICAL),
            SecondAidAction(2, "Immobilize area with padding and splint", ActionUrgency.IMPORTANT),
            ThirdAidAction(3, "Apply ice wrapped in cloth to reduce swelling", ActionUrgency.HELPFUL),
            FourthAidAction(4, "Elevate injury above heart level", ActionUrgency.HELPFUL),
            FifthAidAction(5, "Watch for complications - numbness, pale skin", ActionUrgency.IMPORTANT)
        ),
        listOf("Do NOT try to straighten bone", "Do NOT rub area"),
        listOf("Splint material", "Padding, ice")
    ),
    FirstAidGuide(
        "FG008", "Drowning",
        FirstAidCategory.DROWNING,
        listOf("Unconscious in water", "Not breathing", "Water in lungs"),
        listOf(
            FirstAidAction(1, "Remove person from water immediately - be safe yourself", ActionUrgency.CRITICAL),
            SecondAidAction(2, "Call for help loudly via Ikoro network", ActionUrgency.CRITICAL),
            ThirdAidAction(3, "Start CPR immediately if not breathing", ActionUrgency.CRITICAL),
            FourthAidAction(4, "Remove water from mouth and throat", ActionUrgency.IMPORTANT),
            FifthAidAction(5, "Keep person warm, check for shock", ActionUrgency.IMPORTANT)
        ),
        listOf("Do NOT try to drain water by shaking"),
        listOf("Blanket for warmth")
    ),
    FirstAidGuide(
        "FG009", "Poisoning",
        FirstAidCategory.POISONING,
        listOf("Nausea", "Vomiting", "Dizziness", "Confusion", "Seizures"),
        listOf(
            FirstAidAction(1, "Call for medics via Ikoro network immediately", ActionUrgency.CRITICAL),
            SecondAidAction(2, "Find what was swallowed - bring container if possible", ActionUrgency.IMPORTANT),
            ThirdAidAction(3, "Do NOT induce vomiting unless instructed", ActionUrgency.IMPORTANT),
            FourthAidAction(4, "If person is unconscious, turn on side", ActionUrgency.IMPORTANT),
            FifthAidAction(5, "Monitor breathing and pulse", ActionUrgency.CRITICAL)
        ),
        listOf("Do NOT induce vomiting", "Do NOT give food or drink", "Do NOT try to neutralize"),
        listOf("Poison container if safe to bring")
    ),
    FirstAidGuide(
        "FG010", "Head Injury/Trauma",
        FirstAidCategory.INJURY,
        listOf("Headache", "Dizziness", "Confusion", "Nausea", "Unconsciousness"),
        listOf(
            FirstAidAction(1, "Do NOT move person if neck injury suspected", ActionUrgency.CRITICAL),
            SecondAidAction(2, "Keep head and neck aligned, do NOT twist", ActionUrgency.CRITICAL),
            ThirdAidAction(3, "Apply cold compress to reduce swelling", ActionUrgency.HELPFUL),
            FourthAidAction(4, "Monitor for concussion symptoms", ActionUrgency.IMPORTANT),
            FifthAidAction(5, "Seek medical help if unconscious, vomiting, or confused", ActionUrgency.CRITICAL)
        ),
        listOf("Do NOT remove helmet if present", "Do NOT move if neck injured"),
        listOf("Ice pack, blanket for warmth")
    )
)

/**
 * Emergency Contacts (Offline-first)
 */
data class EmergencyContact(
    val name: String,
    val relationship: String, // family, friend, neighbor
    val phone: String? = null, // only if mesh/cellular available
    val meshId: String, // Ikoro ID
    val canRespond: Boolean, // can physically respond to emergencies
    val location: String, // general area
    val hasVehicle: Boolean = false,
    val hasFirstAidKit: Boolean = false
)