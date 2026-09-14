package com.skinthesia.feature.consultation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.BookingConfirmationRoute
import com.skinthesia.core.navigation.BookingRoute
import com.skinthesia.core.navigation.ExpertsRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MyBookingsRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.CheckCircle
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.FilterPill
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MonogramAvatar
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaConfirmDialog
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDateTime
import com.skinthesia.core.ui.formatPrice
import com.skinthesia.core.ui.formatTime
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Booking
import com.skinthesia.domain.model.BookingStatus
import com.skinthesia.domain.model.ConsultationExpert
import com.skinthesia.domain.model.ConsultationSlot
import com.skinthesia.domain.model.ConsultationType
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.feature.analysis.IconBadge
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle

data class BookingUiState(
    val loading: Boolean = true,
    val expert: ConsultationExpert? = null,
    val type: ConsultationType? = null,
    val date: LocalDate = LocalDate.now(),
    val slots: List<ConsultationSlot> = emptyList(),
    val slotsLoading: Boolean = true,
    val slot: ConsultationSlot? = null,
    val notes: String = "",
    val share: Boolean = false,
    val hasSkinPrint: Boolean = false,
    val booking: Boolean = false,
)

class BookingViewModel(
    val expertId: String,
    private val consultations: ConsultationRepository,
    private val assessments: AssessmentRepository,
    private val clock: () -> Long,
) : ViewModel() {

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()
    private var slotJob: Job? = null
    val dates: List<LocalDate> = (0L until 10L).map { LocalDate.now().plusDays(it) }

    init {
        viewModelScope.launch {
            val expert = consultations.expert(expertId)
            val hasPrint = assessments.completedList().any { it.skinPrint != null }
            _state.update {
                it.copy(
                    loading = false,
                    expert = expert,
                    type = expert?.consultationTypes?.minByOrNull { type -> type.ordinal },
                    hasSkinPrint = hasPrint,
                    share = hasPrint,
                )
            }
        }
        loadSlots(_state.value.date)
    }

    fun selectDate(date: LocalDate) {
        _state.update { it.copy(date = date, slot = null) }
        loadSlots(date)
    }

    fun selectType(type: ConsultationType) = _state.update { it.copy(type = type) }
    fun selectSlot(slot: ConsultationSlot) = _state.update { it.copy(slot = slot) }
    fun setNotes(value: String) = _state.update { it.copy(notes = value.take(500)) }
    fun setShare(value: Boolean) = _state.update { it.copy(share = value) }

    private fun loadSlots(date: LocalDate) {
        slotJob?.cancel()
        slotJob = viewModelScope.launch {
            _state.update { it.copy(slotsLoading = true) }
            val now = clock()
            val slots = consultations.slots(expertId, date).filter { it.isAvailable && it.startAt > now }
            _state.update { it.copy(slots = slots, slotsLoading = false) }
        }
    }

    /** Records a simulated booking locally. No calendar or payment provider is involved. */
    fun book(onBooked: (String) -> Unit) {
        val s = _state.value
        val expert = s.expert ?: return
        val slot = s.slot ?: return
        val type = s.type ?: return
        if (s.booking) return
        _state.update { it.copy(booking = true) }
        viewModelScope.launch {
            val booking = consultations.book(expert, slot, type, s.notes.trim(), s.share && s.hasSkinPrint)
            _state.update { it.copy(booking = false) }
            onBooked(booking.id)
        }
    }
}

