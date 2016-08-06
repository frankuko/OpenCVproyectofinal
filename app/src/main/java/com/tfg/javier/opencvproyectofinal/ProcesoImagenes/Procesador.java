package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

import android.os.Debug;
import android.util.DebugUtils;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

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



        //Vector de candidatos
        Vector<RotatedRect> candidatos = new Vector<>();

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

            boolean anadido = false;

            //Log.println(Log.ERROR,"DEBUG contornos:,String.valueOf(contornos.get(c).size().height));
            double tamanoContorno = contornos.get(c).size().height;

            //test de tamaño entre 12 y º10 pixeles
            if((tamanoContorno>=12.0) && (tamanoContorno<=120)){

                //test de area, comprobar area minima
                 double area = Imgproc.contourArea(contornos.get(c));
                //Log.println(Log.ERROR,"DEBUG area contorno",String.valueOf(area));

                if((area>=20.0) && (area<=1100)){

                    MatOfPoint2f temp=new MatOfPoint2f();
                    temp.fromList(contornos.get(c).toList());
                    RotatedRect elip =Imgproc.fitEllipse(temp);

                    double areaTeorica= 3.1416*elip.size.width*elip.size.height/4.0;
                    double ratioForma= area/areaTeorica;
                    //Log.println(Log.ERROR,"areas", String.valueOf(areaTeorica)+" | "+String.valueOf(area));
                    //Log.println(Log.ERROR,"ratio forma",String.valueOf(ratioForma));

                    //en este test comprobamos si el area teorica
                    //es similar a la calculada, si hay un porcentaje del 98% continuamos
                    if(ratioForma>0.98){
                        //Pasamos al test de color
                        int cuentaClaros= 0;
                        double totalPuntos= 0;

                        //cogemos los puntos totales del contorno y tomamos muestras de 3 en 3
                        for (int k= 0; k<contornos.get(c).size().height; k+= 3) {

                            List<Point> listaPuntos = contornos.get(c).toList();
                            //Cogemos el punto y lo comparamos con su centro de la elipse
                            Point ptInt = new Point((listaPuntos.get(k).x + elip.center.x) / 2,
                                    (listaPuntos.get(k).y + elip.center.y) / 2);

                            Point ptExt = new Point((listaPuntos.get(k).x * 3 - elip.center.x) / 2,
                                    (listaPuntos.get(k).y * 3 - elip.center.y) / 2);


                            double[] pixelInt = lista.get(0).get((int) ptInt.x, (int) ptInt.y);
                            double[] pixelExt = lista.get(0).get((int) ptExt.x, (int) ptExt.y);
                            //double pixelInt = buff[(int)ptInt.x+(int)ptInt.y];
                            //double pixelExt = buff[(int)ptExt.x+(int)ptExt.y];
                            if(pixelInt!=null && pixelExt != null) {
                                cuentaClaros += (pixelInt[0] - 20 > pixelExt[0]) ? 1 : 0;
                                totalPuntos++;
                            }
                            //Log.println(Log.ERROR,"PtInt, x|y",String.valueOf(ptInt.x)+" | "+String.valueOf(ptInt.y));
                            if(pixelInt!=null && pixelExt != null){
                                //Log.println(Log.ERROR,"Pixeles, Int|Ext",String.valueOf(pixelInt[0])+" | "+String.valueOf(pixelExt[0]));


                            }
                        }
                        //Log.println(Log.ERROR,"cuentaClaros, total",String.valueOf(cuentaClaros)+" | "+String.valueOf(totalPuntos));
                        if(cuentaClaros> 8) {

                            // Test de candidato no incluido en otro
                            double minDist= 800*600;
                            for (int o= 0; o<candidatos.size(); o++) {

                                double distancia= Math.abs(elip.center.x-candidatos.get(o).center.x)+Math.abs(elip.center.y-candidatos.get(o).center.y);
                                if (distancia<minDist)
                                    minDist = distancia;
                            }
                            if (minDist>elip.size.width && minDist>elip.size.height) {
                                candidatos.add(elip);
                                anadido= true;
                                Imgproc.drawContours(entrada, contornos, c, new Scalar(0,0,255), 1);

                                Log.println(Log.ERROR,"C añadido | total cands",String.valueOf(c) + " | "+ String.valueOf(candidatos.size()));

                            }
                            //cuentaClaros += lista.get(0).
                            /*Point ptInt= Point((contornos.get(c).g[i][k].x+elip.center.x)/2,
                                    (contours[i][k].y+elip.center.y)/2);
                            Point ptExt= Point((contours[i][k].x*3-elip.center.x)/2,
                                    (contours[i][k].y*3-elip.center.y)/2);*/
                            //cuentaClaros+= (canales[0].at<uchar>(ptInt)-20>canales[0].at<uchar>(ptExt)?1:0);
                           // totalPuntos++;

                        }
                    }


                    //RotatedRect rect = Imgproc.fitEllipse(contornos.get(c));
                }

            }

            if(!anadido)
                Imgproc.drawContours(entrada, contornos, c, new Scalar(255,0,0), 1);

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
