package com.rizzle.sdk.faas.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.ReportBottomSheetLayoutBinding
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.navigation.models.ReportBottomSheetDialogArgs
import com.rizzle.sdk.faas.viewModels.BaseViewModel
import com.rizzle.sdk.network.models.ReportOptionsEnum
import com.rizzle.sdk.network.models.requestmodels.ShareObject

/**
 * Bottom Sheet Dialog used for Report Sheet.
 */
class ReportBottomSheet : BottomSheetDialogFragment() {

    private var _binding: ReportBottomSheetLayoutBinding? = null
    private val binding
        get() = _binding!!
    val model: BaseViewModel by viewModels()

    val host: NavigationFragment by lazy { parentFragment as NavigationFragment }

    var id: String? = null

    var shareObject: ShareObject = ShareObject.POST

    var reportListAdapter: ReportListAdapter? = null

    companion object{
        fun newInstance(bundle: Bundle): ReportBottomSheet {
            val fragment = ReportBottomSheet()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ReportBottomSheetLayoutBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetTransparent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = ReportBottomSheetDialogArgs.fromBundle(requireArguments())
        id = args.postId
        shareObject = args.shareObject

        reportListAdapter = ReportListAdapter {
            when (shareObject) {
                ShareObject.POST -> model.reportPost(id!!, it)
                ShareObject.TRACK -> model.reportTrack(id!!, it)
                ShareObject.HASHTAG -> model.reportHashtag(id!!, it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = ReportBottomSheetDialogArgs.fromBundle(requireArguments())
        id = args.postId

        binding.rvReportList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = reportListAdapter
        }

        reportListAdapter?.setData(
            when (shareObject) {
                ShareObject.POST -> ReportOptionsEnum.getReportPostList()
                ShareObject.TRACK -> ReportOptionsEnum.getReportTrackList()
                ShareObject.HASHTAG -> ReportOptionsEnum.getReportHashtagList()
            }
        )

        model.reported.observe(viewLifecycleOwner){
            if (it) host.model.openAlertDialog(
                shareObject,
                String.format(getString(R.string.report_title_dummy)),
                String.format(getString(R.string.report_description_dummy))
            )
            dismiss()
        }
    }

}