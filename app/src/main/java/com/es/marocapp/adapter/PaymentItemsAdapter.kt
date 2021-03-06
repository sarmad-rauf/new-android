package com.es.marocapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.es.marocapp.R

class PaymentItemsAdapter(private val paymentItems : ArrayList<String>,
                          private val paymentItemsIcons : ArrayList<Int>,
                          var listner : PaymentItemTypeClickListner) : RecyclerView.Adapter<PaymentItemsAdapter.PaymentItemViewHolder>() {

    override fun getItemCount() = paymentItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.payment_items_row_layout, parent, false)
        return PaymentItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentItemViewHolder, position: Int) {
        holder.paymentItem.text = paymentItems[position]
        if(paymentItemsIcons.isEmpty()){
            holder.paymentItemIcon.setBackgroundResource(R.drawable.others)
        }else{
            holder.paymentItemIcon.setBackgroundResource(paymentItemsIcons[position])
        }

        holder.mPaymentItemLayout.setOnClickListener {
            listner.onPaymentItemTypeClick(paymentItems[position])
        }
    }

    class PaymentItemViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var paymentItem : TextView = view.findViewById(R.id.payment_type_name)
        var paymentItemIcon : ImageView = view.findViewById(R.id.img_Info)
        var mPaymentItemLayout : ConstraintLayout = view.findViewById(R.id.containerLayout)
    }


    interface PaymentItemTypeClickListner{
        fun onPaymentItemTypeClick(paymentItems: String)
    }
}

