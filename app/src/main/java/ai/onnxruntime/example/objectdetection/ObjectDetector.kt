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

//    fun detect(inputStream: InputStream, ortEnv: OrtEnvironment, ortSession: OrtSession): Result {
    fun detect(inputStream: InputStream, ortEnv: OrtEnvironment, ortSession: OrtSession, img_bitmap: Bitmap): Result {
        // Step 1: convert image into byte array (raw image bytes)
        val rawImageBytes = inputStream.readBytes()

        // Step 2: get the shape of the byte array and make ort tensor
        val shape = longArrayOf(rawImageBytes.size.toLong())
        Log.i("bytes shape", ""+shape);

        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            ByteBuffer.wrap(rawImageBytes).asReadOnlyBuffer(),
            shape,
            OnnxJavaType.UINT8
        )
        Log.i("ObjectDetector", "This is an info message");
        inputTensor.use {
            // Step 3: call ort inferenceSession run
            // for default provided
             val output = ortSession.run(Collections.singletonMap("image", inputTensor),
//                 setOf("image_out", "scaled_box_out_next")
//                 setOf("image_out")
                 setOf("scaled_box_out")
             )
//            Log.i("ouptut", output.map { it -> it.key }.toString())
//            Log.i("ouptut", (output?.get(1)?.value.toString()))
//            Log.i("ouptut", (output?.get(1)?.info as TensorInfo).toString())
//            Log.i("ouptut", output?.get(0)?.type?.name.toString())


            // Step 4: output analysis
            output.use {
//                val rawOutput = (output?.get(0)?.value) as ByteArray
                val rawOutput1 = (output?.get(0)?.value) as Array<FloatArray>
//                val boxOutput = (output?.get(1)?.value) as Array<FloatArray>
//                val outputImageBitmap = byteArrayToBitmap(rawOutput)

                Log.d("TAG","Item")


                rawOutput1.forEach {
                    Log.d("TAG","Item ${it.joinToString(separator = ",")}")
                }
//
                // Step 5: set output result
//                var result = Result(outputImageBitmap, emptyArray())
                var result = Result(img_bitmap, rawOutput1)
                return result
            }
        }
    }

    private fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}