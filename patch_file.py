import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

# 1. Insert performSave before Column(
with open("snippet.kt", "r") as f:
    snippet = f.read()

target1 = """            else -> context.getString(R.string.title_custom_code)
        }
    }

    Column("""

replacement1 = f"""            else -> context.getString(R.string.title_custom_code)
        }}
    }}

{snippet}

    Column("""

content = content.replace(target1, replacement1)

# 2. Update top button onClick
target2 = """                        onClick = { 
                            // Try to generate directly if possible, or just call onBack for now
                            // We will need to move the generation logic up here or keep the bottom button as well
                            // But visually we need this button.
                        },"""

replacement2 = """                        onClick = performSave,"""

content = content.replace(target2, replacement2)

# 3. Update bottom button onClick
with open("bottom_button.txt", "r") as f:
    bottom_button_lines = f.read()

# I will replace the inside of the onClick with performSave()
# The bottom button onClick goes from line 1840 to 2017
# Let's just use regex to replace it.

pattern = re.compile(r"Button\(\s*onClick\s*=\s*\{\s*// Validation Process.*?Toast\.makeText\(context,\s*context\.getString\(R\.string\.toast_cant_generate\),\s*Toast\.LENGTH_SHORT\)\.show\(\)\s*\}\s*\},", re.DOTALL)

replacement3 = """Button(
                onClick = performSave,"""

content = re.sub(pattern, replacement3, content)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
