package com.marujho.freshsnap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marujho.freshsnap.ui.theme.FreshSnapTheme

@Composable
fun LoginBox(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onLoginClick() },
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
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = icon ?: Icons.Default.Email,
                contentDescription = null
            )
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    FreshSnapTheme {
        LoginBox(onLoginClick = {}, onSignUpClick = {})
    }
}