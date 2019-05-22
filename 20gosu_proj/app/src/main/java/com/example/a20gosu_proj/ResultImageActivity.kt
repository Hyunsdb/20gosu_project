package com.example.a20gosu_proj

import android.content.Context
import android.content.SharedPreferences
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
import com.google.firebase.database.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_result_image.*
import kotlinx.android.synthetic.main.flag_spinner.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.util.*


class ResultImageActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var fileUri: Uri? = null
    private var path:String? = null
    private var check1:Boolean?=true
    private var check2:Boolean?=true
    private var check3:Boolean?=true
    private var check4:Boolean?=true
    private var check5:Boolean?=true
    lateinit var mp:MediaPlayer
    var bitmap:Bitmap? = null
    val builder=StringBuilder()
    lateinit var mTTS:TextToSpeech
    lateinit var sTTS:TextToSpeech
    var langText =LangPreference.getLangText().toString().toLowerCase()

    var words2 = arrayOfNulls<String>(100)
    var resultWord = arrayOfNulls<DBWord>(5)
    private lateinit var database: DatabaseReference

    @IgnoreExtraProperties
    data class DBWord(
        var english: String? = "",
        var spanish: String? = "",
        var korean: String? = ""
    ){
        @Exclude
        fun toMap(): Map<String, Any?>{
            return mapOf(
                "spanish" to spanish,
                "korean" to korean
            )
        }
        fun print(){
            println("DB 출력 "+english+" "+spanish+" "+korean)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
        FirebaseApp.initializeApp(this@ResultImageActivity)
        fileUri = intent?.data
        path = intent.getStringExtra("path")
        database = FirebaseDatabase.getInstance().reference
        imageView = findViewById(R.id.resultImage_imageView)
        imageView!!.setImageURI(fileUri)
        bitmap= MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
        runDetector(bitmap!!)



        println("========")

        println(langText)

        mTTS= TextToSpeech(applicationContext,TextToSpeech.OnInitListener { status ->
            if(status!= TextToSpeech.ERROR){
                mTTS.language= Locale.UK
            }
        })

        sTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if(status!=TextToSpeech.ERROR){
                sTTS.setLanguage(Locale("spa", "ESP"))
            }
        })



        mp = MediaPlayer.create(this, R.raw.wordclicksound)


        resultImage_textView1.setOnClickListener {
            mp.start()
            if(check1==false){
                resultImage_textView1.text=langChanege(0,langText)
                check1=true
            }else{
                resultImage_textView1.text=resultWord[0]?.korean?: ""
                check1=false
            }
        }

        resultImage_textView2.setOnClickListener {
            mp.start()
            if(check2==false){
                resultImage_textView2.text=langChanege(1,langText)
                check2=true
            }else{
                resultImage_textView2.text=resultWord[1]?.korean?: ""
                check2=false
            }
        }

        resultImage_textView3.setOnClickListener {
            mp.start()
            if(check3==false){
                resultImage_textView3.text=langChanege(2,langText)
                check3=true
            }else{
                resultImage_textView3.text=resultWord[2]?.korean?: ""
                check3=false
            }
        }

        resultImage_textView4.setOnClickListener {
            mp.start()
            if(check4==false){
                resultImage_textView4.text=langChanege(3,langText)
                check4=true
            }else{
                resultImage_textView4.text=resultWord[3]?.korean?: ""
                check4=false
            }
        }

        resultImage_textView5.setOnClickListener {
            mp.start()
            if(check5==false){
                resultImage_textView5.text=langChanege(4,langText)
                check5=true
            }else{
                resultImage_textView5.text=resultWord[4]?.korean?: ""
                check5=false
            }
        }
        wordSound1.setOnClickListener{speechWord(0,langText)}
        wordSound2.setOnClickListener{speechWord(1,langText)}
        wordSound3.setOnClickListener{speechWord(2,langText)}
        wordSound4.setOnClickListener{speechWord(3,langText)}
        wordSound5.setOnClickListener{speechWord(4,langText)}

    }

        fun langChanege(i: Int, langText: String): String? {
            if(langText=="english")
                return resultWord[i]?.english
            else {
                return resultWord[i]?.spanish
            }

        }

        fun speechWord(i : Int, langText: String){
            if(langText=="english"){
        if (resultWord[i]?.english == null) {
//            Toast.makeText(this, "단어를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, resultWord[i]?.english, Toast.LENGTH_SHORT).show()
            mTTS.speak(resultWord[i]?.english, TextToSpeech.QUEUE_FLUSH, null)
        }
            }
            if(langText=="spanish") {
                if (resultWord[i]?.spanish == null) {
                    Toast.makeText(this, "단어를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
                } else if (langText == "spanish") {
                    Toast.makeText(this, resultWord[i]?.spanish, Toast.LENGTH_SHORT).show()
                    sTTS.speak(resultWord[i]?.spanish, TextToSpeech.QUEUE_FLUSH, null)
                }
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
                        checkDatabase(words2)
                    }
                }
            })
            .addOnFailureListener{e-> Log.d("Edmmer",e.message)}

    }

    private fun checkDatabase(words: Array<String?>){
        val wordDataListener = object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var dbidx: Int = 0
                for(word in words){
                    if(word == null) break
                    var tmpword:DBWord? = dataSnapshot.child(word).getValue(DBWord::class.java)
                    if(tmpword != null){
                        tmpword.english = word
                        resultWord[dbidx++] = tmpword
                    }
                    if(dbidx == 5)  break
                }
                changeTextView(resultWord)
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@ResultImageActivity, "오류!", Toast.LENGTH_SHORT)
            }
        }
        database.addListenerForSingleValueEvent(wordDataListener)
    }
    private fun changeTextView(dbwords: Array<DBWord?>) {
        if (langText == "spanish") {
            resultImage_textView1.text = resultWord[0]?.spanish ?: ""
            resultImage_textView2.text = resultWord[1]?.spanish ?: ""
            resultImage_textView3.text = resultWord[2]?.spanish ?: ""
            resultImage_textView4.text = resultWord[3]?.spanish ?: ""
            resultImage_textView5.text = resultWord[4]?.spanish ?: ""
        }
        if (langText == "english") {
            resultImage_textView1.text = resultWord[0]?.english ?: ""
            resultImage_textView2.text = resultWord[1]?.english ?: ""
            resultImage_textView3.text = resultWord[2]?.english ?: ""
            resultImage_textView4.text = resultWord[3]?.english ?: ""
            resultImage_textView5.text = resultWord[4]?.english ?: ""
        }
    }
}