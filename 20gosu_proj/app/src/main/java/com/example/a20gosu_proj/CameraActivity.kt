package com.example.a20gosu_proj

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {
    private var photoimageView: ImageView? = null
    private var photofileUri: Uri? = null

    val REQUEST_TAKE_PHOTO = 1
    lateinit var photoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
    }

    fun confirmImage(){
        //다음 액티비티로 전환
        Toast.makeText(this, "이미지 촬영 완료", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ResultImageActivity::class.java)
        intent.data = photofileUri
        startActivity(intent)
        finish()
    }

    fun openCameraApp(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(intent.resolveActivity(packageManager)!=null) {

            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            }catch (e: IOException){}
            if(photoFile != null) {
                val photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider",photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                startActivityForResult(intent,REQUEST_TAKE_PHOTO)

            }
        }
    }

    fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "MyPicture" + timeStamp
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(fileName,".jpg",storageDir)

        photoPath = image.absolutePath

        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            photoimageView!!.setImageURI(data?.data)
        }
    }
}
