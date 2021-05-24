package com.example.grabapplication.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabapplication.BR
import com.example.grabapplication.R
import com.example.grabapplication.databinding.ItemDriverBinding
import com.example.grabapplication.model.DriverInfo

class DriverSuggestAdapter : ListAdapter<DriverInfo, DriverSuggestAdapter.ItemHolder>(Companion) {

    var callback: OnClickItemDriverListener? = null

    inner class ItemHolder constructor(val binding: ItemDriverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(driverInfo: DriverInfo) {
            driverInfo.let {
                binding.setVariable(BR.driverInfo, it)
                binding.executePendingBindings()
            }

            val layoutDriver = itemView.findViewById<LinearLayout>(R.id.layoutDriver)

            layoutDriver.setOnClickListener {
                Log.d("NamTV", "onClick layout driver")
                callback?.clickItemDriver(driverInfo)
            }
        }
    }

    companion object: DiffUtil.ItemCallback<DriverInfo>() {
        override fun areItemsTheSame(oldItem: DriverInfo, newItem: DriverInfo): Boolean {
            if (oldItem === newItem) {
                return true
            }
            return false
        }
        override fun areContentsTheSame(oldItem: DriverInfo, newItem: DriverInfo): Boolean {
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val dataBinding = ItemDriverBinding.inflate(inflater, parent, false)
        return ItemHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentDriver = getItem(position)
        holder.bind(currentDriver)
    }


    interface OnClickItemDriverListener {
        fun clickItemDriver(driverInfo: DriverInfo)
    }
}

@BindingAdapter(value = ["setDriverSuggestAdapter"])
fun RecyclerView.bindDriverSuggestAdapter(adapter: RecyclerView.Adapter<*>) {
    this.run {
        this.setHasFixedSize(true)
        this.adapter = adapter
    }
}