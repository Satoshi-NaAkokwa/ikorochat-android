package com.bitchat.android.features.runtime

import android.util.Log

class FeatureRuntime {
    companion object {
        private const val TAG = "FeatureRuntime"
        
        sealed class Capability {
            object SendMessage : Capability()
            object ReadMessages : Capability()
            object ShowUI : Capability()
            object GetInput : Capability()
            object StoreData : Capability()
            object Broadcast : Capability()
            
            object FilesystemAccess : Capability()
            object KeysAPI : Capability()
            object WalletAPI : Capability()
            object CameraAccess : Capability()
            object MicrophoneAccess : Capability()
            object LocationAccess : Capability()
            object NetworkAccess : Capability()
        }
        
        val BLOCKED_CAPABILITIES = setOf(
            Capability.FilesystemAccess,
            Capability.KeysAPI,
            Capability.WalletAPI,
            Capability.CameraAccess,
            Capability.MicrophoneAccess,
            Capability.LocationAccess,
            Capability.NetworkAccess
        )
        
        fun isBlocked(capability: Capability): Boolean {
            return capability in BLOCKED_CAPABILITIES
        }
    }
    
    fun checkCapability(capability: Capability): Boolean {
        if (isBlocked(capability)) {
            Log.w(TAG, "Blocked capability: ${capability.javaClass.simpleName}")
            return false
        }
        return true
    }
}