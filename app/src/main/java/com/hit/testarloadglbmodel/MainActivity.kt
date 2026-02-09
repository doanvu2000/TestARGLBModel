package com.hit.testarloadglbmodel

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Config
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    companion object {
        private const val DEFAULT_MODEL_FILE = "model/Pocoyo_all_new.glb"
        private const val MODEL_SCALE = 0.2f // Kích thước 20cm
        private const val MODEL_DISTANCE = 1.0f // Khoảng cách 1m từ camera
        private const val DEFAULT_ANIMATION = "Idle"
    }

    lateinit var sceneView: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView

    var modelNode: ModelNode? = null

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
            instructionText.isGone = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instructionText = findViewById(R.id.instructionText)
        loadingView = findViewById(R.id.loadingView)
        sceneView = findViewById<ARSceneView>(R.id.sceneView).apply {
            lifecycle = this@MainActivity.lifecycle
            planeRenderer.isEnabled = false
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
        }

        loadModel()
    }

    private fun loadModel() {
        lifecycleScope.launch {
            isLoading = true
            instructionText.text = "Đang tải model..."

            sceneView.modelLoader.loadModelInstance(DEFAULT_MODEL_FILE)?.let { modelInstance ->
                modelNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = MODEL_SCALE,
                    centerOrigin = Position(x = 0f, y = 0f, z = 0f)
                ).apply {
                    // Đặt model ở giữa màn hình, trước camera 1m
                    position = Position(x = 0f, y = 0f, z = -MODEL_DISTANCE)
                    isEditable = false

                    // Dừng tất cả animation
                    val count = animationCount
                    for (i in 0 until count) {
                        stopAnimation(i)
                    }

                    // Play animation Idle
                    playAnimation(DEFAULT_ANIMATION, loop = true)
                }

                // Xóa child nodes cũ và gắn model vào camera
                sceneView.cameraNode.clearChildNodes()
                sceneView.cameraNode.addChildNode(modelNode!!)
                instructionText.text = ""
            } ?: run {
                instructionText.text = "Lỗi tải model"
            }

            isLoading = false
        }
    }

}




