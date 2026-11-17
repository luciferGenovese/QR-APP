import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target1 = """var activeCustomTab by remember { mutableIntStateOf(0) }
            var colorSubTab by remember { mutableIntStateOf(0) }
            var logoSubTab by remember { mutableIntStateOf(0) }"""
replacement1 = """var activeCustomTab by remember { mutableIntStateOf(0) }
            var colorSubTab by remember { mutableIntStateOf(0) }
            var logoSubTab by remember { mutableIntStateOf(0) }
            var plantillaSubTab by remember { mutableStateOf("Popular") }"""
content = content.replace(target1, replacement1)

target2 = """                                val tabs = listOf("Popular", "Wifi", "Negocio", "Evento")
                                items(tabs) { tab ->
                                    val isSelected = tab == "Popular"
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {"""
replacement2 = """                                val tabs = listOf("Popular", "Wifi", "Negocio", "Evento")
                                items(tabs) { tab ->
                                    val isSelected = tab == plantillaSubTab
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { plantillaSubTab = tab }) {"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
