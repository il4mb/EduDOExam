package com.il4mb.edudoexam.comp

import android.view.View

object Animations {

    fun View.fadeIn(duration: Long = 300L, endAction: () -> Unit = {}) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction(endAction)
            .start()
    }

    fun View.fadeOut(duration: Long = 300L, endAction: () -> Unit = {}) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction(endAction)
            .start()
    }

    fun View.floatingIn(duration: Long = 300L, endAction: () -> Unit = {}) {
        translationY += 20f
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .withEndAction(endAction)
            .start()
    }

    fun View.floatingOut(duration: Long = 300L, endAction: () -> Unit = {}) {
        animate()
            .translationY(translationY + 20f)
            .alpha(0f)
            .setDuration(duration)
            .withEndAction(endAction)
            .start()
    }
}