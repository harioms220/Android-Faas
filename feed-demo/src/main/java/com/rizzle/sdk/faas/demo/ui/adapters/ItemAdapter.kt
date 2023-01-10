package com.rizzle.sdk.faas.demo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rizzle.sdk.faas.demo.R
import com.rizzle.sdk.faas.demo.models.ItemsInfo

class ItemAdapter(private val mList: List<ItemsInfo>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.items_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]

        holder.apply {
            Glide.with(productImage.context)
                .load(item.productImageUrl)
                .into(productImage)

            productName.text = item.productName
            productPrice.text = item.productPrice
            productDescription.text = item.productDescription
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productLike: ImageView = itemView.findViewById(R.id.product_like)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val productDescription: TextView = itemView.findViewById(R.id.product_description)
    }
}