package com.example.realsensecamera

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

        btnCaptureFrame.setOnClickListener {
            Log.d(TAG, "Img capture")
            saveBMP(colorImgBuffer.value, "colorImg")
            saveBMP(fishEyeImgBuffer.value, "fishEyeImg")
            saveBMP(depthImgBuffer.value, "depthImg")
        }
    }

    override fun onResume() {
        mRealSense.bind(this)
        mRealSense.startColorCamera(thisThreadHandler, colorImgBuffer)
        mRealSense.startFishEyeCamera(thisThreadHandler, fishEyeImgBuffer)
        mRealSense.startDepthCamera(thisThreadHandler, depthImgBuffer)
        super.onResume()
    }

    private fun saveBMP(bitmap: Bitmap?, name: String) {
        if (bitmap == null) {
            return
        }

        GlobalScope.launch {
            var instanceCounter = 1

            val bmp = if (bitmap.config == Bitmap.Config.ALPHA_8) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap.copy(bitmap.config, false)
            }
    //        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888,false)

            try {
                var fileName = "$name.png"

                val fOut: FileOutputStream?
                val dir = application.getExternalFilesDir("Images")
                var file = File(dir, fileName)
                while (!file.createNewFile()) {
                    ++instanceCounter
                    fileName = "${name}_${String.format("%03d", instanceCounter)}.png"
                    file = File(dir, fileName)
                }
                fOut = FileOutputStream(file)

                bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun makeAlphaCompressible(bitmap: Bitmap): Bitmap {
        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        val width = bmp.width
        val height = bmp.height

        val alphaArray = IntArray(width * height)
//        bmp.getPixels(alphaArray, 0, width, 0, 0, width, height)


//        val greyScaleArray = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
//                greyScaleArray[y * width + x] = (alphaArray[y * width + x] shl 24) and 0xff000000.toInt()
                alphaArray[y * width + x] = bmp.getPixel(x, y)
            }
        }
//        Log.d("pxl", bmp.getPixel(320, 240).toString())
//        return Bitmap.createBitmap(greyScaleArray, 0, width, width, height, Bitmap.Config.ARGB_8888)
        return Bitmap.createBitmap(alphaArray, 0, width, width, height, Bitmap.Config.ARGB_8888)
    }

//    private fun alpha2Grey(bitmap: Bitmap): Bitmap {
//        val bmp = bitmap.copy(bitmap.config, false).extractAlpha()
//
//    }
}
