package com.menuplayer.model

import com.google.gson.annotations.SerializedName

// ── Top-level API response ──────────────────────────────────────────────────
data class MenuResponse(
    @SerializedName("screen")  val screen: String = "",
    @SerializedName("items")   val items: List<MenuItem> = emptyList(),
    @SerializedName("settings") val settings: ScreenSettings = ScreenSettings()
)

// ── Individual menu item ────────────────────────────────────────────────────
data class MenuItem(
    @SerializedName("name")        val name: String = "",
    @SerializedName("price")       val price: Double = 0.0,
    @SerializedName("image")       val image: String = "",
    @SerializedName("video")       val video: String = "",
    @SerializedName("available")   val available: Boolean = true,
    @SerializedName("category")    val category: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("duration")    val displayDurationSec: Int = 5  // seconds to show this item
) {
    val hasVideo: Boolean get() = video.isNotBlank()
    val hasImage: Boolean get() = image.isNotBlank()
}

// ── Per-screen display settings from ERPNext ────────────────────────────────
data class ScreenSettings(
    @SerializedName("refresh_interval_sec") val refreshIntervalSec: Int = 30,
    @SerializedName("slide_duration_sec")   val slideDurationSec: Int = 5,
    @SerializedName("currency_symbol")      val currencySymbol: String = "Rs",
    @SerializedName("show_price")           val showPrice: Boolean = true,
    @SerializedName("background_color")     val backgroundColor: String = "#000000",
    @SerializedName("text_color")           val textColor: String = "#FFFFFF",
    @SerializedName("logo_url")             val logoUrl: String = ""
)
