package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

import android.os.Debug;
import android.util.DebugUtils;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by javier on 07/06/2016.
 */
public class Procesador {

    private Mat red1;
    private Mat green1;
    private Mat blue;
    private Mat maxGB;

    private Mat mapaX;
    private Mat mapaY;

    private Mat bordes;

    private Mat imgBordes;

    private Mat hierarchy;

    public Procesador(int filas, int columnas){
        red1 = new Mat(filas,columnas,CvType.CV_8UC1);
        green1 = new Mat(filas,columnas,CvType.CV_8UC1);
        blue = new Mat(filas,columnas,CvType.CV_8UC1);
        maxGB = new Mat(filas,columnas,CvType.CV_8UC1);
        mapaX = new Mat();
        mapaY = new Mat();
        bordes = new Mat();
        imgBordes = new Mat();
        hierarchy = new Mat();

    }



    public Mat procesaRojos(Mat entrada) {



        //Probar a inicializar en el constructor
        List<Mat> lista = new ArrayList<Mat>(4) ;
        Core.split(entrada,lista);


        Core.max(lista.get(0),lista.get(1),lista.get(0));
        Core.max(lista.get(0),lista.get(2),lista.get(0)); //en lista de 0 tenemos el maximo de los 3 colores GRISES


// 3. Aplicar el detector de Canny, bajando umbral hasta tener suficientes bordes
        for(int umbral=500;umbral>200;umbral-=100){
            Imgproc.Canny(lista.get(0),bordes,200,400,3,true);
            if(Core.norm(bordes, Core.NORM_L1)/255>400)
                break;
        }

        List<MatOfPoint> contornos = new ArrayList<MatOfPoint>();
        Imgproc.findContours(bordes,contornos,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_NONE);

        for (int c=0; c<contornos.size();c++){
            //test de tamaño entre 12 y º10 pixeles
            boolean añadido = false;

            Log.println(Log.ERROR,"DEBUGACION contornos:",String.valueOf(contornos.size()));
            //if(contornos.get(c).size())


        }

        return entrada;

       /* Core.extractChannel(entrada,red1,0);
        Mat salida = entrada.clone();
        entrada.release();
        //Substraemos del canal R el máximo de los canales G y B
        Core.subtract(red1,maxGB,salida);
        //Devolvemos la matriz que ve rojo.
        return salida;*/
    }

    public void barrelDistortion(Mat entrada, Point centro, Point centroI, Point centroD, double d1, double d2){




        Mat salida = entrada.clone();

        //filas de la imagen
        int rows = entrada.rows();
        //columnas de la imagen
        int cols = entrada.cols();

        double distancia = Math.sqrt(rows*rows+cols*cols);

        double k1 = d1 / Math.pow(distancia,2);

        double k2 = d2 / Math.pow(distancia,4);

        float vx, vy;
        double px, py;
        float dist;

        //array de filas * columnas -> 480x800
        //float[] fxArray = new float[rows * cols];
        //float[] fyArray = new float[rows * cols];

        //matricespara realizar el mapeo,
        mapaX = new Mat(rows, cols, CvType.CV_32FC1 );
        mapaY = new Mat(rows, cols, CvType.CV_32FC1 );

        for( int y = 0; y < rows; y++ ) {
            for( int x = 0; x < cols; x++ ) {
                //GUARDAMOS EN LOS FXYARRAYS los valores de las nuevas posiciones. +k1*dist+k2*dist*dist, Point centro k1 y k2
                if(x<cols/2){
                    vx = x - (float)centroI.x;
                    vy = y - (float)centroI.y;
                }else {
                    vx = x - (float) centroD.x;
                    vy = y - (float) centroD.y;
                }

                dist= vx*vx+vy*vy;
                px= centro.x+vx*(1+k1*dist+k2*dist*dist);
                py= centro.y+vy*(1+k1*dist+k2*dist*dist);


                mapaX.put(y,x,px);
                mapaY.put(y,x,py);

            }
        }


        //guarda la transformacion de fxtemp y fytemp en fxyVertical y fxyMap2
        //Imgproc.convertMaps(fxTemp, fyTemp, fxyVertical, fxyMap2,
         //       CvType.CV_16SC2);


        //Mapea la imagen nueva con los valores obtenidos anteriormente, //probar on vecino mas proximo
        //Imgproc.remap(entrada, salida, fxyVertical, fxyMap2,
         //       Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0));

        //return salida;
    }

    public Mat getMapaX()
    {
        return mapaX;
    }

    public Mat getMapaY()
    {
        return mapaY;
    }
}
