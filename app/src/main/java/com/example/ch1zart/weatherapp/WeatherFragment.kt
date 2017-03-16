package com.example.ch1zart.weatherapp



import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.locationManager
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.uiThread
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.InetAddress
import java.text.DateFormat
import java.util.*



class WeatherFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    lateinit var cityField: TextView
    lateinit var updatedField: TextView
    lateinit var detailsField: TextView
    lateinit var currentTemperatureField: TextView
    lateinit var jsonbtn: Button
    lateinit var imV2: ImageView

    lateinit var mGoogleMap: GoogleMap
    lateinit var mLocationRequest: LocationRequest
    lateinit var mGoogleApiClient: GoogleApiClient
    lateinit var find: EditText

    var LastLat = 0f
    var LastLon = 0f

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mGoogleApiClient = GoogleApiClient.Builder(this.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build()

        val rootView = inflater!!.inflate(R.layout.fragment_weather, container, false)

        return rootView
    }

    override fun onStart() {
        super.onStart()

        mGoogleApiClient.connect()
        jsonbtn = find<Button>(R.id.btn1)

        cityField = find<TextView>(R.id.city_field)
        updatedField = find<TextView>(R.id.updated_field)
        detailsField = find<TextView>(R.id.details_field)
        currentTemperatureField = find<TextView>(R.id.current_temperature_field)


        find = find<EditText>(R.id.et1)
        imV2 = find<ImageView>(R.id.imV2)

        jsonbtn.setOnClickListener {
            if (find.text != null)
                updateWeatherData(true, find.text.toString())

        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                //lazy checking
        if (!gps) {
            toast("Проверьте подключение к GPS и интернет!")
        }

    }

    fun updateWeatherData(find: Boolean, city: String) {
        doAsync {
            var json = RemoteFetch.getJSON(LastLat, LastLon, find, city)

            uiThread {
                renderWeather(json)
            }
        }
    }

    fun isInternetAvailable(): Boolean {
        try {
            val ipAddr = InetAddress.getByName("google.com") //You can replace it with your name
            return !ipAddr.equals("")

        } catch (e: Exception) {
            return false
        }

    }

    private fun renderWeather(json: JSONObject?) {

        cityField.setText(json!!.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"))

        val details = json!!.getJSONArray("weather").getJSONObject(0)
        val main = json.getJSONObject("main")
        detailsField.setText(details.getString("description").toUpperCase(Locale.US) + "\n" + "Влажность: " + main.getString("humidity") + "%" + "\n" + "Давление: " + main.getString("pressure") + " hPa")

        currentTemperatureField.text = String.format("%.2f", main.getDouble("temp")) + " ℃"

        val df = DateFormat.getDateTimeInstance()
        val updatedOn = df.format(Date(json.getLong("dt") * 1000))
        updatedField.text = "Последнее обновление: " + updatedOn

        setWeatherIcon(details.getInt("id"),
                json.getJSONObject("sys").getLong("sunrise") * 1000,
                json.getJSONObject("sys").getLong("sunset") * 1000)
    }


    private fun setWeatherIcon(actualId: Int, sunrise: Long, sunset: Long) {

        val id = actualId / 100
        if (actualId == 800) {
            val currentTime = Date().time
            if (currentTime >= sunrise && currentTime < sunset) {
                imV2.setImageResource(R.drawable.sun)
            } else {
                imV2.setImageResource(R.drawable.clearnight)
            }
        } else {
            when (id) {
                2 -> imV2.setImageResource(R.drawable.storm)
                3 -> imV2.setImageResource(R.drawable.drizzle)
                7 -> imV2.setImageResource(R.drawable.haze)
                8 -> imV2.setImageResource(R.drawable.cloudy)
                6 -> imV2.setImageResource(R.drawable.snowflake)
                5 -> imV2.setImageResource(R.drawable.rain)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient()
                mGoogleMap.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            mGoogleMap.isMyLocationEnabled = true
        }
    }

    @Synchronized protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this.context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()

        mLocationRequest.interval = 640000
        mLocationRequest.maxWaitTime = 640000
        mLocationRequest.fastestInterval = 640000

        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)

    }

    override fun onConnectionSuspended(i: Int) {
        toast("Suspended")
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        toast("failed")    }

    override fun onLocationChanged(location: Location) {

        LastLat = location.latitude.toFloat()
        LastLon = location.longitude.toFloat()
        updateWeatherData(false,"none")
    }

    override fun onStop() {
        mGoogleApiClient.disconnect()
        super.onStop()
    }
}

