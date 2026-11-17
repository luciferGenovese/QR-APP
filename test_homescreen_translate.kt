@Composable
fun getCategoryName(type: String): String {
    return when (type) {
        "SCAN" -> stringResource(R.string.scan_type_scanned)
        "QR_ART" -> stringResource(R.string.create_opt_art)
        "QR_VCARD_PRO" -> stringResource(R.string.create_opt_vcard)
        "QR_WIFI_PRO" -> stringResource(R.string.create_opt_wifi)
        "QR_SECRET" -> stringResource(R.string.create_opt_secret)
        "QR_STICKERS" -> stringResource(R.string.create_opt_stickers)
        "QR_WATERMARK" -> stringResource(R.string.create_opt_watermark)
        "QR_ANIMATED" -> stringResource(R.string.create_opt_animated)
        "QR_TEXT" -> "Texto"
        "QR_CODE" -> stringResource(R.string.scan_type_created)
        else -> type
    }
}
