package com.arkeglorus.dreamsight

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Base64.encodeToString
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.arkeglorus.dreamsight.ui.theme.DreamsightTheme
import com.arkeglorus.dreamsight.v1.NotificationListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("MissingPermission")
class DreamsightV2Activity : ComponentActivity() {

    lateinit var bleManager: LeManager
    private var broadcastReceiver: DreamsightV2Activity.DreamsightNotificationsBroadcastReceiver =
        DreamsightNotificationsBroadcastReceiver()
    val connected = mutableStateOf(false)
    val textToSend = mutableStateOf("")
    val textState = mutableStateOf("{\"next_distance\":\"0 m\",\"next_move\":\"-\",\"distance\":\"147 km\",\"destination\":\"toward Jl. Aralia Raya\",\"eta\":\"2 hr 39 min - 147 km\",\"time\":\"|20:29\",\"icon\":\"[B@5fab933\"}")
    var lastMapText = ""

    private fun onDisconnect() {
        bleManager.disconnect().enqueue()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DreamsightTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BluetoothStatus() {
                        textToSend.value = it
                        bleManager.write(textToSend.value)
                    }
                }
            }
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val leScanner = bluetoothManager.adapter.bluetoothLeScanner
        var address = ""

        bleManager = LeManager(this)
        bluetoothManager.adapter.bluetoothLeScanner.startScan(object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    Log.d(
                        "DeviceListActivity",
                        "onScanResult: ${result?.device?.address} - ${result?.device?.name}"
                    )
                    result?.device?.let {
                        if (it.name != null && it.name.equals("Reflex Add-On")) {
                            address = it.address
                            leScanner.stopScan(this)

                            bleManager.connect(it).retry(3, 1000)
                                .useAutoConnect(true)
                                .done {
                                    connected.value = true
                                    Log.d("BLE", "connected")
                                }.fail{ _: BluetoothDevice, i: Int ->
                                    Log.d("BLE", "failed $i")
                                }
                                .enqueue()
                        }
                    }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    Log.d("DeviceListActivity", "onBatchScanResults:${results.toString()}")
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Log.d("DeviceListActivity", "onScanFailed: $errorCode")
                }
            })

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.arkeglorus.dreamsight")
        registerReceiver(broadcastReceiver, intentFilter)
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                NotificationListener::class.java
            ), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                NotificationListener::class.java
            ), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect().enqueue()
        unregisterReceiver(broadcastReceiver)
    }

    var lastMaps = ""

    inner class DreamsightNotificationsBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val appName = intent.getStringExtra(NotificationListener.APPS_NAME)

            if (appName == NotificationListener.ApplicationPackageNames.GOOGLE_MAPS_PACK_NAME) {
                if (true) {
                    val mapsNextDistance = intent.getStringExtra(NotificationListener.MAPS_NEXT_DISTANCE) ?: "-"
                    val mapsNextMove = intent.getStringExtra(NotificationListener.MAPS_NEXT_MOVE)?: "-"
                    val mapsDistance = intent.getStringExtra(NotificationListener.MAPS_DISTANCE) ?: "-"
                    val mapsDestination = intent.getStringExtra(NotificationListener.MAPS_DESTINATION) ?: "-"
                    val mapsDestinationEta = intent.getStringExtra(NotificationListener.MAPS_DESTINATION_ETA) ?: "-"
                    val text = "$mapsNextDistance $mapsNextMove\n\n$mapsDistance ke $mapsDestination (ETA: $mapsDestinationEta)"
                    val contentJson = JSONObject()
                    contentJson.put("next_distance", mapsNextDistance)
                    contentJson.put("next_move", mapsNextMove)
                    contentJson.put("distance", mapsDistance)
                    contentJson.put("destination", mapsDestination)
                    contentJson.put("eta", mapsDestinationEta+" - "+mapsDistance)
                    contentJson.put("time",
                        "|"+SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
                    (intent.extras?.get(NotificationListener.MAPS_ICON) as? Icon)?.let {
                        val bitmapIcon = it.loadDrawable(context).toBitmap()

                        ///Log.d("Dreamsight", "Send: h:${bitmapIcon.height},w:${bitmapIcon.width}")
                        val resizedBitmap = Bitmap.createScaledBitmap(bitmapIcon, 69, 69, false)

                        val stream = ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val byteArray: ByteArray = stream.toByteArray()

                        bitmapIcon.recycle()
                        resizedBitmap.recycle()
                        val imageArray = JSONArray()
                        for (item in byteArray) {
                            imageArray.put(item)
                        }

                        Log.d("Dreamsight", "image: ${Base64.getEncoder().encodeToString(byteArray)}")
                        //contentJson.put("icon", String(byteArray))
                    }
                    if (contentJson.toString() != lastMapText) {
                    //if (true) {
                            lastMapText = contentJson.toString()
                            bleManager.write(contentJson.toString())
                    }

                }
            } else if (appName == NotificationListener.ApplicationPackageNames.WHATSAPP_PACK_NAME || appName == NotificationListener.ApplicationPackageNames.TELEGRAM_PACK_NAME) {
                if (connected.value) {
                    val title = intent.getStringExtra(NotificationListener.WA_TITLE) ?: "-"
                    val text = intent.getStringExtra(NotificationListener.WA_TEXT) ?: "-"
                    textToSend.value = "Whastapp\n$title\n$text"
                    bleManager.write(textToSend.value)
                }
            }
        }
    }

    @Composable
    fun BluetoothStatus(onSend: (text: String) -> Unit) {
        val connectedState by connected
        //val textState = remember { mutableStateOf(TextFieldValue()) }
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)) {

            if (connectedState){
                Text(text = "Bluetooth connected!")
                if (textToSend.value.isNotEmpty()){
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        Modifier
                            .background(Color.Black)
                            .fillMaxWidth()) {
                        Text(text = textToSend.value, color = Color.White, modifier = Modifier.padding(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = textState.value, onValueChange = {
                    textState.value = it
                }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxSize()) {
                    Button(onClick = { onSend(textState.value) }) {
                        Text(text = "Kirim tulisan")
                    }
                    Button(onClick = { onDisconnect() }) {
                        Text(text = "Disconnect")
                    }
                }

            }else{
                Text(text = "Bluetooth disconnected")
            }

        }

    }
}


