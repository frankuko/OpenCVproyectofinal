package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

import android.os.Debug;
import android.util.DebugUtils;
import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.Console;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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

   // private MatOfPoint3f marcadores;

    private org.opencv.core.MatOfPoint3f marcadores;
    private Mat p3d;

    private Mat K;

    private MatOfDouble calibracion;

    private List<MatOfPoint> contornosFinales;

    //Matriz de referencia de la tarjeta



    //Matriz con unos puntos imaginarios


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

        marcadores = new MatOfPoint3f(new Point3(0,0,0),new Point3(20,0,0),new Point3(0,20,0),new Point3(20,20,0));
        int r = 0;
        int c = 0;
        marcadores.put(r,c,0,0,0,20,0,0,0,20,0,20,20,0);

        p3d = new Mat(4,3,CvType.CV_32FC3);
        p3d.put(r,c,1,1,5,1,2,5,2,2,5,2,1,5,1,1,6,1,2,6,2,2,6,2,1,6,2.5,1.5,5.5);

        K = new Mat(3,3,CvType.CV_32FC3);
        K.put(r,c,800,0,320,0,800,240,0,0,1);

        contornosFinales = new ArrayList<MatOfPoint>();

        calibracion = new MatOfDouble(0.0,0.0,0.0,0.0,0.0);
        //calibracion.put(r,c,)
    /*Mat card = (Mat_<double>(4, 3) <<
            0, 0, 0,
            20, 0, 0,
            0,  20, 0,
            20,  20, 0 );

      Mat p3d = (Mat_<float>(9, 3) <<
       1, 1, 5,
       1, 2, 5,
       2, 2, 5,
       2, 1, 5,
       1, 1, 6,
       1, 2, 6,
       2, 2, 6,
       2, 1, 6,
       2.5, 1.5, 5.5);*/

    }

    public String devolverColor(Mat imagenEntrada, MatOfPoint p){

        Mat crop = new Mat(imagenEntrada,Imgproc.boundingRect(p));
        Mat mask = Mat.zeros(crop.rows(),crop.cols(),CvType.CV_8UC1);


        Point topLeft = Imgproc.boundingRect(p).tl();
        Point minusTopLeft = new Point(-topLeft.x,-topLeft.y);

        List<MatOfPoint> lista = new ArrayList<MatOfPoint>();
        lista.add(p);

        Mat dest = new Mat();
        //Imgproc.dr
        Imgproc.drawContours(mask,lista,-1,new Scalar(255),Core.FILLED,Imgproc.LINE_AA,dest,1,minusTopLeft);

        Scalar media = Core.mean(crop,mask);


        //Imgproc.boundingRect(p).tl()

        /*for (int k= 0; k<p.size().height; k+= 3) {


            List<Point> listaPuntos = p.toList();

            //Cogemos el punto y lo comparamos con su centro de la elipse
            Point ptInt = new Point((listaPuntos.get(k).x + elip.center.x) / 2,
                    (listaPuntos.get(k).y + elip.center.y) / 2);

        }*/
        Log.println(Log.ERROR,"Devolviendo media",String.valueOf(media));


        double[] pixel = media.val;//imagenEntrada.get((int) p.y, (int) p.x);

        if(pixel[0] >= 170.0 && pixel[1]>= 90.0 && pixel[2]>=100.0){
            Log.println(Log.ERROR,"Devolviendo blanco",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
            return "BLANCO";
        }


        if (pixel[0] >= 170.0){
            Log.println(Log.ERROR,"Devolviendo color rojo",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
            return "ROJO";
        }
        if (pixel[1] >= 150.0){
            Log.println(Log.ERROR,"Devolviendo color verde",String.valueOf(pixel[1]));
            return "VERDE";
        }

        if (pixel[2]>= 120.0){
            Log.println(Log.ERROR,"Devolviendo color azul",String.valueOf(pixel[2]));
            return "AZUL";
        }



        Log.println(Log.ERROR,"Devolviendo sin color",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
        return "SIN COLOR";


    }



    public String ordenarColores(Mat imagen, List<MatOfPoint> vector){

        MatOfPoint temp0 = new MatOfPoint();
        MatOfPoint temp1 = new MatOfPoint();
        MatOfPoint temp2 = new MatOfPoint();
        MatOfPoint temp3 = new MatOfPoint();





       //Core.mean(vector.get(i))
        String resultado = " | ";

        for(int i=0;i<vector.size();i++){
            //Core.mean(vector.get(i));
            String color = devolverColor(imagen,vector.get(i));
            resultado+=color+" | ";
            switch (color){
                case "BLANCO":
                    temp0=vector.get(i);
                    break;
                case "ROJO":
                    temp1=vector.get(i);
                    break;
                case "VERDE":
                    temp2=vector.get(i);
                    break;
                case "AZUL":
                    temp3=vector.get(i);
                    break;
            }

        }

        vector.set(0,temp0);
        vector.set(1,temp1);
        vector.set(2,temp2);
        vector.set(3,temp3);

        //return vector;
        //return null;
        return resultado;


    }

    /*public String ordenarColores(Mat imagen, Vector<RotatedRect> vector){

        RotatedRect temp0 = new RotatedRect();
        RotatedRect temp1 = new RotatedRect();
        RotatedRect temp2 = new RotatedRect();
        RotatedRect temp3 = new RotatedRect();

        //Core.mean(vector.get(i))
        String resultado = "";

        for(int i=0;i<vector.size();i++){
            //Core.mean(vector.get(i));
            String color = devolverColor(imagen,vector.get(i));
            resultado+=color+" | ";
            switch (color){
                case "BLANCO":
                    temp0=vector.get(i);
                    break;
                case "ROJO":
                    temp1=vector.get(i);
                    break;
                case "VERDE":
                    temp2=vector.get(i);
                    break;
                case "AZUL":
                    temp3=vector.get(i);
                    break;
            }

        }

        vector.set(0,temp0);
        vector.set(1,temp1);
        vector.set(2,temp2);
        vector.set(3,temp3);

        //return vector;
        //return null;
        return resultado;


    }*/



    public Mat procesaGris(Mat entrada){
        List<Mat> lista = new ArrayList<Mat>(4) ;
        Core.split(entrada,lista);
        Mat maximo = new Mat();

        //lista.get(0).copyTo(maximo);
        Core.max(lista.get(0),lista.get(1),maximo);
        Core.max(maximo,lista.get(2),maximo);
        //for(int i=1;i<lista.size()-1;i++)


        return maximo;
    }

    public Mat procesaRojos(Mat entrada) {



        //Vector de candidatos
        Vector<RotatedRect> candidatos = new Vector<>();

        //Probar a inicializar en el constructor
        List<Mat> lista = new ArrayList<Mat>(4) ;
        Core.split(entrada,lista);
        Mat maximo = new Mat();


        Core.max(lista.get(0),lista.get(1),maximo);
        Core.max(maximo,lista.get(2),maximo); //en lista de 0 tenemos el maximo de los 3 colores GRISES



// 3. Aplicar el detector de Canny, bajando umbral hasta tener suficientes bordes
        for(int umbral=500;umbral>200;umbral-=100){
            Imgproc.Canny(maximo,bordes,200,umbral,3,true);
            if(Core.norm(bordes, Core.NORM_L1)/255>400)
                break;
        }

        List<MatOfPoint> contornos = new ArrayList<MatOfPoint>();
        List<Pair<RotatedRect,MatOfPoint>> listaContornos = new ArrayList<Pair<RotatedRect,MatOfPoint>>();
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

                if((area>=100.0) && (area<=1100)){

                    MatOfPoint2f temp=new MatOfPoint2f();
                    temp.fromList(contornos.get(c).toList());
                    RotatedRect elip =Imgproc.fitEllipse(temp);

                    double areaTeorica= 3.1416*elip.size.width*elip.size.height/4.0;
                    double ratioForma= (area+4)/areaTeorica;
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
                            //Point[] arrayPuntos = contornos.get(c).toArray();
                            //Cogemos el punto y lo comparamos con su centro de la elipse
                            Point ptInt = new Point((listaPuntos.get(k).x + elip.center.x) / 2,
                                    (listaPuntos.get(k).y + elip.center.y) / 2);

                            Point ptExt = new Point((listaPuntos.get(k).x * 3 - elip.center.x) / 2,
                                    (listaPuntos.get(k).y * 3 - elip.center.y) / 2);


                           // Log.println(Log.ERROR,"Pixeles Int, y|x",String.valueOf(ptInt.y)+" | "+String.valueOf(ptInt.x));
                            //Log.println(Log.ERROR,"Mat rows",String.valueOf(lista.get(0).rows()));
                            //Log.println(Log.ERROR,"Mat cols",String.valueOf(lista.get(0).cols()));


                            double[] pixelInt = maximo.get((int) ptInt.y, (int) ptInt.x);
                            double[] pixelExt = maximo.get((int) ptExt.y, (int) ptExt.x);

                            //double pixelInt = buff[(int)ptInt.x+(int)ptInt.y];
                            //double pixelExt = buff[(int)ptExt.x+(int)ptExt.y];
                            if(pixelInt!=null && pixelExt != null) {
                                cuentaClaros += (pixelInt[0] - 20 > pixelExt[0]) ? 1 : 0;
                                totalPuntos++;
                            }
                            //Log.println(Log.ERROR,"PtInt, x|y",String.valueOf(ptInt.x)+" | "+String.valueOf(ptInt.y));

                        }
                        Log.println(Log.ERROR,"cuentaClaros, total",String.valueOf(cuentaClaros)+" | "+String.valueOf(totalPuntos));
                        if(cuentaClaros/totalPuntos> 0.85) {

                            // Test de candidato no incluido en otro, para comprobar que el candidato no esta dentro de otro candidato
                            double minDist= 800*600;
                            for (int o= 0; o<candidatos.size(); o++) {

                                double distancia= Math.abs(elip.center.x-candidatos.get(o).center.x)+Math.abs(elip.center.y-candidatos.get(o).center.y);
                                if (distancia<minDist)
                                    minDist = distancia;
                            }
                            if (minDist>elip.size.width && minDist>elip.size.height) {


                                    candidatos.add(elip);

                                    Pair<RotatedRect,MatOfPoint> par = new Pair<RotatedRect,MatOfPoint>(elip,contornos.get(c));
                                    listaContornos.add(par);


                                    //contornosFinales.add(contornos.get(c));

                                    anadido= true;
                                    Imgproc.drawContours(entrada, contornos, c, new Scalar(255,0,0), 1);
                                    Log.println(Log.ERROR,"C añadido | total cands",String.valueOf(c) + " | "+ String.valueOf(candidatos.size()));



                            }


                        }
                    }


                    //RotatedRect rect = Imgproc.fitEllipse(contornos.get(c));
                }

            }

            /*if(!anadido)
                Imgproc.drawContours(entrada, contornos, c, new Scalar(255,0,0), 1);*/

        }

        if(!listaContornos.isEmpty() && listaContornos.size()>4){
            //reducimos a los candidatos circulares
            //debemos reducir el número a los 4 candidatos más grandes
            Iterator<Pair<RotatedRect,MatOfPoint>> it = listaContornos.iterator();
            //double minTamano = 100000.0;
            double tam0 = 0.0;
            double tam1 = 0.0;
            double tam2 = 0.0;
            double tam3 = 0.0;

            /*for (Pair<RotatedRect,Integer> pair: listaContornos) {

                //Log.println(Log.ERROR,"Pares de contornos",String.valueOf(pair.getRight()));
            }*/
            Log.println(Log.ERROR,"tamano lista",String.valueOf(listaContornos.size()));
            while (it.hasNext()){
                Log.println(Log.ERROR,"tamano lista",String.valueOf(listaContornos.size()));
                Pair<RotatedRect,MatOfPoint> v = it.next();
                if(v.getLeft().angle>10 && v.getLeft().angle<165){
                    it.remove();
                    Log.println(Log.ERROR,"borramos lista",String.valueOf(listaContornos.size()));
                    //listaContornos.
                }

                else if(listaContornos.size()>4) {

                        if(v.getLeft().size.area()>tam0){
                            //tenemos un nuevo valor mayor
                            tam3=tam2;
                            tam2=tam1;
                            tam1=tam0;
                            tam0=v.getLeft().size.area();
                        }
                        else
                            if(v.getLeft().size.area()>tam1){
                                tam3=tam2;
                                tam2=tam1;
                                tam1=v.getLeft().size.area();
                            }

                            else
                                if(v.getLeft().size.area()>tam2){
                                    tam3=tam2;
                                    tam2 = v.getLeft().size.area();
                                }

                                else
                                    if(v.getLeft().size.area()>tam3)
                                        tam3 = v.getLeft().size.area();
                                    else{
                                        Log.println(Log.ERROR,"borramos lista",String.valueOf(listaContornos.size()));
                                        it.remove();
                                    }

                      }



            }
            Log.println(Log.ERROR,"Candidatos",String.valueOf(listaContornos.size()));
        }

        if (!listaContornos.isEmpty() && listaContornos.size()==4){

            List<MatOfPoint> puntosContorno = new ArrayList<MatOfPoint>();
            List<RotatedRect> rectContorno = new ArrayList<RotatedRect>();

            for (Pair<RotatedRect,MatOfPoint> pair: listaContornos) {
                rectContorno.add(pair.getLeft());
                puntosContorno.add(pair.getRight());
            }

           String res = ordenarColores(entrada,puntosContorno);


            /*if(!candidatos.isEmpty() && candidatos.size()==4)
                if(!candidatos.contains(new RotatedRect()))
                    for (int j=0; j<candidatos.size(); ++j){
                        Imgproc.line(entrada, candidatos.get(j).center, candidatos.get((j+1)%candidatos.size()).center, new Scalar(255,255,255));
                    }*/

            if(!rectContorno.isEmpty() && rectContorno.size()==4){
                //if(!rectContorno.contains(new RotatedRect())){
                    Mat rvec = new Mat();
                    Mat tvec = new Mat();

                    List<Point> r = new ArrayList<Point>();
                    for(int y=0; y<rectContorno.size();y++){

                        r.add(rectContorno.get(y).center);

                    }
                    MatOfPoint2f temp=new MatOfPoint2f();
                    temp.fromList(r);

                    double fontScale = 200;
                    Scalar color = new Scalar(fontScale);
                    Point p = new Point(300,300);
                    for (int j=0; j<rectContorno.size(); ++j){
                        Imgproc.line(entrada, rectContorno.get(j).center, rectContorno.get((j+1)%rectContorno.size()).center, new Scalar(255,255,255));
                    }
                    Imgproc.putText(entrada,res,p,Core.FONT_HERSHEY_PLAIN,1,color,1);

                    //Calib3d.solvePnP(marcadores,temp,K,calibracion,rvec,tvec);
                }

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
