package com.example.boatgooglefit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.boatgooglefit.databinding.ActivityHeadTrackLearMoreBinding

class HeadTrackingLearnMore : AppCompatActivity()  {

    private lateinit var binding : ActivityHeadTrackLearMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeadTrackLearMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, HeadTrackingDoneActivity::class.java))
            finish()
        }

        binding.btnLearnMore.setOnClickListener {
            startActivity(Intent(this, HeadTrackingDoneActivity::class.java))
            finish()
        }

    }
}