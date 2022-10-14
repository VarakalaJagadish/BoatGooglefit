package com.example.boatgooglefit

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.boatgooglefit.databinding.ActivityCalibrationBinding

class CalibrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalibrationBinding
    private var calibrateFlow = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalibrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCalibrateFlow()

        binding.backBtn.setOnClickListener { finish() }

        binding.btnCalibrate.setOnClickListener {
            setCalibrateFlow()
        }
        binding.btnSkip.setOnClickListener {
            setCalibrateFlow()
        }
    }

    private fun enableNextBtn(){
        val timer = object: CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                binding.btnCalibrate.isClickable = true
                binding.btnCalibrate.setBackgroundColor(resources.getColor(R.color.solid_color))
            }
        }
        timer.start()
    }

    private fun setCalibrateFlow(){
        when (calibrateFlow) {
            0 -> {
                binding.icCalibrationMode.setImageResource(R.drawable.ic_turn_left)
                binding.calibrationModeText.text = getString(R.string.label_turn_left)
            }
            1 -> {
                binding.icCalibrationMode.setImageResource(R.drawable.ic_turn_right)
                binding.calibrationModeText.text = getString(R.string.label_turn_right)
            }
            2 -> {
                binding.icCalibrationMode.setImageResource(R.drawable.ic_look_up)
                binding.calibrationModeText.text = getString(R.string.label_look_up)
            }
            3 -> {
                binding.icCalibrationMode.setImageResource(R.drawable.ic_look_down)
                binding.calibrationModeText.text = getString(R.string.label_look_down)
            }
            4 -> {
                binding.icCalibrationMode.setImageResource(R.drawable.ic_tilt_left)
                binding.calibrationModeText.text = getString(R.string.label_tilt_left)
            }
            5 -> {
                binding.icCalibrationMode.setImageResource(R.drawable.ic_tilt_right)
                binding.calibrationModeText.text = getString(R.string.label_tilt_right)
            }
            6 ->{
                startActivity(Intent(this, HeadTrackingLearnMore::class.java))
                finish()
            }
        }
        calibrateFlow++
        binding.btnCalibrate.setBackgroundColor(resources.getColor(R.color.calibrate_bg_color))
        binding.btnCalibrate.isClickable = false
        enableNextBtn()
    }
}