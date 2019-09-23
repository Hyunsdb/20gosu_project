package com.example.a20gosu_proj

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import kotlinx.android.synthetic.main.activity_result_image.*
import kotlinx.android.synthetic.main.popup.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*


class ResultImageActivity : AppCompatActivity() {
    private var imageButton: ImageView? = null
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
    lateinit var gTTS:TextToSpeech
    lateinit var iTTS:TextToSpeech
    lateinit var fTTS:TextToSpeech
    var langText =LangPreference.getLangText().toString().toLowerCase()


    var words2 = arrayOfNulls<String>(100)
    var resultWord = arrayOfNulls<DBWord>(5)
    private lateinit var database: DatabaseReference

    @IgnoreExtraProperties
    data class DBWord(
        var english: String? = "",
        var spanish: String? = "",
        var korean: String? = "",
        var french: String? = "",
        var italian: String? = "",
        var german: String? = ""
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)
        FirebaseApp.initializeApp(this@ResultImageActivity)
        fileUri = intent?.data
        path = intent.getStringExtra("path")
        database = FirebaseDatabase.getInstance().reference
        imageButton = findViewById(R.id.resultImage_imageButton)
        imageButton!!.setImageURI(fileUri)
        bitmap= MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
        runDetector(bitmap!!)



        println("========")
        println(langText)

        iTTS= TextToSpeech(applicationContext,TextToSpeech.OnInitListener { status ->
            if(status!= TextToSpeech.ERROR){
                iTTS.language= Locale.ITALIAN
            }
        })
        gTTS= TextToSpeech(applicationContext,TextToSpeech.OnInitListener { status ->
            if(status!= TextToSpeech.ERROR){
                gTTS.language= Locale.GERMAN
            }
        })
        fTTS= TextToSpeech(applicationContext,TextToSpeech.OnInitListener { status ->
            if(status!= TextToSpeech.ERROR){
                fTTS.language= Locale.FRENCH
            }
        })

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

        imageButton!!.setOnClickListener {
            val now: String = System.currentTimeMillis().toString()
//            android.text.format.DateFormat.format("MM-dd_hh:mm", now)
            try{
                val storageDir:String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/WaKnowPic/result"
                val rootView:View = window.decorView.findViewById(R.id.resultImage_layout)
                getBitmapFromView(rootView, this) { bitmap ->
                    var dir:File = File(storageDir)
                    if(!dir.exists())   dir.mkdir()
                    var myfile:File = File(dir, now+".jpg")
                    var fout: OutputStream = FileOutputStream(myfile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fout)
                    Log.d("Y2K2", myfile.absolutePath)
                    fout.flush()
                    fout.close()
                    Toast.makeText(this, "Save "+myfile.absolutePath, Toast.LENGTH_SHORT).show()

                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

        fun langChanege(i: Int, langText: String): String? {
            if(langText=="english")
                return resultWord[i]?.english
            else if(langText=="spanish")
                return resultWord[i]?.spanish
            else if(langText=="german")
                return resultWord[i]?.german
            else if(langText=="italian")
                return resultWord[i]?.italian
            else
                return resultWord[i]?.french


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
            if(langText=="german") {
                if (resultWord[i]?.german == null) {
                    Toast.makeText(this, "단어를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
                } else if (langText == "german") {
                    Toast.makeText(this, resultWord[i]?.german, Toast.LENGTH_SHORT).show()
                    gTTS.speak(resultWord[i]?.german, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
            if(langText=="italian") {
                if (resultWord[i]?.italian == null) {
                    Toast.makeText(this, "단어를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
                } else if (langText == "italian") {
                    Toast.makeText(this, resultWord[i]?.italian, Toast.LENGTH_SHORT).show()
                    iTTS.speak(resultWord[i]?.italian, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
            if(langText=="french") {
                if (resultWord[i]?.french == null) {
                    Toast.makeText(this, "단어를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
                } else if (langText == "french") {
                    Toast.makeText(this, resultWord[i]?.french, Toast.LENGTH_SHORT).show()
                    fTTS.speak(resultWord[i]?.french, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1], locationOfViewInWindow[0] + view.width, locationOfViewInWindow[1] + view.height), bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    }
                    // possible to handle other result codes ...
                }, Handler())
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }

     fun runDetector (bitmap : Bitmap?){

        val image = FirebaseVisionImage.fromBitmap(bitmap!!)
        val options = FirebaseVisionCloudImageLabelerOptions.Builder().setConfidenceThreshold(0.7f)
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
    fun popupFoo(pop : Boolean){
        var popupBuilder = AlertDialog.Builder(this)
        var popupDialogView = layoutInflater.inflate(R.layout.popup, null)

        popupBuilder.setView(popupDialogView)

        if(pop) {
            val pt = popupBuilder.show()


            popupDialogView.popupBtn.setOnClickListener {
                mp.start()
                pt.dismiss()

                this.finish()
            }
        }



    }


    private fun checkDatabase(words: Array<String?>){
        val wordDataListener = object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var dbidx: Int = 0
                for(word in words){
                    if(word == null) {

                        break
                    }
                    var tmpword: ResultImageActivity.DBWord? = dataSnapshot.child(word).getValue(
                        ResultImageActivity.DBWord::class.java)
                    if(tmpword != null){
                        tmpword.english = word
                        resultWord[dbidx++] = tmpword
                    }
                    if(dbidx == 5)  break
                }
                changeTextView(resultWord)
                println(resultWord[0]?.english)
                if (resultWord[0]?.english==null)
                    popupFoo(true)
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@ResultImageActivity, "오류!", Toast.LENGTH_SHORT)
            }
        }
        database.addListenerForSingleValueEvent(wordDataListener)

    }
    private fun changeTextView(dbwords: Array<ResultImageActivity.DBWord?>) {
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
        if (langText == "german") {
            resultImage_textView1.text = resultWord[0]?.german ?: ""
            resultImage_textView2.text = resultWord[1]?.german ?: ""
            resultImage_textView3.text = resultWord[2]?.german ?: ""
            resultImage_textView4.text = resultWord[3]?.german ?: ""
            resultImage_textView5.text = resultWord[4]?.german ?: ""
        }
        if (langText == "italian") {
            resultImage_textView1.text = resultWord[0]?.italian ?: ""
            resultImage_textView2.text = resultWord[1]?.italian ?: ""
            resultImage_textView3.text = resultWord[2]?.italian ?: ""
            resultImage_textView4.text = resultWord[3]?.italian ?: ""
            resultImage_textView5.text = resultWord[4]?.italian ?: ""
        }
        if (langText == "french") {
            resultImage_textView1.text = resultWord[0]?.french ?: ""
            resultImage_textView2.text = resultWord[1]?.french ?: ""
            resultImage_textView3.text = resultWord[2]?.french ?: ""
            resultImage_textView4.text = resultWord[3]?.french ?: ""
            resultImage_textView5.text = resultWord[4]?.french ?: ""
        }
    }
}