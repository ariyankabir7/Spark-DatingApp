package com.ariyan.spark.utils

import androidx.fragment.app.FragmentManager

object LoadingUtils {

    private var loadingDialog: LoadingDialogFragment? = null

    fun show(fragmentManager: FragmentManager, tag: String = LoadingDialogFragment.TAG) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
        }
        if (loadingDialog?.isAdded == false && loadingDialog?.isStateSaved == false) {
             // Check if the fragment manager already has a dialog with this tag
            val existingDialog = fragmentManager.findFragmentByTag(tag) as? LoadingDialogFragment
            if (existingDialog == null) {
                loadingDialog?.show(fragmentManager, tag)
            } else {
                // If a dialog with the same tag exists but our reference is null or different,
                // update our reference. This can happen on configuration changes.
                loadingDialog = existingDialog
            }
        }
    }

    fun hide() {
        if (loadingDialog?.isAdded == true) {
            loadingDialog?.dismissAllowingStateLoss()
        }
        loadingDialog = null // Clear the reference
    }
}