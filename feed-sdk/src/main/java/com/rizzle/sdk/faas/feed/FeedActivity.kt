package com.rizzle.sdk.faas.feed

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.rizzle.sdk.faas.databinding.ActivityFeedBinding
import com.rizzle.sdk.faas.helpers.FeedSdkInitializer
import com.rizzle.sdk.faas.helpers.ShareLinkGenerator

class FeedActivity : AppCompatActivity() {

    private var _binding: ActivityFeedBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFeedBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val sharedData = intent?.getStringExtra(FeedSdkInitializer.SHARE_LINK_DATA)
        sharedData?.let {
            binding.feedContainer.sharedData = ShareLinkGenerator.getIdAndType(it)
        }
    }
}