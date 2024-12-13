package com.il4mb.edudoexam.ui.dashboard.profile

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_HORIZONTAL
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM
import android.widget.RelativeLayout.ALIGN_PARENT_END
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

class ProfileAppbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val editButton: ImageView by lazy {
        ImageView(context).apply {
            val icon = ContextCompat.getDrawable(context, R.drawable.baseline_edit_square_24)
            DrawableCompat.setTint(icon!!, ContextCompat.getColor(context, R.color.primary_light))
            setImageDrawable(icon)
        }
    }

    private val photoWrapperLayout: RelativeLayout by lazy {
        RelativeLayout(context).apply {
            layoutParams = LayoutParams(100.dpToPx(context), 100.dpToPx(context)).apply {
                gravity = CENTER
            }
            addView(profileCardView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            addView(editButton, RelativeLayout.LayoutParams(20.dp, 20.dp).apply {
                addRule(ALIGN_PARENT_END)
                addRule(ALIGN_PARENT_BOTTOM)
                marginEnd = 8.dp
                bottomMargin = 8.dp
            })
        }
    }

    private val profileCardView: MaterialCardView by lazy {
        MaterialCardView(context).apply {
            layoutParams = LayoutParams(100.dpToPx(context), 100.dpToPx(context)).apply {
                gravity = CENTER_HORIZONTAL
                bottomMargin = 8.dpToPx(context)
            }
            radius = 50.dpToPx(context).toFloat()

            addView(profilePhotoImageView)
        }
    }

    private val profilePhotoImageView: ShapeableImageView by lazy {
        ShapeableImageView(context).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(50f)
                .build()

            setImageResource(R.drawable.baseline_person_24)
        }
    }

    private val userIdTextView: TextView by lazy {
        TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 25.dpToPx(context)
                topMargin = 25.dpToPx(context)
            }
            gravity = CENTER
        }
    }

    init {
        orientation = VERTICAL

        addView(photoWrapperLayout)
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
        profilePhotoImageView.setImageResource(imageRes)
    }

    fun setProfileImageUri(imageUri: Uri?) {
        profilePhotoImageView.setImageURI(imageUri)
    }

    fun setProfileImageUri(imageUrl: String) {
        Glide.with(context)
            .load(imageUrl)
            .into(profilePhotoImageView)
    }

    /**
     * Extension function to convert dp to px.
     */
    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}