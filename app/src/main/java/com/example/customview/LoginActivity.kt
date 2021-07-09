package com.example.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customview.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), PatternLockView.OnPatternListener {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.patternlockview.setOnPatternListener(this)
    }

    override fun onComplete(ids: ArrayList<Int>): Boolean {
        val mPassCode = getPassCode()
        return if(getPatternString(ids) == mPassCode) {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun getPassCode(): String? {
        val pref = getSharedPreferences("myPref", MODE_PRIVATE)
        return pref.getString("pass_code", null)
    }

    private fun getPatternString(ids: ArrayList<Int>): String {
        var result = ""
        for (id in ids) {
            result += id.toString()
        }
        return result
    }
}