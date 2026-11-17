import re
with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r") as f:
    content = f.read()

target1 = """    val templates = remember {
        listOf(
            QrTemplate("Clásico", android.graphics.Color.BLACK, android.graphics.Color.WHITE, com.example.utils.BodyShape.SQUARE),
            QrTemplate("Océano", android.graphics.Color.parseColor("#003366"), android.graphics.Color.parseColor("#E6F2FF"), com.example.utils.BodyShape.ROUNDED),
            QrTemplate("Bosque", android.graphics.Color.parseColor("#004D00"), android.graphics.Color.parseColor("#E6FFE6"), com.example.utils.BodyShape.CIRCLE),
            QrTemplate("Neón", android.graphics.Color.parseColor("#FF00FF"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.SQUARE),
            QrTemplate("Atardecer", android.graphics.Color.parseColor("#CC3300"), android.graphics.Color.parseColor("#FFF0E6"), com.example.utils.BodyShape.ROUNDED)
        )
    }"""

replacement1 = """    val templates = remember(plantillaSubTab) {
        when (plantillaSubTab) {
            "Wifi" -> listOf(
                QrTemplate("Clásico", android.graphics.Color.BLACK, android.graphics.Color.WHITE, com.example.utils.BodyShape.SQUARE),
                QrTemplate("Router", android.graphics.Color.parseColor("#0052CC"), android.graphics.Color.parseColor("#E6F0FF"), com.example.utils.BodyShape.ROUNDED),
                QrTemplate("Conexión", android.graphics.Color.parseColor("#FF9900"), android.graphics.Color.parseColor("#FFF5E6"), com.example.utils.BodyShape.SQUARE)
            )
            "Negocio" -> listOf(
                QrTemplate("Elegante", android.graphics.Color.parseColor("#333333"), android.graphics.Color.parseColor("#F5F5F5"), com.example.utils.BodyShape.ROUNDED),
                QrTemplate("Corporativo", android.graphics.Color.parseColor("#003366"), android.graphics.Color.parseColor("#FFFFFF"), com.example.utils.BodyShape.SQUARE),
                QrTemplate("Premium", android.graphics.Color.parseColor("#D4AF37"), android.graphics.Color.parseColor("#000000"), com.example.utils.BodyShape.DIAMOND)
            )
            "Evento" -> listOf(
                QrTemplate("Fiesta", android.graphics.Color.parseColor("#FF00FF"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.CIRCLE),
                QrTemplate("Concierto", android.graphics.Color.parseColor("#FF3300"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.STAR),
                QrTemplate("Boda", android.graphics.Color.parseColor("#A8B5A1"), android.graphics.Color.parseColor("#FFFFFF"), com.example.utils.BodyShape.HEART)
            )
            else -> listOf(
                QrTemplate("Clásico", android.graphics.Color.BLACK, android.graphics.Color.WHITE, com.example.utils.BodyShape.SQUARE),
                QrTemplate("Océano", android.graphics.Color.parseColor("#003366"), android.graphics.Color.parseColor("#E6F2FF"), com.example.utils.BodyShape.ROUNDED),
                QrTemplate("Bosque", android.graphics.Color.parseColor("#004D00"), android.graphics.Color.parseColor("#E6FFE6"), com.example.utils.BodyShape.CIRCLE),
                QrTemplate("Neón", android.graphics.Color.parseColor("#FF00FF"), android.graphics.Color.parseColor("#1A1A1A"), com.example.utils.BodyShape.SQUARE),
                QrTemplate("Atardecer", android.graphics.Color.parseColor("#CC3300"), android.graphics.Color.parseColor("#FFF0E6"), com.example.utils.BodyShape.ROUNDED)
            )
        }
    }"""

if target1 in content:
    content = content.replace(target1, replacement1)
else:
    print("TARGET 1 NOT FOUND")

target2 = """                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(template.foreground), modifier = Modifier.size(64.dp))
                                            }"""

replacement2 = """                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(template.foreground), modifier = Modifier.size(48.dp))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(text = template.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            }"""

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("TARGET 2 NOT FOUND")

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w") as f:
    f.write(content)
