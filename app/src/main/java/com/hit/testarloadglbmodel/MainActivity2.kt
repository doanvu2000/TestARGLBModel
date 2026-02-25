package com.hit.testarloadglbmodel

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hit.testarloadglbmodel.databinding.ActivityMain2Binding
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

class MainActivity2 : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity2"
    }

    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Đợi camera sẵn sàng trước khi load model
        binding.arSceneView.onSessionUpdated = { _, frame ->
            if (frame.camera.trackingState == com.google.ar.core.TrackingState.TRACKING) {
                logDebug("onCreate: Camera đã sẵn sàng, bắt đầu load model")
                binding.arSceneView.onSessionUpdated = null // Chỉ gọi 1 lần
                setupView()
            }
        }
    }

    private fun setupView() {
        logDebug("setupView: Bắt đầu setup AR Scene")
        val arSceneView = binding.arSceneView
        logDebug("setupView: ARSceneView đã được khởi tạo")

        ioScope {
            logDebug("setupView: Đang load model từ file GLB...")
            // 1. Tạo ModelInstance từ file GLB
            arSceneView.modelLoader.loadModelInstance("model/Pocoyo_all_new.glb")?.let { instance ->
                logDebug("setupView: Model đã load thành công!")
                mainScope {
                    logDebug("setupView: Đang tạo ModelNode...")
                    // 2. Khởi tạo ModelNode với instance vừa load
                    val modelNode = ModelNode(
                        modelInstance = instance, scaleToUnits = 0.5f // Chỉnh kích thước 50cm
                    ).apply {
                        logDebug("setupView: ModelNode đã tạo, đang set position và rotation...")
                        // 3. Đặt vị trí tương đối so với Camera (X, Y, Z)
                        // Z = -1.0f: Cách mặt 1 mét
                        // Y = -0.3f: Thấp hơn tầm mắt một chút
                        position = Position(x = 0f, y = -0.3f, z = -1.0f)
                        logDebug("setupView: Position set = $position")

                        // Đảm bảo model luôn nhìn về phía người dùng (nếu cần)
                        rotation = io.github.sceneview.math.Rotation(x = 0f, y = 180f, z = 0f)
                        logDebug("setupView: Rotation set = $rotation")
                    }

                    // 4. Gán làm con của CameraNode để "dính" theo màn hình
                    logDebug("setupView: Đang add ModelNode vào CameraNode...")
                    arSceneView.cameraNode.addChildNode(modelNode)
                    logDebug("setupView: ModelNode đã được add vào CameraNode")
                    logDebug("setupView: ModelNode isVisible = ${modelNode.isVisible}")
                    logDebug("setupView: ModelNode parent = ${modelNode.parent}")

                    // 5. Lưu biến modelNode này lại để dùng cho Animation
                    setupAnimationButtons(modelNode)
                    logDebug("setupView: Hoàn thành setup!")
                }
            } ?: run {
                logError("setupView: Load model THẤT BẠI!")
            }
        }
    }

    private fun setupAnimationButtons(node: ModelNode) {
        binding.btnIdle.setOnClickListener {
            logDebug("Chơi animation: Idle")
            node.playAnimation("Idle", loop = true)
        }

        binding.btnAttack.setOnClickListener {
            logDebug("Chơi animation: Attack")
            node.playAnimation("Attack", loop = false)
        }

        binding.btnRun.setOnClickListener {
            logDebug("Chơi animation: Run")
            node.playAnimation("Run", loop = true)
        }

        binding.btnDance.setOnClickListener {
            logDebug("Chơi animation: Dance")
            node.playAnimation("Dance", loop = true)
        }
    }
}