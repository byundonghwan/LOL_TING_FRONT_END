package com.example.video_chat

class JavascriptInterface(val callActivity: CallActivity) {
    @android.webkit.JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}