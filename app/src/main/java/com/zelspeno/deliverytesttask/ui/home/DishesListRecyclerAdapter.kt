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
import com.zelspeno.deliverytesttask.source.Dish

class DishesListRecyclerAdapter(private var dishes: List<Dish>):
    RecyclerView.Adapter<DishesListRecyclerAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(dish: Dish)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.dishRVName)
        val image: ImageView = itemView.findViewById(R.id.dishRVImage)
        lateinit var dish: Dish

        init {
            itemView.setOnClickListener {
                listener.onItemClick(dish)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.dish_recyclerview_item, parent, false
        )
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = dishes[position].name
        if (dishes[position].imageUrl == "null" || dishes[position].imageUrl == null) {
            holder.image.setImageResource(R.drawable.empty_image)
        } else {
            Picasso.get().load(dishes[position].imageUrl).into(holder.image)
        }
        holder.dish = dishes[position]
    }

    override fun getItemCount(): Int = dishes.size

    fun updateList(list: List<Dish>) {
        this.dishes = list
        notifyDataSetChanged()
    }

    fun getList(): List<Dish> = this.dishes

}