package com.arkeglorus.dreamsight.v1

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GestureDetectorCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arkeglorus.dreamsight.CallManager
import com.arkeglorus.dreamsight.databinding.ActivityDreamsightBinding
import com.arkeglorus.dreamsight.model.*
import com.google.android.gms.location.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


class DreamsightActivity : AppCompatActivity(),GestureDetector.OnGestureListener {
    private var guides: ArrayList<Pair<Float, Float>> = arrayListOf()
    private var position: HeadsUpSkeleton = HeadsUpSkeleton()
    private var touchDistance: Float = 30f
    private var guideDistance: Float = 34f
    private var broadcastReceiver: DreamsightNotificationsBroadcastReceiver =
        DreamsightNotificationsBroadcastReceiver()
    private var callManager: CallManager? = null
    private var usableModel: MutableMap<String, Base> = hashMapOf()
    private var modelQueue: LinkedList<String> = LinkedList<String>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var queue: RequestQueue
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var binding: ActivityDreamsightBinding
    private var lastWeatherRequest: Date = Date(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = params

        binding = ActivityDreamsightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                locationResult.lastLocation?.let { updateWeather(it) }
                val speed = locationResult.lastLocation?.speedAccuracyMetersPerSecond
                (usableModel[Speedometer.identifier] as? Speedometer)?.let { it ->
                    if (speed != null) {
                        it.speedMeterPerSecond = speed
                    }
                    updateUI(it)
                }
                (usableModel[Maps.identifier] as? Maps)?.let { it ->
                    if (speed != null) {
                        it.mapsSpeed = speed
                    }
                    updateUI(it)
                }
            }
        }

        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = GestureDetectorCompat(this, this)

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.arkeglorus.dreamsight")
        registerReceiver(broadcastReceiver, intentFilter)
        callManager = CallManager(this)

        queue = Volley.newRequestQueue(this)

        initModels(
            Direction(binding.directionContainer),
            ClockWeather(binding.clockWeatherContainer),
            Maps(binding.mapsContainer),
            Speedometer(binding.speedometerContainer),
            Whatsapp(binding.whatsAppContainer)
        )
        Log.v("focus", "notif is ${checkNotifService()}")
        toggleNotificationListenerService()

        // Get last masked view to show
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        maskView(
            HeadsUpSkeleton(
                sharedPref.getFloat("centerX",0f),
                sharedPref.getFloat("centerY",0f),
                sharedPref.getFloat("radius",0f),
                sharedPref.getFloat("rotation",0f),
                sharedPref.getBoolean("mirror",true)
            )
        )
    }

    private fun toggleNotificationListenerService() {
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

    private fun checkNotifService(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(this.packageName)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private lateinit var locationCallback: LocationCallback

    fun createLocationRequest(): LocationRequest? {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createLocationRequest()?.let {
            fusedLocationClient.requestLocationUpdates(
                it,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    // Setup module for display
    private fun initModels(vararg models: Base) {
        var i = 0
        for (m in models) {
            i += 1
            usableModel[m.itemIdentifier] = m
            modelQueue.add(m.itemIdentifier)
        }
        usableModel[modelQueue.first]?.let { makeVisible(it) }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event?.run {
            guides = getArrayOfPointer(this)
            if (guides.count() == 2) {
                position = calculateGuidePositionRelative(guides)
                maskView(position)
            } else if (mDetector.onTouchEvent(event)){
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_DPAD_UP || event?.keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            return nextDisplay()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDown: $event")
        return false
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(DEBUG_TAG, "onFling: $event1 $event2")
        val diffY = event2.y - event1.y
        val diffX = event2.x - event1.x
        println("${diffX},${diffY}")
        if (diffY > 0) {
            previousDisplay()
        } else {
            nextDisplay()
        }
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //Log.d(Companion.DEBUG_TAG, "onScroll: $event1 $event2")
        //nextDisplay()
        return false
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    private fun nextDisplay(): Boolean {
        val next = modelQueue.poll()
        modelQueue.add(next)
        next?.let { s ->
            usableModel[s]?.let {
                makeVisible(it)
            }
        }
        usableModel[modelQueue.first]?.let { makeVisible(it) }
        return true
    }

    private fun previousDisplay(): Boolean {
        val next = modelQueue.last()
        modelQueue.remove(next)
        modelQueue.add(0,next)
        usableModel[modelQueue.first]?.let { makeVisible(it) }
        return true
    }

    private fun maskView(position: HeadsUpSkeleton) {
        if (position.centerX + position.centerY + position.radius > 0) {
            binding.headsupsView.guideCenterX = position.centerX
            binding.headsupsView.guideCenterY = position.centerY
            binding.headsupsView.guideRadius = position.radius

            val lp = binding.headsUpContainer.layoutParams as RelativeLayout.LayoutParams
            val width = position.radius * 2 / sqrt(2f) * 0.45
            lp.width = width.toInt()
            lp.height = width.toInt()
            lp.leftMargin = (position.centerX - width / 2).toInt()
            lp.topMargin = (position.centerY - width / 2).toInt()
            binding.headsUpContainer.layoutParams = lp
            binding.headsUpContainer.rotation = position.rotation
            binding.headsUpContainer.scaleX = if (position.mirror) -1f else 0f
            val padding = 0//(position.third / 2).toInt()
            binding.headsUpContainer.setPadding(padding, padding, padding, padding)
        }
    }

    // Return (center x, center y, diameter)
    private fun calculateGuidePositionRelative(guides: ArrayList<Pair<Float, Float>>): HeadsUpSkeleton {

        val topPoint: Pair<Float, Float> =
            if (guides[0].second < guides[1].second) guides[0] else guides[1]
        val bottomPoint: Pair<Float, Float> =
            if (guides[0].second < guides[1].second) guides[1] else guides[0]
        val nextPointOnTheLeft: Boolean = topPoint.first > bottomPoint.first

        val extraRotation = atan2(
            abs(topPoint.first - bottomPoint.first),
            abs(topPoint.second - bottomPoint.second)
        ) * 180 / PI.toFloat()

        val rotation = if (nextPointOnTheLeft) {
            90f + extraRotation
        } else {
            90f - extraRotation
        }
        val centerX = if (nextPointOnTheLeft) {
            topPoint.first - (topPoint.first-bottomPoint.first)+ (guideDistance/touchDistance) * (topPoint.first-bottomPoint.first)
        } else {
            topPoint.first + (bottomPoint.first-topPoint.first)+ (guideDistance/touchDistance) * (bottomPoint.first-topPoint.first)
        }
        val centerY: Float = topPoint.second + (bottomPoint.second-topPoint.second)+ (guideDistance/touchDistance) * (bottomPoint.second-topPoint.second)

        val radius: Float = hypot(
            abs(topPoint.first - bottomPoint.first),
            abs(topPoint.second - bottomPoint.second)
        ) / 2
        println("X length: ${abs(topPoint.first - bottomPoint.first)}")
        println("Y length: ${abs(topPoint.second - bottomPoint.second)}")
        println("${centerX},${centerY},${rotation}")
        return saveLastPosition(HeadsUpSkeleton(centerX, centerY, radius, rotation, true))
    }

    private fun saveLastPosition(skeleton: HeadsUpSkeleton): HeadsUpSkeleton {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return HeadsUpSkeleton()
        with (sharedPref.edit()) {
            putFloat("centerX", skeleton.centerX)
            putFloat("centerY", skeleton.centerY)
            putFloat("radius", skeleton.radius)
            putFloat("rotation", skeleton.rotation)
            putBoolean("mirror", skeleton.mirror)
            apply()
        }
        return skeleton
    }

    private fun getArrayOfPointer(m: MotionEvent): ArrayList<Pair<Float, Float>> {
        val pointerCount = m.pointerCount
        if (pointerCount != 2) return arrayListOf()

        val arrayOfPointer: ArrayList<Pair<Float, Float>> = arrayListOf()

        for (i in 0 until pointerCount) {
            val x = m.getX(i)
            val y = m.getY(i)
            val action = m.actionMasked

            if (action == MotionEvent.ACTION_POINTER_UP) {
                arrayOfPointer.add(Pair(x, y))
            }
        }
        return arrayOfPointer
    }

    // Use this to make model visible
    private fun makeVisible(model: Base) {
        updateUI(model)
        for (item in usableModel) {
            if (item.value.itemIdentifier == model.itemIdentifier && item.value.view.visibility == View.GONE)
                item.value.view.visibility = View.VISIBLE
            else if (item.value.view.visibility == View.VISIBLE)
                item.value.view.visibility = View.GONE
        }
    }

    private fun updateWeather(location: Location) {
        if (Date().time - lastWeatherRequest.time < 60000) return
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&appid=b810ddd432201f1b480257c505f54073&lang=id"
        val weatherRequest = JsonObjectRequest(url,
            { response ->
                try {
                    val id = response.getJSONArray("weather").getJSONObject(0).getString("icon")
                    (usableModel[ClockWeather.identifier] as? ClockWeather)?.let { it ->
                        it.weatherUrl = "http://openweathermap.org/img/w/${id}.png"
                        updateUI(it)
                    }
                } catch (e: Exception) {
                    // Do noting
                }
            },
            {}
        )
        queue.add(weatherRequest)
        lastWeatherRequest = Date()
    }

    // Use this function to change the UI to some model
    private fun updateUI(model: Base) {
        when (model) {
            is Speedometer -> {
                binding.speedometerCurrentSpeed.text =
                    "%.2f km/h".format(model.speedMeterPerSecond * 18 / 5)
                binding.speedometerAverageSpeed.text =
                    "Average\n%.2f km/h".format(model.speedAverageMeterPerSecond * 18 / 5)
            }
            is Maps -> {
                binding.mapsDistance.text = model.mapsDistance
                binding.mapsETA.text = model.mapsDestinationEta
                binding.mapsAvgSpeed.text = "%.2f km/h".format(model.mapsSpeed * 18 / 5)
            }
            is ClockWeather -> {
                binding.clockTime.text = SimpleDateFormat("HH:mm").format(model.currentTime)
                if (model.weatherUrl.isNotEmpty()) {
                    Picasso.get().load(model.weatherUrl).into(binding.weatherImage, object : Callback {
                        override fun onSuccess() {
                            binding.weatherImage.drawable.setTint(Color.GREEN)
                        }

                        override fun onError(e: java.lang.Exception?) {

                        }
                    })
                }
            }
            is Direction -> {
                binding.directionNextDistance.text = model.mapsNextDistance
                binding.directionNextMove.text = model.mapsNextMove
                binding.directionDestination.text = model.mapsDestination
                binding.directionDestinationEta.text = model.mapsDestinationEta // ini yang perlu di fix
                model.mapsIcon?.let {
                    binding.directionIcon.setImageIcon(it.setTint(Color.GREEN))
                }
            }
            is Whatsapp -> {
                binding.waTitle.text = model.waTitle
                binding.waText.text = model.waText
            }
        }
    }

    inner class DreamsightNotificationsBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val appName = intent.getStringExtra(NotificationListener.APPS_NAME)

            if (appName == NotificationListener.ApplicationPackageNames.GOOGLE_MAPS_PACK_NAME) {
                (usableModel[Direction.identifier] as? Direction)?.let {
                    it.setFrom(intent)
                    if (modelQueue.peek() == it.itemIdentifier) {
                        updateUI(it)
                    }
                }
                (usableModel[Maps.identifier] as? Maps)?.let {
                    it.setFrom(intent)
                    if (modelQueue.peek() == it.itemIdentifier) {
                        updateUI(it)
                    }
                }
            } else if (appName == NotificationListener.ApplicationPackageNames.WHATSAPP_PACK_NAME) {
                (usableModel[Whatsapp.identifier] as? Whatsapp)?.let {
                    it.setFrom(intent)
                    if (modelQueue.peek() == it.itemIdentifier) {
                        updateUI(it)
                    }
                }

            }
        }
    }

    companion object {
        private const val DEBUG_TAG: String = "Dreamsight"
    }
}