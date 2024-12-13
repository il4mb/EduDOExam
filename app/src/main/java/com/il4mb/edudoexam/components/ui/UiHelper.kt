package com.il4mb.edudoexam.components.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.asEstimateTime
import com.il4mb.edudoexam.components.Utils.Companion.asLocalDateTime
import com.il4mb.edudoexam.databinding.ViewItemExamBinding
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.models.User

object UiHelper {

    private fun loadDrawableWithGlide(context: Context, url: String, onDrawableReady: (Drawable?) -> Unit) {
        Glide.with(context)
            .load(url)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // This is where you get the Drawable after it has been loaded
                    onDrawableReady(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle cleanup when the resource is cleared (optional)
                    onDrawableReady(placeholder)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    // Handle the case when the image load fails
                    onDrawableReady(errorDrawable)
                }
            })
    }

    fun setupUserImage(context: Context, view: View, user: User) {
        val defaultPhoto = listOf(
            R.drawable.woman, // Default photo for female
            R.drawable.man    // Default photo for male
        )

        if (user.photo != null) {

            if(view is MenuLayout.MenuItem) {
                loadDrawableWithGlide(context, user.photo, view::setImageDrawable)
            } else {
                view.post {
                    Glide.with(context)
                        .load(user.photo)
                        .into(view as? ImageView ?: return@post)
                }
            }
        } else {
            when (view) {
                is ImageView -> {
                    view.setImageResource(defaultPhoto.getOrElse(user.gender) { R.drawable.baseline_person_24 })
                }
                is MenuLayout.MenuItem -> {
                    view.setImageDrawable(defaultPhoto.getOrElse(user.gender) { R.drawable.baseline_person_24 })
                }
                else -> {
                    throw IllegalArgumentException("Unsupported view type: ${view.javaClass}")
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    fun setupExamItemUI(binding: ViewItemExamBinding, exam: Exam) {
        val context = binding.root.context
        binding.apply {
            // Set up basic info
            codeTextView.text = exam.id
            titleView.text = exam.title
            subtitleView.text = exam.subTitle

            // Handle exam state
            when {
                exam.finishAt.time < System.currentTimeMillis() -> {
                    // Exam finished
                    setupDateTime(
                        text = context.getString(R.string.finished),
                        colorRes = R.color.gray,
                        fontRes = R.font.montserrat_bold
                    )
                    deckCard.visibility = View.GONE
                }
                exam.isAnswered and !exam.isOwner -> {
                    // Exam answered
                    setupDateTime(
                        text = context.getString(R.string.answered),
                        colorRes = R.color.primary_200,
                        fontRes = R.font.montserrat_bold
                    )
                    setupOngoingBadge(R.color.primary_200)
                    deckCard.visibility = View.GONE
                }
                exam.isOngoing -> {
                    // Exam ongoing
                    setupDateTime(
                        text = context.getString(R.string.ongoing),
                        colorRes = R.color.secondary,
                        fontRes = R.font.montserrat_bold
                    )
                    setupOngoingBadge(R.color.secondary)
                    deckCard.visibility = View.GONE
                    examCard.strokeColor = context.getColor(R.color.secondary)
                }
                else -> {
                    // Exam starting soon
                    setupDateTime(
                        text = context.getString(
                            R.string.starting_in,
                            exam.startAt.asLocalDateTime.asEstimateTime
                        ),
                        colorRes = R.color.secondary
                    )
                    deckCard.visibility = View.GONE
                }
            }

            // Handle owner details
            if (exam.owner != null) {
                setupUserImage(root.context, ownerPhoto, exam.owner)
                ownerName.text = exam.owner.name

                if (exam.isOwner) {
                    deckCard.visibility = View.VISIBLE
                    userStateLabel.text = context.getString(R.string.teacher)
                    setupUserStateLabel(R.color.primary_light, R.font.montserrat_bold)
                    ownerPhoto.apply {
                        strokeWidth = 6f
                        strokeColor = ColorStateList.valueOf(context.getColor(R.color.primary_light))
                    }
                } else {
                    userStateLabel.text = context.getString(R.string.student)
                    deckCard.visibility = View.GONE
                    setupUserStateLabel(R.color.secondary, R.font.montserrat_regular)
                }
            } else {
                ownerPhoto.visibility = View.GONE
                ownerName.visibility = View.GONE
                userStateLabel.visibility = View.GONE
            }
        }
    }

    // Helper function for setting up the dateTime TextView
    private fun ViewItemExamBinding.setupDateTime(text: String, colorRes: Int, fontRes: Int? = null) {
        dateTime.apply {
            this.text = text
            setTextColor(context.getColor(colorRes))
            fontRes?.let { typeface = resources.getFont(it) }
        }
    }

    // Helper function for setting up the ongoing badge
    private fun ViewItemExamBinding.setupOngoingBadge(colorRes: Int) {
        ongoingBadged.apply {
            visibility = View.VISIBLE
            imageTintList = ColorStateList.valueOf(context.getColor(colorRes))
        }
    }

    // Helper function for setting up the user state label
    private fun ViewItemExamBinding.setupUserStateLabel(colorRes: Int, fontRes: Int) {
        userStateLabel.apply {
            setTextColor(context.getColor(colorRes))
            typeface = resources.getFont(fontRes)
        }
    }

}