package ai.onnxruntime.example.objectdetection

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.TensorInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

internal data class Result(
    var outputBitmap: Bitmap,
    var outputBox: Array<FloatArray>
) {}

internal class ObjectDetector(
) {

    fun detect(inputStream: InputStream, ortEnv: OrtEnvironment, ortSession: OrtSession, imgBitmap: Bitmap): Result {
        val rawImageBytes = inputStream.readBytes()
        val shape = longArrayOf(rawImageBytes.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            ByteBuffer.wrap(rawImageBytes).asReadOnlyBuffer(),
            shape,
            OnnxJavaType.UINT8
        )

        inputTensor.use {
            val output = ortSession.run(
                Collections.singletonMap("image", inputTensor),
                setOf("scaled_box_out")
            )

            output.use {
                val rawOutput1 = (output?.get(0)?.value) as Array<FloatArray>

                Log.d("TAG", "Item")

                rawOutput1.forEach {
                    Log.d("TAG", "Item ${it.joinToString(separator = ",")}")
                }

                return Result(imgBitmap, rawOutput1)
            }
        }
    }


    private fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}