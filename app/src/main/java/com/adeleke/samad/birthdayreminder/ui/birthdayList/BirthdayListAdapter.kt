package com.adeleke.samad.birthdayreminder.ui.birthdayList

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
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


class BirthdayListAdapter(activity: FragmentActivity) :
    RecyclerView.Adapter<BirthdayListAdapter.BirthdayViewHolder>(), Filterable {

    private var dataAll = mutableListOf<Birthday?>()

    var data = mutableListOf<Birthday?>()
        set(value) {
            field = value
            dataAll.addAll(value)
            notifyDataSetChanged()
        }

    var myActivity = activity


    inner class BirthdayViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.birthdayNameItem)
        private val dateTV: TextView = itemView.findViewById(R.id.birthdayDateItem)
        private val birthdayColorIV: ImageView = itemView.findViewById(R.id.birthdayColor)
        private val personIV: ShapeableImageView = itemView.findViewById(R.id.ivItemAvatar)

        init {
            itemView.setOnClickListener {
                val currentBirthdayId = data[adapterPosition]?.id
                navigateToDetail(currentBirthdayId!!)
            }
        }

        fun bind(birthday: Birthday) {
            nameTv.text = birthday.name
            dateTV.text = birthday.date().toFormattedString()
            birthday.monthColor()?.let { birthdayColorIV.setImageResource(it) }
            Glide
                .with(myActivity)
                .load(birthday.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_face)
                .into(personIV)
        }

        private fun navigateToDetail(birthdayId: String) {
            val intent = Intent(myActivity.applicationContext, BirthdayDetailActivity::class.java)
            intent.putExtra(BIRTHDAY_DETAIL_INTENT_KEY, birthdayId)
            myActivity.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_birthday, parent, false)
        return BirthdayViewHolder(view)
    }

    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {
        val birthdayItem = data[position]
        birthdayItem?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    private var mFilter = object: Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            val filteredList = mutableListOf<Birthday?>()

            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(dataAll)
            } else {
                for (birthday in dataAll) {
                    if (birthday?.name?.toLowerCase()!!.contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(birthday)
                    }
                }
            }
            val filteredResult = FilterResults()
            filteredResult.values = filteredList

            return filteredResult
        }

        override fun publishResults(charSequence: CharSequence?, filteredResult: FilterResults?) {
            data.clear()
            data.addAll(filteredResult?.values as Collection<Birthday?>)
            notifyDataSetChanged()
        }

    }


    companion object {
        const val TAG = "BirthdayListAdapter"
    }
}