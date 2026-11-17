import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

billing_str = "val isPremiumLogoUnlocked by billingManager.isPremiumLogoUnlocked.collectAsState()"
billing_str_new = """val isPro by billingManager.isPro.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }
    
    if (showPaywallDialog) {
        PaywallDialog(
            onDismiss = { showPaywallDialog = false },
            onPurchaseLifetime = { 
                showPaywallDialog = false
                activity?.let { billingManager.launchBillingFlow(it, com.example.billing.BillingManager.PRO_LIFETIME) } 
            },
            onPurchaseMonthly = { 
                showPaywallDialog = false
                activity?.let { billingManager.launchBillingFlow(it, com.example.billing.BillingManager.PRO_MONTHLY) } 
            },
            onPurchaseYearly = { 
                showPaywallDialog = false
                activity?.let { billingManager.launchBillingFlow(it, com.example.billing.BillingManager.PRO_YEARLY) } 
            }
        )
    }"""

text = text.replace(billing_str, billing_str_new)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

