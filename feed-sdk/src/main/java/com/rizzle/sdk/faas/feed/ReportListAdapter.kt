package com.rizzle.sdk.faas.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rizzle.sdk.faas.databinding.ReportBottomSheetRowBinding
import com.rizzle.sdk.faas.helpers.clearAndAddAll
import com.rizzle.sdk.network.models.ReportOptionsEnum

class ReportListAdapter(private var listener: OnReportClickedListener) : RecyclerView.Adapter<ReportListAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: ReportBottomSheetRowBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ReportBottomSheetRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    private var reportOptionsList = mutableListOf<ReportOptionsEnum>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.actionTextView.text = reportOptionsList[position].reportValue
        holder.itemView.setOnClickListener {
            listener.onReportClick(reportOptionsList[position])
        }
    }

    override fun getItemCount() = reportOptionsList.size


    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<ReportOptionsEnum>){
        this.reportOptionsList.clearAndAddAll(list)
        notifyDataSetChanged()
    }

    fun interface OnReportClickedListener{

        fun onReportClick(reportOptionsEnum: ReportOptionsEnum)
    }
}