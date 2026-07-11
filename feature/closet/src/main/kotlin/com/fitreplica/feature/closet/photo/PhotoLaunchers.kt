package com.fitreplica.feature.closet.photo

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Bundles the two ActivityResultLaunchers a photo-capture/pick UI needs. `capture` opens the
 * system camera app (needs a pre-created FileProvider destination since TakePicture writes
 * into a URI we hand it, rather than returning one); `pick` opens the Android Photo Picker.
 * Both report the resulting source URI (as a String, so callers stay Android-agnostic) via
 * onPhotoReady.
 */
class PhotoLaunchers(
    val capture: () -> Unit,
    val pick: () -> Unit,
)

@Composable
fun rememberPhotoLaunchers(onPhotoReady: (String) -> Unit): PhotoLaunchers {
    val context = LocalContext.current
    // Stored as a String (not Uri) and via rememberSaveable so a captured photo isn't
    // silently dropped if the camera app causes a configuration change (e.g. rotation)
    // while it's in the foreground — `remember` alone resets to null across recreation,
    // and the TakePicture result would then have nowhere to report its success to.
    var pendingCaptureUri by rememberSaveable { mutableStateOf<String?>(null) }

    val captureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = pendingCaptureUri
            if (success && uri != null) onPhotoReady(uri)
        }
    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onPhotoReady(uri.toString())
        }

    return remember(captureLauncher, pickLauncher) {
        PhotoLaunchers(
            capture = {
                val uri = createCaptureDestination(context)
                pendingCaptureUri = uri.toString()
                captureLauncher.launch(uri)
            },
            pick = { pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        )
    }
}

// Only one capture can be in flight per screen instance at a time, so any file already in
// this directory when a *new* capture starts is guaranteed to be either already consumed
// (copied into permanent storage by ImageRepositoryImpl) or abandoned (the user cancelled a
// previous capture) — safe to clear before creating the next destination, otherwise these
// temp files would accumulate in app storage indefinitely.
private fun createCaptureDestination(context: Context): Uri {
    val dir = File(context.filesDir, "images/pending").apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() }
    val file = File(dir, "${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
