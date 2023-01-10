package com.rizzle.sdk.faas.feed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.ShareSheetLayoutBinding
import com.rizzle.sdk.faas.helpers.Constants
import com.rizzle.sdk.faas.helpers.RizzleLogging
import com.rizzle.sdk.faas.helpers.touch
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.navigation.models.ShareSheetDialogArgs
import com.rizzle.sdk.faas.sharesheet.ShareTypeDetails
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.faas.viewModels.ShareViewModel
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * Bottom Sheet Dialog used for Share sheet.
 */
class ShareSheetDialog : BottomSheetDialogFragment() {
    var id: String? = null
    var playerTimeInMillis = 0L
    var shareObject: ShareObject = ShareObject.POST
    var shareableLink: String = Constants.EMPTY_STRING
    private var TAG = javaClass.simpleName

    private val parent: NavigationFragment by lazy { parentFragment as NavigationFragment }
    private var _binding: ShareSheetLayoutBinding? = null
    private val shareViewModel: ShareViewModel by viewModels()

    private val binding
        get() = _binding!!

    companion object{
        fun newInstance(bundle: Bundle): ShareSheetDialog {
            val fragment = ShareSheetDialog()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetTransparent
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = ShareSheetDialogArgs.fromBundle(requireArguments())
        playerTimeInMillis = args.timeOfActionInMillis
        id = args.id
        shareObject = args.shareObject
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ShareSheetLayoutBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.tag(TAG).d("postId: $id shareObject: $shareObject playerTimeInMillis: $playerTimeInMillis")
        getShareLink()
        binding.shareSheetSocialRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = ShareSheetSocialAdapter { shareTypeDetails ->
                handleShareableLink(shareTypeDetails)
            }
        }
        binding.apply {
            tvShareTitle.text = when (shareObject) {
                ShareObject.POST -> getString(R.string.text_share_video)
                ShareObject.HASHTAG -> getString(R.string.text_share_hashtag)
                ShareObject.TRACK -> getString(R.string.text_share_track)
            }
            ivCopyLink.touch {
                Toast.makeText(context, getString(R.string.text_link_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                handleShareableLink(ShareTypeDetails.COPY_LINK)
            }
            ivShareLink.touch {
                handleShareableLink(ShareTypeDetails.SHARE_LINK)
            }

            ivReport.touch {
                dismiss()
                parent.model.openReportBottomSheet(id!!, shareObject)
            }
        }
    }

    private fun getShareLink() {
        // TODO() inform the backend of player current duration (playerTimeInMillis) to create data for engagement.
        shareViewModel.getShareableLink(id!!, shareObject, timeOfActionInMillis = playerTimeInMillis.toInt())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                shareableLink = it
            }, {
                RizzleLogging.logError(it)
            })
    }

    private fun handleShareableLink(type: ShareTypeDetails) {
        if(shareableLink.isEmpty()) return
        dismiss()
        id?.let { shareViewModel.logShareEvent(it, shareObject, type.shareType) }
        when (type) {
            ShareTypeDetails.MORE -> shareDeepLink(shareableLink)
            ShareTypeDetails.MESSAGE -> {
                val defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(requireContext())
                shareDeepLinkToPackage(shareableLink, defaultSmsPackageName)
            }

            ShareTypeDetails.COPY_LINK -> {
                copyTextToClipboard(shareableLink)
            }

            ShareTypeDetails.SHARE_LINK -> {
                shareDeepLink(shareableLink)
            }

            else -> {
                shareDeepLinkToPackage(shareableLink, type.packageName)
            }
        }
    }


    /**
     * Share text with specific plateform
     * @param packageName name of the app package for which text will share
     */
    private fun shareDeepLinkToPackage(text: String, packageName: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.setPackage(packageName)
        startActivity(intent)
    }


    private fun shareDeepLink(shareLink: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareLink)
        val chooserIntent = Intent.createChooser(intent, "Share with")
        startActivity(chooserIntent)
    }

    private fun copyTextToClipboard(text: String) {
        val clipboard = InternalUtils.application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("URL", text)
        clipboard.setPrimaryClip(clip)
    }
}
