package com.example.a20gosu_proj

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class SelectImageActivity : AppCompatActivity() {
    private var buttonOk :Button? = null
    private var buttonCancel: Button? = null
    private var imageView: ImageView? = null
    private var fileUri: Uri? = null

    companion object {
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)


        buttonOk = findViewById(R.id.selectImage_button_ok) as Button
        buttonCancel = findViewById(R.id.selectImage_button_cancel) as Button
        imageView = findViewById(R.id.selectImage_imageView) as ImageView

        fileUri = intent?.data
        imageView!!.setImageURI(fileUri)

        buttonCancel!!.setOnClickListener { selectImageInAlbum() }
        buttonOk!!.setOnClickListener { confirmImage() }
    }




    fun confirmImage(){
        //다음 액티비티로 전환
        Toast.makeText(this, "이미지 선택 완료", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ResultImageActivity::class.java)
        intent.data = fileUri
        startActivity(intent)
        finish()
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
            imageView!!.setImageURI(data?.data)
        }
    }
}
