import re

def replace_in_file(file_path, replacements):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

settings_replacements = [
    ('title = "Rosa pastel"', 'title = stringResource(R.string.set_theme_pink)'),
    ('title = "Liquid Glass"', 'title = stringResource(R.string.set_theme_liquid_glass)'),
    ('title = "Liquid Glass Dark"', 'title = stringResource(R.string.set_theme_liquid_glass_dark)'),
    ('text = "🔊 Sonido de Confirmación de Escaneo"', 'text = stringResource(R.string.set_scan_sound_title)'),
    ('text = "🛒 Buscador de Productos & Códigos de Barras"', 'text = stringResource(R.string.set_scan_search_title)'),
    ('"💰 Caja Registradora"', 'stringResource(R.string.sound_cash_register)'),
    ('"⚡ Láser Scifi"', 'stringResource(R.string.sound_laser)'),
    ('"🔔 Campana"', 'stringResource(R.string.sound_bell)'),
    ('"👾 GameBoy"', 'stringResource(R.string.sound_gameboy)'),
    ('"📳 Solo Vibración"', 'stringResource(R.string.sound_haptic)'),
    ('"🔍 Google Search"', 'stringResource(R.string.search_google)'),
    ('"🥦 Open Food Facts"', 'stringResource(R.string.search_openfoodfacts)'),
    ('"📦 Amazon Store"', 'stringResource(R.string.search_amazon)'),
    ('"🏷️ Barcode Lookup"', 'stringResource(R.string.search_barcode)'),
]

generate_replacements = [
    ('text = "📥 Opciones de Descarga e Impresión"', 'text = stringResource(R.string.download_options_title)'),
    ('Text("💾 Generar y Guardar Código"', 'Text(stringResource(R.string.generate_button)'),
    ('Text("¡Código Generado con Éxito!"', 'Text(stringResource(R.string.success_generated_title)'),
    ('text = "Tu código está listo y guardado en tu historial. Puedes descargarlo en alta calidad:"', 'text = stringResource(R.string.success_generated_desc)'),
    ('"Ingresa los datos para previsualizar y generar tu código"', 'stringResource(R.string.generate_instruction)'),
    ('"Permitir Cámara"', 'stringResource(R.string.permission_camera)'),
    ('"▶️ Reproduciendo QR animado..."', 'stringResource(R.string.anim_playing)'),
    ('"⏸️ Reproducción pausada"', 'stringResource(R.string.anim_paused)'),
    ('"⚡ Velocidad de Reproducción:"', 'stringResource(R.string.anim_speed_title)'),
    ('"Se reproducirá en secuencia animada de códigos QR a la velocidad que elijas."', 'stringResource(R.string.anim_desc)'),
    ('"Los datos serán cifrados con algoritmo AES-256 militar. Nadie podrá leer el contenido sin el PIN exacto."', 'stringResource(R.string.secret_desc)'),
    ('"Escribe contraseñas, claves privadas, mensajes..."', 'stringResource(R.string.secret_placeholder)'),
    ('"PIN o Clave de Desencriptación *"', 'stringResource(R.string.secret_label)'),
    ('"Guarda este PIN para descifrarlo en la pestaña Escanear"', 'stringResource(R.string.secret_support)'),
    ('"Teléfono / Móvil *"', 'stringResource(R.string.phone_label)'),
    ('"Correo Electrónico"', 'stringResource(R.string.email_label)'),
    ('"Empresa u Organización"', 'stringResource(R.string.company_label)'),
    ('"Texto, Enlace o Contenido del QR Artístico *"', 'stringResource(R.string.art_content_label)'),
    ('"Título de la Etiqueta (Ej: Wi-Fi Invitados, Mesa 4) *"', 'stringResource(R.string.tag_label)'),
    ('"200ms (Rápida)"', 'stringResource(R.string.anim_speed_fast)'),
    ('"Ingresa el PIN o contraseña de desencriptación"', 'context.getString(R.string.err_pin_empty)'),
    ('"Ingresa el teléfono"', 'context.getString(R.string.err_phone_empty)'),
    ('"Ingresa el texto o URL para el QR artístico"', 'context.getString(R.string.err_art_empty)'),
    ('"Ingresa el contenido del código"', 'context.getString(R.string.err_content_empty)'),
    ('"Debe tener exactamente $reqLength dígitos"', 'context.getString(R.string.err_exact_length, reqLength)'),
    ('"Debe tener un número par de dígitos"', 'context.getString(R.string.err_even_length)'),
    ('"Permiso denegado para acceder a la galería"', 'context.getString(R.string.toast_gallery_denied)'),
    ('"Código Generado - QR OFICIAL"', 'context.getString(R.string.pdf_watermark_1)'),
    ('"Generado por la aplicación nativa de Android: QR OFICIAL"', 'context.getString(R.string.pdf_watermark_2)'),
    ('"Corta por las líneas punteadas para usar tus etiquetas adhesivas."', 'context.getString(R.string.pdf_instruction)'),
    ('"Escanéame con tu móvil"', 'context.getString(R.string.pdf_scan_me)'),
    ('"Hoja de 12 Stickers en PDF generada con éxito"', 'context.getString(R.string.toast_pdf_success)'),
    ('"📶 Conexión WiFi Pro"', 'stringResource(R.string.qr_type_wifi_pro)'),
    ('"🎨 QR Artístico y Diseños"', 'stringResource(R.string.qr_type_art)'),
]

home_replacements = [
    ('"No hay códigos guardados en esta categoría"', 'stringResource(R.string.home_no_codes)'),
]

replace_in_file('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', settings_replacements)
replace_in_file('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', generate_replacements)
replace_in_file('app/src/main/java/com/example/ui/screens/HomeScreen.kt', home_replacements)
print("Replaced strings in UI components")
