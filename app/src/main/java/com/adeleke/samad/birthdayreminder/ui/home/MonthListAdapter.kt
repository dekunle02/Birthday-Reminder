package com.adeleke.samad.birthdayreminder.ui.home

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.adeleke.samad.birthdayreminder.BIRTHDAY_DETAIL_INTENT_KEY
import com.adeleke.samad.birthdayreminder.R
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.adeleke.samad.birthdayreminder.toFormattedString
import com.adeleke.samad.birthdayreminder.ui.birthdayDetail.BirthdayDetailActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import kotlin.math.log

class MonthListAdapter(activity: FragmentActivity) :
    RecyclerView.Adapter<MonthListAdapter.BirthdayViewHolder>() {

    var data = listOf<Birthday?>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var myActivity = activity


    inner class BirthdayViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tvMonthBirthdayName)
        private val dayTV: TextView = itemView.findViewById(R.id.tvMonthDay)
        private val colorIv: ImageView = itemView.findViewById(R.id.ivMonthBirthdayColor)

        init {
            itemView.setOnClickListener {
                val currentBirthdayId = data[adapterPosition]?.id
                navigateToDetail(currentBirthdayId!!)
            }
        }

        fun bind(birthday: Birthday) {
            Log.d(TAG, "bind: Bind Called")
            nameTv.text = birthday.name
            dayTV.text = birthday.dayOfBirth.toString()
            birthday.monthColor()?.let { colorIv.setImageResource(it) }
        }

        private fun navigateToDetail(birthdayId: String) {
            val intent = Intent(myActivity.applicationContext, BirthdayDetailActivity::class.java)
            intent.putExtra(BIRTHDAY_DETAIL_INTENT_KEY, birthdayId)
            myActivity.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_month_birthday, parent, false)
        return BirthdayViewHolder(view)
    }

    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {
        val birthdayItem = data[position]
        birthdayItem?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    companion object {
        const val TAG = "RecentListAdapter"
    }


}