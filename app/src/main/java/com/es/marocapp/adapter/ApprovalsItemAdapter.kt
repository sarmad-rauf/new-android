package com.es.marocapp.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.es.marocapp.R
import com.es.marocapp.model.responses.Approvaldetail

class ApprovalsItemAdapter(private val approvalsItems: ArrayList<Approvaldetail>,
                           var listner: ApprovalItemClickListner) : RecyclerView.Adapter<ApprovalsItemAdapter.ApprovalsItemViewHolder>() {

    override fun getItemCount() = approvalsItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalsItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.approvals_row_layout, parent, false)
        return ApprovalsItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovalsItemViewHolder, position: Int) {
        holder.approvalItemName.text = approvalsItems[position].approvaltype
        holder.approvalItemType.text = approvalsItems[position].amount?.currency.plus(" ").plus(
            approvalsItems[position].amount?.amount)

        holder.mApprovalsItemLayout.setOnClickListener {
            listner.onApprovalItemTypeClick(position)
        }
    }

    class ApprovalsItemViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var approvalItemName : TextView = view.findViewById(R.id.approvalName)
        var approvalItemType : TextView = view.findViewById(R.id.approvalType)
        var mApprovalsItemLayout : ConstraintLayout = view.findViewById(R.id.containerLayout)
    }


    interface ApprovalItemClickListner{
        fun onApprovalItemTypeClick(position: Int)
    }
}