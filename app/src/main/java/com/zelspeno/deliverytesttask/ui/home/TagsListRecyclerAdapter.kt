package com.zelspeno.deliverytesttask.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.source.Category

//class TagsListRecyclerAdapter(private var tags: List<String>):
//    RecyclerView.Adapter<TagsListRecyclerAdapter.MyViewHolder>() {
//
//    private lateinit var mListener: onItemClickListener
//
//    interface onItemClickListener {
//        fun onItemClick(tag: String)
//    }
//
//    fun setOnItemClickListener(listener: onItemClickListener) {
//        mListener = listener
//    }
//
//    class MyViewHolder(itemView: View, listener: onItemClickListener) :
//        RecyclerView.ViewHolder(itemView) {
//
//        val name: TextView = itemView.findViewById(R.id.homeRVName)
//        val image: ImageView = itemView.findViewById(R.id.homeRVImage)
//        lateinit var tag: String
//
//        init {
//            itemView.setOnClickListener {
//                listener.onItemClick(tag)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val itemView = LayoutInflater.from(parent.context).inflate(
//            R.layout.home_recyclerview_item, parent, false
//        )
//        return MyViewHolder(itemView, mListener)
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.name.text = tags[position].name
//        Picasso.get().load(tags[position].imageUrl)
//            .error(R.drawable.categories_example)
//            .into(holder.image)
//
//        holder.category = tags[position]
//    }
//
//    override fun getItemCount(): Int = tags.size
//
//    fun updateList(list: List<Category>) {
//        this.tags = list
//        notifyDataSetChanged()
//    }
//
//    fun getList(): List<Category> = this.tags
//
//}