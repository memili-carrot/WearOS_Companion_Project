package com.example.wearosreceiver

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 메시지 리스너 등록
        Wearable.getMessageClient(this).addListener(this)

        // 연결된 워치 노드 확인
        checkConnectedNodes()
    }

    // 메시지 수신 처리
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sensor_data") {
            val receivedMessage = String(messageEvent.data)
            Log.d("WearOS", "📡 받은 데이터: $receivedMessage")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)
    }

    private fun checkConnectedNodes() {
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                for (node in nodes) {
                    Log.d("WearOS", "📱 연결된 워치 노드: ${node.id} (${node.displayName})")
                }
            }
            .addOnFailureListener { e ->
                Log.e("WearOS", "⚠️ 노드 조회 실패: ${e.message}")
            }
    }
}
