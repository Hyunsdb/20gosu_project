package com.example.knowpic

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mainButtonGallery: Button? =null
    companion object {
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 0
    }

    val CAMERA_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()

        mainButtonGallery = findViewById<View>(R.id.main_button_gallery) as Button
        mainButtonGallery!!.setOnClickListener { selectImageInAlbum() }

        main_button_camera.setOnClickListener { openCameraApp() }
    }
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        if(permission!=PackageManager.PERMISSION_GRANTED) {makeRequest()}
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),CAMERA_REQUEST_CODE)
    }

    private var doubleBackToExitPressedOnce =false
    override fun onBackPressed() {
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce =true
        Toast.makeText(this,"한 번 더 뒤로가기 버튼을 클릭하면 종료됩니다.",Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce=false },2000)
    }

    fun openCameraApp(){
        val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(callCameraIntent.resolveActivity(packageManager)!=null) {
            startActivityForResult(callCameraIntent,CAMERA_REQUEST_CODE )
        }
    }

    fun selectImageInAlbum(){
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if(intent.resolveActivity(packageManager)!=null){
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK
                && requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM){
            var intent = Intent(this, SelectImageActivity::class.java)
            intent.data = data?.data
            startActivity(intent)
        }
    }
}
