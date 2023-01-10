package com.rizzle.sdk.faas.feed

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.LayoutReportDialogBinding
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.navigation.models.AlertDialogArgs
import com.rizzle.sdk.faas.uistylers.UiConfig
import com.rizzle.sdk.faas.uistylers.setCardCornerRadius
import com.rizzle.sdk.network.models.requestmodels.ShareObject

class AlertDialog : DialogFragment() {

    private var _binding: LayoutReportDialogBinding? = null
    private val binding get() = _binding!!

    private val parent: NavigationFragment by lazy { parentFragment as NavigationFragment }

    private var shareObject: ShareObject? = null

    private var dialogTitle: String? = null

    private var dialogMessage: String? = null

    companion object{
        fun newInstance(bundle: Bundle): AlertDialog {
            val fragment = AlertDialog()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareObject = arguments?.let { AlertDialogArgs.fromBundle(it).shareObject }
        dialogTitle = arguments?.let { AlertDialogArgs.fromBundle(it).dialogTitle }
        dialogMessage = arguments?.let { AlertDialogArgs.fromBundle(it).dialogMessage }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LayoutReportDialogBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            dialogCardView.setCardCornerRadius(UiConfig.cardCornerRadius)
            dialogTvTitle.text = dialogTitle
            dialogTvMessage.text = dialogMessage
        }

        isCancelable = false

        Handler(Looper.getMainLooper()).postDelayed({
            if(shareObject == ShareObject.POST) parent.model.setReportPostFlag(true)
            dismiss()
            }, 3000
        )
    }
}