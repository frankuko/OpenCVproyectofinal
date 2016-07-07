package com.tfg.javier.opencvproyectofinal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.tfg.javier.opencvproyectofinal.ProcesoImagenes.Procesador;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OPENCV CAMERA PROYECTO";

    private static final String STATE_CAMERA_INDEX = "cameraIndex";

    private CameraBridgeViewBase cameraView;

    Mat mRgba;

    private int cam_anchura = 800;
    private int cam_altura = 600;

    private int framecount = 0;
   // Mat fxyMap2;
    Procesador processor;


    private BaseLoaderCallback mLoaderCallback =
            new BaseLoaderCallback(this) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS:
                            Log.d(TAG, "OpenCV se cargo correctamente");
                            cameraView.setMaxFrameSize(cam_anchura*2 , cam_altura);
                            cameraView.enableView();

                            mRgba = new Mat();
                            //fxyMap2 = new Mat();

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



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        inputFrame.rgba().copyTo(mRgba);



        Mat salida = new Mat();

        Scalar s = new Scalar(0,0,0);

        if (processor == null){
            int columnas = mRgba.cols();
            int filas = mRgba.rows();
            processor = new Procesador();
            framecount = 0;


            processor.barrelDistortion(mRgba,new Point(columnas/2,filas/2),
                    new Point(columnas*5/16, filas/2), new Point(columnas*11/16, filas/2),0.00000001,0.00000002);
        }

        framecount +=1;

        if(framecount == 50){
            System.gc();
        }





        Imgproc.remap(mRgba, salida, Procesador.fxyVertical, Procesador.fxyMap2,
                Imgproc.INTER_NEAREST, Core.BORDER_TRANSPARENT, s);

        mRgba.release();


        return salida;





        //pasamos a concatenar la matriz
/*
        mats = Arrays.asList(matriz,matriz);

        if(mcopia == null){
            mcopia = new Mat();
        }





        Core.hconcat(mats, mcopia);
        matriz.release();


        Imgproc.resize(mcopia,mcopia,new Size(cam_anchura,cam_altura));

        return mcopia;
*/
        /*if(salida.channels() == 1)
            Imgproc.cvtColor(salida,salida,Imgproc.COLOR_GRAY2RGBA);
        return salida;*/
    }
}
