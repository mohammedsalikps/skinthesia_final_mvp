package com.skinthesia.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AccountRoute
import com.skinthesia.core.navigation.ConnectedDeviceRoute
import com.skinthesia.core.navigation.DataControlsRoute
import com.skinthesia.core.navigation.EditGoalsRoute
import com.skinthesia.core.navigation.EditProfileRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MeasurementHistoryRoute
import com.skinthesia.core.navigation.MyBookingsRoute
import com.skinthesia.core.navigation.MyProductsRoute
import com.skinthesia.core.navigation.OrdersRoute
import com.skinthesia.core.navigation.PlanRoute
import com.skinthesia.core.navigation.PrivacyRoute
import com.skinthesia.core.navigation.SkinPrintRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.ListRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MiniRing
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.BookingStatus
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.domain.repository.OrderRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val latest: Assessment? = null,
    val currentWeek: Int = 0,
    val orders: Int = 0,
    val upcomingBookings: Int = 0,
    val sessions: Int = 0,
)

class ProfileViewModel(
    profiles: UserProfileRepository,
    assessments: AssessmentRepository,
    orders: OrderRepository,
    consultations: ConsultationRepository,
    sessions: SkinProbeRepository,
    private val clock: () -> Long,
) : ViewModel() {
    val state: StateFlow<ProfileUiState> = combine(
        profiles.profile,
        assessments.completed(),
        orders.orders,
        consultations.bookings,
        sessions.sessions(),
    ) { profile, completed, orderList, bookings, sessionList ->
        ProfileUiState(
            loading = false,
            profile = profile,
            latest = completed.lastOrNull { it.skinPrint != null },
            currentWeek = completed.maxOfOrNull { it.week } ?: 0,
            orders = orderList.size,
            upcomingBookings = bookings.count { it.status == BookingStatus.CONFIRMED && it.startAt > clock() },
            sessions = sessionList.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}

private fun count(n: Int, one: String, many: String, none: String): String = when (n) {
    0 -> none
    1 -> "1 $one"
    else -> "$n $many"
}

/** Screen 33: profile and settings. SkinPrint, goals, routine, products, devices, privacy and account. */
@Composable
fun ProfileScreen(@Suppress("UNUSED_PARAMETER") onSelectTab: (Int) -> Unit) {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { ProfileViewModel(profiles, assessments, orders, consultations, probeRepository, clock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val context = LocalContext.current
    val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "" }

    SkinthesiaScreen(insetBottom = false) {
        if (state.loading) {
            LoadingState(message = "Loading your profile")
            return@SkinthesiaScreen
        }
        val profile = state.profile
        Spacer(Modifier.height(spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(colors.primaryMist), contentAlignment = Alignment.Center) {
                Text(text = profile.firstName.take(1).uppercase().ifBlank { "S" }, style = typography.title, color = colors.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(text = profile.firstName.ifBlank { "Your profile" }, style = typography.display, color = colors.textPrimary)
                Text(
                    text = listOfNotNull(
                        "Week ${state.currentWeek.coerceAtLeast(1)} of ${profile.goals.durationWeeks}",
                        profile.skin.skinType?.let { "${it.label} skin" },
                    ).joinToString(" · "),
                    style = typography.caption,
                    color = colors.textMuted,
                )
            }
        }
        Spacer(Modifier.height(spacing.lg))

        val latest = state.latest
        val skinPrint = latest?.skinPrint
        if (latest != null && skinPrint != null) {
            FadeInUp {
                SkinthesiaCard(onClick = { navigator.navigate(SkinPrintRoute(latest.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiniRing(value = skinPrint.overall, size = 58.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            SectionOverline(text = "My SkinPrint")
                            Spacer(Modifier.height(2.dp))
                            Text(text = skinPrint.band.label + " · " + latest.weekLabel, style = typography.labelLarge, color = colors.textPrimary)
                        }
                        Icon(SkinthesiaIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(spacing.lg))
        }

        ProfileSection("Your skin") {
            ListRow(
                title = "Goals",
                subtitle = profile.goals.ranked.take(3).joinToString(", ") { it.label }.ifEmpty { "Choose what to work on" },
                leadingIcon = SkinthesiaIcons.Target,
                onClick = { navigator.navigate(EditGoalsRoute) },
            )
            SkinthesiaDivider()
            ListRow(title = "Routine", subtitle = "Morning and evening steps", leadingIcon = SkinthesiaIcons.Layers, onClick = { navigator.navigate(PlanRoute()) })
            SkinthesiaDivider()
            ListRow(title = "Products", subtitle = "Your routine, orders and what you use", leadingIcon = SkinthesiaIcons.Products, onClick = { navigator.navigate(MyProductsRoute) })
            SkinthesiaDivider()
            ListRow(title = "Skin profile", subtitle = "Skin type, concerns and preferences", leadingIcon = SkinthesiaIcons.Face, onClick = { navigator.navigate(EditProfileRoute) })
        }
        ProfileSection("Measurements") {
            ListRow(
                title = "Measurement history",
                subtitle = count(state.sessions, "probe session", "probe sessions", "No probe sessions yet"),
                leadingIcon = SkinthesiaIcons.Clock,
                onClick = { navigator.navigate(MeasurementHistoryRoute) },
            )
            SkinthesiaDivider()
            ListRow(
                title = "Connected device",
                subtitle = if (profile.pairedDeviceId != null) "Skinthesia Probe · paired" else "No probe paired",
                leadingIcon = SkinthesiaIcons.Bluetooth,
                onClick = { navigator.navigate(ConnectedDeviceRoute) },
            )
        }
        ProfileSection("Shop and care") {
            ListRow(title = "Orders", subtitle = count(state.orders, "order", "orders", "No orders yet"), leadingIcon = SkinthesiaIcons.Receipt, onClick = { navigator.navigate(OrdersRoute) })
            SkinthesiaDivider()
            ListRow(
                title = "Consultations",
                subtitle = count(state.upcomingBookings, "upcoming booking", "upcoming bookings", "No upcoming bookings"),
                leadingIcon = SkinthesiaIcons.Calendar,
                onClick = { navigator.navigate(MyBookingsRoute) },
            )
        }
        ProfileSection("Privacy and data") {
            ListRow(title = "Privacy", subtitle = "How your data is handled", leadingIcon = SkinthesiaIcons.Lock, onClick = { navigator.navigate(PrivacyRoute) })
            SkinthesiaDivider()
            ListRow(title = "Data controls", subtitle = "Demo settings and deletion", leadingIcon = SkinthesiaIcons.Shield, onClick = { navigator.navigate(DataControlsRoute) })
        }
        ProfileSection("Account") {
            ListRow(title = "Account", subtitle = "Stored on this device only", leadingIcon = SkinthesiaIcons.User, onClick = { navigator.navigate(AccountRoute) })
        }
        Text(
            text = "Skinthesia $version · demonstration build",
            style = typography.caption,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
internal fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    SectionOverline(text = title)
    Spacer(Modifier.height(10.dp))
    SkinthesiaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), content = content)
    Spacer(Modifier.height(SkinthesiaTheme.spacing.md))
}
