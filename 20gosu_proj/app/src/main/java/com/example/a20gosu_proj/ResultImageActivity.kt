package com.example.a20gosu_proj

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.example.a20gosu_proj.BuildConfig.ApiKey
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.UploadTask


class ResultImageActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var fileUri: Uri? = null
    lateinit var storage: FirebaseStorage
    private var path:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
        FirebaseApp.initializeApp(this)
        fileUri = intent?.data
        path = intent.getStringExtra("path")
        storage = FirebaseStorage.getInstance("gs://gosuproj-2685d.appspot.com/")

        imageView = findViewById(R.id.resultImage_imageView)
        imageView!!.setImageURI(fileUri)
//        sendPost()
        uploadToCloud()
    }

    fun uploadToCloud(){
        val storageRef = storage.reference

        var file = Uri.fromFile(File(path))
        val riversRef = storageRef.child("images/${file.lastPathSegment}")
        val uploadTask = riversRef.putFile(file)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }

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
            } else {
                // Handle failures
                // ...
            }
        }

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }

    }
    fun sendPost(){
        val url = "https://vision.googleapis.com/v1/images:annotate?key=" + ApiKey
        val client = OkHttpClient()
        val JSON = MediaType.get("application/json; charset=utf-8")
        val body= RequestBody.create(JSON, "{\n" +
                "  \"requests\": [\n" +
                "    {\n" +
                "      \"image\": {\n" +
                "        \"source\": {\n" +
                "          \"imageUri\": \"gs://wearethegreatgosu/demo-image.jpg\"\n" +
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

        Thread(){
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            println(response.request())
            println(response.body()!!.string())
        }.start()
    }
}
