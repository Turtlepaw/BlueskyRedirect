package dev.zwander.shared.util

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.zwander.shared.R


@Composable
fun RedirectorTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val isAtLeastAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val interFamily = FontFamily(
        Font(R.font.inter_light, FontWeight.Light),
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold)
    )

    // build typography by copying default styles and replacing just the fontFamily
    val base = androidx.compose.material3.Typography()
    val appTypography = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = interFamily),
        displayMedium = base.displayMedium.copy(fontFamily = interFamily),
        displaySmall = base.displaySmall.copy(fontFamily = interFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = interFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = interFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = interFamily),
        titleLarge = base.titleLarge.copy(fontFamily = interFamily),
        titleMedium = base.titleMedium.copy(fontFamily = interFamily),
        titleSmall = base.titleSmall.copy(fontFamily = interFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = interFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = interFamily),
        bodySmall = base.bodySmall.copy(fontFamily = interFamily),
        labelLarge = base.labelLarge.copy(fontFamily = interFamily),
        labelMedium = base.labelMedium.copy(fontFamily = interFamily),
        labelSmall = base.labelSmall.copy(fontFamily = interFamily)
    )

    val colors = when {
        isAtLeastAndroid12 && isDarkTheme -> dynamicDarkColorScheme(context)
        isAtLeastAndroid12 && !isDarkTheme -> dynamicLightColorScheme(context)
        !isAtLeastAndroid12 && isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colors,
        typography = appTypography,
        content = content
    )
}
