package com.example.myparkea

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.LayoutInflaterCompat
import com.example.myparkea.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.firestore.FirebaseFirestore


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    companion object {
        const val MY_CHANNEL = "myChannel"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocation: FusedLocationProviderClient
    private var ultimoMarcador : Marker? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var ubicacion : LatLng


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createChannel()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.isAdded
        mapFragment.getMapAsync(this)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        try {
            val success : Boolean = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_standard))
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }
        } catch (e : Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't fine style. Error: $e")
        }

        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false



        fusedLocation.lastLocation.addOnSuccessListener {
            if(it != null){
                ubicacion = LatLng(it.latitude, it.longitude)
                val cameraPosition : CameraPosition = CameraPosition.Builder().target(ubicacion).zoom(17.5f).build()
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }

        db.collection("markers").get().addOnSuccessListener {
            for(documento in it){
                val coordenadas = LatLng(documento.get("latitud") as Double,documento.get("longitud") as Double)
                val marker : MarkerOptions = MarkerOptions().position(coordenadas).title(documento.get("Nombre") as String)
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                mMap.addMarker(marker)
                mMap.setOnMarkerClickListener(this)
            }
        }

        mMap.setOnMapLongClickListener {

            val markerOptions = MarkerOptions().position(it).flat(false).snippet("mensaje\ndireccion\nabierto hasta las 12")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            val nombreUbicacion = obtenerDireccion(it)
            markerOptions.title(nombreUbicacion)

            if(ultimoMarcador != null){
                ultimoMarcador!!.remove()
            }

            ultimoMarcador = mMap.addMarker(markerOptions)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 18f))


            /*val url : String = getDirectionsUrl(ubicacion,ultimoMarcador!!.position)

            val downloadTask : DownloadTask = DownloadTask()
            downloadTask.execute(url)*/

        }
    }

    private fun obtenerDireccion(latLng: LatLng) : String {
        val geocoder = Geocoder(this)
        val direcciones : List<Address>?
        val prDireccion : Address
        var txtDireccion = ""

        try {
            direcciones = geocoder.getFromLocation(
                latLng.latitude, latLng.longitude, 1
            )

            if((direcciones != null) && direcciones.isNotEmpty()) {
                prDireccion = direcciones[0]

                // Si la dirección tiene varias líneas
                if(prDireccion.maxAddressLineIndex > 0) {
                    for(i in 0 .. prDireccion.maxAddressLineIndex) {
                        txtDireccion += prDireccion.getAddressLine(i) + "\n"
                    }
                }
                // Si hay principal y secundario
                else {
                    txtDireccion += prDireccion.thoroughfare + ", " + prDireccion.subThoroughfare + "\n"
                }
            }
        } catch (e : Exception) {
            txtDireccion = "Dirección no encontrada"
        }

        return txtDireccion
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        val modalBottomSheet = ModalBottomSheet(this)
        modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
        modalBottomSheet.direccion(marker,packageManager)

        return false
    }

    private fun createChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MY_CHANNEL,
                "MySuperChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacion de Reserva"
            }

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

/*
    @SuppressLint("StaticFieldLeak")
    inner class DownloadTask : AsyncTask<String?, Void?, String>() {
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parseTask = ParserTask()
            parseTask.execute(result)
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                data = downloadUrl(url[0].toString())
            }catch (e: java.lang.Exception) {
                Log.d("Background Task ", e.toString())
            }
            return data
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ParserTask : AsyncTask<String?, Int?, List<List<HashMap<String,String>>>?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String,String>>>? = null

            try {
                jObject = jsonData[0]?.let { JSONObject(it) }!!
                val parser = DataParser()
                routes = parser.parse(jObject)
            }catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return routes
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: List<List<HashMap<String,String>>>?) {
            val points = ArrayList<LatLng?>()
            val lineOptions = PolylineOptions()

            for(i in result!!.indices) {
                val path = result[i]

                for(j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat,lng)
                    points.add(position)
                }

                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.BLUE)
                lineOptions.geodesic(true)
            }

            if(points.size != 0)
                mMap.addPolyline(lineOptions)
        }

    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String?

            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }

            data = sb.toString()
            br.close()

        }catch (e: java.lang.Exception) {
            Log.d("Exception ", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }

        return data
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        val mode = "mode=driving"
        val parameters = "$strOrigin&$strDest$mode"
        val output = "json"

        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyBV7eau74VhalFjWundbrucMiYSaqIQWR0"
    }*/
}

