package com.tfg.javier.opencvproyectofinal;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.tfg.javier.opencvproyectofinal.filtros.BitmapWorkerAsync;
import com.tfg.javier.opencvproyectofinal.filtros.Funciones;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraGLSurfaceView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OPENCV CAMERA PROYECTO";

    private static final String STATE_CAMERA_INDEX = "cameraIndex";

    private CameraBridgeViewBase cameraView;

    private int cam_anchura = 800;
    private int cam_altura = 600;
    Size tamano;
    Mat mcopia;
    List<Mat> mats;


    private BaseLoaderCallback mLoaderCallback =
            new BaseLoaderCallback(this) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS:
                            Log.d(TAG, "OpenCV se cargo correctamente");
                            cameraView.setMaxFrameSize(cam_anchura*2 , cam_altura);
                            cameraView.enableView();
                            Mat mcopia = new Mat();
                            tamano = new Size(cam_anchura,cam_altura);

                            break;
                        default:
                            super.onManagerConnected(status);
                            break;

                    }
                }

            };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        cameraView = (CameraBridgeViewBase) findViewById(R.id.vista_camara);
        cameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onPause() {

        super.onPause();
        if (cameraView != null)
            cameraView.disableView();

    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        cam_altura = height;
        cam_anchura = width;
    }

    @Override
    public void onCameraViewStopped() {

    }

    //Funciones f = new Funciones();

    public void loadBitmap(int resId,Mat m){
        BitmapWorkerAsync bmwrk = new BitmapWorkerAsync(m);
        bmwrk.execute(resId);
    }

    //List<Mat> mats;


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat matriz = inputFrame.rgba();


        //if(mats == null)
           /*List<Mat>  */ mats = Arrays.asList(matriz,matriz);
        /*Size size = new Size(matriz.width(), matriz.height());

        Bitmap b = Bitmap.createBitmap(matriz.width(), matriz.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matriz, b);
        //f.barrel(b,(float)0.5);
        Utils.bitmapToMat(b, matriz);*/

        if(mcopia == null){
            mcopia = new Mat();
        }

       // Mat mcopia = new Mat();






        Core.hconcat(mats, mcopia);
        matriz.release();


        Imgproc.resize(mcopia,mcopia,new Size(cam_anchura,cam_altura));

        return mcopia;
    }
}
