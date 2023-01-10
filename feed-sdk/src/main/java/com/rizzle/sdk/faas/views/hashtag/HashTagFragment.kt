package com.rizzle.sdk.faas.views.hashtag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faltenreich.skeletonlayout.Skeleton
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.FragmentHashTagBinding
import com.rizzle.sdk.faas.helpers.*
import com.rizzle.sdk.faas.models.PaginationError
import com.rizzle.sdk.faas.models.QueryAlreadyInProgress
import com.rizzle.sdk.faas.models.SecondaryFeedData
import com.rizzle.sdk.faas.models.SecondaryFeedLaunchSource
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.navigation.models.HashTagFragmentArgs
import com.rizzle.sdk.faas.shimmer.ShimmerEffect
import com.rizzle.sdk.faas.viewModels.HashTagViewModel
import com.rizzle.sdk.faas.views.baseViews.BaseFragment
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Fragment used for Hashtag screen.
 */
internal class HashTagFragment : BaseFragment<FragmentHashTagBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHashTagBinding = FragmentHashTagBinding::inflate

    private var skeleton: Skeleton? = null
    private val model: HashTagViewModel by viewModels()
    private val parent: NavigationFragment by lazy { parentFragment as NavigationFragment }
    private lateinit var postGridAdapter: PostGridAdapter
    private var postId: String? = null
    private var hashTag: String? = null
    private var hashTagId: String? = null

    companion object {
        fun newInstance(bundle: Bundle): HashTagFragment {
            val fragment = HashTagFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = HashTagFragmentArgs.fromBundle(requireArguments())
        postId = args.postId
        hashTag = args.hashtagName
        hashTagId = args.hashTagId
        postGridAdapter = PostGridAdapter(postId) { posts, position ->
            parent.model.navigateToSecondaryFeed(
                SecondaryFeedData(
                    posts.map { it.id },
                    posts[position].id,
                    SecondaryFeedLaunchSource.HASHTAG,
                    position,
                    model.hashtagPostsQueryState.endCursor
                )
            )
        }
        postGridAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun setup() {
        model.hashtagPostsQueryState.reset()
        binding.apply {
            postsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            postsRecyclerView.attachPagination { fetchPosts(true) }
            postsRecyclerView.adapter = postGridAdapter

            skeleton = ShimmerEffect.SkeletonBuilder(
                ShimmerEffect.Type.RECYCLERVIEW,
                postsRecyclerView,
                R.layout.skeleton_grid_layout,
                9
            ).build()

            hashTagName.text = hashTag?.hashify()

            headerRow.apply {
                genericHeaderCenteredLeftIcon.touch { requireActivity().onBackPressedDispatcher.onBackPressed() }
                genericHeaderCenteredRight.touch {
                    parent.model.openShareSheet(hashTag!!, 0, ShareObject.HASHTAG)
                }
            }

            errorLayout.errorActionBtn.click {
                fetchData()
            }
        }

        model.infoQueryInProgress.observe(viewLifecycleOwner) {
            showLoading(it, HashTagFragment::class.java.simpleName)
        }

        fetchData()
    }

    private fun fetchData() {
        fetchHashTagDetails()
    }

    private fun fetchHashTagDetails() {
        subscriptions += model
            .getHashTagInfo(hashTag!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.hashTagViewsCount.text = String.format(getString(R.string.text_total_views), it.viewCount?.toLong()?.formatCool())
                fetchPosts()
            }, {
                setState(ViewState.ERROR)
            })
    }

    private fun fetchPosts(pageCall: Boolean = false) {
        var needToAppend = pageCall

        // fetch the justWatchedPost in feed if this screen is launched from feed screen and pageCall is false.
        val justWatchedPostCompletable = postId?.let {
            if (pageCall.not()) {
                model.getPostsByIds(listOf(it))
                    .doOnSuccess { posts ->
                        postGridAdapter.setData(posts, needToAppend)
                        needToAppend = true
                    }.ignoreElement()
            } else Completable.complete()
        } ?: Completable.complete()

        justWatchedPostCompletable.andThen(model.getHashTagPosts(hashTag!!))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { if (needToAppend.not()) setState(ViewState.LOADING) }
            .subscribe({ list ->
                if (!pageCall) if (list.isEmpty()) setState(ViewState.EMPTY_LIST) else setState(
                    ViewState.LOADED
                )
                postGridAdapter.setData(list.filter { it.id != postId }, needToAppend)
                postGridAdapter.noMoreData = model.hashtagPostsQueryState.isLastCursor()
            }, {
                when (it) {
                    is QueryAlreadyInProgress, is PaginationError -> Unit
                    else -> setState(ViewState.ERROR)
                }
            })

    }

    private fun setState(state: ViewState) {
        when (state) {
            ViewState.LOADED -> {
                skeleton?.showOriginal()
                binding.apply {
                    errorLayout.root.hide()
                    postsRecyclerView.show()
                }
            }
            ViewState.ERROR -> {
                skeleton?.showOriginal()
                binding.errorLayout.root.show()
            }
            ViewState.EMPTY_LIST -> {
                skeleton?.showOriginal()
                binding.apply {
                    postsRecyclerView.gone()
                    noHashtagAvailable.show()
                    errorLayout.root.hide()
                }
            }
            ViewState.LOADING -> {
                skeleton?.showSkeleton()
                binding.errorLayout.root.hide()
            }
        }
    }
}