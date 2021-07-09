package com.example.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customview.databinding.ActivityCreatChangePatternBinding

class CreateChangePatternActivity : AppCompatActivity(), PatternLockView.OnPatternListener {
    private var isFirstTime = true
    private var mIdList: ArrayList<Int> = arrayListOf()
    private lateinit var binding: ActivityCreatChangePatternBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatChangePatternBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvHeader.text = "Draw your pattern"
        binding.patternlockview.setOnPatternListener(this)
    }

    private fun savePattern(data: String) {
        val pref = getSharedPreferences("myPref", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("pass_code", data)
        editor.apply()
    }

    private fun getPatternString(ids: ArrayList<Int>): String {
        var result = ""
        for (id in ids) {
            result += id.toString()
        }
        return result
    }

    override fun onComplete(ids: ArrayList<Int>): Boolean {
        if (isFirstTime) {
            return if (ids.size >= 4) {
                mIdList = ids
                isFirstTime = false
                binding.tvHeader.text = "Confirm your pattern"
                true
            } else {
                false
            }
        } else {
            return if (mIdList == ids) {
                val passCode = getPatternString(mIdList)
                savePattern(passCode)
                binding.tvHeader.text = "Done"
                Toast.makeText(this, "Save!", Toast.LENGTH_SHORT).show()
                isFirstTime = true
                true
            } else {
                Toast.makeText(this, "Not Match", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}