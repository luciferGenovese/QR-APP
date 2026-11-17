fun getCategoryName(context: android.content.Context, type: String): String {
    return when (type) {
        "SCAN" -> context.getString(R.string.scan_type_scanned)
        "QR_ART" -> context.getString(R.string.create_opt_art)
        "QR_VCARD_PRO" -> context.getString(R.string.create_opt_vcard)
        "QR_WIFI_PRO" -> context.getString(R.string.create_opt_wifi)
        "QR_SECRET" -> context.getString(R.string.create_opt_secret)
        "QR_STICKERS" -> context.getString(R.string.create_opt_stickers)
        "QR_WATERMARK" -> context.getString(R.string.create_opt_watermark)
        "QR_ANIMATED" -> context.getString(R.string.create_opt_animated)
        "QR_TEXT" -> "Texto"
        else -> type
    }
}
