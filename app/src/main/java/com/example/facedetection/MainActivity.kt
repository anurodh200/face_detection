package com.example.facedetection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btn)

        btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            } else {
                // Permission already granted, launch camera
                launchCamera()
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Oops, no camera app found...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                launchCamera()
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val extras = data?.extras
            val bitmap = extras?.get("data") as? Bitmap
            detectFace(bitmap)
        }
    }

    private fun detectFace(bitmap: Bitmap?) {
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(highAccuracyOpts)
        val image = InputImage.fromBitmap(bitmap!!, 0)

        val result = detector.process(image)
            .addOnSuccessListener { faces ->
                var resultText = " "
                var i = 1
                for (face in faces) {
                    resultText = "Face Number $i" +
                            "\n Smile%age: ${face.smilingProbability?.times(100)} %" +
                            "\n Left Eye Open: ${face.leftEyeOpenProbability?.times(100)} %" +
                            "\n Right Eye Open: ${face.rightEyeOpenProbability?.times(100)} %"

                    i++
                }

                if (faces.isEmpty()) {
                    Toast.makeText(this, "Oops, no faces detected...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, resultText, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Oops, something went wrong...", Toast.LENGTH_SHORT).show()
            }
    }
}
