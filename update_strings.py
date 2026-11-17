import os
import re
import xml.etree.ElementTree as ET

def add_strings_to_xml(file_path, new_strings):
    tree = ET.parse(file_path)
    root = tree.getroot()
    existing_names = {child.attrib.get('name') for child in root.findall('string')}
    
    for name, value in new_strings.items():
        if name not in existing_names:
            elem = ET.Element('string', {'name': name})
            elem.text = value
            root.append(elem)
            
    ET.indent(tree, space="    ", level=0)
    tree.write(file_path, encoding='utf-8', xml_declaration=True)

# Strings to add
es_strings = {
    'set_theme_pink': 'Rosa pastel',
    'set_theme_liquid_glass': 'Liquid Glass',
    'set_theme_liquid_glass_dark': 'Liquid Glass Dark',
    'rate_us_title': '¡Califícanos!',
    'rate_us_desc': '¿Qué te parece nuestra aplicación?',
    'set_scan_sound_title': '🔊 Sonido de Confirmación de Escaneo',
    'set_scan_search_title': '🛒 Buscador de Productos & Códigos de Barras',
    'sound_cash_register': '💰 Caja Registradora',
    'sound_laser': '⚡ Láser Scifi',
    'sound_bell': '🔔 Campana',
    'sound_gameboy': '👾 GameBoy',
    'sound_haptic': '📳 Solo Vibración',
    'search_google': '🔍 Google Search',
    'search_openfoodfacts': '🥦 Open Food Facts',
    'search_amazon': '📦 Amazon Store',
    'search_barcode': '🏷️ Barcode Lookup',
    'download_options_title': '📥 Opciones de Descarga e Impresión',
    'generate_instruction': 'Ingresa los datos para previsualizar y generar tu código',
    'generate_button': '💾 Generar y Guardar Código',
    'success_generated_title': '¡Código Generado con Éxito!',
    'success_generated_desc': 'Tu código está listo y guardado en tu historial. Puedes descargarlo en alta calidad:',
    'qr_type_wifi_pro': '📶 Conexión WiFi Pro',
    'qr_type_art': '🎨 QR Artístico y Diseños',
    'permission_camera': 'Permitir Cámara',
    'anim_playing': '▶️ Reproduciendo QR animado...',
    'anim_paused': '⏸️ Reproducción pausada',
    'anim_speed_title': '⚡ Velocidad de Reproducción:',
    'anim_desc': 'Se reproducirá en secuencia animada de códigos QR a la velocidad que elijas.',
    'secret_desc': 'Los datos serán cifrados con algoritmo AES-256 militar. Nadie podrá leer el contenido sin el PIN exacto.',
    'secret_placeholder': 'Escribe contraseñas, claves privadas, mensajes...',
    'secret_label': 'PIN o Clave de Desencriptación *',
    'secret_support': 'Guarda este PIN para descifrarlo en la pestaña Escanear',
    'phone_label': 'Teléfono / Móvil *',
    'email_label': 'Correo Electrónico',
    'company_label': 'Empresa u Organización',
    'art_content_label': 'Texto, Enlace o Contenido del QR Artístico *',
    'tag_label': 'Título de la Etiqueta (Ej: Wi-Fi Invitados, Mesa 4) *',
    'anim_speed_fast': '200ms (Rápida)',
    'err_pin_empty': 'Ingresa el PIN o contraseña de desencriptación',
    'err_phone_empty': 'Ingresa el teléfono',
    'err_art_empty': 'Ingresa el texto o URL para el QR artístico',
    'err_content_empty': 'Ingresa el contenido del código',
    'err_exact_length': 'Debe tener exactamente %1$d dígitos',
    'err_even_length': 'Debe tener un número par de dígitos',
    'toast_gallery_denied': 'Permiso denegado para acceder a la galería',
    'pdf_watermark_1': 'Código Generado - QR OFICIAL',
    'pdf_watermark_2': 'Generado por la aplicación nativa de Android: QR OFICIAL',
    'pdf_instruction': 'Corta por las líneas punteadas para usar tus etiquetas adhesivas.',
    'pdf_scan_me': 'Escanéame con tu móvil',
    'toast_pdf_success': 'Hoja de 12 Stickers en PDF generada con éxito',
    'home_no_codes': 'No hay códigos guardados en esta categoría'
}

en_strings = {
    'set_theme_pink': 'Pastel Pink',
    'set_theme_liquid_glass': 'Liquid Glass',
    'set_theme_liquid_glass_dark': 'Liquid Glass Dark',
    'rate_us_title': 'Rate Us!',
    'rate_us_desc': 'What do you think about our app?',
    'set_scan_sound_title': '🔊 Scan Confirmation Sound',
    'set_scan_search_title': '🛒 Product & Barcode Search Engine',
    'sound_cash_register': '💰 Cash Register',
    'sound_laser': '⚡ Scifi Laser',
    'sound_bell': '🔔 Bell',
    'sound_gameboy': '👾 GameBoy',
    'sound_haptic': '📳 Haptic Only',
    'search_google': '🔍 Google Search',
    'search_openfoodfacts': '🥦 Open Food Facts',
    'search_amazon': '📦 Amazon Store',
    'search_barcode': '🏷️ Barcode Lookup',
    'download_options_title': '📥 Download & Print Options',
    'generate_instruction': 'Enter data to preview and generate your code',
    'generate_button': '💾 Generate & Save Code',
    'success_generated_title': 'Code Generated Successfully!',
    'success_generated_desc': 'Your code is ready and saved in your history. You can download it in high quality:',
    'qr_type_wifi_pro': '📶 WiFi Pro Connection',
    'qr_type_art': '🎨 Art & Design QR',
    'permission_camera': 'Allow Camera',
    'anim_playing': '▶️ Playing animated QR...',
    'anim_paused': '⏸️ Playback paused',
    'anim_speed_title': '⚡ Playback Speed:',
    'anim_desc': 'Will play an animated sequence of QR codes at your chosen speed.',
    'secret_desc': 'Data will be encrypted with military AES-256. No one can read it without the exact PIN.',
    'secret_placeholder': 'Type passwords, private keys, messages...',
    'secret_label': 'PIN or Decryption Key *',
    'secret_support': 'Save this PIN to decrypt in the Scan tab',
    'phone_label': 'Phone / Mobile *',
    'email_label': 'Email Address',
    'company_label': 'Company or Organization',
    'art_content_label': 'Text, Link, or Art QR Content *',
    'tag_label': 'Tag Title (Ex: Guest Wi-Fi, Table 4) *',
    'anim_speed_fast': '200ms (Fast)',
    'err_pin_empty': 'Enter the PIN or decryption password',
    'err_phone_empty': 'Enter the phone number',
    'err_art_empty': 'Enter text or URL for the art QR',
    'err_content_empty': 'Enter the code content',
    'err_exact_length': 'Must have exactly %1$d digits',
    'err_even_length': 'Must have an even number of digits',
    'toast_gallery_denied': 'Permission denied to access gallery',
    'pdf_watermark_1': 'Generated Code - OFFICIAL QR',
    'pdf_watermark_2': 'Generated by the native Android app: OFFICIAL QR',
    'pdf_instruction': 'Cut along the dotted lines to use your stickers.',
    'pdf_scan_me': 'Scan me with your phone',
    'toast_pdf_success': '12 Sticker PDF Sheet generated successfully',
    'home_no_codes': 'No saved codes in this category'
}

add_strings_to_xml('app/src/main/res/values/strings.xml', es_strings)
add_strings_to_xml('app/src/main/res/values-en/strings.xml', en_strings)
print("Updated XML strings.")
