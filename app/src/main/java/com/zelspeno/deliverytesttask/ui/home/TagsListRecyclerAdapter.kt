package com.zelspeno.deliverytesttask.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.source.Category
import com.zelspeno.deliverytesttask.source.Tag
import kotlin.coroutines.coroutineContext

class TagsListRecyclerAdapter(private var tags: List<Tag>):
    RecyclerView.Adapter<TagsListRecyclerAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(tag: Tag)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.tagRVName)
        val container: ConstraintLayout = itemView.findViewById(R.id.tagRVContainer)
        lateinit var tag: Tag

        init {
            itemView.setOnClickListener {
                listener.onItemClick(tag)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.tag_recyclerview_item, parent, false
        )
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val context = holder.container.context
        if (tags[position].onClick) {
            holder.name.setTextColor(context.resources.getColor(R.color.white))
            holder.container.background = AppCompatResources.getDrawable(context, R.drawable.clicked_tag_style)
        } else {
            holder.name.setTextColor(context.resources.getColor(R.color.mainTextColor))
            holder.container.background = AppCompatResources.getDrawable(context, R.color.dishCardBackgroundColor)
        }
        holder.name.text = tags[position].name
        holder.tag = tags[position]
    }

    override fun getItemCount(): Int = tags.size

    fun updateList(list: List<Tag>) {
        this.tags = list
        notifyDataSetChanged()
    }

    fun getList(): List<Tag> = this.tags

}