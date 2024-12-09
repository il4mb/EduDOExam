package com.capstone.edudoexam.ui.dashboard.profile

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_HORIZONTAL
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.capstone.edudoexam.R
import com.google.android.material.card.MaterialCardView

class ProfileAppbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val profileCardView: MaterialCardView by lazy {
        MaterialCardView(context).apply {
            layoutParams = LayoutParams(100.dpToPx(context), 100.dpToPx(context)).apply {
                gravity = CENTER_HORIZONTAL
                bottomMargin = 8.dpToPx(context)
            }
            radius = 50.dpToPx(context).toFloat()

            addView(profileImageView)
        }
    }

    private val profileImageView: ImageView by lazy {
        ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setImageResource(R.drawable.baseline_image_24)
        }
    }

    private val userIdTextView: TextView by lazy {
        TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 25.dpToPx(context)
            }
            gravity = CENTER
        }
    }

    init {
        orientation = VERTICAL

        addView(profileCardView)
        addView(userIdTextView)
    }

    /**
     * Sets the user ID text.
     */
    fun setUserIdText(userId: String) {
        userIdTextView.text = userId.uppercase()
    }

    /**
     * Sets the profile image resource.
     */
    fun setProfileImageResource(imageRes: Int) {
        profileImageView.setImageResource(imageRes)
    }

    /**
     * Extension function to convert dp to px.
     */
    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}