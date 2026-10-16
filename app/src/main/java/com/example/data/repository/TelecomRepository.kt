package com.example.data.repository

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.example.data.model.SimAccountInfo

class TelecomRepository(private val context: Context) {
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    private val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(RoleManager::class.java)
    } else null

    fun isDefaultDialer(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            telecomManager?.defaultDialerPackage == context.packageName
        }
    }

    fun createDefaultDialerIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else {
            @Suppress("DEPRECATION")
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
        }
    }

    fun getAvailableSimAccounts(): List<SimAccountInfo> {
        val tm = telecomManager ?: return emptyList()
        val accounts = mutableListOf<SimAccountInfo>()

        try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
                val callCapableAccounts = tm.callCapablePhoneAccounts
                callCapableAccounts.forEachIndexed { index, handle ->
                    val phoneAccount = tm.getPhoneAccount(handle)
                    val label = phoneAccount?.label?.toString() ?: "SIM ${index + 1}"
                    val address = phoneAccount?.address?.schemeSpecificPart
                    accounts.add(
                        SimAccountInfo(
                            slotIndex = index,
                            displayName = label,
                            carrierName = label,
                            phoneNumber = address,
                            phoneAccountHandle = handle
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted yet
        } catch (e: Exception) {
            // Handle edge case
        }
        return accounts
    }

    fun placeCall(number: String, accountHandle: PhoneAccountHandle? = null): Boolean {
        val tm = telecomManager ?: return false
        val uri = Uri.fromParts("tel", number.trim(), null)
        val hasCallPhone = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val extras = Bundle().apply {
            accountHandle?.let {
                putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
            }
        }

        return if (hasCallPhone) {
            try {
                tm.placeCall(uri, extras)
                true
            } catch (e: Exception) {
                // Fallback to dial intent
                launchDialIntent(uri)
                false
            }
        } else {
            launchDialIntent(uri)
            false
        }
    }

    private fun launchDialIntent(uri: Uri) {
        val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)
    }
}
