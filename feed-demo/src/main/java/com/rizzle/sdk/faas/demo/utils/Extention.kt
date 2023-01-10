package com.rizzle.sdk.faas.demo.utils

import android.view.View

fun View.click(punchTime: Long = 300L, lambdaAction: (view: View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        var startTime = 0L
        override fun onClick(view: View) {
            if (System.currentTimeMillis() - startTime < punchTime) return
            else lambdaAction(view)
            startTime = System.currentTimeMillis()
        }
    })
}