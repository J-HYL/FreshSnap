package com.marujho.freshsnap

import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marujho.freshsnap.ui.theme.FreshSnapTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshSnapTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginBox(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginBox(name: String, modifier: Modifier = Modifier) {
    var emailInput by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        IconText()
        Spacer(modifier = Modifier.height(24.dp))
        EditTextField(
            label = "Email",
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next),
            value = emailInput,
            onValueChange = { emailInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth())
        EditTextField(
            label = "Password",
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next),
            value = emailInput,
            onValueChange = { emailInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {/*Mandar al main*/},
                modifier = Modifier.weight(1f)
            ) {
                Text("Iniciar Sesión")
            }
            OutlinedButton(
                onClick = {emailInput = ""},
                modifier = Modifier.weight(1f)
            ) {
                Text("Crear Cuenta")
            }
        }
    }


}
@Composable
fun IconText(modifier: Modifier = Modifier){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_freshsnap_logo),
            contentDescription = null,
            modifier = Modifier
                .size(128.dp),
//                    tint = DarkBlue
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
//                    color = DarkBlue,
            modifier = Modifier
                .paddingFromBaseline(top = 64.dp)
        )
    }
}
@Composable
fun EditTextField(
    label: String = "Email",
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions,
    icon : ImageVector? = null
){
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
        label = {
            Text(
                label
            )
        },
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FreshSnapTheme {
        LoginBox("Android")
    }
}