import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

# I will append the PaywallDialog composable at the very end of the file
paywall_code = """
@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onPurchaseLifetime: () -> Unit,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Stars, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("Hazte PRO", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Desbloquea todo el potencial de la aplicación:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val features = listOf(
                    "Logotipos personalizados sin límites",
                    "Todas las plantillas premium (Negocios, Eventos)",
                    "Transferencia Óptica Ultra (Alta Velocidad)",
                    "Sin anuncios ni interrupciones"
                )
                
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(feature, style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Pricing Options
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPurchaseMonthly() }.padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suscripción Mensual", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$1.00 / mes", color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPurchaseYearly() }.padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suscripción Anual", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$10.00 / año", color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPurchaseLifetime() }.padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pago Único (De por vida)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("$4.99", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
"""

if "fun PaywallDialog" not in text:
    text += paywall_code

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

