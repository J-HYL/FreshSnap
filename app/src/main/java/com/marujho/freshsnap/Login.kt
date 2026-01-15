package com.marujho.freshsnap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marujho.freshsnap.ui.theme.FreshSnapTheme
import com.marujho.freshsnap.ui.theme.Green
import com.marujho.freshsnap.ui.theme.Grey
import com.marujho.freshsnap.ui.theme.LightGreen

@Composable
fun LoginBox(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    Column(modifier = modifier
        .background(Grey)
        .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        IconText()
        Spacer(modifier = Modifier.height(24.dp))

        EditTextField(
            label = "Email",
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            value = emailInput,
            onValueChange = { emailInput = it },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        )

        EditTextField(
            label = "Password",
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            value = passwordInput,
            onValueChange = { passwordInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onLoginClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Iniciar Sesión")
            }
            OutlinedButton(
                onClick = { onSignUpClick() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Crear Cuenta")
            }
        }
    }
}

@Composable
fun IconText(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .size(128.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_freshsnap_logo),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
fun EditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions,
    icon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = icon ?: Icons.Default.Email,
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            disabledContainerColor = White,
        ),
        shape = RoundedCornerShape(24.dp),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    FreshSnapTheme {
        LoginBox(onLoginClick = {}, onSignUpClick = {})
    }
}