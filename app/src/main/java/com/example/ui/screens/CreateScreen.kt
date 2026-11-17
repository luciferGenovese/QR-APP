package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CreateOption(
    val title: String,
    val icon: ImageVector? = null,
    val type: String,
    val containerColor: Color,
    val iconTint: Color, val iconRes: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    onOptionSelected: (String) -> Unit
) {
    // Colors for modern look
    val primaryGroupColor = MaterialTheme.colorScheme.primaryContainer
    val primaryIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    
    val secondaryGroupColor = MaterialTheme.colorScheme.secondaryContainer
    val secondaryIconColor = MaterialTheme.colorScheme.onSecondaryContainer

    val barcodesGroupColor = MaterialTheme.colorScheme.tertiaryContainer
    val barcodesIconColor = MaterialTheme.colorScheme.onTertiaryContainer

    val proOptions = listOf(
        CreateOption(stringResource(R.string.create_opt_vcard), Icons.Rounded.Badge, "QR_VCARD_PRO", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.create_opt_wifi), Icons.Rounded.Wifi, "QR_WIFI_PRO", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.create_opt_secret), Icons.Rounded.Lock, "QR_SECRET", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.create_opt_stickers), Icons.Rounded.Print, "QR_STICKERS", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.create_opt_watermark), Icons.Rounded.VerifiedUser, "QR_WATERMARK", primaryGroupColor, primaryIconColor)
    )

    val personalOptions = listOf(
        CreateOption(stringResource(R.string.option_link), Icons.Rounded.Language, "QR_URL", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_email), Icons.Rounded.Email, "QR_EMAIL", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_phone), Icons.Rounded.Phone, "QR_PHONE", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_sms), Icons.Rounded.Sms, "QR_SMS", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_contact), Icons.Rounded.Person, "QR_CONTACT", primaryGroupColor, primaryIconColor)
    )

    val toolsOptions = listOf(
        CreateOption(stringResource(R.string.option_text), Icons.Rounded.TextFields, "QR_TEXT", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_wifi), Icons.Rounded.Wifi, "QR_WIFI", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_app), Icons.Rounded.Apps, "QR_APP", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.create_opt_animated), Icons.Rounded.FilePresent, "QR_ANIMATED", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_event), Icons.Rounded.Event, "QR_CALENDAR", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_location), Icons.Rounded.LocationOn, "QR_GEO", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_barcode), Icons.Rounded.ViewColumn, "QR_BARCODE", primaryGroupColor, primaryIconColor),
        CreateOption(stringResource(R.string.option_reservation), Icons.Rounded.MenuBook, "QR_RESERVATION", primaryGroupColor, primaryIconColor)
    )

    val socialOptions = listOf(
        CreateOption(stringResource(R.string.option_whatsapp), null, "QR_WHATSAPP", primaryGroupColor, primaryIconColor, R.drawable.ic_whatsapp),
        CreateOption(stringResource(R.string.option_twitter), null, "QR_TWITTER", primaryGroupColor, primaryIconColor, R.drawable.ic_twitter),
        CreateOption(stringResource(R.string.option_facebook), null, "QR_FACEBOOK", primaryGroupColor, primaryIconColor, R.drawable.ic_facebook),
        CreateOption(stringResource(R.string.option_messenger), null, "QR_MESSENGER", primaryGroupColor, primaryIconColor, R.drawable.ic_messenger),
        CreateOption(stringResource(R.string.option_youtube), null, "QR_YOUTUBE", primaryGroupColor, primaryIconColor, R.drawable.ic_youtube),
        CreateOption(stringResource(R.string.option_instagram), null, "QR_INSTAGRAM", primaryGroupColor, primaryIconColor, R.drawable.ic_instagram),
        CreateOption(stringResource(R.string.option_linkedin), null, "QR_LINKEDIN", primaryGroupColor, primaryIconColor, R.drawable.ic_linkedin),
        CreateOption(stringResource(R.string.option_tiktok), null, "QR_TIKTOK", primaryGroupColor, primaryIconColor, R.drawable.ic_tiktok),
        CreateOption(stringResource(R.string.option_telegram), null, "QR_TELEGRAM", primaryGroupColor, primaryIconColor, R.drawable.ic_telegram)
    )

    val barcodeOptions = listOf(
        CreateOption("EAN_8", Icons.Rounded.ViewColumn, "EAN_8", barcodesGroupColor, barcodesIconColor),
        CreateOption("EAN_13", Icons.Rounded.ViewColumn, "EAN_13", barcodesGroupColor, barcodesIconColor),
        CreateOption("UPC_E", Icons.Rounded.ViewColumn, "UPC_E", barcodesGroupColor, barcodesIconColor),
        CreateOption("UPC_A", Icons.Rounded.ViewColumn, "UPC_A", barcodesGroupColor, barcodesIconColor),
        CreateOption("CODE_39", Icons.Rounded.Money, "CODE_39", barcodesGroupColor, barcodesIconColor),
        CreateOption("CODE_93", Icons.Rounded.Money, "CODE_93", barcodesGroupColor, barcodesIconColor),
        CreateOption("CODE_128", Icons.Rounded.Money, "CODE_128", barcodesGroupColor, barcodesIconColor),
        CreateOption("ITF", Icons.Rounded.WrapText, "ITF", barcodesGroupColor, barcodesIconColor),
        CreateOption("PDF_417", Icons.Rounded.DocumentScanner, "PDF_417", barcodesGroupColor, barcodesIconColor),
        CreateOption("CODABAR", Icons.Rounded.ViewColumn, "CODABAR", barcodesGroupColor, barcodesIconColor),
        CreateOption("DATA_MATRIX", Icons.Rounded.Apps, "DATA_MATRIX", barcodesGroupColor, barcodesIconColor),
        CreateOption("AZTEC", Icons.Rounded.Dashboard, "AZTEC", barcodesGroupColor, barcodesIconColor)
    )

    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Text(
                "🌟 Novedades & Herramientas Pro",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            modernGroupedList(proOptions, onOptionSelected)
        }

        item {
            Text(
                stringResource(R.string.title_category_personal),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
             modernGroupedList(personalOptions, onOptionSelected)
        }

        item {
            Text(
                stringResource(R.string.title_category_social),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
             modernGroupedList(socialOptions, onOptionSelected)
        }

        item {
            Text(
                stringResource(R.string.title_category_tools),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
             modernGroupedList(toolsOptions, onOptionSelected)
        }

        item {
            Text(
                stringResource(R.string.title_barcodes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            modernGroupedList(barcodeOptions, onOptionSelected)
        }
    }
}

@Composable
fun modernGroupedList(options: List<CreateOption>, onOptionSelected: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column {
            options.forEachIndexed { index, option ->
                ModernRowItem(option, onClick = { onOptionSelected(option.type) })
                if (index < options.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernRowItem(option: CreateOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (option.iconRes != null) Color.Transparent else option.containerColor)
                .then(
                    if (option.iconRes != null) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (option.iconRes != null) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = option.iconRes),
                    contentDescription = option.title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )
            } else if (option.icon != null) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.title,
                    tint = option.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = option.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
