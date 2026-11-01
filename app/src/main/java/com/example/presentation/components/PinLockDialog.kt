package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDangerRed
import com.example.ui.theme.NovaDarkCard
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

enum class PinMode {
    VERIFY,
    SETUP,
    CONFIRM_SETUP,
    RECOVERY
}

@Composable
fun PinLockDialog(
    mode: PinMode = PinMode.VERIFY,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    verifyPinHash: ((String) -> Boolean)? = null,
    onSaveNewPin: ((String) -> Unit)? = null,
    recoveryPhraseExpected: String? = null
) {
    var currentPin by remember { mutableStateOf("") }
    var tempSetupPin by remember { mutableStateOf("") }
    var currentMode by remember { mutableStateOf(mode) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recoveryInput by remember { mutableStateOf("") }
    var showRecoveryFlow by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pin_lock_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NovaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaElectricViolet.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(NovaElectricViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = NovaCyanNeon,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val headerTitle = when {
                    showRecoveryFlow -> "Recovery Mode"
                    currentMode == PinMode.SETUP -> "Create Security PIN"
                    currentMode == PinMode.CONFIRM_SETUP -> "Confirm Security PIN"
                    else -> "Nova Screen Guard Lock"
                }

                val headerSubtitle = when {
                    showRecoveryFlow -> "Enter recovery phrase to reset PIN"
                    currentMode == PinMode.SETUP -> "Enter a 4-digit PIN for your privacy vault"
                    currentMode == PinMode.CONFIRM_SETUP -> "Re-enter the 4 digits to confirm"
                    else -> "Enter your 4-digit PIN or authenticate"
                }

                Text(
                    text = headerTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = headerSubtitle,
                    fontSize = 12.sp,
                    color = NovaTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots (4 digits)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < currentPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) NovaCyanNeon else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) NovaCyanNeon else NovaDarkCardBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = NovaDangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Number Pad (1-9, bio/empty, 0, backspace)
                val numRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in numRows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (key in row) {
                                KeypadButton(
                                    key = key,
                                    hasBiometric = onBiometricClick != null,
                                    onClick = {
                                        errorMessage = null
                                        when (key) {
                                            "DEL" -> {
                                                if (currentPin.isNotEmpty()) {
                                                    currentPin = currentPin.dropLast(1)
                                                }
                                            }
                                            "BIO" -> {
                                                onBiometricClick?.invoke()
                                            }
                                            else -> {
                                                if (currentPin.length < 4) {
                                                    currentPin += key
                                                    if (currentPin.length == 4) {
                                                        // Handle completed 4 digits
                                                        handlePinSubmit(
                                                            pin = currentPin,
                                                            currentMode = currentMode,
                                                            tempSetupPin = tempSetupPin,
                                                            verifyPinHash = verifyPinHash,
                                                            onSaveNewPin = onSaveNewPin,
                                                            onSuccess = onSuccess,
                                                            onTransitionMode = { nextMode, tempPin ->
                                                                currentMode = nextMode
                                                                tempSetupPin = tempPin
                                                                currentPin = ""
                                                            },
                                                            onError = { err ->
                                                                errorMessage = err
                                                                currentPin = ""
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pin_cancel_button")
                    ) {
                        Text("Cancel", color = NovaTextSecondary, fontSize = 13.sp)
                    }

                    if (mode == PinMode.VERIFY) {
                        TextButton(
                            onClick = {
                                showRecoveryFlow = !showRecoveryFlow
                                currentPin = ""
                                errorMessage = "Use recovery phrase from Settings"
                            },
                            modifier = Modifier.testTag("pin_forgot_button")
                        ) {
                            Text("Forgot PIN?", color = NovaElectricViolet, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun handlePinSubmit(
    pin: String,
    currentMode: PinMode,
    tempSetupPin: String,
    verifyPinHash: ((String) -> Boolean)?,
    onSaveNewPin: ((String) -> Unit)?,
    onSuccess: () -> Unit,
    onTransitionMode: (PinMode, String) -> Unit,
    onError: (String) -> Unit
) {
    when (currentMode) {
        PinMode.SETUP -> {
            onTransitionMode(PinMode.CONFIRM_SETUP, pin)
        }
        PinMode.CONFIRM_SETUP -> {
            if (pin == tempSetupPin) {
                onSaveNewPin?.invoke(pin)
                onSuccess()
            } else {
                onError("PINs did not match. Please try again.")
            }
        }
        PinMode.VERIFY -> {
            if (verifyPinHash?.invoke(pin) == true) {
                onSuccess()
            } else {
                onError("Incorrect PIN. Please re-enter.")
            }
        }
        PinMode.RECOVERY -> {
            onSuccess()
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    hasBiometric: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(NovaDarkCard)
            .clickable(onClick = onClick)
            .testTag("keypad_$key"),
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "DEL" -> {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = NovaCyanNeon,
                    modifier = Modifier.size(22.dp)
                )
            }
            "BIO" -> {
                if (hasBiometric) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric",
                        tint = NovaElectricViolet,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            else -> {
                Text(
                    text = key,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
            }
        }
    }
}
