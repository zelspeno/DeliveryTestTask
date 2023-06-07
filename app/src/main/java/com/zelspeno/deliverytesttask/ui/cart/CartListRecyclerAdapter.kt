package com.zelspeno.deliverytesttask.ui.cart

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.source.CartDish
import com.zelspeno.deliverytesttask.source.Category

class CartListRecyclerAdapter(private var cartDishes: List<CartDish>):
    RecyclerView.Adapter<CartListRecyclerAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(cartDish: CartDish)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.cartRVName)
        val cost: TextView = itemView.findViewById(R.id.cartRVCost)
        val weight: TextView = itemView.findViewById(R.id.cartRVWeight)
        val minusButton: ImageView = itemView.findViewById(R.id.cartMinus)
        val plusButton: ImageView = itemView.findViewById(R.id.cartPlus)
        val counter: TextView = itemView.findViewById(R.id.cartCount)
        val image: ImageView = itemView.findViewById(R.id.cartRVImage)
        lateinit var cartDish: CartDish

        init {
            minusButton.setOnClickListener {
                cartDish.count = cartDish.count!! - 1
                counter.text = cartDish.count.toString()
                listener.onItemClick(cartDish)
            }
            plusButton.setOnClickListener {
                cartDish.count = cartDish.count!! + 1
                counter.text = cartDish.count.toString()
                listener.onItemClick(cartDish)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.cart_recyclerview_item, parent, false
        )
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.cartDish = cartDishes[position]
        holder.name.text = cartDishes[position].name
        holder.cost.text = "${cartDishes[position].price} ₽"
        holder.weight.text = "· ${cartDishes[position].weight}г"
        holder.counter.text = cartDishes[position].count.toString()
        Picasso.get().load(cartDishes[position].imageUrl)
            .error(R.drawable.categories_example)
            .into(holder.image)
    }

    override fun getItemCount(): Int = cartDishes.size

    fun updateList(list: List<CartDish>) {
        this.cartDishes = list
        notifyDataSetChanged()
    }

    fun getList(): List<CartDish> = this.cartDishes.toMutableList()

    fun updateSumPay(cartPay: Button) {
        var sum = 0
        for (i in this.cartDishes.indices) {
            sum += cartDishes[i].count!! * cartDishes[i].price!!.toInt()
        }
        cartPay.text = "Оплатить $sum ₽"
    }

}
