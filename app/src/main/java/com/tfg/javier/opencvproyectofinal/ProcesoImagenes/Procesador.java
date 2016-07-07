package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by javier on 07/06/2016.
 */
public class Procesador {

    /*Mat red;
    Mat green;
    Mat blue;
    Mat maxGB;*/

    public static Mat fxyVertical;
    public static Mat fxyMap2;

    public Procesador(){
        //red = new Mat();
        //green = new Mat();
        //blue = new Mat();
        //maxGB = new Mat();
        fxyVertical = new Mat();
        fxyMap2 = new Mat();
    }

    /*public Mat procesaRojos(Mat entrada) {
        Mat salida = new Mat();
        Core.extractChannel(entrada,red,0);
        Core.extractChannel(entrada,green,1);
        Core.extractChannel(entrada,blue,2);
        //En las zonas rojas la componente G y B serán casi nulas, mientras que en las zonas azules y verdes la componente R sera pequeña
        //Calculamos el maximo entre los canales 1 y 2
        Core.max(green,blue,maxGB);
        //Substraemos del canal R el máximo de los canales G y B
        Core.subtract(red,maxGB,salida);
        //Devolvemos la matriz que ve rojo.
        return salida;
    }*/

    public void barrelDistortion(Mat entrada, Point centro, Point centroI, Point centroD, double k1, double k2){

        Mat salida = entrada.clone();

        //filas de la imagen
        int rows = entrada.rows();
        //columnas de la imagen
        int cols = entrada.cols();

        float vx, vy;
        double px, py;
        float dist;

        //array de filas * columnas -> 480x800
        //float[] fxArray = new float[rows * cols];
        //float[] fyArray = new float[rows * cols];

        //matricespara realizar el mapeo,
        Mat fxTemp = new Mat(rows, cols, CvType.CV_32FC1 );
        Mat fyTemp = new Mat(rows, cols, CvType.CV_32FC1 );
        fxyVertical = new Mat(rows, cols, CvType.CV_16SC2);

        for( int y = 0; y < rows; y++ ) {
            for( int x = 0; x < cols; x++ ) {
                //GUARDAMOS EN LOS FXYARRAYS los valores de las nuevas posiciones. +k1*dist+k2*dist*dist, Point centro k1 y k2
                vx = x - (float)centro.x;
                vy = y - (float)centro.y;

                dist= vx*vx+vy*vy;
                px= centro.x+vx*(1+k1*dist+k2*dist*dist);
                py= centro.y+vy*(1+k1*dist+k2*dist*dist);


                fxTemp.put(y,x,px);
                fyTemp.put(y,x,py);
                //fxArray[y*cols + x] = (float)px;
                //fyArray[y*cols + x] = (float)py;
            }
        }
        //fxTemp.put(0, 0, fxArray);
        //fyTemp.put(0, 0, fyArray);


        //guarda la transformacion de fxtemp y fytemp en fxyVertical y fxyMap2
        Imgproc.convertMaps(fxTemp, fyTemp, fxyVertical, fxyMap2,
                CvType.CV_16SC2);

        fxTemp.release();
        fyTemp.release();
        //salida.release();
        //entrada.release();

        //Mapea la imagen nueva con los valores obtenidos anteriormente, //probar on vecino mas proximo
        //Imgproc.remap(entrada, salida, fxyVertical, fxyMap2,
         //       Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0));

        //return salida;
    }
}
