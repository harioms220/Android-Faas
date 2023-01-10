package com.rizzle.sdk.faas.viewModels

import com.rizzle.sdk.faas.repos.ShareSheetRepo
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.network.models.requestmodels.ShareType
import com.rizzle.sdk.network.models.requestmodels.ShareableLinkInput
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

class ShareViewModel : BaseViewModel() {
    private val TAG = javaClass.simpleName
    private val shareSheetRepo by lazy { ShareSheetRepo() }

    fun getShareableLink(objectId: String, objectType: ShareObject, timeOfActionInMillis: Int): Single<String> {
        return shareSheetRepo.getShareableLink(ShareableLinkInput(objectId, objectType, timeOfActionInMillis = timeOfActionInMillis))
    }

    fun logShareEvent(objectId: String, objectType: ShareObject, platform: ShareType) {
        shareSheetRepo.logShareEvent(objectId, objectType, platform)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(TAG).d("logShareEvent Success")
            },{
                Timber.tag(TAG).d("Error while logShareEvent with message ${it.message}")
            })
    }
}