package com.example.a20gosu_proj

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.example.a20gosu_proj.BuildConfig.ApiKey
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import kotlin.coroutines.*
import org.json.JSONArray
import org.json.JSONObject


class ResultImageActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var fileUri: Uri? = null
    lateinit var storage: FirebaseStorage
    private var path:String? = null
    var downloadUriToString:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
        FirebaseApp.initializeApp(this)
        fileUri = intent?.data
        path = intent.getStringExtra("path")
        storage = FirebaseStorage.getInstance("gs://gosuproj-2685d.appspot.com/")


        imageView = findViewById(R.id.resultImage_imageView)
        imageView!!.setImageURI(fileUri)
        uploadToCloud()


    }

    fun uploadToCloud(){
        val storageRef = storage.reference

        var file = Uri.fromFile(File(path))
        val riversRef = storageRef.child("images/${file.lastPathSegment}")
        var uploadTask = riversRef.putFile(file)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }

        val ref = storageRef.child("images/${file.lastPathSegment}")
        uploadTask = ref.putFile(file)

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation riversRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                downloadUriToString = downloadUri.toString()
                sendPost()
                //Log.d("Tag: value is ", downloadUriToString)

            } else {
                // Handle failures
                // ...
            }
        }
    }

    class Post {
        var mid: String? = null
        var name: String? = null
        var score: String? = null

        constructor() : super()

        constructor(Source: String, Target: String, Translatedtext: String) : super() {
            this.mid = Source
            this.name = Target
            this.score = Translatedtext
        }

    }
    fun sendPost(){
        val url = "https://vision.googleapis.com/v1/images:annotate?key=" + ApiKey
        val client = OkHttpClient()
        val JSON = MediaType.get("application/json; charset=utf-8")
        var  body= RequestBody.create(JSON, "{\n" +
                "  \"requests\": [\n" +
                "    {\n" +
                "      \"image\": {\n" +
                "        \"source\": {\n" +
                "          \"imageUri\": \"${downloadUriToString}\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"features\": [\n" +
                "        {\n" +
                "          \"type\": \"OBJECT_LOCALIZATION\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}")


        Thread {
            //Thread.sleep(6000L)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            println(response.request())
            //println(response.body()!!.string()) //response 결과 확인
            var gson = Gson() //Gson object 생성

            //json 에서 -> Gson object
            val parser = JsonParser()
            val rootObj = parser.parse(response.body()!!.string())
            var wordparsing = gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(0).asJsonObject.get("name").asString)
            //단어 파싱함. 한번에 접근하여 name만 가져옴.

            var resultword = wordparsing.replace("\"","")//출력될 단어 정리

            var word1 = findViewById<TextView>(R.id.resultImage_textView1)
            word1.setText(resultword)
            var word2 = findViewById<TextView>(R.id.resultImage_textView2)
            word2.setText("")
            var word3 = findViewById<TextView>(R.id.resultImage_textView3)
            word3.setText("")
            var word4 = findViewById<TextView>(R.id.resultImage_textView4)
            word4.setText("")
            var word5 = findViewById<TextView>(R.id.resultImage_textView5)
            word5.setText("")
        }.start()
    }


}
