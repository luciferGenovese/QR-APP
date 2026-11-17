import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_column = """                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {"""

new_column = """                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    if (shapeEnum == com.example.utils.BodyShape.EMOJI && !isPro) {
                                                        Icon(Icons.Filled.Lock, contentDescription = "PRO", modifier = Modifier.align(Alignment.End).size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }"""
text = text.replace(old_column, new_column)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

