package com.example.realsensecamera

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val thisThreadHandler = Handler()

    private lateinit var mRealSense: LoomoRealSense

    var colorImgBuffer = MutableLiveData<Bitmap>()
    var fishEyeImgBuffer = MutableLiveData<Bitmap>()
    var depthImgBuffer = MutableLiveData<Bitmap>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        mRealSense = LoomoRealSense()

        colorImgBuffer.observe(this, Observer { imgViewColor.setImageBitmap(it) })
        fishEyeImgBuffer.observe(this, Observer { imgViewFishEye.setImageBitmap(it) })
        depthImgBuffer.observe(this, Observer { imgViewDepth.setImageBitmap(it) })
        imgViewColor.visibility = ImageView.GONE
        imgViewFishEye.visibility = ImageView.GONE
        imgViewDepth.visibility = ImageView.GONE

        // Onclicklisteners
        var camViewState = 0
        btnCycleCamView.setOnClickListener {
            Log.d(TAG, "CamCycleBtn clicked")
            ++camViewState
            when (camViewState) {
                1 -> {
                    imgViewColor.visibility = ImageView.GONE
                    imgViewFishEye.visibility = ImageView.GONE
                    imgViewDepth.visibility = ImageView.VISIBLE
                }
                2 -> {
                    imgViewColor.visibility = ImageView.VISIBLE
                    imgViewFishEye.visibility = ImageView.GONE
                    imgViewDepth.visibility = ImageView.GONE
                }
                else -> {
                    camViewState = 0
                    imgViewColor.visibility = ImageView.GONE
                    imgViewFishEye.visibility = ImageView.VISIBLE
                    imgViewDepth.visibility = ImageView.GONE
                }
            }
        }
    }

    override fun onResume() {
        mRealSense.bind(this)
        mRealSense.startColorCamera(thisThreadHandler, colorImgBuffer)
        mRealSense.startFishEyeCamera(thisThreadHandler, fishEyeImgBuffer)
        mRealSense.startDepthCamera(thisThreadHandler, depthImgBuffer)
        super.onResume()
    }


}
