import re

def replace_in_file(file_path, replacements):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

create_replacements = [
    ('"🎨 QR Artístico & Estilos IA"', 'stringResource(R.string.create_opt_art)'),
    ('"📇 Tarjeta Digital / Link-in-Bio"', 'stringResource(R.string.create_opt_vcard)'),
    ('"☕ Cartel WiFi para Mesas / Cafés"', 'stringResource(R.string.create_opt_wifi)'),
    ('"🔒 Mensaje Secreto Encriptado (PIN)"', 'stringResource(R.string.create_opt_secret)'),
    ('"🖨️ Hoja de Stickers / Etiquetas (A4)"', 'stringResource(R.string.create_opt_stickers)'),
    ('"🛡️ Sello y Marca de Seguridad"', 'stringResource(R.string.create_opt_watermark)'),
    ('"Transferir Archivo"', 'stringResource(R.string.create_opt_animated)'),
]

generate_replacements = [
    ('"🔒 QR Secreto Cifrado (AES-256)"', 'context.getString(R.string.type_secret)'),
    ('"📇 Tarjeta de Contacto vCard Pro"', 'context.getString(R.string.type_vcard)'),
    ('"🏷️ Hoja de Stickers para Imprimir"', 'context.getString(R.string.type_stickers)'),
    ('"🛡️ QR con Marca de Agua"', 'context.getString(R.string.type_watermark)'),
]

replace_in_file('app/src/main/java/com/example/ui/screens/CreateScreen.kt', create_replacements)
replace_in_file('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', generate_replacements)
print("Replaced strings in UI components")
