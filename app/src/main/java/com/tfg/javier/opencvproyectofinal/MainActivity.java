package com.tfg.javier.opencvproyectofinal;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.tfg.javier.opencvproyectofinal.ProcesoImagenes.Procesador;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OPENCV CAMERA PROYECTO";

    private static final String STATE_CAMERA_INDEX = "cameraIndex";

    private CameraBridgeViewBase cameraView;

    Mat _mRgba;
    Mat salida;

    private int cam_anchura = 960; // 960 x 720
    private int cam_altura = 720;

    private int cam_anchura_nativa; // 320 x 240
    private int cam_altura_nativa;

    private int framecount = 0;
    private int counter = 0;
   // Mat fxyMap2;
    Procesador processor;

    private int indiceCamara; // 0 -> camara trasera y 1 -> camara delantera


    //valores originales
    /*private double k1 = 0.000001;
    private double k2 = 0.0000000001;*/

    //valores para 960x720
    /*private double k1 = 0.0000001;
    private double k2 = 0.0000000005;*/

    private double k1 = 2;
    private double k2 = 1;

    static {
        if(OpenCVLoader.initDebug()){
            Log.i(TAG,"OpenCV se cargo bien");
        }
        else
        {
            Log.i(TAG,"OpenCV crasheo");
        }
    }


    private BaseLoaderCallback mLoaderCallback =
            new BaseLoaderCallback(this) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS:
                            Log.d(TAG, "OpenCV se cargo correctamente");
                            cameraView.setMaxFrameSize(cam_anchura , cam_altura);
                            cameraView.enableView();


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
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);

        cameraView = (CameraBridgeViewBase) findViewById(R.id.vista_camara);
        cameraView.setCvCameraViewListener(this);

        if(savedInstanceState != null){
            indiceCamara = savedInstanceState.getInt(STATE_CAMERA_INDEX,0);
        }
        else{
            indiceCamara = 0;
        }

        cameraView.setCameraIndex(indiceCamara);


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
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        //menu.clear();
        // MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.cambiarCamara:
                indiceCamara++;
                if (indiceCamara == Camera.getNumberOfCameras()){
                    indiceCamara = 0;
                }
                recreate();
                break;
            case R.id.resolucion_1920x1080:
                cam_anchura = cam_anchura_nativa;
                cam_altura = cam_altura_nativa;
                reiniciarResolucion();
                break;
            case R.id.resolucion_800x600:
                cam_anchura = cam_anchura_nativa/2;
                cam_altura = cam_altura_nativa/2;
                reiniciarResolucion();
                break;
            case R.id.resolucion_640x480:
                cam_anchura = cam_anchura_nativa/3;
                cam_altura = cam_altura_nativa/3;
                reiniciarResolucion();
                break;
            case R.id.resolucion_320x240:
                cam_anchura = cam_anchura_nativa/4;
                cam_altura = cam_altura_nativa/4;
                reiniciarResolucion();
                break;

        }
        String msg = "W="+Integer.toString(cam_anchura)+" H="+
                Integer.toString(cam_altura)+ " Cam ="+
                Integer.toBinaryString(indiceCamara);
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
        return true;
    }


    public void reiniciarResolucion(){
        cameraView.disableView();
        processor = null;
        cameraView.setMaxFrameSize(cam_anchura,cam_altura);
        cameraView.enableView();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {



        /*cam_altura = height;
        cam_anchura = width;*/

        cam_altura = height;
        cam_anchura = width;

        cam_altura_nativa = cameraView.getHeight();
        cam_anchura_nativa = cameraView.getWidth();

        _mRgba = new Mat(cam_altura,cam_anchura, CvType.CV_8UC4);
        salida = new Mat(cam_altura,cam_anchura, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {

    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        //Mat mRgba = new Mat();
        inputFrame.rgba().copyTo(_mRgba);

        //Mat salida = new Mat();

        Scalar s = new Scalar(0,0,0);

        if (processor == null){
            int columnas = _mRgba.cols();
            int filas = _mRgba.rows();
            processor = new Procesador(filas,columnas);
            framecount = 0;


            processor.barrelDistortion(_mRgba,new Point(columnas/2,filas/2),
                    new Point(columnas*4/16, filas/2), new Point(columnas*12/16, filas/2),k1,k2);

           // salida = processor.procesaRojos(_mRgba);
        }

        framecount +=1;

        if(framecount == 50){
            System.gc();
        }

        if(processor!=null)
            salida = processor.procesaRojos(_mRgba);

        /*Imgproc.remap(_mRgba, salida, processor.getMapaX(), processor.getMapaY(),
                Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, s);*/



        return salida;


    }
}
