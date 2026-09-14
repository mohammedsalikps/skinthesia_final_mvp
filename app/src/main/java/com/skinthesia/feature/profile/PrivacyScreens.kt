package com.skinthesia.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.DataControlsRoute
import com.skinthesia.core.navigation.EditProfileRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ProductDetailRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.ListRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaConfirmDialog
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDay
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AppSettings
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.RoutineStep
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.domain.repository.OrderRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.DataControlsUseCase
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.plan.ProductThumb
import com.skinthesia.hardware.probe.SkinProbeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How data is handled in this build, in plain words. */
@Composable
fun PrivacyScreen() {
    val navigator = LocalAppNavigator.current
    val spacing = SkinthesiaTheme.spacing
    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Privacy", onBack = navigator::back) },
        bottomBar = { SkinthesiaSecondaryButton(text = "Open Data Controls", onClick = { navigator.navigate(DataControlsRoute) }) },
    ) {
        ScreenHeader(title = "Your data stays with you", subtitle = "What Skinthesia keeps, where it lives and what is simulated in this build.")
        Spacer(Modifier.height(spacing.lg))
        PrivacyPoint(
            icon = SkinthesiaIcons.Lock,
            title = "Everything stays on this device",
            body = "Your profile, photos, readings, plans, orders, bookings and posts are stored in the app's private storage. This build has no servers, no analytics and no advertising.",
        )
        PrivacyPoint(
            icon = SkinthesiaIcons.Camera,
            title = "Photos are private",
            body = "Selfies are used for on-device quality checks and your progress comparisons. They are never uploaded, never shown to experts and never posted to the community.",
        )
        PrivacyPoint(
            icon = SkinthesiaIcons.Eye,
            title = "Minimal permissions",
            body = "The camera is requested only when you take a selfie. Nearby-device access will be requested only once real probe hardware is supported.",
        )
        PrivacyPoint(
            icon = SkinthesiaIcons.Info,
            title = "What's simulated",
            body = "Visual estimates come from a development model, probe readings come from a simulator, and checkout and bookings are demonstrations. Each is labelled wherever it appears.",
        )
        PrivacyPoint(
            icon = SkinthesiaIcons.Shield,
            title = "Not a medical device",
            body = "Skinthesia describes how skin looks and reads to support your routine. It doesn't diagnose conditions. Speak to a dermatologist about anything that worries you.",
        )
        PrivacyPoint(
            icon = SkinthesiaIcons.Trash,
            title = "You're in control",
            body = "Delete your photos, or every piece of data, at any time from Data controls.",
        )
    }
}

@Composable
private fun PrivacyPoint(icon: ImageVector, title: String, body: String) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(Modifier.fillMaxWidth().padding(bottom = 18.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.Top) {
        IconBadge(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, style = typography.labelLarge, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = typography.bodySmall, color = colors.textSecondary)
        }
    }
}

data class DataSummary(val photos: Int, val analyses: Int, val sessions: Int, val orders: Int, val bookings: Int)

class DataControlsViewModel(
    private val settings: SettingsRepository,
    assessments: AssessmentRepository,
    sessions: SkinProbeRepository,
    orders: OrderRepository,
    consultations: ConsultationRepository,
    private val dataControls: DataControlsUseCase,
    private val probe: SkinProbeManager,
) : ViewModel() {

    val settingsState: StateFlow<AppSettings?> = settings.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val summary: StateFlow<DataSummary?> = combine(assessments.completed(), sessions.sessions(), orders.orders, consultations.bookings) { a, s, o, b ->
        DataSummary(photos = a.count { it.photo != null }, analyses = a.size, sessions = s.size, orders = o.size, bookings = b.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settings.update(transform) }
    }

    fun deletePhotos() {
        viewModelScope.launch {
            dataControls.deletePhotos()
            _message.value = "Your photos were deleted. Scores and readings are kept."
        }
    }

    fun deleteEverything(onDone: () -> Unit) {
        viewModelScope.launch {
            probe.disconnect()
            dataControls.deleteEverything()
            onDone()
        }
    }
}

