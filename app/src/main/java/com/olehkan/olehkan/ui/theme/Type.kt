package com.olehkan.olehkan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.olehkan.olehkan.R

val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)


object OlehKanType {

    // ---- Headers ----
    val Hero = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 27.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1).sp,
    )

    val H1 = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 25.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.9).sp,
    )

    val H2 = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.8).sp,
    )

    val H3 = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
    )

    val SubHeaderTitle = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    )

    val Body = TextStyle(
        fontFamily = DmSans,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal,
    )

    val BodySmall = TextStyle(
        fontFamily = DmSans,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    )

    val BodyBold = TextStyle(
        fontFamily = DmSans,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Bold,
    )

    val Caption = TextStyle(
        fontFamily = DmSans,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Normal,
    )

    val CaptionBold = TextStyle(
        fontFamily = DmSans,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
    )

    val Tiny = TextStyle(
        fontFamily = DmSans,
        fontSize = 9.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.Normal,
    )

    val Eyebrow = TextStyle(
        fontFamily = DmSans,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )

    val ProductPlace = TextStyle(
        fontFamily = DmSans,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.9.sp,
    )

    val ProductName = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    val ProductPrice = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
    )

    val ButtonPrimary = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Bold,
    )

    val ButtonLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    val TabLabel = TextStyle(
        fontFamily = DmSans,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val BottomNavLabel = TextStyle(
        fontFamily = DmSans,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val ChipLabel = TextStyle(
        fontFamily = DmSans,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val PriceBig = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    val RatingBig = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-2).sp,
    )
}
val Typography = Typography(
    displayLarge = OlehKanType.Hero,
    displayMedium = OlehKanType.H1,
    headlineLarge = OlehKanType.H1,
    headlineMedium = OlehKanType.H2,
    headlineSmall = OlehKanType.SubHeaderTitle,
    titleLarge = OlehKanType.H2,
    titleMedium = OlehKanType.H3,
    titleSmall = OlehKanType.BodyBold,
    bodyLarge = OlehKanType.Body,
    bodyMedium = OlehKanType.BodySmall,
    bodySmall = OlehKanType.Caption,
    labelLarge = OlehKanType.ButtonPrimary,
    labelMedium = OlehKanType.CaptionBold,
    labelSmall = OlehKanType.Tiny,
)