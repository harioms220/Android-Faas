package com.rizzle.sdk.faas.views.hashtag

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.rizzle.sdk.faas.databinding.ItemLoadingBinding
import com.rizzle.sdk.faas.databinding.ItemVerticalPostBinding
import com.rizzle.sdk.faas.helpers.clearAndAddAll
import com.rizzle.sdk.faas.helpers.click
import com.rizzle.sdk.faas.helpers.formatCool
import com.rizzle.sdk.faas.helpers.showIf
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.views.baseViews.BaseAdapter

class PostGridAdapter(private val postId: String?, private var postClickedListener: OnPostClickedListener) :
    BaseAdapter<PostGridAdapter.ViewHolder>() {
    private val posts: MutableList<Post> = mutableListOf()

    companion object {
        const val TYPE_POST = 0
    }

    inner class ViewHolder(var binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_POST -> getPostsHolder(parent)
            TYPE_LAST_ITEM -> ViewHolder(
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> getPostsHolder(parent)
        }
    }

    private fun getPostsHolder(parent: ViewGroup) = ViewHolder(
        ItemVerticalPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_POST -> {
                (holder.binding as ItemVerticalPostBinding).apply {
                    val post = posts[position]
                    Glide.with(this.root.context)
                        .load(posts[position].video?.webpUrl)
                        .into(verticalPostImageView)
                    details.viewsCount.text = post.viewCount?.formatCool()
                    verticalPostJustWatchedLayout.root.showIf(post.id == postId)
                    root.click {
                        postClickedListener.onPostClicked(posts, position)
                    }
                }
            }
            TYPE_LAST_ITEM -> {
                (holder.binding as ItemLoadingBinding).apply {
                    progressBar.showIf(!noMoreData)
                }
            }
        }
    }

    override fun getItemCount() = if (posts.isEmpty()) 0 else posts.size + 1

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> TYPE_LAST_ITEM
            else -> TYPE_POST
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(postsList: List<Post>, append: Boolean) {
        if (append) {
            noMoreData = false
            posts.addAll(postsList)
            notifyItemRangeInserted(posts.size, postsList.size)
        } else {
            posts.clearAndAddAll(postsList)
            notifyDataSetChanged()
        }
    }

    fun interface OnPostClickedListener{
        fun onPostClicked(postsList: List<Post>, position: Int)
    }
}