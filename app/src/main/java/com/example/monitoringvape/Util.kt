package com.example.monitoringvape

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun peso(v: Double): String = "₱" + String.format(Locale.US, "%,.2f", v)

fun formatTime(t: Long): String =
    SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(t))

fun receiptLabel(no: Int): String = "#" + no.toString().padStart(5, '0')

/** Start of the day, [daysBack] days ago (0 = today 12:00 AM). */
fun startOfDay(daysBack: Int): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    c.add(Calendar.DAY_OF_YEAR, -daysBack)
    return c.timeInMillis
}

@Composable
fun StatusBadge(status: Status) {
    val (label, color) = when (status) {
        Status.OK -> "OK" to Color(0xFF059669)
        Status.LOW -> "Low" to Color(0xFFD97706)
        Status.OUT -> "Ubos" to Color(0xFFDC2626)
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
