package com.phantompad.client.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantompad.client.protocol.DiscoveryResponse
import com.phantompad.client.protocol.ServerIcon
import com.phantompad.client.state.ConnectionState
import com.phantompad.client.ui.theme.*

@Composable
fun ConnectionScreen(
    connectionState: ConnectionState,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectToServer: (DiscoveryResponse) -> Unit,
    onConnectToAddress: (String) -> Unit,
    onDismissError: () -> Unit,
) {
    var manualIp by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }

    // Auto-start discovery when showing this screen
    LaunchedEffect(Unit) {
        onStartDiscovery()
    }

    DisposableEffect(Unit) {
        onDispose { onStopDiscovery() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF12121A))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = "PHANTOM PAD",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPurple,
                letterSpacing = 4.sp,
            )

            Text(
                text = "Open Source Game Controller",
                fontSize = 13.sp,
                color = DarkOnSurfaceVariant,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(20.dp))

            // Player name field
            OutlinedTextField(
                value = playerName,
                onValueChange = onPlayerNameChange,
                label = { Text("Player Name") },
                singleLine = true,
                modifier = Modifier.width(280.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = ButtonBorder,
                    cursorColor = AccentPurple,
                    focusedLabelColor = AccentPurple,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(16.dp))

            // State-dependent content
            when (connectionState) {
                is ConnectionState.Disconnected -> {
                    // Scanning indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (connectionState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AccentCyan,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = if (connectionState.isScanning) "Searching for servers..."
                            else "Tap to search",
                            fontSize = 14.sp,
                            color = DarkOnSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Server list
                    if (connectionState.servers.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(connectionState.servers) { server ->
                                ServerListItem(
                                    server = server,
                                    onClick = { onConnectToServer(server) },
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No servers found yet.\nMake sure PhantomPad Server is running\non your PC and both devices are on\nthe same Wi-Fi network.",
                                fontSize = 13.sp,
                                color = DarkOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }

                is ConnectionState.Connecting -> {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = AccentPurple,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Connecting to ${connectionState.serverName}...",
                        fontSize = 16.sp,
                        color = DarkOnSurface,
                    )
                    Spacer(Modifier.weight(1f))
                }

                is ConnectionState.Error -> {
                    Spacer(Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.widthIn(max = 360.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = StatusError.copy(alpha = 0.15f),
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Connection Failed",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusError,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = connectionState.message,
                                fontSize = 14.sp,
                                color = DarkOnSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onDismissError,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentPurple,
                                ),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }

                is ConnectionState.Connected -> {
                    // Should not be shown in this screen
                }
            }

            // Manual IP entry toggle
            if (connectionState is ConnectionState.Disconnected) {
                Spacer(Modifier.height(8.dp))

                if (showManualEntry) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.widthIn(max = 360.dp),
                    ) {
                        OutlinedTextField(
                            value = manualIp,
                            onValueChange = { manualIp = it },
                            label = { Text("Server IP") },
                            placeholder = { Text("192.168.1.100") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (manualIp.isNotBlank()) {
                                        onConnectToAddress(manualIp.trim())
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = ButtonBorder,
                                cursorColor = AccentPurple,
                                focusedLabelColor = AccentPurple,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualIp.isNotBlank()) {
                                    onConnectToAddress(manualIp.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("Connect")
                        }
                    }
                }

                TextButton(
                    onClick = { showManualEntry = !showManualEntry },
                ) {
                    Text(
                        text = if (showManualEntry) "Hide manual entry" else "Enter IP manually",
                        color = AccentPurple,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerListItem(
    server: DiscoveryResponse,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Server icon
            Text(
                text = serverIconEmoji(server.icon),
                fontSize = 24.sp,
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkOnSurface,
                )
                Text(
                    text = server.address,
                    fontSize = 12.sp,
                    color = DarkOnSurfaceVariant,
                )
            }

            // Latency badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            server.latencyMs < 10 -> StatusConnected.copy(alpha = 0.2f)
                            server.latencyMs < 50 -> Color(0xFFFBBC04).copy(alpha = 0.2f)
                            else -> StatusError.copy(alpha = 0.2f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${server.latencyMs}ms",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        server.latencyMs < 10 -> StatusConnected
                        server.latencyMs < 50 -> Color(0xFFFBBC04)
                        else -> StatusError
                    },
                )
            }
        }
    }
}

private fun serverIconEmoji(icon: ServerIcon): String = when (icon) {
    ServerIcon.Desktop -> "\uD83D\uDDA5" // desktop monitor
    ServerIcon.Laptop -> "\uD83D\uDCBB"  // laptop
    ServerIcon.HTPC -> "\uD83D\uDCFA"    // TV
    ServerIcon.SteamDeck -> "\uD83C\uDFAE" // game controller
    ServerIcon.Generic -> "\uD83D\uDDA5"
}
