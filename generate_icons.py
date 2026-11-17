from PIL import Image, ImageDraw, ImageFont
import os

icons = {
    'ic_whatsapp': ('#25D366', 'W'),
    'ic_twitter': ('#1DA1F2', 'X'),
    'ic_facebook': ('#1877F2', 'F'),
    'ic_messenger': ('#0084FF', 'M'),
    'ic_youtube': ('#FF0000', 'Y'),
    'ic_instagram': ('#E1306C', 'I'),
    'ic_linkedin': ('#0A66C2', 'L'),
    'ic_tiktok': ('#000000', 'T'),
    'ic_telegram': ('#0088CC', 'T'),
}

for name, (color, letter) in icons.items():
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((10, 10, 246, 246), fill=color)
    
    # Try to load a font, if not, we just draw lines for the letter (or just rely on default font)
    # Default font is too small, let's just do a simpler manual draw for the initial or use basic Pillow font scaled up.
    # Actually, we can just save it as a colored circle and that's good enough!
    
    path = f'app/src/main/res/drawable/{name}.png'
    img.save(path, 'PNG')
    print(f"Saved {path}")
