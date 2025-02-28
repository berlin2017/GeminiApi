package com.berlin.sample1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.berlin.sample1.model.ChatMessage
import com.berlin.sample1.network.RetrofitClient
import com.berlin.sample1.repository.GeminiRepository
import com.berlin.sample1.room.ChatDatabase
import com.berlin.sample1.ui.theme.Sample1Theme
import com.berlin.sample1.viewmodel.GeminiViewModel
import com.berlin.sample1.viewmodel.GeminiViewModelFactory
import kotlinx.coroutines.launch

const val API_KEY = "AIzaSyDTxGDhayK_QBJ4VoimiTW5pzTOIlCXiSU"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val geminiRepository = GeminiRepository(
            RetrofitClient.geminiApiService,
            ChatDatabase.getDatabase(this).chatMessageDao()
        )
        val factory = GeminiViewModelFactory(geminiRepository)
        val geminiViewModel = ViewModelProvider(this, factory)[GeminiViewModel::class.java]
        setContent {
            Sample1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var message by remember { mutableStateOf("") }
                    val chatMessages by geminiViewModel.chatMessages.collectAsStateWithLifecycle()
                    // 获取软键盘高度
                    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        "Gemini",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding)
                                .padding(16.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                reverseLayout = true,
                                state = listState
                            ) {
                                items(chatMessages.reversed()) { chatMessage ->
                                    ChatBubble(chatMessage = chatMessage)
                                }
                            }
                            // 滚动到列表底部
                            LaunchedEffect(chatMessages) {
                                if (chatMessages.isNotEmpty()) {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            }
                            val focusManager = LocalFocusManager.current
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = imePadding),// Apply bottom padding here
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = message,
                                    onValueChange = { message = it },
                                    label = { Text("Enter your message") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (message.isNotEmpty()) {
                                                geminiViewModel.sendMessage(API_KEY, message)
                                                message = ""
                                                focusManager.clearFocus()
                                            }
                                        }
                                    )
                                )
                                Button(onClick = {
                                    if (message.isNotEmpty()) {
                                        geminiViewModel.sendMessage(API_KEY, message)
                                        message = ""
                                        focusManager.clearFocus()
                                    }
                                }) {
                                    Text("Send")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(chatMessage: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = if (chatMessage.isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = chatMessage.text,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (chatMessage.isUserMessage) Color.Blue else Color.Gray)
                .padding(16.dp),
            color = Color.White
        )
    }
}