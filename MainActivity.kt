package com.example.a20gosu_proj

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mainButtonGallery: Button? =null
    companion object {
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 0
        private val REQUEST_TAKE_PHOTO = 1
        private val REQUEST_WRITE_EXTERNAL = 2
    }
    lateinit var photoPath: String
    var currentPhotoPath: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupCameraPermissions()

        mainButtonGallery = findViewById<View>(R.id.main_button_gallery) as Button
        mainButtonGallery!!.setOnClickListener { selectImageInAlbum() }

        main_button_camera.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL)
            }else {
                openCameraApp()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_EXTERNAL) openCameraApp()
    }
    private fun setupCameraPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        if(cameraPermission!=PackageManager.PERMISSION_GRANTED) {makeRequest()}

    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),REQUEST_TAKE_PHOTO)
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
    //카메라 실행
    fun openCameraApp(){
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createImageFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.android.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri)
        startActivityForResult(intent, REQUEST_TAKE_PHOTO)
    }
    //이미지 파일 생성 및 경로 지정
//    fun createImageFile(): File? {
//        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val fileName = "MyPicture" + timeStamp
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        val image = File.createTempFile(fileName,".jpg",storageDir)
//
//        photoPath = image.absolutePath
//
//        return image
//    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/MyPicture")
        if(!storageDir.exists()){
            storageDir.mkdir()
        }
        //val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun addToGallery() {
        val mediaScan = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(currentPhotoPath)
        val uri = Uri.fromFile(file)
        mediaScan.setData(uri)
        this.sendBroadcast(mediaScan)
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
        //바로 결과화면으로 가게 했습니다.
        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            addToGallery()
            Toast.makeText(this, "이미지 촬영 완료", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ResultImageActivity::class.java)
            intent.data = Uri.fromFile(File(currentPhotoPath))
            startActivity(intent)
        }
    }
}
