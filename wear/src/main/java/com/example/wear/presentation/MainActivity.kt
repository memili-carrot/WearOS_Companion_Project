package com.example.wearossender

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private var nodeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스마트폰 노드 찾기
        findPhoneNode()

        // 센서 데이터 예제 전송
        sendSensorData("-0.21,6.13,7.67|-0.22,6.03,7.76|-0.28,5.9,7.83|-0.21,6.19,7.28|-0.13,5.92,7.78")
    }

    private fun findPhoneNode() {
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) {
                    nodeId = nodes.first().id
                    Log.d("WearOS", "⌚ 메시지 전송 대상 노드: $nodeId")
                } else {
                    Log.e("WearOS", "⚠️ 연결된 스마트폰 없음!")
                }
            }
    }

    private fun sendSensorData(sensorData: String) {
        nodeId?.let { targetNode ->
            val payload = sensorData.toByteArray()
            Wearable.getMessageClient(this).sendMessage(targetNode, "/sensor_data", payload)
                .addOnSuccessListener {
                    Log.d("WearOS", "✅ 메시지 전송 성공: $sensorData")
                }
                .addOnFailureListener { e ->
                    Log.e("WearOS", "⚠️ 메시지 전송 실패: ${e.message}")
                }
        } ?: Log.e("WearOS", "⚠️ 스마트폰 노드를 찾지 못함!")
    }
}
