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
    'exp_secret': 'Cifra tus mensajes con AES-256. Solo serán legibles al escanear con esta app y usando el PIN correcto.',
    'exp_vcard': 'Estructura tus datos para que se guarden automáticamente como un nuevo contacto en el teléfono de quien escanea.',
    'exp_wifi': 'Crea un acceso directo para que tus invitados se conecten a tu red WiFi al instante, sin escribir contraseñas.',
    'exp_stickers': 'Genera un documento PDF tamaño A4 listo para imprimir con 12 etiquetas adhesivas de tu código.',
    'exp_watermark': 'Incrusta un texto visual sobre el código para protegerlo de capturas de pantalla, manteniéndolo funcional.',
    'exp_animated': 'Transmite un archivo a otro dispositivo sin usar internet. El receptor debe usar esta app para escanear.'
}

en_strings = {
    'exp_secret': 'Encrypt your messages with AES-256. Only readable when scanned with this app using the correct PIN.',
    'exp_vcard': 'Structures your data to automatically save as a new contact in the phonebook of whoever scans it.',
    'exp_wifi': 'Creates a shortcut so your guests can connect to your WiFi network instantly, without typing passwords.',
    'exp_stickers': 'Generates a ready-to-print A4 size PDF document with 12 sticker labels of your code.',
    'exp_watermark': 'Embeds visual text over the code to protect it from screenshots while keeping it functional.',
    'exp_animated': 'Transfer a file to another device without internet. The receiver must use this app to scan.'
}

add_strings_to_xml('app/src/main/res/values/strings.xml', es_strings)
add_strings_to_xml('app/src/main/res/values-en/strings.xml', en_strings)
print("Updated XML strings.")
