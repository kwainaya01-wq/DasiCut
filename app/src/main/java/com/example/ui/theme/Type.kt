package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OutfitFontFamily = FontFamily(
  Font(R.font.outfit, FontWeight.Normal),
  Font(R.font.outfit, FontWeight.Medium),
  Font(R.font.outfit, FontWeight.SemiBold),
  Font(R.font.outfit, FontWeight.Bold)
)

val BebasNeueFontFamily = FontFamily(
  Font(R.font.bebas_neue, FontWeight.Normal)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = BebasNeueFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    letterSpacing = 1.sp
  ),
  displayMedium = TextStyle(
    fontFamily = BebasNeueFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    letterSpacing = 0.5.sp
  ),
  titleLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp
  ),
  titleMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
  ),
  labelLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    letterSpacing = 0.2.sp
  ),
  labelMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    letterSpacing = 0.1.sp
  ),
  labelSmall = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    letterSpacing = 0.1.sp
  )
)
