package com.rizzle.sdk.faas.feed

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rizzle.sdk.faas.databinding.LayoutLoadingBinding

class LoadingFragment : DialogFragment() {
    private var _binding: LayoutLoadingBinding? = null
    private val binding
        get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LayoutLoadingBinding.inflate(inflater, container, false).also { _binding = it }.root
    }
}