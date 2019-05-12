package com.example.a20gosu_proj

import android.content.Intent
import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.AppIntroFragment

class IntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(AppIntroFragment.newInstance("WaKnowPic!!", "사진을 찍어 외국어를 공부합니다!", R.drawable.intro, Color.parseColor("#F291A3")))
        addSlide(AppIntroFragment.newInstance("사진을 찍어요!", "카메라로 사진을 찍어요!", R.drawable.camera, Color.parseColor("#6BBF90")))
        addSlide(AppIntroFragment.newInstance("앨범에서 골라요!", "앨범에서 사진을 골라요!", R.drawable.gallery, Color.parseColor("#80A690")))
        addSlide(AppIntroFragment.newInstance("결과를 확인해요!", "영어, 스페인어 둘 중의 하나를 골라 확인합니다! 발음도 들을 수 있어요!", R.drawable.rotation_2, Color.parseColor("#F2C6AC")))
        setFadeAnimation();
        setDepthAnimation()
        setFadeAnimation();
        setZoomAnimation();
        setFlowAnimation();
        setSlideOverAnimation();
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Do something when users tap on Skip button.
        //Toast.makeText(this, "넘김!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Do something when users tap on Done button.
        //Toast.makeText(this, "설명 끝!!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        // Do something when the slide changes.

    }

}