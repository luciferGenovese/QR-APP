import re

files = [
    'app/src/main/java/com/example/ui/screens/GenerateScreen.kt',
    'app/src/main/java/com/example/ui/screens/TemplatesScreen.kt'
]

for file in files:
    with open(file, 'r') as f:
        text = f.read()
    
    # Remove all the missing parameter arguments from renderTemplateBitmap calls
    text = re.sub(r',\s*qrBodyShape\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*qrLogoBitmap\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*qrErrorCorrectionLevel\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*qrLogoSizeRatio\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*qrLogoBadgeShape\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*isBold\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*isUppercase\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*showSubtext\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*qrScale\s*=\s*[^,)]+', '', text)
    text = re.sub(r',\s*qrOffsetY\s*=\s*[^,)]+', '', text)

    with open(file, 'w') as f:
        f.write(text)
