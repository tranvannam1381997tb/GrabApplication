package com.example.grabapplication.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabapplication.BR
import com.example.grabapplication.R
import com.example.grabapplication.databinding.ItemPlaceBinding
import com.example.grabapplication.googlemaps.models.PlaceModel

class ListPlaceAdapter : ListAdapter<PlaceModel, ListPlaceAdapter.ItemHolder>(Companion) {

    var callback: OnClickItemPlaceListener? = null

    inner class ItemHolder constructor(val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(placeModel: PlaceModel) {
            placeModel.let {
                binding.setVariable(BR.placeModel, it)
                binding.executePendingBindings()
            }

            val layoutPlace = itemView.findViewById<LinearLayout>(R.id.layoutPlace)

            layoutPlace.setOnClickListener {
                Log.d("NamTV", "onClick")
                callback?.clickItemPlace(placeModel)
            }
        }
    }

    companion object: DiffUtil.ItemCallback<PlaceModel>() {
        override fun areItemsTheSame(oldItem: PlaceModel, newItem: PlaceModel): Boolean {
            if (oldItem === newItem) {
                return true
            }
            return false
        }
        override fun areContentsTheSame(oldItem: PlaceModel, newItem: PlaceModel): Boolean {
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val dataBinding = ItemPlaceBinding.inflate(inflater, parent, false)
        return ItemHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentPlace = getItem(position)
        holder.bind(currentPlace)
    }


    interface OnClickItemPlaceListener {
        fun clickItemPlace(placeModel: PlaceModel)
    }
}

@BindingAdapter(value = ["setAdapter"])
fun RecyclerView.bindRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
    this.run {
        this.setHasFixedSize(true)
        this.adapter = adapter
    }
}