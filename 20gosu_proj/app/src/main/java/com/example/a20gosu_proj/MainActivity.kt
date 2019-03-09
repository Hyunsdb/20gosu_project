package com.example.a20gosu_proj

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private var mainButtonGallery: Button? =null
    companion object {
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainButtonGallery = findViewById<View>(R.id.main_button_gallery) as Button
        mainButtonGallery!!.setOnClickListener { selectImageInAlbum() }
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
