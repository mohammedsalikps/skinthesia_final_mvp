package com.skinthesia.feature.onboarding

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.skinthesia.R
import com.skinthesia.camera.CameraPreview
import com.skinthesia.camera.SkinCameraController
import com.skinthesia.camera.rememberSkinCameraController
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.OnboardingRoutes
import com.skinthesia.core.ui.components.FaceGuideOverlay
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SkinthesiaIconButton
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.pressScale
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.core.utils.findActivity
import com.skinthesia.core.utils.hasCameraPermission
import com.skinthesia.core.utils.openAppSettings
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.LensFacing
import kotlinx.coroutines.delay
import java.io.File

private const val NOTICE_DURATION_MS = 3200L

/**
 * Screen 02: live CameraX viewfinder with a face guide, capture, flip, retake
 * and gallery import. On standard phones the viewfinder claims the remaining
 * height so the whole step fits on one screen; short screens and large text
 * fall back to a scrolling layout.
 */
@Composable
fun TakePhotoScreen(
    state: OnboardingUiState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onCapture: (LensFacing, suspend (File) -> Unit) -> Unit,
    onImport: (Uri) -> Unit,
    onRetake: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(context.hasCameraPermission()) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (!granted) {
            val activity = context.findActivity()
            permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onImport(uri)
    }
    val openGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val photo = state.photo
    val busy = state.isBusyWithPhoto
    val showError = state.notice == OnboardingNotice.CAPTURE_FAILED || state.notice == OnboardingNotice.IMPORT_FAILED

    LaunchedEffect(Unit) {
        if (!permissionGranted && !permissionRequested && photo == null) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LifecycleResumeEffect(Unit) {
        permissionGranted = context.hasCameraPermission()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.notice) {
        if (showError) {
            delay(NOTICE_DURATION_MS)
            onDismissNotice()
        }
    }

    // The camera is bound only while it is needed, so capturing a photo releases it.
    val camera: SkinCameraController? = if (permissionGranted && photo == null) rememberSkinCameraController() else null
    val cameraUsable = camera != null && camera.isReady && camera.hasAnyCamera

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val fontScale = LocalDensity.current.fontScale
        // Regular phones: the viewfinder claims the remaining height. Short phones keep
        // that single-screen layout by folding the gallery link into the controls row.
        // Very short screens or large text scroll instead, so nothing is ever clipped.
        val layout = when {
            fontScale >= 1.3f || maxHeight < 560.dp -> PhotoLayout.SCROLL
            maxHeight < 680.dp -> PhotoLayout.SHORT
            else -> PhotoLayout.REGULAR
        }
        val compact = layout == PhotoLayout.SCROLL

        OnboardingScaffold(
            step = 2,
            totalSteps = OnboardingRoutes.TOTAL_STEPS,
            onBack = onBack,
            scrollable = compact,
            bottomBar = {
                Text(
                    text = stringResource(
                        when {
                            photo != null -> R.string.photo_hint_captured
                            cameraUsable -> R.string.photo_hint_align
                            else -> R.string.photo_hint_required
                        },
                    ),
                    style = typography.caption,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                SkinthesiaPrimaryButton(
                    text = stringResource(R.string.action_continue),
                    onClick = onContinue,
                    enabled = state.canContinueFromPhoto,
                )
            },
        ) {
            OnboardingHeader(
                title = stringResource(R.string.photo_title),
                subtitle = stringResource(R.string.photo_instruction),
            )
            Spacer(Modifier.height(spacing.md))

            FadeInUp(
                delayMillis = motion.stagger(1),
                modifier = if (compact) Modifier.fillMaxWidth() else Modifier.weight(1f).fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (compact) Modifier else Modifier.fillMaxHeight()),
                    contentAlignment = Alignment.Center,
                ) {
                    Viewfinder(
                        modifier = if (compact) {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.82f)
                        } else {
                            Modifier.aspectRatio(0.8f, matchHeightConstraintsFirst = true)
                        },
                        photoPath = photo?.filePath,
                        camera = camera,
                        busy = busy,
                        busyLabel = stringResource(
                            if (state.isImporting) R.string.photo_importing else R.string.photo_capturing,
                        ),
                        permanentlyDenied = permanentlyDenied,
                        onRequestPermission = {
                            if (permanentlyDenied) {
                                context.openAppSettings()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onOpenGallery = openGallery,
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            FadeInUp(delayMillis = motion.stagger(2)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top,
                ) {
                    // Short phones fold the gallery link into this slot until a photo exists.
                    if (layout == PhotoLayout.SHORT && photo == null) {
                        SkinthesiaIconButton(
                            icon = SkinthesiaIcons.Gallery,
                            contentDescription = stringResource(R.string.photo_gallery),
                            label = stringResource(R.string.photo_gallery_short),
                            enabled = !busy,
                            onClick = openGallery,
                        )
                    } else {
                        SkinthesiaIconButton(
                            icon = SkinthesiaIcons.Retake,
                            contentDescription = stringResource(R.string.photo_retake),
                            label = stringResource(R.string.photo_retake),
                            enabled = photo != null && !busy,
                            onClick = onRetake,
                        )
                    }
                    ShutterButton(
                        enabled = cameraUsable && photo == null && !busy,
                        onClick = {
                            camera?.let { active ->
                                onCapture(active.lensFacing) { file -> active.capture(file) }
                            }
                        },
                    )
                    SkinthesiaIconButton(
                        icon = SkinthesiaIcons.Flip,
                        contentDescription = stringResource(R.string.photo_flip),
                        label = stringResource(R.string.photo_flip),
                        enabled = camera != null && camera.canFlip && photo == null && !busy,
                        onClick = { camera?.flip() },
                    )
                }
            }

            if (layout != PhotoLayout.SHORT) {
                SkinthesiaTextButton(
                    text = stringResource(R.string.photo_gallery),
                    leadingIcon = SkinthesiaIcons.Gallery,
                    enabled = !busy,
                    onClick = openGallery,
                )
            }

            AnimatedVisibility(
                visible = showError,
                enter = fadeIn(tween(motion.duration(motion.base))) + expandVertically(tween(motion.duration(motion.base))),
                exit = fadeOut(tween(motion.duration(motion.fast))) + shrinkVertically(tween(motion.duration(motion.fast))),
            ) {
                InfoNotice(
                    text = stringResource(
                        if (state.notice == OnboardingNotice.IMPORT_FAILED) R.string.photo_error_import else R.string.photo_error_capture,
                    ),
                )
            }
        }
    }
}

/** The framed capture area: captured photo, live preview, or a calm permission state. */
@Composable
private fun Viewfinder(
    photoPath: String?,
    camera: SkinCameraController?,
    busy: Boolean,
    busyLabel: String,
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val previewDescription = stringResource(R.string.photo_preview_cd)

    Box(
        modifier = modifier
            .clip(SkinthesiaTheme.shapes.image)
            .background(colors.surfaceMuted)
            .border(spacing.borderThin, colors.border, SkinthesiaTheme.shapes.image),
    ) {
        when {
            photoPath != null -> {
                SkinthesiaImage(
                    source = ImageSource.LocalFile(photoPath),
                    contentDescription = stringResource(R.string.photo_captured_cd),
                    modifier = Modifier.fillMaxSize(),
                )
                FaceGuideOverlay(
                    bracketColor = colors.surface,
                    guideColor = Color.Transparent,
                )
            }

            camera != null && camera.isReady && !camera.hasAnyCamera -> {
                ViewfinderMessage(
                    icon = SkinthesiaIcons.Camera,
                    title = stringResource(R.string.photo_permission_title),
                    body = stringResource(R.string.photo_no_camera),
                    primaryLabel = stringResource(R.string.photo_gallery),
                    onPrimary = onOpenGallery,
                )
            }

            camera != null -> {
                CameraPreview(
                    controller = camera,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = previewDescription },
                )
                FaceGuideOverlay(scanning = true)
                if (!camera.isReady) {
                    Text(
                        text = stringResource(R.string.photo_starting_camera),
                        style = typography.caption,
                        color = colors.textOnPhoto,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = spacing.lg),
                    )
                }
            }

            else -> {
                ViewfinderMessage(
                    icon = SkinthesiaIcons.Camera,
                    title = stringResource(R.string.photo_permission_title),
                    body = stringResource(R.string.photo_permission_body),
                    primaryLabel = stringResource(
                        if (permanentlyDenied) R.string.photo_permission_settings else R.string.photo_permission_allow,
                    ),
                    onPrimary = onRequestPermission,
                    secondaryLabel = stringResource(R.string.photo_gallery),
                    onSecondary = onOpenGallery,
                )
            }
        }

        if (busy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.scrim),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = colors.textOnPhoto,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = busyLabel,
                        style = typography.label,
                        color = colors.textOnPhoto,
                    )
                }
            }
        }
    }
}