/** Demonstration settings and deletion, with honest descriptions of what each switch does. */
@Composable
fun DataControlsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel {
        DataControlsViewModel(settings, assessments, probeRepository, orders, consultations, dataControls, probeManager)
    }
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    var confirmPhotos by remember { mutableStateOf(false) }
    var confirmAll by remember { mutableStateOf(false) }

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "Data controls", onBack = navigator::back) }) {
        val s = settingsState
        if (s == null) {
            LoadingState(message = "Loading")
            return@SkinthesiaScreen
        }
        summary?.let { data ->
            SectionOverline(text = "Stored on this device")
            Spacer(Modifier.height(10.dp))
            SkinthesiaCard {
                KeyValueRow(label = "Photos", value = data.photos.toString())
                KeyValueRow(label = "Analyses", value = data.analyses.toString())
                KeyValueRow(label = "Probe sessions", value = data.sessions.toString())
                KeyValueRow(label = "Orders", value = data.orders.toString())
                KeyValueRow(label = "Bookings", value = data.bookings.toString())
            }
            Spacer(Modifier.height(spacing.lg))
        }
        SectionOverline(text = "Demonstration settings")
        Spacer(Modifier.height(10.dp))
        SkinthesiaCard {
            SettingSwitch(
                title = "Demo timeline",
                body = "Each check-in advances to the next milestone week (4, 8, 12), so the programme can be shown in one sitting.",
                checked = s.demoTimeline,
                onChange = { v -> viewModel.update { it.copy(demoTimeline = v) } },
            )
            SkinthesiaDivider()
            SettingSwitch(
                title = "Simulate probe problems",
                body = "The next connection fails once and the forehead reading loses contact once, so recovery can be shown.",
                checked = s.simulateProbeFailure,
                onChange = { v -> viewModel.update { it.copy(simulateProbeFailure = v) } },
            )
            SkinthesiaDivider()
            SettingSwitch(
                title = "Personalised content",
                body = "Picks guides and community posts around your goals.",
                checked = s.personalizedContent,
                onChange = { v -> viewModel.update { it.copy(personalizedContent = v) } },
            )
            SkinthesiaDivider()
            SettingSwitch(
                title = "Routine reminders",
                body = "Saved as a preference. Reminders aren't scheduled in this build.",
                checked = s.routineReminders,
                onChange = { v -> viewModel.update { it.copy(routineReminders = v) } },
            )
            SkinthesiaDivider()
            SettingSwitch(
                title = "Share anonymous usage",
                body = "This build collects no analytics, so there is nothing to share.",
                checked = false,
                enabled = false,
                onChange = {},
            )
        }
        Spacer(Modifier.height(spacing.lg))
        SectionOverline(text = "Delete data")
        Spacer(Modifier.height(10.dp))
        SkinthesiaCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            ListRow(
                title = "Delete my photos",
                subtitle = "Removes every selfie. Scores and readings stay.",
                leadingIcon = SkinthesiaIcons.Trash,
                onClick = { confirmPhotos = true },
            )
            SkinthesiaDivider()
            ListRow(
                title = "Delete everything",
                subtitle = "Erases all data on this device and starts over.",
                leadingIcon = SkinthesiaIcons.Trash,
                onClick = { confirmAll = true },
                titleColor = colors.negative,
                iconTint = colors.negative,
            )
        }
        message?.let {
            Spacer(Modifier.height(spacing.md))
            Row(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
                Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = it, style = typography.bodySmall, color = colors.textSecondary)
            }
        }
    }

    if (confirmPhotos) {
        SkinthesiaConfirmDialog(
            title = "Delete your photos?",
            body = "Every selfie is removed from this device. Your SkinPrint scores and probe readings are kept.",
            confirmLabel = "Delete photos",
            destructive = true,
            onConfirm = {
                viewModel.deletePhotos()
                confirmPhotos = false
            },
            onDismiss = { confirmPhotos = false },
        )
    }
    if (confirmAll) {
        SkinthesiaConfirmDialog(
            title = "Delete everything?",
            body = "Your profile, photos, readings, plans, orders, bookings and posts are erased from this device. This can't be undone.",
            confirmLabel = "Delete everything",
            destructive = true,
            onConfirm = {
                confirmAll = false
                viewModel.deleteEverything(navigator::restart)
            },
            onDismiss = { confirmAll = false },
        )
    }
}

@Composable
internal fun SettingSwitch(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = typography.labelLarge, color = if (enabled) colors.textPrimary else colors.textMuted)
            Spacer(Modifier.height(2.dp))
            Text(text = body, style = typography.bodySmall, color = colors.textSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.surface,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.track,
                uncheckedBorderColor = colors.border,
            ),
        )
    }
}

