package ai.onnxruntime.example.objectdetection

import ai.onnxruntime.*
import ai.onnxruntime.extensions.OrtxPackage
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.InputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private var ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private lateinit var ortSession: OrtSession
    private lateinit var inputImage: ImageView
    private lateinit var outputImage: ImageView
    private lateinit var objectDetectionButton: Button
    private var imageid = 0;
    private lateinit var classes:List<String>

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        print("Model read complete")
        inputImage = findViewById(R.id.imageView1)
        outputImage = findViewById(R.id.imageView2)
        objectDetectionButton = findViewById(R.id.object_detection_button)
        inputImage.setImageBitmap(
            BitmapFactory.decodeStream(readInputImage())
        );
        imageid = 0
        classes = readClasses();
        // Initialize Ort Session and register the onnxruntime extensions package that contains the custom operators.
        // Note: These are used to decode the input image into the format the original model requires,
        // and to encode the model output into png format
        val sessionOptions: OrtSession.SessionOptions = OrtSession.SessionOptions()
        sessionOptions.registerCustomOpLibrary(OrtxPackage.getLibraryPath())
        ortSession = ortEnv.createSession(readModel(), sessionOptions)

        objectDetectionButton.setOnClickListener {
            try {
                Coroutines.io {
                    performObjectDetection(ortSession)
                }
                Toast.makeText(baseContext, "ObjectDetection performed!", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Log.e(TAG, "Exception caught when perform ObjectDetection", e)
                Toast.makeText(baseContext, "Failed to perform ObjectDetection", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        Log.i("MainActivity", "This is an info message");
    }

    override fun onDestroy() {
        super.onDestroy()
        ortEnv.close()
        ortSession.close()
    }

    private fun updateUI(result: Result, elapsedTime: Long, fps: Double) {
        val mutableBitmap: Bitmap = result.outputBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.color = Color.RED // Text Color

        paint.textSize = 28f // Text Size

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern

        canvas.drawBitmap(mutableBitmap, 0.0f, 0.0f, paint)
        var boxit = result.outputBox.iterator()
        while(boxit.hasNext()) {
            var boxInfo = boxit.next()
            canvas.drawText("%s:%.2f".format(classes[boxInfo[5].toInt()],boxInfo[4]), boxInfo[0]-boxInfo[2]/2, boxInfo[1]-boxInfo[3]/2, paint.apply {
                style = Paint.Style.FILL
            })

            // Draw bounding box:
            val left = boxInfo[0] - (boxInfo[2] /2)
            val top =  boxInfo[1] - (boxInfo[3] /2)
            val right =  boxInfo[0] + (boxInfo[2] /2)
            val bottom = boxInfo[1] + (boxInfo[3] /2)

            canvas.drawRect(left, top, right, bottom, paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.0f
            })
        }

        Coroutines.main {
            outputImage.setImageBitmap(mutableBitmap)
        }

        //Log
        Log.d("PerformanceOut: ","ElapsedTime: $elapsedTime,  FPS: $fps")

        // draw
        canvas.drawText("ElapsedTime: $elapsedTime,  FPS: $fps", 0.0f, 50.0f, paint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = 20.0f
            setShadowLayer(5.0f, 0.0f, 0.0f, Color.BLACK)
        })
    }

    private fun readModel(): ByteArray {
//        val modelID = R.raw.yolov8n_with_pre_post_processing
        val modelID = R.raw.yolov8n_c_with_pre_post_processing_box_out
//        val modelID = R.raw.bird_best_with_pre_post_processing_box_out
        return resources.openRawResource(modelID).readBytes()
    }

    private fun readClasses(): List<String> {
        return resources.openRawResource(R.raw.classes).bufferedReader().readLines()
//        return resources.openRawResource(R.raw.bird_classes).bufferedReader().readLines()
    }

    private fun readInputImage(): InputStream {
        imageid = imageid.xor(1)
        return assets.open("test_object_detection_${imageid}.jpg")
    }

    private fun performObjectDetection(ortSession: OrtSession) {
        val objDetector = ObjectDetector()
        val startTime = System.currentTimeMillis()
        val imageStream = readInputImage()
        val imgBitmap = BitmapFactory.decodeStream(imageStream)

        Coroutines.main {
            inputImage.setImageBitmap(imgBitmap)
        }

        imageStream.reset()
        val result = objDetector.detect(imageStream, ortEnv, ortSession, imgBitmap)
        val endTime = System.currentTimeMillis()

        val elapsedTime = endTime - startTime
        val fps = 1000.0 / elapsedTime

        updateUI(result, elapsedTime, fps)
    }

    companion object {
        const val TAG = "ORTObjectDetection"
    }
}