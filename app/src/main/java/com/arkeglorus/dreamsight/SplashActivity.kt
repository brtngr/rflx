package com.arkeglorus.dreamsight

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkeglorus.dreamsight.ui.theme.DreamsightTheme
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            Text("Hello world!")
            DreamsightTheme {
                // A surface container using the 'background' color from the theme
                Greeting("Android")
            }
        }

        GlobalScope.launch{
            delay(2000)
            // do something after 1 second
            FirebaseAuth.getInstance().currentUser?.let {
                val i = Intent(this@SplashActivity, HomeActivity::class.java)
                startActivity(i)
                finish()
                return@launch
            }
            val i = Intent(this@SplashActivity, LoginActivity::class.java)

            startActivity(i)
            finish()
        }

    }
}

@Composable
fun Greeting(name: String) {
    LaunchedEffect(key1 = true) {
        delay(2000L)
    }
    Column(
        modifier = Modifier.fillMaxSize().background(color = Color(0xFF000461)).padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_trans),
            contentDescription = "Logo",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    }
}