/** Large clay shutter with an outer ring, centred between Retake and Flip. */
@Composable
private fun ShutterButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = SkinthesiaTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val description = stringResource(R.string.photo_shutter_cd)
    Box(
        modifier = Modifier
            .pressScale(interaction, pressedScale = 0.92f)
            .size(74.dp)
            .clip(SkinthesiaTheme.shapes.pill)
            .background(colors.surface)
            .border(2.dp, if (enabled) colors.primary else colors.borderStrong, SkinthesiaTheme.shapes.pill)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(SkinthesiaTheme.shapes.pill)
                .background(if (enabled) colors.primary else colors.disabledContainer),
        )
    }
}

/** Calm in-viewfinder state for permission or missing-camera situations. */
@Composable
private fun ViewfinderMessage(
    icon: ImageVector,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(SkinthesiaTheme.shapes.pill)
                .background(colors.surface)
                .border(spacing.borderThin, colors.border, SkinthesiaTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(spacing.md))
        Text(text = title, style = typography.titleSmall, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(spacing.xs))
        Text(text = body, style = typography.bodySmall, color = colors.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(spacing.md))
        SkinthesiaTextButton(text = primaryLabel, onClick = onPrimary)
        if (secondaryLabel != null && onSecondary != null) {
            SkinthesiaTextButton(text = secondaryLabel, onClick = onSecondary, leadingIcon = SkinthesiaIcons.Gallery)
        }
    }
}

private enum class PhotoLayout { REGULAR, SHORT, SCROLL }
