package com.ikoro.android.wallet.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ikoro.android.IkoroApplication
import com.ikoro.android.wallet.WalletService

class WalletViewModelFactory(private val app: IkoroApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            return WalletViewModel(WalletService.getInstance(app)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