/** Booking: type, date, time, a note, and whether to share the SkinPrint report. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> BookingViewModel(handle.toRoute<BookingRoute>().expertId, consultations, assessments, clock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val expert = state.expert

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Book a consultation", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = if (state.slot == null) "Choose a time" else "Book Consultation · " + formatPrice(expert?.fee ?: 0),
                enabled = state.slot != null && state.type != null && !state.booking,
                loading = state.booking,
                onClick = { viewModel.book { id -> navigator.replace(BookingConfirmationRoute(id)) } },
            )
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading availability")
            expert == null -> ScreenHeader(title = "Expert not found", centered = true)
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonogramAvatar(name = expert.name, size = 46.dp, isExpert = true)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = expert.name, style = typography.labelLarge, color = colors.textPrimary)
                        Text(text = expert.title, style = typography.caption, color = colors.textMuted)
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "Consultation type")
                Spacer(Modifier.height(10.dp))
                expert.consultationTypes.sortedBy { it.ordinal }.forEach { type ->
                    TypeOption(type = type, isSelected = state.type == type, onClick = { viewModel.selectType(type) })
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(spacing.sm))
                SectionOverline(text = "Date")
                Spacer(Modifier.height(10.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.dates.forEach { date ->
                        DateChip(date = date, isSelected = date == state.date, isToday = date == viewModel.dates.first()) { viewModel.selectDate(date) }
                    }
                }
                Spacer(Modifier.height(spacing.md))
                SectionOverline(text = "Time")
                Spacer(Modifier.height(10.dp))
                when {
                    state.slotsLoading -> Text(text = "Checking availability…", style = typography.body, color = colors.textMuted)
                    state.slots.isEmpty() -> Text(text = "No times left on this day. Try another date.", style = typography.body, color = colors.textSecondary)
                    else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.slots.forEach { slot ->
                            FilterPill(label = formatTime(slot.startAt), selected = state.slot?.id == slot.id, onClick = { viewModel.selectSlot(slot) })
                        }
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "What would you like to discuss?")
                Spacer(Modifier.height(10.dp))
                SkinthesiaTextField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    placeholder = "Optional. For example, dryness on my cheeks.",
                    singleLine = false,
                    minLines = 3,
                    maxLength = 500,
                )
                Spacer(Modifier.height(spacing.md))
                ShareCard(enabled = state.hasSkinPrint, checked = state.share && state.hasSkinPrint, onChange = viewModel::setShare)
                Spacer(Modifier.height(spacing.md))
                InfoNotice(text = "Booking is simulated in this build. No consultation takes place and no payment is taken.")
            }
        }
    }
}

@Composable
private fun TypeOption(type: ConsultationType, isSelected: Boolean, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(
        modifier = Modifier.semantics {
            selected = isSelected
            role = Role.RadioButton
        },
        onClick = onClick,
        containerColor = if (isSelected) colors.primaryMist else colors.surface,
        borderColor = if (isSelected) colors.primary else colors.border,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(type.icon, container = if (isSelected) colors.surface else colors.primaryMist)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = type.label, style = typography.labelLarge, color = colors.textPrimary)
                Text(text = type.description + " · ${type.durationMinutes} min", style = typography.bodySmall, color = colors.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            CheckCircle(selected = isSelected, size = 22.dp)
        }
    }
}

@Composable
private fun DateChip(date: LocalDate, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val shape = SkinthesiaTheme.shapes.tile
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(shape)
            .background(if (isSelected) colors.primary else colors.surface)
            .border(1.dp, if (isSelected) colors.primary else colors.border, shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (isToday) "Today" else date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, locale),
            style = typography.caption,
            color = if (isSelected) colors.textOnPrimary.copy(alpha = 0.85f) else colors.textMuted,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = date.dayOfMonth.toString(), style = typography.titleSmall, color = if (isSelected) colors.textOnPrimary else colors.textPrimary)
        Text(
            text = date.month.getDisplayName(DateTextStyle.SHORT, locale),
            style = typography.caption,
            color = if (isSelected) colors.textOnPrimary.copy(alpha = 0.85f) else colors.textMuted,
        )
    }
}

@Composable
private fun ShareCard(enabled: Boolean, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard {
        Row(
            modifier = Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = "Share my SkinPrint report", style = typography.labelLarge, color = if (enabled) colors.textPrimary else colors.textMuted)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (enabled) "Your expert sees your latest report and readings. Photos are never shared." else "Available after your first analysis.",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
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
}

class BookingConfirmationViewModel(val bookingId: String, consultations: ConsultationRepository) : ViewModel() {
    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _booking.value = consultations.booking(bookingId)
            _loading.value = false
        }
    }
}

/** Booking confirmation, clearly marked as simulated. */
@Composable
fun BookingConfirmationScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> BookingConfirmationViewModel(handle.toRoute<BookingConfirmationRoute>().bookingId, consultations) }
    val booking by viewModel.booking.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    BackHandler { navigator.goHome() }

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Booking confirmed", onBack = navigator::goHome) },
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Back to Home", onClick = navigator::goHome)
            SkinthesiaTextButton(text = "View my bookings", onClick = { navigator.navigate(MyBookingsRoute) })
        },
    ) {
        val b = booking
        when {
            loading -> LoadingState(message = "Confirming")
            b == null -> ScreenHeader(title = "Booking not found", centered = true)
            else -> {
                FadeInUp {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(68.dp).clip(CircleShape).background(colors.successSoft), contentAlignment = Alignment.Center) {
                            Icon(SkinthesiaIcons.Calendar, contentDescription = null, tint = colors.success, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(spacing.md))
                        Text(text = "You're booked in", style = typography.title, color = colors.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(text = formatDateTime(b.startAt), style = typography.subtitle, color = colors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        if (b.isSimulated) Tag(text = "Simulated booking", tone = TagTone.GOLD, dot = true)
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                SkinthesiaCard {
                    KeyValueRow(label = "Expert", value = b.expertName)
                    KeyValueRow(label = "Type", value = b.type.label)
                    KeyValueRow(label = "Duration", value = "${b.durationMinutes} min")
                    KeyValueRow(label = "SkinPrint report", value = if (b.shareSkinPrint) "Shared" else "Not shared")
                }
                if (b.notes.isNotBlank()) {
                    Spacer(Modifier.height(spacing.md))
                    SkinthesiaCard {
                        SectionOverline(text = "Your note")
                        Spacer(Modifier.height(6.dp))
                        Text(text = b.notes, style = typography.body, color = colors.textPrimary)
                    }
                }
                Spacer(Modifier.height(spacing.md))
                Text(
                    text = "This is a demonstration booking. No consultation will take place.",
                    style = typography.caption,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

class MyBookingsViewModel(private val consultations: ConsultationRepository, private val clock: () -> Long) : ViewModel() {
    val bookings: StateFlow<List<Booking>?> = consultations.bookings
        .map { list -> list.sortedBy { it.startAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun now(): Long = clock()

    fun cancel(id: String) {
        viewModelScope.launch { consultations.cancel(id) }
    }
}

/** Upcoming and past consultations, with cancellation for upcoming ones. */
@Composable
fun MyBookingsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { MyBookingsViewModel(consultations, clock) }
    val list by viewModel.bookings.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    var cancelling by remember { mutableStateOf<Booking?>(null) }

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "My bookings", onBack = navigator::back) }) {
        val bookings = list
        when {
            bookings == null -> LoadingState(message = "Loading bookings")
            bookings.isEmpty() -> EmptyState(
                icon = SkinthesiaIcons.Calendar,
                title = "No bookings yet",
                body = "Book a consultation with an expert and it appears here.",
                actionLabel = "Find an expert",
                onAction = { navigator.navigate(ExpertsRoute) },
            )
            else -> {
                val now = viewModel.now()
                val (upcoming, past) = bookings.partition { it.status == BookingStatus.CONFIRMED && it.startAt > now }
                if (upcoming.isNotEmpty()) {
                    SectionHeader(title = "Upcoming")
                    Spacer(Modifier.height(12.dp))
                    upcoming.forEach { booking ->
                        BookingCard(booking = booking, onCancel = { cancelling = booking })
                        Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.height(spacing.sm))
                }
                if (past.isNotEmpty()) {
                    SectionHeader(title = "Past and cancelled")
                    Spacer(Modifier.height(12.dp))
                    past.asReversed().forEach { booking ->
                        BookingCard(booking = booking, onCancel = null)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    cancelling?.let { booking ->
        SkinthesiaConfirmDialog(
            title = "Cancel this booking?",
            body = "Your ${booking.type.label.lowercase()} with ${booking.expertName} on ${formatDateTime(booking.startAt)} will be cancelled.",
            confirmLabel = "Cancel booking",
            dismissLabel = "Keep it",
            destructive = true,
            onConfirm = {
                viewModel.cancel(booking.id)
                cancelling = null
            },
            onDismiss = { cancelling = null },
        )
    }
}

@Composable
private fun BookingCard(booking: Booking, onCancel: (() -> Unit)?) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonogramAvatar(name = booking.expertName, size = 42.dp, isExpert = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = booking.expertName, style = typography.labelLarge, color = colors.textPrimary)
                Text(text = booking.type.label + " · " + formatDateTime(booking.startAt), style = typography.caption, color = colors.textMuted)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(text = booking.status.label, tone = if (booking.status == BookingStatus.CONFIRMED) TagTone.SAGE else TagTone.NEUTRAL)
            if (booking.isSimulated) {
                Spacer(Modifier.width(8.dp))
                Tag(text = "Simulated", tone = TagTone.GOLD, dot = true)
            }
            Spacer(Modifier.weight(1f))
            if (onCancel != null) {
                Text(
                    text = "Cancel",
                    style = typography.label,
                    color = colors.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onCancel)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
