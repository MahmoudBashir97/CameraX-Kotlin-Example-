package com.mahmoudbashir.cameraxapp

import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mahmoudbashir.cameraxapp.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var imageCapture:ImageCapture? = null
    private lateinit var outputDirectory:File
    lateinit var cameraExecuter : ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        outputDirectory = getOutPutDirectory()
        cameraExecuter = Executors.newSingleThreadExecutor()

        if (allPermissionGranted()){
           // Toast.makeText(this,"Have Permission ",Toast.LENGTH_LONG).show()
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this,Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSION
            )
        }

        binding.takePicBtn.setOnClickListener{
            takePhoto()
        }
    }

    private fun getOutPutDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {mfile->
            File(mfile,resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if(mediaDir != null && mediaDir.exists())
               mediaDir else filesDir
    }

    private fun takePhoto() {
        val imgCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(Constants.FILE_NAME_FORMATE,
            Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOption = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        imgCapture.takePicture(
            outputOption,ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo Saved"

                    Toast.makeText(
                        this@MainActivity,
                        "$msg  $savedUri",
                        Toast.LENGTH_LONG
                    ).show()

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constants.TAG,
                    "onError: ${exception.message}",
                        exception)
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSION){

           if (allPermissionGranted()){
               // our code
               startCamera()
           }else{
               Toast.makeText(this,"Permission Not Granted",Toast.LENGTH_SHORT).show()

               finish()
           }
       }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this)

        val cameraProvider:ProcessCameraProvider = cameraProviderFuture.get()
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder()
                    .build()
                    .also {mPreview ->
                        mPreview.setSurfaceProvider(binding.previewV.surfaceProvider)
                    }
                imageCapture = ImageCapture.Builder()
                    .build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this,cameraSelector,
                    preview,imageCapture)

                }catch (e:Exception){
                    e.message
                }

            },ContextCompat.getMainExecutor(this)
        )
    }

    private fun allPermissionGranted() = Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext,it) == PackageManager.PERMISSION_GRANTED }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecuter.shutdown()
    }
}