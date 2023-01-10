package com.rizzle.sdk.faas.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.FragmentNavigationBinding
import com.rizzle.sdk.faas.feed.*
import com.rizzle.sdk.faas.helpers.RizzleLogging
import com.rizzle.sdk.faas.navigation.models.NavigationAction
import com.rizzle.sdk.faas.navigation.models.NavigationData
import com.rizzle.sdk.faas.uistylers.UiConfig
import com.rizzle.sdk.faas.views.baseViews.BaseFragment
import com.rizzle.sdk.faas.views.hashtag.HashTagFragment
import com.rizzle.sdk.faas.views.track.TrackFragment
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import timber.log.Timber

class NavigationFragment : Fragment() {

    private var TAG = javaClass.simpleName

    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!
    val model: NavigationViewModel by viewModels()

    var containerWidth = 0
    var containerHeight = 0
    var feedOrientation = FeedContainer.VERTICAL
    var sharedData: Pair<ShareObject, String>? = null

    private val loadingDialog by lazy { LoadingFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return FragmentNavigationBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.navigation.observe(viewLifecycleOwner) {
            handleNavigation(it)
        }
        UiConfig.isParsed.observe(viewLifecycleOwner){
            if(it) {
                showLoading(false, javaClass.name)
                renderFirstScreen()
            }
            else showLoading(true, javaClass.name)
        }
    }

    private fun renderFirstScreen() {
        sharedData?.let {
            when(it.first){
                ShareObject.POST -> { model.navigateToSharedPostLink(it.second) }
                ShareObject.TRACK -> { model.navigateToTrackScreen(it.second) }
                ShareObject.HASHTAG -> { model.navigateToHashTagScreen(it.second) }
            }
        } ?: addFragment(FeedFragment())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Remove all fragments from the childFragmentManager
                if (childFragmentManager.backStackEntryCount > 1) {
                    childFragmentManager.popBackStack()
                    return
                }

                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    fun showLoading(show: Boolean, tag: String) {
        if (show) loadingDialog.show(childFragmentManager, tag)
        else {
            if(loadingDialog.isVisible) {
                loadingDialog.dismissNow()
            }
        }
    }

    private fun handleNavigation(navigationData: NavigationData) {
        navigationData.run {
            when (action) {
                NavigationAction.ACTION_FEED_SCREEN -> addFragment(FeedFragment.newInstance(data))
                NavigationAction.ACTION_HASHTAG_SCREEN -> {
                    addFragment(HashTagFragment.newInstance(data))
                }
                NavigationAction.ACTION_TRACK_SCREEN -> addFragment(TrackFragment.newInstance(data))
                NavigationAction.ACTION_SHARE_SHEET -> showShareSheet(data)
                NavigationAction.ACTION_REPORT_BOTTOM_SHEET -> showReportBottomSheet(data)
                NavigationAction.ACTION_REPORT_DIALOG -> showAlertDialog(data)
            }
        }
    }

    private fun showShareSheet(bundle: Bundle) {
        ShareSheetDialog.newInstance(bundle).show(childFragmentManager, ShareSheetDialog::class.java.name)
    }

    private fun showReportBottomSheet(bundle: Bundle){
        ReportBottomSheet.newInstance(bundle).show(childFragmentManager, ReportBottomSheet::class.java.name)
    }

    private fun showAlertDialog(bundle: Bundle){
        AlertDialog.newInstance(bundle).show(childFragmentManager, AlertDialog::class.java.name)
    }


    private fun addFragment(targetScreen: Fragment, addToBackStack: Boolean = true, replace: Boolean = false) {
        val transaction = childFragmentManager
            .beginTransaction()
        if (replace) transaction.replace(binding.fragContainer.id, targetScreen)
        else {
            val currentTopFragment = childFragmentManager.fragments.lastOrNull()
            Timber.tag(TAG).d("current fragment:$currentTopFragment adding fragment: $targetScreen all fragments:${childFragmentManager.fragments}")
            transaction.add(binding.fragContainer.id, targetScreen)
            currentTopFragment?.let {
                Timber.tag(TAG).d("setting max lifecycle")
                transaction.setMaxLifecycle(it, Lifecycle.State.STARTED)
            }
        }
        if (addToBackStack) transaction.addToBackStack(targetScreen::class.java.name)
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        transaction.commitAllowingStateLoss()
    }

    /** Since we are dealing with the fragments inside some custom view, visibility change callback is
     * manually triggered, handle player related thing (if required) like on parent visibility changed
     * to gone, player can release resource.
     */
    fun notifyVisibilityChanged(visibility: Int) {
        try{
            val currentTopFragment = childFragmentManager.fragments.lastOrNull()
            if(currentTopFragment is BaseFragment<*>){
                currentTopFragment.onVisibilityChanged(visibility)
            }
        }catch (ex: Exception){
            RizzleLogging.logError(ex)
        }
    }
}