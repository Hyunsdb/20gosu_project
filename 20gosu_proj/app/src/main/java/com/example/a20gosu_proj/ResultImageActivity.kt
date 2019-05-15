package com.example.a20gosu_proj

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.example.a20gosu_proj.BuildConfig.ApiKey
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_result_image.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.util.*


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
    lateinit var mp:MediaPlayer
    var bitmap:Bitmap? = null
    var wordpilec:String=""
    var wordArray= emptyArray<String>()
    val builder=StringBuilder()
    lateinit var mTTS:TextToSpeech

    var words2 = arrayOfNulls<String>(100)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
        FirebaseApp.initializeApp(this)
        fileUri = intent?.data
        path = intent.getStringExtra("path")
        storage = FirebaseStorage.getInstance("gs://gosuproj-2685d.appspot.com/")
        imageView = findViewById(R.id.resultImage_imageView)
        imageView!!.setImageURI(fileUri)
        //uploadToCloud()
        bitmap= MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
        runDetector(bitmap!!)

        mTTS= TextToSpeech(applicationContext,TextToSpeech.OnInitListener { status ->
            if(status!= TextToSpeech.ERROR){
                mTTS.language= Locale.US
            }
        })



        mp = MediaPlayer.create(this, R.raw.wordclicksound)


        resultImage_textView1.setOnClickListener {
            mp.start()
            if(check1==false){
                resultImage_textView1.text=words2[0]
                check1=true
            }else{
                resultImage_textView1.text="뜻"
                check1=false
            }
        }

        resultImage_textView2.setOnClickListener {
            mp.start()
            if(check2==false){
                resultImage_textView2.text=words2[1]

                check2=true
            }else{
                resultImage_textView2.text="뜻"
                check2=false
            }
        }

        resultImage_textView3.setOnClickListener {
            mp.start()
            if(check3==false){
                resultImage_textView3.text=words2[2]
                check3=true
            }else{
                resultImage_textView3.text="뜻"
                check3=false
            }
        }

        resultImage_textView4.setOnClickListener {
            mp.start()
            if(check4==false){
                resultImage_textView4.text=words2[3]
                check4=true
            }else{
                resultImage_textView4.text="뜻"
                check4=false
            }
        }

        resultImage_textView5.setOnClickListener {
            mp.start()
            if(check5==false){
                resultImage_textView5.text=words2[4]
                check5=true
            }else{
                resultImage_textView5.text="뜻"
                check5=false
            }
        }
        wordSound1.setOnClickListener{speechWord(0)}
        wordSound2.setOnClickListener{speechWord(1)}
        wordSound3.setOnClickListener{speechWord(2)}
        wordSound4.setOnClickListener{speechWord(3)}
        wordSound5.setOnClickListener{speechWord(4)}

    }
    fun compressImage(bitmap:Bitmap, quality:Int):Bitmap {
        // Initialize a new ByteArrayStream
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        val byteArray = stream.toByteArray()

        // Finally, return the compressed bitmap
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
        fun speechWord(i : Int){
        if (words2[i] == null) {
            Toast.makeText(this, "단어를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, words2[i], Toast.LENGTH_SHORT).show()
            mTTS.speak(words2[i], TextToSpeech.QUEUE_FLUSH, null)
        }
    }

     fun runDetector (bitmap : Bitmap?){

        val image = FirebaseVisionImage.fromBitmap(bitmap!!)
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .build()
        val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
        labeler.processImage(image)
            .addOnSuccessListener (object : OnSuccessListener<List<FirebaseVisionImageLabel>>{
                override fun onSuccess(labels: List<FirebaseVisionImageLabel>?) {
                    if (labels != null) {

                        var j=0

                        for (label in labels){
                            builder.append(label.text).append(",")

                        words2[j]=label.text
                           j++



                        }
                        changeTextView(words2)


                    }
                }
            })
            .addOnFailureListener{e-> Log.d("Edmmer",e.message)}

    }

    private fun changeTextView(word: Array<String?>){
        if(word[0]!=null){
            resultImage_textView1.text = word[0]
            resultImage_textView2.text = word[1]
            resultImage_textView3.text = word[2]
            resultImage_textView4.text = word[3]
            resultImage_textView5.text = word[4]
        }
    }

    fun arrayReading(array: Array<String>){
        for (i in array.indices)
            println(array[i])
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