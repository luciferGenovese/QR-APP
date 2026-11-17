import re

with open('app/build.gradle.kts', 'r') as f:
    text = f.read()

target = """    versionCode = 18
    versionName = "18.0\""""

replacement = """    versionCode = 19
    versionName = "19.0\""""

if target in text:
    text = text.replace(target, replacement)
    with open('app/build.gradle.kts', 'w') as f:
        f.write(text)
    print("Version bumped to 19")
else:
    print("Target not found")
