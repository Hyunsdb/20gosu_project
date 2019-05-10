package com.example.a20gosu_proj

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
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
import android.widget.Button
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
    private var word1:String?=null
    private var word2:String?=null
    private var word3:String?=null
    private var word4:String?=null
    private var word5:String?=null
    private var check1:Boolean?=true
    private var check2:Boolean?=true
    private var check3:Boolean?=true
    private var check4:Boolean?=true
    private var check5:Boolean?=true
    private val m = null;

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

        val progressionDialog = ProgressDialog(this)
        progressionDialog.setMessage("단어를 불러오고 있어요~")
        progressionDialog.setCancelable(false)
        progressionDialog.show()

        Handler().postDelayed({progressionDialog.dismiss()},3000)

        resultImage_textView1.setOnClickListener {
            if(check1==false){
                resultImage_textView1.text=word1
                check1=true
            }else{
                resultImage_textView1.text="뜻"
                check1=false
            }
        }

        resultImage_textView2.setOnClickListener {
            if(check2==false){
                resultImage_textView2.text=word2
                check2=true
            }else{
                resultImage_textView2.text="뜻"
                check2=false
            }
        }

        resultImage_textView3.setOnClickListener {
            if(check3==false){
                resultImage_textView3.text=word3
                check3=true
            }else{
                resultImage_textView3.text="뜻"
                check3=false
            }
        }

        resultImage_textView4.setOnClickListener {
            if(check4==false){
                resultImage_textView4.text=word4
                check4=true
            }else{
                resultImage_textView4.text="뜻"
                check4=false
            }
        }

        resultImage_textView5.setOnClickListener {
            if(check5==false){
                resultImage_textView5.text=word5
                check5=true
            }else{
                resultImage_textView5.text="뜻"
                check5=false
            }
        }



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
                Thread {
                    sendPost(task.result)
                }.start()
                //Log.d("Tag: value is ", downloadUriToString)

            } else {
                // Handle failures
                // ...
            }
        }
    }
    fun sendPost(downloadUrl: Uri?) {
        downloadUriToString = downloadUrl.toString()
        println("다운로드 " + downloadUriToString)
        val url = "https://vision.googleapis.com/v1/images:annotate?key=" + ApiKey
        val client = OkHttpClient()
        val JSON = MediaType.get("application/json; charset=utf-8")
        var body = RequestBody.create(
            JSON, "{\n" +
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
                    "}"
        )

        var response: Response? = null
        Thread {
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            response = client.newCall(request).execute()
            this@ResultImageActivity.runOnUiThread(java.lang.Runnable {
                var gson = Gson() //Gson object 생성

                //json 에서 -> Gson object
                val parser = JsonParser()
                val rootObj = parser.parse(response!!.body()!!.string())
                println(rootObj)
                var num = if ((Integer.parseInt(
                        (gson.toJson(
                            rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.size()
                        ))
                    )) > 5
                ) {
                    4
                } else {
                    Integer.parseInt(
                        gson.toJson(
                            rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get(
                                "localizedObjectAnnotations"
                            ).asJsonArray.size()
                        )
                    ) - 1
                }
                var i = 0
                while (num >= 0) {
                    var wordparsing = gson.toJson(
                        rootObj.asJsonObject.get("responses").asJsonArray.get(0).asJsonObject.get("localizedObjectAnnotations").asJsonArray.get(
                            i
                        ).asJsonObject.get("name").asString
                    )
                    var resultword = wordparsing.replace("\"", "")


                    if (i == 0) {
                        word1=resultword
                        resultImage_textView1!!.text = word1
                    } else if (i == 1) {
                        word2=resultword
                        resultImage_textView2!!.text = word2
                    } else if (i == 2) {
                        word3=resultword
                        resultImage_textView3!!.text = word3
                    } else if (i == 3) {
                        word4=resultword
                        resultImage_textView4!!.text = word4
                    } else if (i == 4) {
                        word5=resultword
                        resultImage_textView5!!.text = word5
                    }
                    i++
                    num--
                }
            })
        }.start()

    }

}