class AccountViewModel(profiles: UserProfileRepository) : ViewModel() {
    val profile: StateFlow<UserProfile?> = profiles.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** The local account: what exists, where it lives, and what sign-in would add later. */
@Composable
fun AccountScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { AccountViewModel(profiles) }
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val context = LocalContext.current
    val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "" }

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Account", onBack = navigator::back) },
        bottomBar = { SkinthesiaSecondaryButton(text = "Edit Profile", onClick = { navigator.navigate(EditProfileRoute) }) },
    ) {
        val p = profile
        if (p == null) {
            LoadingState(message = "Loading")
            return@SkinthesiaScreen
        }
        SkinthesiaCard {
            KeyValueRow(label = "Name", value = p.firstName.ifBlank { "Not set" })
            KeyValueRow(label = "Profile created", value = if (p.createdAt > 0) formatDay(p.createdAt) else "Today")
            KeyValueRow(label = "Programme started", value = p.goals.startedAt?.let(::formatDay) ?: "After your first analysis")
            KeyValueRow(label = "Stored", value = "On this device only")
            KeyValueRow(label = "App version", value = version)
        }
        Spacer(Modifier.height(spacing.md))
        SkinthesiaCard(containerColor = colors.blushMist, borderColor = Color.Transparent) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(SkinthesiaIcons.Info, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Sign-in and cloud backup aren't part of this demonstration build. Your journey lives only on this device, and removing the app removes it.",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

data class MyProductsUiState(
    val loading: Boolean = true,
    val routine: List<Pair<RoutineStep, Product>> = emptyList(),
    val ordered: List<Product> = emptyList(),
    val categories: Set<ProductCategory> = emptySet(),
    val note: String = "",
)

class MyProductsViewModel(
    plans: PlanRepository,
    catalog: ProductCatalogRepository,
    orders: OrderRepository,
    profiles: UserProfileRepository,
) : ViewModel() {
    val state: StateFlow<MyProductsUiState> = combine(plans.currentPlan, catalog.products, orders.orders, profiles.profile) { plan, products, orderList, profile ->
        val byId = products.associateBy { it.id }
        MyProductsUiState(
            loading = false,
            routine = plan?.allSteps.orEmpty()
                .mapNotNull { step -> step.productId?.let(byId::get)?.let { step to it } }
                .distinctBy { it.second.id },
            ordered = orderList.flatMap { it.lines }.map { it.productId }.distinct().mapNotNull(byId::get),
            categories = profile.skin.currentProducts,
            note = profile.skin.existingProductsNote,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyProductsUiState())
}

/** Products in the routine, from orders, and what the user already owns. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyProductsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { MyProductsViewModel(plans, catalog, orders, profiles) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "My products", onBack = navigator::back) }) {
        if (state.loading) {
            LoadingState(message = "Loading")
            return@SkinthesiaScreen
        }
        if (state.routine.isEmpty() && state.ordered.isEmpty() && state.categories.isEmpty()) {
            EmptyState(icon = SkinthesiaIcons.Products, title = "No products yet", body = "Products from your plan and orders appear here.")
            return@SkinthesiaScreen
        }
        if (state.routine.isNotEmpty()) {
            SectionOverline(text = "In your routine")
            Spacer(Modifier.height(10.dp))
            state.routine.forEach { (step, product) ->
                ProductLine(product = product, detail = step.time.label + " · " + step.type.label) { navigator.navigate(ProductDetailRoute(product.id)) }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(spacing.md))
        }
        if (state.ordered.isNotEmpty()) {
            SectionOverline(text = "From your orders")
            Spacer(Modifier.height(10.dp))
            state.ordered.forEach { product ->
                ProductLine(product = product, detail = product.category.label + " · Simulated order") { navigator.navigate(ProductDetailRoute(product.id)) }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(spacing.md))
        }
        if (state.categories.isNotEmpty() || state.note.isNotBlank()) {
            SectionOverline(text = "What you told us you use")
            Spacer(Modifier.height(10.dp))
            SkinthesiaCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categories.forEach { Tag(text = it.label, tone = TagTone.NEUTRAL) }
                }
                if (state.note.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(text = "“${state.note}”", style = typography.body, color = colors.textSecondary)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "Your plan keeps what already works for you where it can.", style = typography.caption, color = colors.textMuted)
            }
        }
    }
}

@Composable
private fun ProductLine(product: Product, detail: String, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProductThumb(product, size = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = product.brand, style = typography.caption, color = colors.textMuted)
                Text(text = product.name, style = typography.label, color = colors.textPrimary)
                Text(text = detail, style = typography.caption, color = colors.textSecondary)
            }
            Icon(SkinthesiaIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
        }
    }
}
