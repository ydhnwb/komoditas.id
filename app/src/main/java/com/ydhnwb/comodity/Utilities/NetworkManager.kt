package com.ydhnwb.comodity.Utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkManager {

    companion object {
        private const val NETWORK_IS_CONNECTED = 10
        private const val NETWORK_IS_MOBILE = 11
        private const val NETWORK_IS_WIFI = 12

        fun isConnected(c : Context) : Boolean {
            val myNetworkStatus = getConnectivityStatus(c)
            return myNetworkStatus == NETWORK_IS_MOBILE || myNetworkStatus == NETWORK_IS_WIFI
        }

        private fun getConnectivityStatus(c : Context) : Int{
            val cm = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val ni = cm.activeNetworkInfo
            if(ni != null) {
                if(ni.type == ConnectivityManager.TYPE_WIFI && ni.state ==  NetworkInfo.State.CONNECTED){
                    return NETWORK_IS_WIFI
                }else if (ni.type == ConnectivityManager.TYPE_MOBILE && ni.state ==  NetworkInfo.State.CONNECTED){
                    return NETWORK_IS_MOBILE
                }
            }
            return NETWORK_IS_CONNECTED
        }
    }

}