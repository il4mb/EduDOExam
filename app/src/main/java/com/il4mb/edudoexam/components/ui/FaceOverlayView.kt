package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FaceOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val faceBounds: MutableList<Pair<Rect, String>> = mutableListOf()
    private val paint = Paint().apply {
        color = 0xFFFF0000.toInt() // Red
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private val textPaint = Paint().apply {
        color = 0xFFFF0000.toInt() // Red
        textSize = 40f
        style = Paint.Style.FILL
    }

    // Update faces with bounding boxes and corresponding labels
    fun updateFaces(faces: List<Pair<Rect, String>>) {
        faceBounds.clear()
        faceBounds.addAll(faces)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        faceBounds.forEach { (rect, label) ->
            // Draw rectangle
            canvas.drawRect(rect, paint)

            // Draw label below the rectangle
            val textX = rect.left.toFloat()
            val textY = rect.bottom + 40f // Add some padding below the rectangle
            canvas.drawText(label, textX, textY, textPaint)
        }
    }
}
