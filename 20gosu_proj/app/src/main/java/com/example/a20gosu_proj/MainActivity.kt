package com.example.a20gosu_proj

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var mainButtonGallery: Button? =null
    companion object {
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 0
    }

    lateinit var photoPath: String
    val REQUEST_TAKE_PHOTO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()

        mainButtonGallery = findViewById<View>(R.id.main_button_gallery) as Button
        mainButtonGallery!!.setOnClickListener { selectImageInAlbum() }

        main_button_camera.setOnClickListener { openCameraApp() }
    }
    private fun setupPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        val galleryPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(cameraPermission!=PackageManager.PERMISSION_GRANTED) {makeRequest()}
        if(galleryPermission!=PackageManager.PERMISSION_GRANTED) {makeRequest()}
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),REQUEST_TAKE_PHOTO)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),REQUEST_TAKE_PHOTO)
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

            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            }catch (e: IOException){}
            if(photoFile != null) {
                val photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider",photoFile)
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(callCameraIntent,REQUEST_TAKE_PHOTO )
            }
        }
    }

    fun createImageFile(): File {
        val fileName = "MyPicture"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(fileName,".jpg",storageDir)

        photoPath = image.absolutePath

        return image
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
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {

        }
    }
}
