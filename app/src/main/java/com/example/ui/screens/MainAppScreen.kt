package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatMessage
import com.example.data.db.ChatThread
import com.example.ui.components.TypingIndicator
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.Localizer
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ChatViewModel) {
    val language by viewModel.language.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModelId by viewModel.selectedModelId.collectAsState()
    val allThreads by viewModel.allThreads.collectAsState()
    val selectedThreadId by viewModel.selectedThreadId.collectAsState()
    val currentMessages by viewModel.currentMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val fetchStatus by viewModel.fetchStatus.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentTab by remember { mutableStateOf("chat") }

    // Enforce dynamic layout direction based on system translations RTL rules
    val direction = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Toast notifications on sync status shifts
    LaunchedEffect(fetchStatus) {
        if (fetchStatus != null) {
            val resKey = when (fetchStatus) {
                "SUCCESS" -> "fetch_models_success"
                "FAILED" -> "fetch_models_failed"
                else -> null
            }
            if (resKey != null) {
                Toast.makeText(context, Localizer.get(resKey, language), Toast.LENGTH_SHORT).show()
                viewModel.clearStatusFeedback()
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Localizer.get("threads", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            viewModel.createNewThread()
                            scope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("drawer_new_chat_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Localizer.get("new_chat", language))
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    if (allThreads.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Localizer.get("no_threads", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            items(allThreads) { thread ->
                                val isSelected = thread.id == selectedThreadId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.selectThread(thread.id)
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                        )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = thread.title,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                        IconButton(
                                            onClick = { viewModel.deleteThread(thread) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = Localizer.get("delete_thread", language),
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                            label = { Text(text = Localizer.get("chat", language)) },
                            selected = currentTab == "chat",
                            onClick = { currentTab = "chat" },
                            modifier = Modifier.testTag("tab_button_chat")
                        )
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                            label = { Text(text = Localizer.get("settings", language)) },
                            selected = currentTab == "settings",
                            onClick = { currentTab = "settings" },
                            modifier = Modifier.testTag("tab_button_settings")
                        )
                    }
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        slideInHorizontally(animationSpec = tween(300)) { if (targetState == "settings") it else -it } + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { if (targetState == "chat") it else -it } + fadeOut(tween(300))
                    },
                    label = "tab_transitions",
                    modifier = Modifier.padding(innerPadding)
                ) { targetTab ->
                    when (targetTab) {
                        "chat" -> ChatScreenContent(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            availableModels = availableModels,
                            selectedModelId = selectedModelId,
                            currentMessages = currentMessages,
                            isGenerating = isGenerating,
                            language = language
                        )
                        "settings" -> SettingsScreenContent(
                            viewModel = viewModel,
                            language = language,
                            themeSetting = themeSetting,
                            fetchStatus = fetchStatus
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    viewModel: ChatViewModel,
    onOpenDrawer: () -> Unit,
    availableModels: List<String>,
    selectedModelId: String,
    currentMessages: List<ChatMessage>,
    isGenerating: Boolean,
    language: AppLanguage
) {
    val isImageMode by viewModel.isImageMode.collectAsState()
    var textMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var showModelMenu by remember { mutableStateOf(false) }

    // Auto-scroll on new responses arriving
    LaunchedEffect(currentMessages.size, isGenerating) {
        if (currentMessages.isNotEmpty() || isGenerating) {
            scope.launch {
                listState.animateScrollToItem((currentMessages.size + (if (isGenerating) 1 else 0)))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Styled Header Top App Bar with Glow Accent Base
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "History")
                }

                // Selected Model dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .clickable { showModelMenu = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedModelId,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showModelMenu,
                        onDismissRequest = { showModelMenu = false }
                    ) {
                        availableModels.forEach { mId ->
                            DropdownMenuItem(
                                text = { Text(text = mId) },
                                onClick = {
                                    viewModel.selectModel(mId)
                                    showModelMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = { viewModel.createNewThread() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = Localizer.get("new_chat", language),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Live Chat Space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (currentMessages.isEmpty()) {
                // Polished Placeholder / First run welcome panel
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Localizer.get("welcome_title", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Localizer.get("welcome_subtitle", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Localizer.get("get_key_title", language),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = Localizer.get("get_key_desc", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                ) {
                    items(items = currentMessages, key = { it.id }) { msg ->
                        val isUser = msg.role == "user"
                        ChatBubbleItem(message = msg, isUser = isUser)
                    }

                    if (isGenerating) {
                        item {
                            ChatBubbleGeneratingItem()
                        }
                    }
                }
            }
        }

        // Mode Choice Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!isImageMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { viewModel.setImageMode(false) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.PERSIAN) "پیام متنی" else "Text Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!isImageMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isImageMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { viewModel.setImageMode(true) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.PERSIAN) "تولید تصویر" else "Image Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isImageMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Send bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    placeholder = {
                        Text(
                            text = if (isImageMode) Localizer.get("placeholder_image", language) else Localizer.get("placeholder_chat", language),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textMessage.isNotBlank()) {
                                viewModel.sendMessage(textMessage) { errKey ->
                                    Toast.makeText(context, Localizer.get(errKey, language), Toast.LENGTH_LONG).show()
                                }
                                textMessage = ""
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (textMessage.isNotBlank()) {
                            viewModel.sendMessage(textMessage) { errKey ->
                                Toast.makeText(context, Localizer.get(errKey, language), Toast.LENGTH_LONG).show()
                            }
                            textMessage = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (textMessage.isNotBlank()) {
                                if (isImageMode) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            }
                        )
                        .testTag("chat_send_button"),
                    enabled = textMessage.isNotBlank()
                ) {
                    Icon(
                        imageVector = if (isImageMode) Icons.Default.Face else Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun parseMarkdownToAnnotatedString(text: String, baseColor: Color, primaryColor: Color): androidx.compose.ui.text.AnnotatedString {
    return remember(text, baseColor, primaryColor) {
        buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                when {
                    text.startsWith("**", i) -> {
                        val endIdx = text.indexOf("**", i + 2)
                        if (endIdx != -1) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                                append(text.substring(i + 2, endIdx))
                            }
                            i = endIdx + 2
                        } else {
                            append("**")
                            i += 2
                        }
                    }
                    text.startsWith("`", i) -> {
                        val endIdx = text.indexOf("`", i + 1)
                        if (endIdx != -1) {
                            withStyle(SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = primaryColor,
                                background = primaryColor.copy(alpha = 0.15f),
                                fontSize = 14.sp
                            )) {
                                append(" " + text.substring(i + 1, endIdx) + " ")
                            }
                            i = endIdx + 1
                        } else {
                            append("`")
                            i += 1
                        }
                    }
                    else -> {
                        append(text[i].toString())
                        i++
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownText(text: String, baseColor: Color, primaryColor: Color) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.substring(4),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = baseColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.substring(3),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = baseColor,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.substring(2),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = baseColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseMarkdownToAnnotatedString(line.substring(2), baseColor, primaryColor),
                            style = MaterialTheme.typography.bodyLarge,
                            color = baseColor,
                            lineHeight = 22.sp
                        )
                    }
                }
                else -> {
                    if (line.isNotBlank()) {
                        Text(
                            text = parseMarkdownToAnnotatedString(line, baseColor, primaryColor),
                            style = MaterialTheme.typography.bodyLarge,
                            color = baseColor,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(code: String, language: String?, isUser: Boolean) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val label = language?.replaceFirstChar { it.uppercase() } ?: "Code"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
                
                IconButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "Copy code",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RichMarkdownContent(text: String, isUser: Boolean, baseColor: Color, primaryColor: Color) {
    val parts = text.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                val lines = part.split("\n", limit = 2)
                val lang = if (lines.size > 1 && lines[0].trim().isNotEmpty() && lines[0].trim().length < 15) {
                    lines[0].trim()
                } else null
                val code = if (lang != null) lines[1] else part
                
                CodeBlockView(code = code.trim(), language = lang, isUser = isUser)
            } else {
                if (part.isNotBlank()) {
                    MarkdownText(text = part, baseColor = baseColor, primaryColor = primaryColor)
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: com.example.data.db.ChatMessage, isUser: Boolean) {
    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    val bubbleBgColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignAlignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = alignAlignment
    ) {
        if (message.content.startsWith("IMAGE_URL:")) {
            val imageUrl = message.content.substring(10)
            Card(
                shape = bubbleShape,
                colors = CardDefaults.cardColors(containerColor = bubbleBgColor),
                elevation = CardDefaults.cardElevation(0.5.dp),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Generated Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Text(
                        text = "Generated Image",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            Card(
                shape = bubbleShape,
                colors = CardDefaults.cardColors(containerColor = bubbleBgColor),
                elevation = CardDefaults.cardElevation(0.5.dp),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    RichMarkdownContent(
                        text = message.content, 
                        isUser = isUser, 
                        baseColor = textColor, 
                        primaryColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleGeneratingItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.width(76.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TypingIndicator(dotSize = 6.dp)
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    viewModel: ChatViewModel,
    language: AppLanguage,
    themeSetting: AppThemeSetting,
    fetchStatus: String?
) {
    val apiKey by viewModel.apiKey.collectAsState()
    var rawKeyText by remember { mutableStateOf(apiKey) }
    var keyVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // Settings Card Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Localizer.get("settings_title", language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Credentials Card Box Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localizer.get("api_key_label", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rawKeyText,
                    onValueChange = { rawKeyText = it },
                    placeholder = { Text(text = Localizer.get("api_key_placeholder", language)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_key_field"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Toggle visibility",
                                tint = if (keyVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        }
                    },
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setApiKey(rawKeyText)
                            Toast.makeText(context, Localizer.get("key_saved", language), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_save_key_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = Localizer.get("save_key", language))
                    }

                    if (fetchStatus == "LOADING") {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterVertically)
                        )
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.syncModels() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Localizer.get("fetch_models", language), fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = Localizer.get("anonymous_notice", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Material Themes selection Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localizer.get("theme_label", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeSelectRow(
                        themeTitle = Localizer.get("theme_dark", language),
                        isSelected = themeSetting == AppThemeSetting.DARK,
                        bgColor = Color(0xFF130E26),
                        outlineColor = DarkPrimary,
                        textColor = Color.White,
                        onClick = { viewModel.setTheme(AppThemeSetting.DARK) }
                    )
                    
                    ThemeSelectRow(
                        themeTitle = Localizer.get("theme_light", language),
                        isSelected = themeSetting == AppThemeSetting.WHITE,
                        bgColor = Color(0xFFFFFFFF),
                        outlineColor = LightPrimary,
                        textColor = Color(0xFF0F172A),
                        onClick = { viewModel.setTheme(AppThemeSetting.WHITE) }
                    )

                    ThemeSelectRow(
                        themeTitle = Localizer.get("theme_colorful", language),
                        isSelected = themeSetting == AppThemeSetting.COLORFUL,
                        bgColor = Color(0xFFFFF1F2),
                        outlineColor = ColorfulPrimary,
                        textColor = Color(0xFF881337),
                        onClick = { viewModel.setTheme(AppThemeSetting.COLORFUL) }
                    )
                }
            }
        }

        // System Languages selection Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localizer.get("language_label", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSel = lang == language
                        ElevatedFilterChip(
                            selected = isSel,
                            onClick = { viewModel.setLanguage(lang) },
                            label = { 
                                Text(
                                    text = lang.displayName,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Secured locally notice card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = Localizer.get("api_key_secured", language),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Localizer.get("api_key_secured_desc", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ThemeSelectRow(
    themeTitle: String,
    isSelected: Boolean,
    bgColor: Color,
    outlineColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = themeTitle,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = outlineColor,
                unselectedColor = textColor.copy(alpha = 0.4f)
            )
        )
    }
}
