package com.example.scanner

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import github.nisrulz.qreader.QRDataListener
import github.nisrulz.qreader.QREader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {
    private var txt_result: TextView? = null
    private var surfaceView: SurfaceView? = null
    private var qrEader: QREader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Zatraži dopuštenje
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        setupCamera()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Toast.makeText(this@MainActivity, "Morate omogućiti ovo dopuštenje ", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {}
                }).check()
    }

    //POST
    private fun postMethod() {
        rawJSON()
    }

    private fun rawJSON() {

        // Create Retrofit
        val retrofit = Retrofit.Builder()
                .baseUrl("http://dummy.restapiexample.com")
                .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("name", "Jack")
        jsonObject.put("salary", "3540")
        jsonObject.put("age", "23")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            val response = service.createEmployee(requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                    response.body()
                                            ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                            )
                    )
                    Log.d("Pretty Printed JSON :", prettyJson)

                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)

                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }
    }

    private fun formData() {

        // Create Retrofit
        val retrofit = Retrofit.Builder()
                .baseUrl("https://httpbin.org")
                .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        // List of all MIME Types you can upload: https://www.freeformatter.com/mime-types-list.html

        // Get file from assets folder
        val file = getFileStreamPath("lorem_ipsum.txt")

        val fields: HashMap<String?, RequestBody?> = HashMap()
        fields["email"] = ("jack@email.com").toRequestBody("text/plain".toMediaTypeOrNull())
        fields["file\"; filename=\"upload_file.txt\" "] =
                (file).asRequestBody("text/plain".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {

            // Do the POST request and get response
            val response = service.uploadEmployeeData(fields)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                    response.body()
                                            ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                            )
                    )
                    Log.d("Pretty Printed JSON :", prettyJson)

                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)

                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }
    }

    private fun setupCamera() {
        txt_result = findViewById<View>(R.id.code_info) as TextView
        val btn_on_off = findViewById<View>(R.id.btn_enable_disable) as ToggleButton
        btn_on_off.setOnClickListener {
            if (qrEader!!.isCameraRunning) {
                btn_on_off.isChecked = false
                qrEader!!.stop()
            } else {
                btn_on_off.isChecked = true
                qrEader!!.start()
            }
        }
        surfaceView = findViewById<View>(R.id.camera_view) as SurfaceView
        setupQREader()
    }

    //ČITANJE QR KODA
    private fun setupQREader() {                                //POZIVANJE POST METODE
        qrEader = QREader.Builder(this, surfaceView, QRDataListener { postMethod() }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(surfaceView!!.height)
                .width(surfaceView!!.width)
                .build()
    }

    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        if (qrEader != null) qrEader!!.initAndStart(surfaceView)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Toast.makeText(this@MainActivity, "Morate omogućiti ovo dopuštenje ", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {}
                }).check()
    }

    override fun onPause() {
        super.onPause()
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        if (qrEader != null) qrEader!!.releaseAndCleanup()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Toast.makeText(this@MainActivity, "Morate omogućiti ovo dopuštenje ", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {}
                }).check()
    }
}