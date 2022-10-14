package com.example.boatgooglefit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.boatgooglefit.adapter.ViewPagerAdapter
import com.example.boatgooglefit.databinding.ActivityHeadTrackingIntroSlidesBinding

class HeadTrackingIntroActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHeadTrackingIntroSlidesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeadTrackingIntroSlidesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager2.adapter = ViewPagerAdapter(supportFragmentManager)
        binding.viewPager2.setSwipePagingEnabled(false)

        checkViewPager()

        binding.btnNext.setOnClickListener {
            if(binding.viewPager2.currentItem == 2){
                startActivity(Intent(this, HeadTrackingActivity::class.java))
                finish()
            }
            binding.viewPager2.arrowScroll(View.FOCUS_RIGHT)
            checkViewPager()

        }

        binding.btnBack.setOnClickListener {
            finish()
        }

    }


    private fun checkViewPager() {
        if (binding.viewPager2.currentItem == 0) {
            binding.circleIndicator.visibility = View.GONE
            binding.btnNext.text = getString(R.string.btn_lets_go)
            binding.viewpaggertitle.text = getString(R.string.label_3d_music)
        } else {
            binding.viewpaggertitle.text = getString(R.string.label_Controls)
            binding.btnNext.text = getString(R.string.btn_next)
            binding.circleIndicator.visibility = View.VISIBLE
            binding.circleIndicator.setDotSelection(binding.viewPager2.currentItem)
        }
    }


}