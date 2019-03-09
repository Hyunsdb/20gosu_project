package com.example.a20gosu_proj

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView

class ResultImageActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
        fileUri = intent?.data

        imageView = findViewById(R.id.resultImage_imageView)
        imageView!!.setImageURI(fileUri)
    }
}
