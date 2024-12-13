package com.il4mb.edudoexam.ui.dashboard.histories.teacher

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.databinding.ViewItemUserBinding
import com.il4mb.edudoexam.models.User

class UserSpinnerAdapter(
    private val context: Context,
    private val users: MutableList<User>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = users.size

    override fun getItem(position: Int): User = users[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ViewItemUserBinding
        val view: View

        // Reuse the convertView if possible
        if (convertView == null) {
            binding = ViewItemUserBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding // Store the binding in the view's tag for reuse
        } else {
            view = convertView
            binding = view.tag as ViewItemUserBinding
        }

        // Get the current user
        val user = users[position]

        // Set the user details to the view
        binding.apply {
            root.apply {
                background = ColorDrawable()
                strokeWidth = 0
            }
            userName.text = user.name
            userEmail.text = user.email
            if(user.photo != null) {
                Glide.with(context)
                    .load(user.photo)
                    .into(userPhoto)
            } else {
                userPhoto.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        if (user.gender == 1) R.drawable.man else R.drawable.woman
                    )
                )
            }
            actionButton.visibility = View.GONE
        }

        return view
    }
}
