package com.zelspeno.deliverytesttask.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.source.Category

class CategoriesListRecyclerAdapter(private var categories: List<Category>):
    RecyclerView.Adapter<CategoriesListRecyclerAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(category: Category)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.homeRVName)
        val image: ImageView = itemView.findViewById(R.id.homeRVImage)
        lateinit var category: Category

        init {
            itemView.setOnClickListener {
                listener.onItemClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.home_recyclerview_item, parent, false
        )
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = categories[position].name
        Picasso.get().load(categories[position].imageUrl)
            .error(R.drawable.categories_example)
            .into(holder.image)

        holder.category = categories[position]
    }

    override fun getItemCount(): Int = categories.size

    fun updateList(list: List<Category>) {
        this.categories = list
        notifyDataSetChanged()
    }

    fun getList(): List<Category> = this.categories

}
