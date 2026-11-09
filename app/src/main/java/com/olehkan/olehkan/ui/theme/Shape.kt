package com.olehkan.olehkan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object OlehKanShapes {
    val ExtraSmall = RoundedCornerShape(7.dp)
    val Small = RoundedCornerShape(10.dp)
    val SmallMedium = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(14.dp)
    val MediumLarge = RoundedCornerShape(15.dp)
    val Large = RoundedCornerShape(17.dp)
    val XL = RoundedCornerShape(18.dp)
    val XXL = RoundedCornerShape(24.dp)
    val Hero = RoundedCornerShape(16.dp)
    val DetailSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val HeroBottom = RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)
    val Pill = RoundedCornerShape(999.dp)
    val Circle = RoundedCornerShape(50)
}

val OlehKanMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(7.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)