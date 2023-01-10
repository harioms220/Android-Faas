package com.rizzle.sdk.faas.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rizzle.sdk.faas.demo.ui.planeFragment.BlankFragment

class SimpleFragmentLaunchActivity : AppCompatActivity() {
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_fragment_launch)

        launchFragment()
    }

    private fun launchFragment() {
        currentFragment = BlankFragment.newInstance("FaaS", "Integration")
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment, currentFragment!!, "FaaS_TAG")
            .commit()
    }
}