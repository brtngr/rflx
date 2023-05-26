package com.arkeglorus.dreamsight

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.arkeglorus.dreamsight.databinding.ActivityHomeBinding
import com.arkeglorus.dreamsight.v1.DreamsightActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        requestPermission()

        binding.freeroam.setOnClickListener {
            if (binding.switch1.isChecked) {
                val i = Intent(this, DreamsightV2Activity::class.java)
                startActivity(i)
            } else {
                val i = Intent(this, DreamsightActivity::class.java)
                startActivity(i)
            }
        }

        binding.go.setOnClickListener {
            if (binding.editDestination.text.isNotEmpty()) {
                if (binding.switch1.isChecked) {
                    val i = Intent(this, DreamsightV2Activity::class.java)
                    startActivity(i)
                } else {
                    val i = Intent(this, DreamsightActivity::class.java)
                    startActivity(i)
                    val gmmIntentUri =
                        Uri.parse("google.navigation:q=${binding.editDestination.text}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            } else {
                Snackbar.make(it, "Destination can not empty", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val i = Intent(this, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(i)
            finish()
        }
        FirebaseAuth.getInstance().currentUser?.displayName?.let {
            binding.welcome.text = "Hi $it"
            binding.logout.visibility = View.VISIBLE
        }
    }

    private var enableNotificationListenerAlertDialog: AlertDialog? = null
    private fun isNotificationServiceEnabled(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.notification_listener_service)
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation)
        alertDialogBuilder.setPositiveButton(R.string.yes) { _, _ ->
            startActivity(
                Intent(
                    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                )
            )
        }
        alertDialogBuilder.setNegativeButton(R.string.no) { _, _ ->
            // If you choose to not enable the notification listener
            // the app. will not work as expected
        }
        return alertDialogBuilder.create()
    }

    private fun requestPermission() {
        if (!isNotificationServiceEnabled(this)) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog()
            enableNotificationListenerAlertDialog?.show()
        }

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            }).check()
    }

    // See: https://developer.android.com/training/basics/intents/result
    private fun isSignedIn(): FirebaseUser? {
        FirebaseAuth.getInstance().currentUser?.let {
            return it
        }
        // Firebase Authentication
        val providers = arrayListOf(
            //AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
        return null
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }
}