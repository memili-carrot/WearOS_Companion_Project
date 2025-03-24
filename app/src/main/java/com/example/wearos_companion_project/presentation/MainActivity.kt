package com.example.wearos_companion_project.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.wearos_companion_project.presentation.theme.WearOS_Companion_ProjectTheme
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.charset.Charset
import java.util.LinkedList
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var selectedSensor: Sensor? = null
    private val sensorDataBuffer = LinkedList<FloatArray>()
    private val MAX_BUFFER_SIZE = 10
    private val MESSAGE_PATH = "/sensor_data"
    private val decimalFormat = DecimalFormat("#.##").apply { roundingMode = RoundingMode.DOWN }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setContent {
            WearApp(
                onSelectAccelerometer = { selectSensor(Sensor.TYPE_ACCELEROMETER) },
                onSelectGyroscope = { selectSensor(Sensor.TYPE_GYROSCOPE) },
                onSendData = { sendSensorData() }
            )
        }
    }

    private fun selectSensor(sensorType: Int) {
        selectedSensor?.let { sensorManager.unregisterListener(this, it) }
        selectedSensor = sensorManager.getDefaultSensor(sensorType)
        selectedSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (sensorDataBuffer.size >= MAX_BUFFER_SIZE) {
                sensorDataBuffer.poll()
            }
            sensorDataBuffer.add(it.values.clone())
        }
    }

    private fun sendSensorData() {
        if (sensorDataBuffer.size < 5) return // 데이터가 부족하면 전송하지 않음

        val recentData = sensorDataBuffer.takeLast(5)
            .map { it.joinToString(",") { value -> decimalFormat.format(value) } }
            .joinToString("|")

        lifecycleScope.launch {
            val nodes = Wearable.getNodeClient(applicationContext).connectedNodes.await()
            for (node in nodes) {
                Wearable.getMessageClient(applicationContext)
                    .sendMessage(node.id, MESSAGE_PATH, recentData.toByteArray(Charset.defaultCharset()))
                    .await()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun WearApp(
    onSelectAccelerometer: () -> Unit,
    onSelectGyroscope: () -> Unit,
    onSendData: () -> Unit
) {
    WearOS_Companion_ProjectTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = onSelectAccelerometer) {
                Text("Select Accelerometer")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSelectGyroscope) {
                Text("Select Gyroscope")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSendData) {
                Text("Send Data")
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    WearApp(
        onSelectAccelerometer = {},
        onSelectGyroscope = {},
        onSendData = {}
    )
}