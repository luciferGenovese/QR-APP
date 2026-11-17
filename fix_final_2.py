file_path = "app/src/main/java/com/example/ui/screens/GenerateScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix the duplicate android.graphics
while "android.graphics.android.graphics." in content:
    content = content.replace("android.graphics.android.graphics.", "android.graphics.")

# Fix Icons that are missing from material-icons (since we don't have extended)
content = content.replace("Icons.Default.Person", "Icons.Default.AccountBox")
content = content.replace("Icons.Default.Menu", "Icons.Default.List") # List actually IS in standard? Wait, I saw List missing earlier.
# Wait! Let's check which icons are in standard.
# Instead of guessing, let's use standard icons that are DEFINITELY there.
# Default Icons available: Add, ArrowBack, Build, Check, Close, Create, Delete, Done, Edit, Email, ExitToApp, Face, Favorite, Home, Info, LocationOn, Lock, MailOutline, Menu, Notifications, Person, Phone, Place, PlayArrow, Refresh, Search, Send, Settings, Share, ShoppingCart, Star, ThumbUp, Warning
content = content.replace("Icons.Default.AccountBox", "Icons.Default.Person") 
content = content.replace("Icons.Default.Menu", "Icons.Default.List") # Wait! I will just use Icons.Default.Build
content = content.replace("Icons.Default.List", "Icons.Default.Menu") # Let's try Menu instead of List
content = content.replace("Icons.Default.LocationOn", "Icons.Default.Place") # Place IS in standard
content = content.replace("Icons.Default.Place", "Icons.Default.Home") # Just in case

# Actually I'll just change the entire logos list to use safe ones
safe_logos = "Icons.Default.ThumbUp, Icons.Default.PlayArrow, Icons.Default.Call, Icons.Default.Email, Icons.Default.Person, Icons.Default.Home, Icons.Default.Info, Icons.Default.Face, Icons.Default.Star, Icons.Default.ThumbUp, Icons.Default.PlayArrow, Icons.Default.Call, Icons.Default.Email, Icons.Default.Person, Icons.Default.Home, Icons.Default.Star"
# Regex to replace the `val logos = listOf(...)` block
import re
content = re.sub(r'val logos = listOf\([^)]+\)', f'val logos = listOf({safe_logos})', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done fixing final 2")
