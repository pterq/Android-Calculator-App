package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Build
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        var btnSimpleCalculator = findViewById<Button>(R.id.btn_simple_calculator)
        btnSimpleCalculator.setOnClickListener{
            val intent = Intent(this, SimpleCalculator::class.java)
            startActivity(intent)
        }

        var btnAdvancedCalculator = findViewById<Button>(R.id.btn_advanced_calculator)
        btnAdvancedCalculator.setOnClickListener{
            val intent = Intent(this, AdvancedCalculator::class.java)
            startActivity(intent)
        }

        var btnAbout = findViewById<Button>(R.id.btn_about)
        btnAbout.setOnClickListener{
            val intent = Intent(this, AboutPage::class.java)
            startActivity(intent)
        }

        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }


        /*
        var btnTest = findViewById<Button>(R.id.btn_test)
        btnTest.setOnClickListener{
            val intent = Intent(this, test::class.java)
            startActivity(intent)
        }

        */
    }
}