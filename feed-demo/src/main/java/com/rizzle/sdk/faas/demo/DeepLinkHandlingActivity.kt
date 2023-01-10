package com.rizzle.sdk.faas.demo

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rizzle.sdk.faas.demo.databinding.ActivityDeepLinkHandlingBinding
import com.rizzle.sdk.faas.helpers.FeedSdkInitializer

class DeepLinkHandlingActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private var _binding: ActivityDeepLinkHandlingBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDeepLinkHandlingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FeedSdkInitializer.setDomainUrl("https://rizzle.tv")

        val uri : Uri? = intent.data
        val sharedUrlOfFaas = uri?.let {
            it.scheme in listOf("http", "https") && it.host == "rizzle.tv" && FeedSdkInitializer.isFaasUrI(it)
        } ?: false

        if(sharedUrlOfFaas){
            FeedSdkInitializer.initializeWithShareLink(this, uri.toString().split("/?")[1])
        }
    }
}