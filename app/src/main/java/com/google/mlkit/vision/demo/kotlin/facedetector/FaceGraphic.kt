/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.kotlin.facedetector

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.demo.GraphicOverlay
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic
import com.google.mlkit.vision.demo.R
import com.google.mlkit.vision.face.Face
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */
class FaceGraphic constructor(overlay: GraphicOverlay?, context: Context, private val face: Face?) : Graphic(overlay) {
  private val faceOverlayPaint: Paint
  private val facePositionPaint: Paint
  private val textPaint: Paint
  private var hatGraphic: Drawable?

  init {

    val resources = context.resources
    hatGraphic = resources.getDrawable(R.drawable.red_hat)

    textPaint = Paint()
    textPaint.color = Color.WHITE
    textPaint.textSize = 80f
    textPaint.setShadowLayer(60.0f, 0f, 0f, Color.BLACK)

    facePositionPaint = Paint()
    facePositionPaint.color = Color.argb(100, 255, 255, 255)
    facePositionPaint.style = Paint.Style.STROKE
    facePositionPaint.strokeWidth = 20f

    faceOverlayPaint = Paint()
    faceOverlayPaint.isAntiAlias = true;
    faceOverlayPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD);
    faceOverlayPaint.color = Color.argb(100, 255, 0, 0)
    faceOverlayPaint.style = Paint.Style.STROKE
    faceOverlayPaint.strokeWidth = 20f
  }

  /** Draws the face annotations for position on the supplied canvas. */
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun draw(canvas: Canvas) {

    face?.let {
      Log.d("CIRCLE", "trackingId: " + it.trackingId + " trackiedId: " + trackedId)
      // Draws a circle at the position of the detected face
      val x = translateX(face.boundingBox.centerX().toFloat())
      val y = translateY(face.boundingBox.centerY().toFloat())

      val left = x - scale(face.boundingBox.width() / 2.0f)
      val top = y - scale(face.boundingBox.height() / 2.0f)

      val scale = scale(face.boundingBox.width().toFloat()) / canvas.width.toFloat()

      val hatHeight = hatGraphic?.intrinsicHeight!! * scale

      val transformation = Matrix()
      transformation.preTranslate(left, top - hatHeight/2)
      transformation.preScale(2 * scale, 2 * scale)

      val faceCircleRadius = face.boundingBox.width().toFloat() * 2.0
      val fixedCircleRadius = canvas.width.toFloat()/2.3
      val distSq = sqrt(((canvas.width / 2.0 - x).pow(2.0)) + ((canvas.height / 2.0 - y).pow(2.0)))

      if (it.trackingId != null && trackedId == it.trackingId) {
        canvas.drawBitmap((hatGraphic as BitmapDrawable).bitmap, transformation, null)

        val text = "Perfect! Rotate your"
        val text2 = "phone for some fun"
        val textWidth1 = textPaint.measureText(text)
        val textWidth2 = textPaint.measureText(text2)
        canvas.drawText(text, abs(canvas.width - textWidth1)/2, 100.0f, textPaint)
        canvas.drawText(text2, abs(canvas.width - textWidth2)/2, 180.0f, textPaint)
      }
      else if (abs(distSq) < 15.0 && abs(faceCircleRadius - fixedCircleRadius) < 15.0) {
        trackedId = it.trackingId
        canvas.drawBitmap((hatGraphic as BitmapDrawable).bitmap, transformation, null)

        val text = "Perfect! Rotate your"
        val text2 = "phone for some fun"
        val textWidth1 = textPaint.measureText(text)
        val textWidth2 = textPaint.measureText(text2)
        canvas.drawText(text, abs(canvas.width - textWidth1)/2, 100.0f, textPaint)
        canvas.drawText(text2, abs(canvas.width - textWidth2)/2, 180.0f, textPaint)
      }
      else if (it.trackingId == null || it.trackingId != trackedId) {
        val text = "How perfectly can you"
        val text2 = "align the circles"
        val textWidth1 = textPaint.measureText(text)
        val textWidth2 = textPaint.measureText(text2)
        canvas.drawText(text, abs(canvas.width - textWidth1)/2, 100.0f, textPaint)
        canvas.drawText(text2, abs(canvas.width - textWidth2)/2, 180.0f, textPaint)

        canvas.drawCircle(canvas.width/2f, canvas.height/2f, canvas.width.toFloat()/2.3f, faceOverlayPaint)
        canvas.drawCircle(x, y, face.boundingBox.width().toFloat()*2, facePositionPaint)
      }
    } ?: run {
      trackedId = null
      val text = "Point the camera"
      val text2 = "at a friend"
      val textWidth1 = textPaint.measureText(text)
      val textWidth2 = textPaint.measureText(text2)
      canvas.drawText(text, abs(canvas.width - textWidth1)/2, 100.0f, textPaint)
      canvas.drawText(text2, abs(canvas.width - textWidth2)/2, 180.0f, textPaint)
    }
  }

  companion object {
    private var trackedId: Int? = null
  }
}
