import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

target = """            composable("edit_code") {
                val editingCode by viewModel.editingCode.collectAsState()
                val code = editingCode
                if (code != null) {
                    val resolvedType = when {"""

replacement = """            composable("edit_code") {
                val editingCode by viewModel.editingCode.collectAsState()
                
                // Hold a stable reference during exit animation
                var stableCode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(editingCode) }
                androidx.compose.runtime.LaunchedEffect(editingCode) {
                    if (editingCode != null) {
                        stableCode = editingCode
                    }
                }
                val code = stableCode
                
                if (code != null) {
                    val resolvedType = when {"""

if target in text:
    text = text.replace(target, replacement)
    print("Found target 1")
else:
    print("Could not find target 1")

target2 = """                    GenerateScreen(
                        type = resolvedType,
                        editingCode = code,
                        onSave = { updatedContent, updatedFormat, bitmap ->
                            viewModel.updateCode(
                                code.copy(
                                    content = updatedContent,
                                    format = updatedFormat
                                ),
                                bitmap = bitmap
                            )
                            viewModel.setEditingCode(null)
                            navController.popBackStack()
                        },
                        onBack = {
                            viewModel.setEditingCode(null)
                            navController.popBackStack()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }"""

replacement2 = """                    GenerateScreen(
                        type = resolvedType,
                        editingCode = code,
                        onSave = { updatedContent, updatedFormat, bitmap ->
                            viewModel.updateCode(
                                code.copy(
                                    content = updatedContent,
                                    format = updatedFormat
                                ),
                                bitmap = bitmap
                            )
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }"""

if target2 in text:
    text = text.replace(target2, replacement2)
    print("Found target 2")
else:
    print("Could not find target 2")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

