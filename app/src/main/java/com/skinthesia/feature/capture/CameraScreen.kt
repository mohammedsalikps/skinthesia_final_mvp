package com.skinthesia.feature.capture

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.skinthesia.camera.CameraPreview
import com.skinthesia.camera.LiveLight
import com.skinthesia.camera.rememberSkinCameraController
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CameraRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.SkinthesiaPillButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.pressScale
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.core.utils.findActivity
import com.skinthesia.core.utils.hasCameraPermission
import com.skinthesia.core.utils.openAppSettings

/**
 * The capture step of screen 03: a full-bleed CameraX viewfinder dimmed outside a face
 * oval, live lighting guidance from the analysis stream, flip, gallery and a clay
 * shutter. Handles permission denial, permanent denial and devices without a camera.
 */
@Composable
fun CameraScreen() {
    val navigator = LocalAppNavigator.current
    val context = LocalContext.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<CameraRoute>()
        CaptureViewModel(route.assessmentId, photoStore, attachPhoto, clock, route.flow)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val flow = viewModel.flow.toFlowKind()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography

    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        if (!ok) {
            val activity = context.findActivity()
            permanentlyDenied = activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importPhoto(uri.toString()) { navigator.photoCaptured(viewModel.assessmentId, flow) }
    }
    val openGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    LaunchedEffect(Unit) {
        if (!granted && !requested) {
            requested = true
            permission.launch(Manifest.permission.CAMERA)
        }
    }
    LifecycleResumeEffect(Unit) {
        granted = context.hasCameraPermission()
        onPauseOrDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkinthesiaPalette.Ink),
    ) {
        if (granted) {
            val camera = rememberSkinCameraController()
            val light by camera.liveLight.collectAsStateWithLifecycle()
            if (camera.isReady && !camera.hasAnyCamera) {
                CameraMessage(
                    icon = SkinthesiaIcons.Camera,
                    title = "No camera available",
                    body = "This device has no camera we can use. You can still choose a photo from your gallery.",
                    primary = "Choose from gallery" to openGallery,
                )
            } else {
                CameraPreview(controller = camera, modifier = Modifier.fillMaxSize().semantics { contentDescription = "Camera preview" })
                FaceMask()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconAction(SkinthesiaIcons.Close, "Close camera", navigator::back, tint = Color.White)
                        Spacer(Modifier.weight(1f))
                        Text(text = "Selfie", style = typography.titleSmall, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        IconAction(SkinthesiaIcons.Flip, "Switch camera", { camera.flip() }, tint = if (camera.canFlip) Color.White else Color.White.copy(alpha = 0.35f))
                    }
                    Spacer(Modifier.height(6.dp))
                    LightGuide(light = light, ready = camera.isReady)
                }
                CaptureBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enabled = camera.isReady && !state.processing,
                    onGallery = openGallery,
                    onCapture = { viewModel.capture(camera.lensFacing, { file -> camera.capture(file) }) { navigator.photoCaptured(viewModel.assessmentId, flow) } },
                    onFlip = { camera.flip() },
                    canFlip = camera.canFlip,
                )
            }
        } else {
            CameraMessage(
                icon = SkinthesiaIcons.Camera,
                title = "Camera access needed",
                body = "Skinthesia uses your camera only to capture your skin photo. It stays on this device.",
                primary = (if (permanentlyDenied) "Open settings" else "Allow camera") to {
                    if (permanentlyDenied) context.openAppSettings() else permission.launch(Manifest.permission.CAMERA)
                },
                secondary = "Choose from gallery" to openGallery,
                onClose = navigator::back,
            )
        }

        AnimatedVisibility(visible = state.processing, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(colors.scrim), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(14.dp))
                    Text(text = state.message, style = typography.label, color = Color.White)
                }
            }
        }
        state.error?.let { message ->
            Box(Modifier.align(Alignment.Center).padding(24.dp).clip(SkinthesiaTheme.shapes.card).background(colors.surfaceElevated).padding(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = message, style = typography.body, color = colors.textPrimary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    SkinthesiaPillButton(text = "Try again", onClick = viewModel::dismissError)
                }
            }
        }
    }
}

