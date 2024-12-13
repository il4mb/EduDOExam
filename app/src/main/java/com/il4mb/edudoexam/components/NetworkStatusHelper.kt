package com.il4mb.edudoexam.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkStatusHelper(context: Context, private val onNetworkStatusChanged: (Boolean) -> Unit) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                onNetworkStatusChanged(true)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            onNetworkStatusChanged(false)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, capabilities)
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                onNetworkStatusChanged(true)
            } else {
                onNetworkStatusChanged(false)
            }
        }
    }

    fun startListening() {
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}