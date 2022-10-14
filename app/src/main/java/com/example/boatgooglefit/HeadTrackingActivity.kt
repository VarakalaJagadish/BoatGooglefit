package com.example.boatgooglefit

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.boatgooglefit.databinding.ActivityHeadTrackingBinding

class HeadTrackingActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHeadTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeadTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCalibrate.isClickable = false

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                binding.btnCalibrate.isClickable = true
                binding.faceIcon.setImageResource(R.drawable.image_face_color)
                binding.btnSkip.visibility = View.VISIBLE
                binding.btnCalibrate.setBackgroundColor(resources.getColor(R.color.solid_color))
            }
        }
        timer.start()

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, CalibrationActivity::class.java))
        }

        binding.btnCalibrate.setOnClickListener {
            startActivity(Intent(this, CalibrationActivity::class.java))
        }

        binding.backBtn.setOnClickListener { finish() }
    }
}