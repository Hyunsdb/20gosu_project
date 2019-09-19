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
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.widget.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private var mainButtonGallery: ImageButton? =null
    companion object {
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 0
        private val REQUEST_TAKE_PHOTO = 1
        private val REQUEST_WRITE_EXTERNAL = 2
    }
    lateinit var photoPath: String
    var currentPhotoPath: String=""

    var imgRes = intArrayOf(R.drawable.ukemo, R.drawable.spainemo)
    var data1 = arrayOf("English","Spanish")


    var langText:String?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()
        var flagList=ArrayList<HashMap<String,Any>>()
        var flagidx=0
        while(flagidx<data1.size){
            var map= HashMap<String,Any>()
            map.put("photo",imgRes[flagidx])
            map.put("data1",data1[flagidx])
            flagList.add(map)
            flagidx++
        }
        var sharedPreferences=getSharedPreferences("Language", Context.MODE_PRIVATE)
        val keys=arrayOf("photo","data1")
        val ids= intArrayOf(R.id.flagImage,R.id.flagText)
        var spinnerAdapter: SimpleAdapter = SimpleAdapter(this,flagList, R.layout.flag_spinner, keys, ids)
        var spinner: Spinner = flagspinner
        spinner?.adapter = spinnerAdapter
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                var language= parent?.getItemAtPosition(position).toString()
                langText=langParse(language)
               LangPreference.setLangText(langText!!)



            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }








    val t = Thread(Runnable {
            //  Initialize SharedPreferences
            val getPrefs = PreferenceManager
                .getDefaultSharedPreferences(baseContext)

            //  Create a new boolean and preference and set it to true
            val isFirstStart = getPrefs.getBoolean("firstStart", true)

            //  If the activity has never started before...
            if (isFirstStart) {

                //  Launch app intro
                val i = Intent(this@MainActivity, IntroActivity::class.java)

                runOnUiThread { startActivity(i) }

                //  Make a new preferences editor
                val e = getPrefs.edit()

                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("firstStart", false)

                //  Apply changes
                e.apply()
            }
        })

        // Start the thread
        t.start()

        mainButtonGallery = findViewById<View>(R.id.main_button_gallery) as ImageButton
        mainButtonGallery!!.setOnClickListener {
            selectImageInAlbum()
        }

        main_button_camera.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL)
            }else {
                openCameraApp()

            }
        }
//        main_button_result_gallery.setOnClickListener {
//            reultImageInAlbum()
//        }
    }



    fun langParse(data:String): String {
        val onestep=data.substringBefore(",")
        val twostep =onestep.substringAfter("=")
        return twostep

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_EXTERNAL) openCameraApp()
    }
    private fun setupPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        val readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(cameraPermission!=PackageManager.PERMISSION_GRANTED) {ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),REQUEST_TAKE_PHOTO)}
        if(readStoragePermission!=PackageManager.PERMISSION_GRANTED) {ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_SELECT_IMAGE_IN_ALBUM)}
        if(writeStoragePermission!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL)}
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
        val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/WaKnowPic")
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
    fun reultImageInAlbum(){
        val storageDir:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/WaKnowPic/result")
        val targetUri:Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val storageUri: Uri = FileProvider.getUriForFile(applicationContext, "com.example.android.fileprovider", storageDir)
        Log.d("Y2K2", targetUri.toString())
        Log.d("Y2K22", storageUri.toString())
        Log.d("Y2K222", storageUri.path)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(storageUri, "image/*")
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(intent.resolveActivity(packageManager)!=null){
            startActivity(intent)
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
            //addToGallery()
            Toast.makeText(this, "이미지 촬영 완료", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ResultImageActivity::class.java)
            intent.data = Uri.fromFile(File(currentPhotoPath))
            intent.putExtra("path", currentPhotoPath)
            startActivity(intent)
            addToGallery()
        }
    }
}



