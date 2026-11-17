with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    lines = f.read().splitlines()

# find where GenerateScreen ends
for i, l in enumerate(lines):
    if l.startswith("private fun saveBitmapToGallery"):
        print(f"saveBitmapToGallery is at {i}")
        # print 5 lines above it
        for j in range(i-5, i+1):
            print(f"{j}: {lines[j]}")
        break
