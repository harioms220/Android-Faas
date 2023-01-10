package com.rizzle.sdk.faas.navigation

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rizzle.sdk.faas.navigation.models.NavigationAction
import com.rizzle.sdk.faas.navigation.models.NavigationData
import com.rizzle.sdk.faas.models.SecondaryFeedData
import com.rizzle.sdk.network.models.requestmodels.ShareObject

class NavigationViewModel : ViewModel() {

    private var navigationLiveData: MutableLiveData<NavigationData> = MutableLiveData()

    val navigation: LiveData<NavigationData> get() = navigationLiveData

    val isPostReported: MutableLiveData<Boolean?> = MutableLiveData()

    fun setReportPostFlag(boolean: Boolean?) {
        isPostReported.postValue(boolean)
    }

    fun navigateToHashTagScreen(hashtagName: String, postId: String? = "", hashTagId: String? = "") {
        val data = Bundle().apply {
            postId?.let { putString(NavigationConstants.POST_ID, it) }
            putString(NavigationConstants.HASHTAG_NAME, hashtagName)
            hashTagId?.let {  putString(NavigationConstants.HASHTAG_ID,it) }
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_HASHTAG_SCREEN)
    }

    fun navigateToTrackScreen(trackId: String, postId: String? = "") {
        val data = Bundle().apply {
            postId?.let { putString(NavigationConstants.POST_ID, postId) }
            putString(NavigationConstants.TRACK_ID, trackId)
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_TRACK_SCREEN)
    }

    fun navigateToSecondaryFeed(secondaryFeedData: SecondaryFeedData?) {
        val data = Bundle().apply {
            putParcelable(NavigationConstants.SECONDARY_FEED_DATA, secondaryFeedData)
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_FEED_SCREEN)
    }

    fun navigateToSharedPostLink(sharedPostId: String){
        val data = Bundle().apply {
            putString(NavigationConstants.POST_ID, sharedPostId)
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_FEED_SCREEN)
    }

    fun openShareSheet(id: String, timeOfActionInMillis: Long, shareObject: ShareObject) {
        val data = Bundle().apply {
            putString(NavigationConstants.OBJECT_ID, id)
            putLong(NavigationConstants.TIME_OF_ACTION, timeOfActionInMillis)
            putSerializable(NavigationConstants.SHARE_OBJECT, shareObject)
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_SHARE_SHEET)
    }

    fun openReportBottomSheet(id: String, shareObject: ShareObject) {
        val data = Bundle().apply {
            putString(NavigationConstants.OBJECT_ID, id)
            putParcelable(NavigationConstants.SHARE_OBJECT, shareObject)
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_REPORT_BOTTOM_SHEET)
    }

    fun openAlertDialog(shareObject: ShareObject? = null, title: String, message: String) {
        val data = Bundle().apply {
            shareObject?.let { putParcelable(NavigationConstants.SHARE_OBJECT, it) }
            putString(NavigationConstants.DIALOG_TITLE, title)
            putString(NavigationConstants.DIALOG_MESSAGE, message)
        }
        navigationLiveData.value = NavigationData(data, NavigationAction.ACTION_REPORT_DIALOG)
    }
}