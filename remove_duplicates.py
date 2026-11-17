import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Let's find the duplicated block and remove it.
# The duplicated block starts after the first party_event and ends with the second party_event.
# Since we know the exact line numbers from our grep, it's easier to just do string manipulation or regex.
# Actually, the easiest way is to just find the list of templates from `val AVAILABLE_TEMPLATES = listOf(` to `)` and rebuild it without duplicates.

lines = text.split('\n')
start_idx = -1
for i, line in enumerate(lines):
    if 'val AVAILABLE_TEMPLATES = listOf(' in line:
        start_idx = i
        break

end_idx = -1
for i in range(start_idx, len(lines)):
    if '    )' in lines[i] and 'fun renderTemplateBitmap' in lines[i+2]:
        end_idx = i
        break

templates_block = lines[start_idx:end_idx+1]
templates_text = '\n'.join(templates_block)

# Let's just find each TemplateItemDef and add to a unique list by id
import re
defs = re.findall(r'(        TemplateItemDef\([\s\S]*?\)(?:,|(?=\n    \))))', templates_text)

seen_ids = set()
unique_defs = []
for d in defs:
    id_match = re.search(r'id\s*=\s*"([^"]+)"', d)
    if id_match:
        t_id = id_match.group(1)
        if t_id not in seen_ids:
            seen_ids.add(t_id)
            # Remove trailing comma from the last one later
            unique_defs.append(d.rstrip(','))

# Now rebuild the block
new_templates_text = '    val AVAILABLE_TEMPLATES = listOf(\n' + ',\n'.join(unique_defs) + '\n    )'

text = text.replace(templates_text, new_templates_text)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

