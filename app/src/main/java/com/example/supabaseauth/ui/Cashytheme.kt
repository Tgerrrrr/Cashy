package com.example.supabaseauth.ui

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale
import com.example.supabaseauth.ui.formatRupiah

// ─────────────────────────────────────────────
// CASHY COLOR PALETTE  (sesuai Figma)
// ─────────────────────────────────────────────
object CashyColors {
    val Primary        = Color(0xFF1A3C40)   // dark teal — card utama
    val PrimaryLight   = Color(0xFF2E6B70)   // hover / secondary
    val Accent         = Color(0xFF4DB6AC)   // teal muda — highlight
    val Background     = Color(0xFFF4F6F8)   // bg abu terang
    val Surface        = Color(0xFFFFFFFF)   // card putih
    val TextPrimary    = Color(0xFF0D1B2A)   // hitam gelap
    val TextSecondary  = Color(0xFF607D8B)   // abu sedang
    val TextOnDark     = Color(0xFFFFFFFF)   // teks di atas teal
    val Success        = Color(0xFF2E7D32)
    val SuccessBg      = Color(0xFFE8F5E9)
    val Warning        = Color(0xFFE65100)
    val WarningBg      = Color(0xFFFFF3E0)
    val Error          = Color(0xFFC62828)
    val ErrorBg        = Color(0xFFFFEBEE)
    val Divider        = Color(0xFFECEFF1)
    val NavSelected    = Color(0xFF1A3C40)
    val NavUnselected  = Color(0xFF90A4AE)
    val NavIndicator   = Color(0xFFE0F2F1)
    val PrimaryDark = Color(0xFF002F3B)
}

// ─────────────────────────────────────────────
// FORMATTER
// ─────────────────────────────────────────────
fun formatRupiah(amount: Double): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    nf.maximumFractionDigits = 0
    return nf.format(amount)
}