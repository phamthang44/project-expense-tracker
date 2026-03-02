package com.thang.projectexpensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary Palette: Vivid Blue ───────────────────────────────────────
val md_theme_light_primary            = Color(0xFF2563EB)   // vivid blue
val md_theme_light_onPrimary          = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer   = Color(0xFFDBEAFE)   // sky-100
val md_theme_light_onPrimaryContainer = Color(0xFF1E3A8A)   // blue-900

val md_theme_dark_primary             = Color(0xFF93C5FD)   // blue-300
val md_theme_dark_onPrimary           = Color(0xFF1E3A8A)   // blue-900
val md_theme_dark_primaryContainer    = Color(0xFF1D4ED8)   // blue-700
val md_theme_dark_onPrimaryContainer  = Color(0xFFDBEAFE)   // sky-100

// ── Secondary Palette: Teal Green ──────────────────────────────────
val md_theme_light_secondary = Color(0xFF006B5E)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFF7CF8E1)
val md_theme_light_onSecondaryContainer = Color(0xFF00201B)

val md_theme_dark_secondary = Color(0xFF5EDBC5)
val md_theme_dark_onSecondary = Color(0xFF003730)
val md_theme_dark_secondaryContainer = Color(0xFF005047)
val md_theme_dark_onSecondaryContainer = Color(0xFF7CF8E1)

// ── Tertiary Palette: Warm Amber ───────────────────────────────────
val md_theme_light_tertiary = Color(0xFF86521A)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFDCC2)
val md_theme_light_onTertiaryContainer = Color(0xFF2D1600)

val md_theme_dark_tertiary = Color(0xFFFBB975)
val md_theme_dark_onTertiary = Color(0xFF4A2800)
val md_theme_dark_tertiaryContainer = Color(0xFF693C00)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDCC2)

// ── Error ──────────────────────────────────────────────────────────
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// ── Neutral / Surface ──────────────────────────────────────────────
val md_theme_light_background = Color(0xFFF9F9FF)
val md_theme_light_onBackground = Color(0xFF191C20)
val md_theme_light_surface = Color(0xFFF9F9FF)
val md_theme_light_onSurface = Color(0xFF191C20)
val md_theme_light_surfaceVariant = Color(0xFFE0E2EC)
val md_theme_light_onSurfaceVariant = Color(0xFF44474E)
val md_theme_light_outline = Color(0xFF74777F)
val md_theme_light_outlineVariant = Color(0xFFC4C6D0)
val md_theme_light_inverseSurface = Color(0xFF2E3036)
val md_theme_light_inverseOnSurface = Color(0xFFF0F0F7)
val md_theme_light_inversePrimary = Color(0xFFAAC7FF)
val md_theme_light_surfaceTint        = Color(0xFF2563EB)

val md_theme_dark_background = Color(0xFF111318)
val md_theme_dark_onBackground = Color(0xFFE2E2E9)
val md_theme_dark_surface = Color(0xFF111318)
val md_theme_dark_onSurface = Color(0xFFE2E2E9)
val md_theme_dark_surfaceVariant = Color(0xFF44474E)
val md_theme_dark_onSurfaceVariant = Color(0xFFC4C6D0)
val md_theme_dark_outline = Color(0xFF8E9099)
val md_theme_dark_outlineVariant = Color(0xFF44474E)
val md_theme_dark_inverseSurface = Color(0xFFE2E2E9)
val md_theme_dark_inverseOnSurface = Color(0xFF2E3036)
val md_theme_dark_inversePrimary      = Color(0xFF2563EB)
val md_theme_dark_surfaceTint         = Color(0xFF93C5FD)

// ── Status / Semantic Colors ─────────────────────────────────────────────
// Spec: COMP1786 UI/UX Design Specification §2 – Semantic Status System

// ACTIVE → Deep Green  (#1B5E20 / #E8F5E9)
val StatusActiveContainer = Color(0xFFE8F5E9)
val StatusActiveContent   = Color(0xFF1B5E20)

// COMPLETED → Blue  (#01579B / #E1F5FE)
val StatusCompletedContainer = Color(0xFFE1F5FE)
val StatusCompletedContent   = Color(0xFF01579B)

// ON HOLD / PENDING → Deep Orange  (#E65100 / #FFF3E0)
val StatusOnHoldContainer = Color(0xFFFFF3E0)
val StatusOnHoldContent   = Color(0xFFE65100)

// CANCELLED / REJECTED → Dark Red  (#B71C1C / #FFEBEE)
val StatusCancelledContainer = Color(0xFFFFEBEE)
val StatusCancelledContent   = Color(0xFFB71C1C)

// ── Priority Colors ───────────────────────────────────────────────────────
val PriorityHighContainer   = Color(0xFFFFEBEE)
val PriorityHighContent     = Color(0xFFB71C1C)
val PriorityNormalContainer = Color(0xFFE1F5FE)
val PriorityNormalContent   = Color(0xFF01579B)
val PriorityLowContainer    = Color(0xFFE8F5E9)
val PriorityLowContent      = Color(0xFF1B5E20)

// ── Expense Payment Status Colors ─────────────────────────────────────────
// PAID → Purple  (#4A148C / #F3E5F5)
val PaidContainer       = Color(0xFFF3E5F5)
val PaidContent         = Color(0xFF4A148C)
// PENDING → Deep Orange  (#E65100 / #FFF3E0)
val PendingContainer    = Color(0xFFFFF3E0)
val PendingContent      = Color(0xFFE65100)
// REIMBURSED → Blue  (#01579B / #E1F5FE)
val ReimbursedContainer = Color(0xFFE1F5FE)
val ReimbursedContent   = Color(0xFF01579B)

// ── Dark-mode variants ────────────────────────────────────────────────────
val StatusActiveContainerDark    = Color(0xFF1B5E20)
val StatusActiveContentDark      = Color(0xFFE8F5E9)
val StatusCompletedContainerDark = Color(0xFF01579B)
val StatusCompletedContentDark   = Color(0xFFE1F5FE)
val StatusOnHoldContainerDark    = Color(0xFFE65100)
val StatusOnHoldContentDark      = Color(0xFFFFF3E0)
val StatusCancelledContainerDark = Color(0xFFB71C1C)
val StatusCancelledContentDark   = Color(0xFFFFEBEE)

// Keep old colors for backward compatibility
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)