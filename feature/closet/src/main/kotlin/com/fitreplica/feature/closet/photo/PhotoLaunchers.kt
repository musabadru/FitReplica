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
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val captureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = pendingCaptureUri
            if (success && uri != null) onPhotoReady(uri.toString())
        }
    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onPhotoReady(uri.toString())
        }

    return remember(captureLauncher, pickLauncher) {
        PhotoLaunchers(
            capture = {
                val uri = createCaptureDestination(context)
                pendingCaptureUri = uri
                captureLauncher.launch(uri)
            },
            pick = { pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        )
    }
}

private fun createCaptureDestination(context: Context): Uri {
    val dir = File(context.filesDir, "images/pending").apply { mkdirs() }
    val file = File(dir, "${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
