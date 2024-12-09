package com.capstone.edudoexam.components.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.google.android.material.card.MaterialCardView

class QuestionInfoWindow private constructor(layoutInflater: LayoutInflater) {

    internal val context = layoutInflater.context
    private var _root: MaterialCardView = MaterialCardView(layoutInflater.context).apply {
    }
    val root = _root

    private var _linearLayout: LinearLayout = LinearLayout(layoutInflater.context) .apply {
        orientation = LinearLayout.VERTICAL
        setPadding(12.dp)
    }
    private val linearLayout = _linearLayout

    private var _title: TextView = TextView(layoutInflater.context).apply {
        textSize = 18f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    var title : String
        get() = _title.text.toString()
        set(value) {
            _title.text = value
        }

    private var _message: TextView = TextView(layoutInflater.context)
    var message : CharSequence
        get() = _message.text
        set(value) {
            _message.text = value
        }

    fun addView(view: View) {
        linearLayout.addView(view)
    }

    init {
        _root.addView(linearLayout)
        linearLayout.addView(_title)
        linearLayout.addView(_message)
        _root.apply {
            scaleX = .8f
            scaleY = .8f
        }
    }

    companion object {
        fun inflate(layoutInflater: LayoutInflater): QuestionInfoWindow {
            return QuestionInfoWindow(layoutInflater)
        }
    }

    private val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()
}
