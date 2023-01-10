package com.rizzle.sdk.faas.feed

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.FragmentFeedBinding
import com.rizzle.sdk.faas.helpers.click
import com.rizzle.sdk.faas.helpers.hide
import com.rizzle.sdk.faas.helpers.show
import com.rizzle.sdk.faas.models.HashTag
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.navigation.models.FeedFragmentArgs
import com.rizzle.sdk.faas.viewModels.FeedViewModel
import com.rizzle.sdk.faas.views.baseViews.BaseFragment
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import timber.log.Timber


/**
 * Pre-Built Fragment which can be used in a container to load the Feed.
 * This fragment has been written keeping in mind that it will be used in
 * navigation architecture as well.
 */
class FeedFragment internal constructor() : BaseFragment<FragmentFeedBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFeedBinding = FragmentFeedBinding::inflate

    private val model: FeedViewModel by viewModels()

    private val parent: NavigationFragment by lazy { parentFragment as NavigationFragment }

    private var feedAdapter: FeedAdapter? = null

    private val feedListener = object : FeedListener {
        override fun onPostAppeared(post: Post) {

        }

        override fun onPaused(post: Post) {

        }

        override fun onResumed(post: Post) {

        }

        override fun onShareClicked(post: Post, shareObject: ShareObject, timeOfActionInMillis: Long) {
            parent.model.openShareSheet(post.id, timeOfActionInMillis, shareObject)
        }

        override fun onWatchNowClicked(post: Post) {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(post.cta?.deepLink)
            })
        }

        override fun onFeedBackPressed() {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        override fun onProfileClicked(post: Post) {
            /** For demonstration, hardcoding one of the activity in the demo application to be launched.
             *  In real scenario, expecting this name to be provided in the [Post] model itself
             */
            startActivity(Intent(requireContext(), Class.forName("com.rizzle.sdk.faas.demo.DeepLinkHandlingActivity")))
        }

        override fun onHashtagClicked(hashTag: HashTag, post: Post) {
            parent.model.navigateToHashTagScreen(hashTag.name!!, post.id, hashTag.id)
        }

        override fun onTrackClicked(trackId: String, post: Post) {
            parent.model.navigateToTrackScreen(trackId, post.id)
        }

    }


    companion object{
        fun newInstance(bundle: Bundle): FeedFragment {
            val fragment = FeedFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = FeedFragmentArgs.fromBundle(arguments)
        model.setSecondaryFeedData(args?.secondaryFeedData)
        model.setSharedLinkPostId(args?.sharedLinkPostId)
        Timber.tag(TAG).d("secondary feed data ${args?.secondaryFeedData}")

        feedAdapter = FeedAdapter(model, parent.feedOrientation, feedListener)
        feedAdapter?.itemHeight = parent.containerHeight
        feedAdapter?.itemWidth = parent.containerWidth
        feedAdapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }


    override fun setup() {
        binding.feedView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = this@FeedFragment.parent.feedOrientation
            }
            adapter = feedAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }
        feedAdapter?.let { viewLifecycleOwner.lifecycle.addObserver(it) }
        model.fetchFeed(false)

        model.isLoading.observe(viewLifecycleOwner) {
            showLoading(it, "FeedLoading")
            if (it.not()) binding.swipeToRefreshContainer.isRefreshing = it
        }

        model.postsData.observe(viewLifecycleOwner) { newFeed ->
            Timber.tag(TAG).d("New Feed: $newFeed")
            newFeed?.let {
                feedAdapter?.addMoreItems(it.posts, it.isRefresh, it.scrollToPosition)
            }
        }

        model.error.observe(viewLifecycleOwner) {
            if (it) {
                if (feedAdapter?.hasData()?.not() == true) {
                    binding.errorLayout.errorParent.show()
                    binding.errorLayout.errorActionBtn.click {
                        model.getPrimaryFeed()
                        binding.errorLayout.errorParent.hide()
                    }
                    binding.swipeToRefreshContainer.isRefreshing = false
                } else {
                    parent.model.openAlertDialog(
                        title = String.format(getString(R.string.network_error)),
                        message = String.format(getString(R.string.network_error_message))
                    )
                }
            } else {
                binding.errorLayout.errorParent.hide()
            }
        }

        parent.model.isPostReported.observe(viewLifecycleOwner){ reported ->
            reported?.let {
                if(it) feedAdapter?.removeCurrentPost()
                // setting it null immediately so that re-removal don't happen if any consumer in
                // child fragment observing this live data after above statement.
                parent.model.setReportPostFlag(null)
            }
        }

        binding.swipeToRefreshContainer.isEnabled = model.isSecondaryFeed().not() == true

        binding.swipeToRefreshContainer.setOnRefreshListener {
            //model.resetPrimaryQuery()
            feedAdapter?.markCurrentPostViewed()
            model.fetchFeed(false)
        }
    }

    override fun onDestroyView() {
        model.cancelFetchFeed()
        feedAdapter?.let { viewLifecycleOwner.lifecycle.removeObserver(it) }
        super.onDestroyView()
    }

    override fun onVisibilityChanged(visibility: Int) {
        feedAdapter?.notifyVisibility(visibility)
    }
}
