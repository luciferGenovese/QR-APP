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

es_strings = {
    'create_opt_art': '🎨 QR Artístico & Estilos IA',
    'create_opt_vcard': '📇 Tarjeta Digital / Link-in-Bio',
    'create_opt_wifi': '☕ Cartel WiFi para Mesas / Cafés',
    'create_opt_secret': '🔒 Mensaje Secreto Encriptado (PIN)',
    'create_opt_stickers': '🖨️ Hoja de Stickers / Etiquetas (A4)',
    'create_opt_watermark': '🛡️ Sello y Marca de Seguridad',
    'create_opt_animated': 'Transferir Archivo',
    
    'type_secret': '🔒 QR Secreto Cifrado (AES-256)',
    'type_vcard': '📇 Tarjeta de Contacto vCard Pro',
    'type_wifi_pro': '☕ Cartel WiFi para Mesas / Cafés',
    'type_stickers': '🏷️ Hoja de Stickers para Imprimir',
    'type_watermark': '🛡️ QR con Marca de Agua'
}

en_strings = {
    'create_opt_art': '🎨 Art QR & AI Styles',
    'create_opt_vcard': '📇 Digital Card / Link-in-Bio',
    'create_opt_wifi': '☕ WiFi Sign for Tables / Cafes',
    'create_opt_secret': '🔒 Encrypted Secret Message (PIN)',
    'create_opt_stickers': '🖨️ Sticker / Label Sheet (A4)',
    'create_opt_watermark': '🛡️ Security Seal & Watermark',
    'create_opt_animated': 'Transfer File',
    
    'type_secret': '🔒 Encrypted Secret QR (AES-256)',
    'type_vcard': '📇 vCard Pro Contact Card',
    'type_wifi_pro': '☕ WiFi Sign for Tables / Cafes',
    'type_stickers': '🏷️ Printable Sticker Sheet',
    'type_watermark': '🛡️ QR with Watermark'
}

add_strings_to_xml('app/src/main/res/values/strings.xml', es_strings)
add_strings_to_xml('app/src/main/res/values-en/strings.xml', en_strings)
print("Updated XML strings.")
