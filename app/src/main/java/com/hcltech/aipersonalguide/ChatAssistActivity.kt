package com.hcltech.aipersonalguide

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hcltech.aipersonalguide.ui.theme.AIPersonalGuideTheme
import com.hcltech.aipersonalguide.viewmodel.MainViewModel
import java.io.ByteArrayOutputStream

class ChatAssistActivity :  ComponentActivity() {
    @OptIn(
        ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainViewModel by viewModels<MainViewModel>()
        setContent {

            AIPersonalGuideTheme {

                var promptText by remember {
                    mutableStateOf("")
                }

                val conversations = mainViewModel.conversations
                val isGenerating by mainViewModel.isGenerating
                val keyboardController = LocalSoftwareKeyboardController.current
                val imageBitmaps: SnapshotStateList<Bitmap> = remember {
                    mutableStateListOf()
                }

                val context = LocalContext.current

                val takePicture =
                    rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                        it?.let { bitmap ->

                            val bytes = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                            val path = MediaStore.Images.Media.insertImage(
                                context.contentResolver,
                                bitmap,
                                "Title",
                                null
                            )

                            imageBitmaps.add(
                                MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    Uri.parse(path.toString())
                                )
                            )
                        }

                    }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    if (it) {
                        takePicture.launch()
                    } else {
                        Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
                val photoPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
                        uris.forEach { uri ->
                            imageBitmaps.add(
                                MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    uri
                                )
                            )
                        }
                    }


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.purple_500),
                ) {

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(text = "AI Personal Tour Guide") },
                            )
                        },
                        bottomBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(imageBitmaps.size) { index ->
                                        val imageBitmap = imageBitmaps[index]
                                        Image(
                                            bitmap = imageBitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .height(100.dp)
                                                .animateItemPlacement()
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                )
                                                .clickable {
                                                    imageBitmaps.remove(imageBitmap)
                                                }
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                        .wrapContentHeight(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = promptText,
                                        onValueChange = { promptText = it },
                                        label = { Text(text = "Message") },
                                        modifier = Modifier.weight(1f),
                                        trailingIcon = {
                                            IconButton(onClick = {

                                                val permissionCheckResult =
                                                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                                    takePicture.launch()
                                                } else {
                                                    // Request a permission
                                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                                }


                                            }) {
                                                Icon(
                                                    painter = painterResource(R.drawable.baseline_camera_alt_24),
                                                    tint = colorResource(id = R.color.purple_500),
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            IconButton(onClick = {
                                                photoPicker.launch(PickVisualMediaRequest())
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircle,
                                                    tint = colorResource(id = R.color.purple_500),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    FloatingActionButton(
                                        elevation = FloatingActionButtonDefaults.elevation(
                                            defaultElevation = if (isGenerating) 0.dp else 6.dp,
                                            pressedElevation = 0.dp
                                        ),
                                        onClick = {
                                            if (promptText.isNotBlank() && isGenerating.not()) {
                                                mainViewModel.sendText(promptText, imageBitmaps)
                                                promptText = ""
                                                imageBitmaps.clear()
                                                keyboardController?.hide()
                                            } else if (promptText.isBlank()) {
                                                Toast.makeText(
                                                    context,
                                                    "Please enter a message",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    ) {
                                        AnimatedContent(
                                            targetState = isGenerating,
                                            label = ""
                                        ) { generating ->
                                            if (generating) {
                                                CircularProgressIndicator()
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { contentPadding ->
                        ConversationScreen(
                            conversations = conversations,
                            modifier = Modifier.padding(contentPadding)
                        )
                    }
                }
            }
        }

    }

}


@JvmOverloads
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    conversations: SnapshotStateList<Triple<String, String, List<Bitmap>?>>,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(conversations.size) { index ->
            val conversation = conversations[index]
            MessageItem(
                isInComing = conversation.first == "received",
                images = conversation.third ?: emptyList(),
                content = conversation.second,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
            )
        }
    }
}


@JvmOverloads
@Composable
fun MessageItem(
    isInComing: Boolean,
    images: List<Bitmap>,
    content: String,
    modifier: Modifier = Modifier
) {

    val cardShape by remember {
        derivedStateOf {
            if (isInComing) {
                RoundedCornerShape(
                    16.dp,
                    16.dp,
                    16.dp,
                    0.dp
                )
            } else {
                RoundedCornerShape(
                    16.dp,
                    16.dp,
                    0.dp,
                    16.dp
                )
            }
        }
    }

    val cardPadding by remember {
        derivedStateOf {
            if (isInComing) {
                PaddingValues(end = 24.dp)
            } else {
                PaddingValues(start = 24.dp)
            }
        }
    }

    Column(modifier = modifier) {
        if (images.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
            ) {
                items(images.size) { index ->
                    val image = images[index]
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(60.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            )
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(cardPadding),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isInComing) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring()
                    )
                )
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()

                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()

                }
            ) {
                Text("Dismiss")
            }
        }
    )
}