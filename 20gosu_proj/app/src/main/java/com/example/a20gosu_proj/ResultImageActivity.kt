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
import android.support.annotation.NonNull
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_result_image.*
import okhttp3.*
import kotlin.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text


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
//                val downloadUri: Uri? = task.result
//                downloadUriToString = downloadUri.toString()
                Thread(){
                    sendPost(task.result)
                }.start()
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
    fun sendPost(downloadUrl: Uri?){
        downloadUriToString = downloadUrl.toString()
        println("다운로드 "+downloadUriToString)
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

        var response:Response? = null
        Thread {
            //Thread.sleep(6000L)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            response = client.newCall(request).execute()
//            println(response!!.request())
//            println(response!!.body()!!.string()) //response 결과 확인
            //val res = response!!.body()!!.string()
            this@ResultImageActivity.runOnUiThread(java.lang.Runnable {
                var gson = Gson() //Gson object 생성

                //json 에서 -> Gson object
                val parser = JsonParser()
                val rootObj = parser.parse(response!!.body()!!.string())
                println(rootObj)
                var num=if((Integer.parseInt((gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.size()))))>5){
                    4
                }else{
                    Integer.parseInt(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.size()))-1
                }
                var i=0
                while(num>=0){
                    var wordparsing=gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("name").asString)
                    var resultword = wordparsing.replace("\"","")
                    var word1:TextView = findViewById(R.id.resultImage_textView1)
                    var word2:TextView = findViewById(R.id.resultImage_textView2)
                    var word3:TextView = findViewById(R.id.resultImage_textView3)
                    var word4:TextView = findViewById(R.id.resultImage_textView4)
                    var word5:TextView = findViewById(R.id.resultImage_textView5)





                    if(i==0){
                        word1.setText(resultword)
                    }else if(i==1){
                        word2.setText(resultword)
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("y")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("y")))

                    }else if(i==2){
                        word3.setText(resultword)
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("y")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("y")))

                    }else if(i==3){
                        word4.setText(resultword)
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("y")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("y")))

                    }else if(i==4){
                        word5.setText(resultword)
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(0).asJsonObject.get("y")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("x")))
                        println(gson.toJson(rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(i).asJsonObject.get("boundingPoly").asJsonObject.get("normalizedVertices").asJsonArray.get(1).asJsonObject.get("y")))

                    }
                    i++;
                    num--;
                }


            })
        }.start()

    }


}
