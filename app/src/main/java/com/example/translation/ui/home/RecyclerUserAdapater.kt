package com.example.translation.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.translation.R

class RecyclerUserAdapater(private val items: ArrayList<ListData>) : RecyclerView.Adapter<RecyclerUserAdapater.ViewHolder>() {
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerUserAdapater.ViewHolder, position: Int) {
        val item = items[position]
        val listener = View.OnClickListener { it ->

        }
        holder.apply {
            bind(listener,item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerUserAdapater.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        return RecyclerUserAdapater.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v){
        private var view : View = v
        fun bind(listener: View.OnClickListener, item: ListData){
            view.setOnClickListener(listener)
        }
    }
}