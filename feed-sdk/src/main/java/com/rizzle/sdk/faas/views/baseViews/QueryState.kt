package com.rizzle.sdk.faas.views.baseViews

import com.rizzle.sdk.faas.helpers.Constants
import com.rizzle.sdk.faas.helpers.isNullString
import com.rizzle.sdk.faas.models.ErrorNoMoreData
import com.rizzle.sdk.faas.models.PaginationError
import com.rizzle.sdk.faas.models.QueryAlreadyInProgress
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

class QueryState {
    private val TAG = javaClass.simpleName
    var endCursor: String? = Constants.EMPTY_STRING
    var inProgress: Boolean = false

    /**
     * This function checks if
     * - Query is in progress
     * - EndCursor for the query is null
     *
     * Also attaches the inProgress state to provided Single
     * but the endCursor needs to be updated manually
     *
     * @throws QueryAlreadyInProgress when the attached Single's query is in progress
     * @throws ErrorNoMoreData when the endCursor for the query is updated to null
     *
     * */
    fun <T: Any> preCheck(proceed: () -> Single<T>): Single<T> {
        return when {
            inProgress -> {
                Timber.tag(TAG).d("Query already in progress: ignoring the request")
                Single.error(QueryAlreadyInProgress())
            }
            isLastCursor() -> {
                Timber.tag(TAG).d("Last cursor reached, no more data")
                Single.error(ErrorNoMoreData())
            }
            else -> proceed()
                .doOnSubscribe {
                    Timber.tag(TAG).d("started query execution")
                    inProgress = true
                }
                .onErrorResumeNext {
                    if (isPaginationCall()) Single.error<T>(PaginationError())
                    else Single.error(it)
                }
                .doFinally {
                    Timber.tag(TAG).d("Query executed successfully")
                    inProgress = false
                }
        }
    }

    fun reset() {
        endCursor = Constants.EMPTY_STRING
        inProgress = false
    }

    fun isLastCursor(): Boolean {
        return endCursor.isNullString()
    }

    private fun isPaginationCall(): Boolean {
        return (endCursor?.isNotEmpty() == true)
    }
}