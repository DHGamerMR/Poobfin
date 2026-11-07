package com.github.damontecres.wholphin.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.Typography
import com.github.damontecres.wholphin.R

val AppTypography = Typography()

/** Seuss display font for Poob titles, headers and the sidebar */
val SeussFont = FontFamily(Font(R.font.seuss))

/** Fredoka (variable font) for Poob body text and labels */
@OptIn(ExperimentalTextApi::class)
val FredokaFont =
    FontFamily(
        listOf(FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
            Font(
                R.font.fredoka,
                weight = weight,
                variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
            )
        },
    )

private fun TextStyle.withFont(family: FontFamily) = copy(fontFamily = family)

/** Poob typography: Seuss for display/headline/title styles, Fredoka for body and label styles */
val PoobTypography =
    with(Typography()) {
        copy(
            displayLarge = displayLarge.withFont(SeussFont),
            displayMedium = displayMedium.withFont(SeussFont),
            displaySmall = displaySmall.withFont(SeussFont),
            headlineLarge = headlineLarge.withFont(SeussFont),
            headlineMedium = headlineMedium.withFont(SeussFont),
            headlineSmall = headlineSmall.withFont(SeussFont),
            titleLarge = titleLarge.withFont(SeussFont),
            titleMedium = titleMedium.withFont(SeussFont),
            titleSmall = titleSmall.withFont(SeussFont),
            bodyLarge = bodyLarge.withFont(FredokaFont),
            bodyMedium = bodyMedium.withFont(FredokaFont),
            bodySmall = bodySmall.withFont(FredokaFont),
            labelLarge = labelLarge.withFont(FredokaFont),
            labelMedium = labelMedium.withFont(FredokaFont),
            labelSmall = labelSmall.withFont(FredokaFont),
        )
    }
