package com.rizzle.sdk.faas.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rizzle.sdk.faas.demo.databinding.ActivityMainBinding
import com.rizzle.sdk.faas.demo.utils.click
import com.rizzle.sdk.faas.helpers.FeedSdkInitializer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var sharedUrlOfFaas: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleUIClick()
    }

    private fun handleUIClick() {
        binding.launchActivity.click {
            startActivity(Intent(this, ActivityIntegration::class.java))
        }

        binding.launchFragment.click {
            startActivity(Intent(this, SimpleFragmentLaunchActivity::class.java))
        }

        binding.launchNavigation.click {
            startActivity(Intent(this, BottomNavigation::class.java))
        }

        FeedSdkInitializer.setDomainUrl("https://rizzle.tv")

        val uri : Uri? = intent.data
        sharedUrlOfFaas = uri?.let {
            it.scheme in listOf("http", "https") && it.host == "rizzle.tv" && FeedSdkInitializer.isFaasUrI(it)
        } ?: false

        if(sharedUrlOfFaas){
            FeedSdkInitializer.initializeWithShareLink(this, uri.toString().split("/?")[1])
        }
    }
}