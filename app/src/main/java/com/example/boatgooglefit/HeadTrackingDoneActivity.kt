package com.example.boatgooglefit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.boatgooglefit.databinding.ActivityHeadTrackingDoneBinding

class HeadTrackingDoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeadTrackingDoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeadTrackingDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDone.setOnClickListener { finish() }

        binding.btnRestart.setOnClickListener {
            startActivity(Intent(this, CalibrationActivity::class.java))
            finish()
        }

    }
}