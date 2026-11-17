with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

billing_str = """    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val billingManager = remember(context) { com.example.billing.BillingManager(context) }
    androidx.compose.runtime.DisposableEffect(billingManager) {
        onDispose { billingManager.terminate() }
    }
    val isPremiumLogoUnlocked by billingManager.isPremiumLogoUnlocked.collectAsState()"""

# Replace the second occurrence back to just the context.
parts = text.split(billing_str)
if len(parts) == 3:
    # Meaning there were exactly 2 occurrences
    new_text = parts[0] + billing_str + parts[1] + "    val context = LocalContext.current" + parts[2]
    with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
        f.write(new_text)
