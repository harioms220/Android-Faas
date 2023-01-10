package com.rizzle.sdk.faas.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rizzle.sdk.faas.databinding.ItemShareSheetBinding
import com.rizzle.sdk.faas.helpers.SystemUtils
import com.rizzle.sdk.faas.helpers.clearAndAddAll
import com.rizzle.sdk.faas.helpers.click
import com.rizzle.sdk.faas.sharesheet.ShareTypeDetails
import com.rizzle.sdk.faas.utils.InternalUtils.string

class ShareSheetSocialAdapter(private var listener: OnShareTypeClickListener) : RecyclerView.Adapter<ShareSheetSocialAdapter.ViewHolder>() {
    private val TAG = javaClass.simpleName
    inner class ViewHolder(var binding: ItemShareSheetBinding) : RecyclerView.ViewHolder(binding.root)

    private var shareItemsList = mutableListOf<ShareTypeDetails>()

    init {
        shareItemsList.clearAndAddAll(getInstalledAppsToShare())
    }

    private fun getInstalledAppsToShare(): List<ShareTypeDetails> {
        val installedPackages = SystemUtils.getAvailableInstalledShareableApps().map { it.packageName }
        val socialApps = ShareTypeDetails.getListOfSocialApps().filter { shareType -> shareType.packageName in installedPackages }
        return mutableListOf<ShareTypeDetails>().apply {
            addAll(socialApps)
            add(ShareTypeDetails.MESSAGE)
            add(ShareTypeDetails.MORE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemShareSheetBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            shareText.text = string(shareItemsList[position].displayName)
            shareIcon.setImageResource(shareItemsList[position].iconRes)
        }

        holder.binding.root.click {
            listener.onShareTypeClicked(shareItemsList[position])
        }
    }

    override fun getItemCount() = shareItemsList.size

    fun interface OnShareTypeClickListener{
        fun onShareTypeClicked(shareType: ShareTypeDetails)
    }
}