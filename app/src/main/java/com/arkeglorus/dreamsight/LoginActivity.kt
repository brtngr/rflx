package com.arkeglorus.dreamsight

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkeglorus.dreamsight.ui.theme.DreamsightTheme
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DreamsightTheme {
                Greeting2(onLogin = { signIn() }, onTnCClick = { tnc() })
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("522377477714-hb747u60vobcegl0ni66a47m23dbm2oa.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
    }

    // See: https://developer.android.com/training/basics/intents/result
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }

    private fun tnc() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val i = Intent(this@LoginActivity, HomeActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(i)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Gagal Login", Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object {
        private const val TAG = "Login Activity"
        private const val RC_SIGN_IN = 9001
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Greeting2(onLogin: () -> Unit, onTnCClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append("By taping log in button you agree to our ")
        pushStringAnnotation(tag = "terms", annotation = "https://google.com/policy")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("terms and condition")
        }
        pop()
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val tncText = stringResource(id = R.string.tnc)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Terms & Condition")
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.apply {
                                        if (isCollapsed) {
                                            expand()
                                        } else {
                                            collapse()
                                        }
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Close, "closeIcon")
                            }
                        },
                        backgroundColor = Color.White,
                        contentColor = Color.Black,
                        elevation = 0.dp
                    )
                }) { contentPadding ->
                // Screen content
                Column(modifier = Modifier.padding(contentPadding).verticalScroll(scrollState)) {
                    Text(
                        tncText,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White)
                            .padding(horizontal = 16.dp),
                        color = Color.Black,
                    )
                }
            }
        },
        // Defaults to BottomSheetScaffoldDefaults.SheetPeekHeight
        sheetPeekHeight = 0.dp,
        sheetGesturesEnabled = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.welcome_illus),
                contentDescription = "Selamat Datang",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "terms",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            Log.d("terms URL", it.item)
                            onTnCClick()
                            scope.launch {
                                scaffoldState.bottomSheetState.apply {
                                    if (isCollapsed) {
                                        expand()
                                    } else {
                                        collapse()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = TextStyle(textAlign = TextAlign.Center)
                )
                Button(
                    onClick = onLogin, modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_google),
                        contentDescription = "Google logo",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp)
                    )
                    Text(text = "Login with Google Account", color = Color.White)
                }
            }

        }
    }
}