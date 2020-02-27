/**
 * A class that is meant to make the Intel RealSense available for the rest of the code
 */
package com.example.realsensecamera

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.calibration.ColorDepthCalibration
import com.segway.robot.sdk.vision.stream.StreamType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class LoomoRealSense {
    companion object {
        const val TAG = "LoomoRealSense"

        const val COLOR_WIDTH = 640
        const val COLOR_HEIGHT = 480

        const val FISHEYE_WIDTH = 640
        const val FISHEYE_HEIGHT = 480

        const val DEPTH_WIDTH = 320
        const val DEPTH_HEIGHT = 240

        val mColorDepthCalibration = ColorDepthCalibration()
    }

    var mVision = Vision.getInstance()
    private var waitingForServiceToBind = false

    private var mImgColor = Bitmap.createBitmap(COLOR_WIDTH, COLOR_HEIGHT, Bitmap.Config.ARGB_8888)
    private var mImgFishEye = Bitmap.createBitmap(FISHEYE_WIDTH, FISHEYE_HEIGHT, Bitmap.Config.ALPHA_8)
    private var mImgDepth = Bitmap.createBitmap(DEPTH_WIDTH, DEPTH_HEIGHT, Bitmap.Config.RGB_565)


    fun bind(context: Context) {
        if (!mVision.isBind and !waitingForServiceToBind) {
            Log.d(TAG, "Started Vision.bindService")
            waitingForServiceToBind = true
            mVision.bindService(
                context.applicationContext,
                object : ServiceBinder.BindStateListener {
                    override fun onBind() {
                        Log.d(TAG, "Vision onBind")
                        waitingForServiceToBind = false
                    }

                    override fun onUnbind(reason: String?) {
                        Log.d(TAG, "Vision onUnbind")
                        stopActiveCameras()
                    }
                })
        } else {
            Log.d(
                TAG,
                "Vision.isBind = ${mVision.isBind}${if (waitingForServiceToBind) ", but binding is in progress" else ""}"
            )
        }
    }


    fun stopActiveCameras() {
        if (mVision.isBind) {
            mVision.stopListenFrame(StreamType.COLOR)
            mVision.stopListenFrame(StreamType.FISH_EYE)
            mVision.stopListenFrame(StreamType.DEPTH)
        }
    }

    fun startColorCamera(threadHandler: Handler, imgBuffer: MutableLiveData<Bitmap>) {
        GlobalScope.launch {
            if (mVision.isBind) {
                try {
                    mVision.startListenFrame(
                        StreamType.COLOR
                    ) { streamType, frame ->
                        mImgColor.copyPixelsFromBuffer(frame.byteBuffer)
                        threadHandler.post {
                            imgBuffer.value = mImgColor
                        }
                    }
                    Log.d(TAG, "Color cam started")
                } catch (e: IllegalArgumentException) {
                    Log.d(
                        TAG,
                        "Exception in Vision.startListenFrame: Probably already listening to COLOR(1): $e"
                    )
                }
            } else if (!mVision.isBind and waitingForServiceToBind) {
                Log.d(TAG, "Waiting for service to bind before starting color camera")
                while (!mVision.isBind) {
                }
                mVision.stopListenFrame(StreamType.COLOR)
                startColorCamera(threadHandler, imgBuffer) // This recursion is safe.
            } else {
                Log.d(TAG, "Color camera not started. Bind Vision service first")
            }
        }
    }

    fun startFishEyeCamera(threadHandler: Handler, imgBuffer: MutableLiveData<Bitmap>) {
        GlobalScope.launch {
            if (mVision.isBind) {
                try {
                    mVision.startListenFrame(
                        StreamType.FISH_EYE
                    ) { streamType, frame ->
                        mImgFishEye.copyPixelsFromBuffer(frame.byteBuffer)
                        threadHandler.post {
                            imgBuffer.value = mImgFishEye
                        }
                    }
                    Log.d(TAG, "Fish Eye cam started")
                } catch (e: IllegalArgumentException) {
                    Log.d(
                        TAG,
                        "Exception in Vision.startListenFrame: Probably already listening to FISH_EYE(256): $e"
                    )
                }
            } else if (!mVision.isBind and waitingForServiceToBind) {
                Log.d(TAG, "Waiting for service to bind before starting fish eye camera")
                while (!mVision.isBind) {
                }
                mVision.stopListenFrame(StreamType.FISH_EYE)
                startFishEyeCamera(threadHandler, imgBuffer) // This recursion is safe.
            } else {
                Log.d(TAG, "FishEye cam not started. Bind Vision service first")
            }
        }
    }

    fun startDepthCamera(threadHandler: Handler, imgBuffer: MutableLiveData<Bitmap>) {
        GlobalScope.launch {
            if (mVision.isBind) {
                try {
                    mVision.startListenFrame(
                        StreamType.DEPTH
                    ) { streamType, frame ->
                        mImgDepth.copyPixelsFromBuffer(frame.byteBuffer)
                        threadHandler.post {
                            imgBuffer.value = mImgDepth
                        }
                    }
                    Log.d(TAG, "Depth cam started")
                } catch (e: IllegalArgumentException) {
                    Log.d(
                        TAG,
                        "Exception in Vision.startListenFrame: Probably already listening to DEPTH(2): $e"
                    )
                }
            } else if (!mVision.isBind and waitingForServiceToBind) {
                Log.d(TAG, "Waiting for service to bind before starting depth camera")
                while (!mVision.isBind) {
                }
                mVision.stopListenFrame(StreamType.DEPTH)
                startDepthCamera(threadHandler,imgBuffer) // This recursion is safe
            } else {
                Log.d(TAG, "Depth cam not started. Bind Vision service first")
            }
        }
    }
}