/** Dims everything outside the face oval and draws clay corner marks and a dashed guide. */
@Composable
private fun FaceMask() {
    val colors = SkinthesiaTheme.colors
    Canvas(Modifier.fillMaxSize()) {
        val ovalW = size.width * 0.66f
        val ovalH = ovalW * 1.32f
        val top = size.height * 0.44f - ovalH / 2
        val rect = Rect(Offset((size.width - ovalW) / 2, top), Size(ovalW, ovalH))
        val oval = Path().apply { addOval(rect) }
        clipPath(oval, clipOp = ClipOp.Difference) {
            drawRect(SkinthesiaPalette.Ink.copy(alpha = 0.45f))
        }
        drawOval(Color.White.copy(alpha = 0.9f), rect.topLeft, rect.size, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))))
        val arm = 26.dp.toPx()
        val inset = 18.dp.toPx()
        val box = Rect(rect.left - inset, rect.top - inset, rect.right + inset, rect.bottom + inset)
        listOf(
            Triple(box.topLeft, 1f, 1f),
            Triple(Offset(box.right, box.top), -1f, 1f),
            Triple(Offset(box.left, box.bottom), 1f, -1f),
            Triple(box.bottomRight, -1f, -1f),
        ).forEach { (corner, dx, dy) ->
            drawLine(colors.accentBlushDeep, corner, Offset(corner.x + dx * arm, corner.y), 2.5.dp.toPx(), StrokeCap.Round)
            drawLine(colors.accentBlushDeep, corner, Offset(corner.x, corner.y + dy * arm), 2.5.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun LightGuide(light: LiveLight, ready: Boolean) {
    val typography = SkinthesiaTheme.typography
    val colors = SkinthesiaTheme.colors
    val (text, tint) = when {
        !ready -> "Starting camera…" to Color.White
        light == LiveLight.TOO_DARK -> "Too dark — face a window or soft light" to colors.gold
        light == LiveLight.DIM -> "A little dim — more light will help" to colors.gold
        light == LiveLight.BRIGHT -> "Very bright — step out of direct sun" to colors.gold
        else -> "Good light · center your face in the oval" to colors.success
    }
    AnimatedContent(targetState = text, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) }, label = "lightGuide") { value ->
        Row(
            modifier = Modifier
                .clip(SkinthesiaTheme.shapes.pill)
                .background(SkinthesiaPalette.Ink.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.width(8.dp))
            Text(text = value, style = typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun CaptureBar(
    modifier: Modifier,
    enabled: Boolean,
    onGallery: () -> Unit,
    onCapture: () -> Unit,
    onFlip: () -> Unit,
    canFlip: Boolean,
) {
    val colors = SkinthesiaTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, SkinthesiaPalette.Ink.copy(alpha = 0.55f))))
            .navigationBarsPadding()
            .padding(top = 36.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundGlassButton(SkinthesiaIcons.Gallery, "Choose from gallery", onGallery)
            Box(
                modifier = Modifier
                    .pressScale(interaction, pressedScale = 0.92f)
                    .size(78.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (enabled) colors.primary else colors.disabledContainer)
                    .clickable(enabled = enabled, interactionSource = interaction, indication = null, role = Role.Button, onClick = onCapture)
                    .semantics { contentDescription = "Take photo" },
            )
            RoundGlassButton(SkinthesiaIcons.Flip, "Switch camera", onFlip, enabled = canFlip)
        }
    }
}

@Composable
private fun RoundGlassButton(icon: ImageVector, description: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = if (enabled) Color.White else Color.White.copy(alpha = 0.35f), modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun CameraMessage(
    icon: ImageVector,
    title: String,
    body: String,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>? = null,
    onClose: (() -> Unit)? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Box(Modifier.fillMaxSize().background(colors.background)) {
        if (onClose != null) {
            IconAction(SkinthesiaIcons.Close, "Close", onClose, modifier = Modifier.statusBarsPadding().padding(4.dp))
        }
        Column(
            modifier = Modifier.align(Alignment.Center).padding(32.dp).widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(colors.primaryMist), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(text = title, style = typography.titleMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(text = body, style = typography.body, color = colors.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            SkinthesiaPillButton(text = primary.first, onClick = primary.second)
            if (secondary != null) {
                Spacer(Modifier.height(4.dp))
                SkinthesiaTextButton(text = secondary.first, onClick = secondary.second, leadingIcon = SkinthesiaIcons.Gallery)
            }
        }
    }
}
