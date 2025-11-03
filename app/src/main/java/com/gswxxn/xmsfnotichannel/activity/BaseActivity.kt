package com.gswxxn.xmsfnotichannel.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View

abstract class BaseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
        onCreate()
    }

    abstract fun onCreate()

    open fun showView(isShow: Boolean = true, vararg views: View?) {
        for (element in views) {
            element?.visibility = if (isShow) View.VISIBLE else View.GONE
        }
    }
}