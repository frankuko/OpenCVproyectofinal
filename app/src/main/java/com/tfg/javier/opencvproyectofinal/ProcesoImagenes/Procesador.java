package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

import android.opengl.GLES20;
import android.os.Debug;
import android.util.DebugUtils;
import android.util.Log;

import com.tfg.javier.opencvproyectofinal.adapters.CameraProjectionAdapter;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.video.Video;

import java.io.Console;
import java.io.PipedOutputStream;
import java.nio.DoubleBuffer;
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


    private Mat mapaX;
    private Mat mapaY;

    private Mat bordes;

    //private Mat imgBordes;

    private Mat hierarchy;

    private MatOfPoint3f marcadores;

   // private org.opencv.core.MatOfPoint3f marcadores;
    private Mat p3d;
    private MatOfPoint3 p3d3;

    private Mat K;

    private MatOfDouble calibracion;

    private float[] mGLPose;

    /*private List<MatOfPoint> contornosFinales;

    private boolean primerFrame;

    private Mat prevImg;*/
    /*private Mat currImg;
    private MatOfPoint2f prevPts;
    private MatOfPoint2f nextPts;
    private MatOfPoint2f safePts;*/

    //Matriz de referencia de la tarjeta



    //Matriz con unos puntos imaginarios


    public Procesador(int filas, int columnas){


        mapaX = new Mat();
        mapaY = new Mat();
        bordes = new Mat();
        hierarchy = new Mat();

        mGLPose = new float[12];


        /*prevImg = new Mat();
        currImg = new Mat();
        primerFrame = true;
        nextPts = new MatOfPoint2f();
        prevPts = new MatOfPoint2f();
        safePts = new MatOfPoint2f();*/

        //MARCADORES ES EL TAMAÑO DEL MARCADOR QUE QUEREMOS RECONOCES

        marcadores = new MatOfPoint3f(new Point3(0,0,0),new Point3(20,0,0),new Point3(0,20,0),new Point3(20,20,0));
        int r = 0;
        int c = 0;
        //marcadores.put(r,c,0,0,0,20,0,0,0,20,0,20,20,0);

        //P3D ES LO QUE QUEREMOS DIBUJAR

        p3d = new Mat(9,3,CvType.CV_32FC3);

        p3d.put(r,c,1,1,5,1,2,5,2,2,5,2,1,5,1,1,6,1,2,6,2,2,6,2,1,6,2.5,1.5,5.5);

        p3d3 = new MatOfPoint3(new Point3(1,1,5),
                new Point3(1,2,5),
                new Point3(2,2,5),
                new Point3(2,1,5),
                new Point3(1,1,6),
                new Point3(1,2,6),
                new Point3(2,2,6),
                new Point3(2,1,6),
                new Point3(2.5,1.5,5.5));

        //p3d.put(r,c,1,1,5,1,2,5,2,2,5,2,1,5,1,1,6,1,2,6,2,2,6,2,1,6,2.5,1.5,5.5);

        //MATRIZ DE CALIBRACION

        K = new Mat(3,3,CvType.CV_32FC1);
        K.put(r,c,800,0,320,0,800,240,0,0,1);


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

    void drawCube(Mat dst, Mat object){

        int [] array = {1,2,3,4,1,5,6,7,8,5,6,2,6,7,3,7,8,4,9,3,7,9,8};

       /* int npixels = (int)RT.total() * (int)RT.elemSize();
        double[] pixels = new double[npixels];
        RT.get(0,0,pixels);

        Rt.put(0,0,pixels);*/
        Mat C = new Mat();

        int size = (int) (object.total() * object.channels() * object.elemSize());
        float[] temp = new float[size]; // use double[] instead of byte[]
        object.get(0, 0, temp);
        for (int j = 0; j < size; j++)
            temp[j] = (temp[j] / 2);  // no more casting required.
        C.put(0, 0, temp);




        for(int i=0; i<22; i++){

            //List<Point> listaPuntos = object.toList();
            //Point[] arrayPuntos = contornos.get(c).toArray();
            //Cogemos el punto y lo comparamos con su centro de la elipse

            //line(dst, Point(object.at<float>(array[i]-1,0),object.at<float>(array[i]-1,1)),
            // Point(object.at<float>(array[i+1]-1,0),object.at<float>(array[i+1]-1,1)),
            // CV_RGB(0,255,0));



            long x0 = object.dataAddr() + object.step1(0)*i + object.step1(1)*0;
            long x1 = object.dataAddr() + object.step1(0)*i + object.step1(1)*1;

            double[] pt1 = object.get(array[i]-1,0);
            double[] pt2 = object.get(array[i]-1,1);
            Point p1 = new Point();
           // p1.set(pt1,pt2);
            //Point pt2 = listaPuntos.get(array[i]);


            double[] pt3 = object.get(array[i+1]-1,0);
            double[] pt4 = object.get(array[i+1]-1,1);

            //Punto de
/*
            object.at<float>(array[i]-1,0)
            //a
            object.at<float>(array[i]-1,1))

            //Punto de
            object.at<float>(array[i+1]-1,0)
            //a
            object.at<float>(array[i+1]-1,1)*/

            //COLORCV_RGB(0,255,0)

            //Imgproc.line(dst,);
        }

    }

    public List<Pair<RotatedRect,MatOfPoint>> ordernaEsquinas(List<Pair<RotatedRect,MatOfPoint>> listaContornos){

        //Todos los contornos que forman parte de los rotatedrect, cada mat of point es un contorno
        List<MatOfPoint> puntosContorno = new ArrayList<MatOfPoint>();
        //Los 4 objetos rotated red
        List<RotatedRect> rectContorno = new ArrayList<RotatedRect>();

        //Sacamos de la lista los contornos(MatofPoint) y los RotatedRect
        for (Pair<RotatedRect,MatOfPoint> pair: listaContornos) {
            rectContorno.add(pair.getLeft());
            puntosContorno.add(pair.getRight());
        }

        Point centerRect = new Point();

        //Obtener el punto central del contorno
        for (int j = 0; j < rectContorno.size(); j++)
        {

            centerRect.x += rectContorno.get(j).center.x;
            centerRect.y += rectContorno.get(j).center.y;
        }
        double inv = 1.0 / rectContorno.size();
        centerRect.x = centerRect.x * inv;
        centerRect.y = centerRect.y * inv;



        Vector<Point> top = new Vector<Point>();
        Vector<Point> bot = new Vector<Point>();

        //puntos cogidos de la parte izquierda, getLeft
        MatOfPoint2f puntosCentrales2f = new MatOfPoint2f(listaContornos.get(0).getLeft().center,listaContornos.get(1).getLeft().center,
                listaContornos.get(2).getLeft().center,listaContornos.get(3).getLeft().center);

        List<Point> listaPuntos = puntosCentrales2f.toList();
        List<Pair<RotatedRect,MatOfPoint>> result = new ArrayList<Pair<RotatedRect,MatOfPoint>>();;



        List<Pair<RotatedRect,MatOfPoint>> resultTop = new ArrayList<Pair<RotatedRect,MatOfPoint>>();
        List<Pair<RotatedRect,MatOfPoint>> resultBot = new ArrayList<Pair<RotatedRect,MatOfPoint>>();

        for (int i = 0; i < listaPuntos.size(); i++)
        {
            if (listaPuntos.get(i).y < centerRect.y)
                resultTop.add(listaContornos.get(i));
            else
                resultBot.add(listaContornos.get(i));

        }

        //Comprueba que en la lista de vectores de top hay 2 puntos y en la lista de bot hay 2
        if (resultTop.size() == 2 && resultBot.size() == 2){
            //corners.release();

            Pair<RotatedRect,MatOfPoint> tl =
                    resultTop.get(0).getLeft().center.x > resultTop.get(1).getLeft().center.x ? resultTop.get(1) : resultTop.get(0);
            Pair<RotatedRect,MatOfPoint> tr =
                    resultTop.get(0).getLeft().center.x > resultTop.get(1).getLeft().center.x ? resultTop.get(0) : resultTop.get(1);

            Pair<RotatedRect,MatOfPoint> bl =
                    resultBot.get(0).getLeft().center.x > resultBot.get(1).getLeft().center.x ? resultBot.get(1) : resultBot.get(0);
            Pair<RotatedRect,MatOfPoint> br =
                    resultBot.get(0).getLeft().center.x > resultBot.get(1).getLeft().center.x ? resultBot.get(0) : resultBot.get(1);


            result.add(0,tl);
            result.add(1,tr);
            result.add(2,br);
            result.add(3,bl);
            /*corners.push_back(tl);
            corners.push_back(tr);
            corners.push_back(br);
            corners.push_back(bl);*/

        }

        return result;

    }

    public MatOfPoint2f ordernaEsquinas(MatOfPoint2f corners,Point centro){

        Vector<Point> top = new Vector<Point>();
        Vector<Point> bot = new Vector<Point>();
        List<Point> listaPuntos = corners.toList();
        MatOfPoint2f result = new MatOfPoint2f();
        for (int i = 0; i < corners.size().height; i++)
        {
            if (listaPuntos.get(i).y < centro.y)
                top.add(listaPuntos.get(i));
            else
                bot.add(listaPuntos.get(i));

        }

        //Comprueba que en la lista de vectores de top hay 2 puntos y en la lista de bot hay 2
        if (top.size() == 2 && bot.size() == 2){
            //corners.release();

            Point tl = top.get(0).x > top.get(1).x ? top.get(1) : top.get(0);
            Point tr = top.get(0).x > top.get(1).x ? top.get(0) : top.get(1);
            Point bl = bot.get(0).x > bot.get(1).x ? bot.get(1) : bot.get(0);
            Point br = bot.get(0).x > bot.get(1).x ? bot.get(0) : bot.get(1);
            result = new MatOfPoint2f(tl,tr,br,bl);

        }
        return result;

    }

    public String devolverColorRGB(Mat imagenEntrada, List<Point> pp){

        String r = "| ";

        double maximoBlanco = 0.0;

        //Suponemos que siempre es correcto, es decir, el primer punto será el blanco.
        Point puntoBlanco = new Point();

        for (int k=0; k<pp.size();k++) {

            double[] pixel = imagenEntrada.get((int) pp.get(k).y, (int) pp.get(k).x);
            double pixelSuma = pixel[0]+pixel[1]+pixel[2];

            if(pixelSuma>maximoBlanco){
                //actualizamos el máximo
                maximoBlanco=pixelSuma;
                puntoBlanco = pp.get(k);
            }


        }




        //Creamos una lista con todos los puntos menos el blanco
        List<Point> resultado = new ArrayList<Point>();
        List<Point> aux = new ArrayList<>();
        boolean encontrado = false;

        for (int j=0; j<pp.size();j++)  {

            //diferenciar entre antes del blanco y despues del blanco

            if(pp.get(j).equals(puntoBlanco)){
                encontrado = true;
            }

            if(!encontrado)
                aux.add(pp.get(j));
            else
                resultado.add(pp.get(j));

        }
        if(!aux.isEmpty())
            resultado.addAll(aux);


        //En este punto tenemos el puntoBlanco

        r += "BLANCO | ";

        double[] pixelBlanco = imagenEntrada.get((int) puntoBlanco.y, (int) puntoBlanco.x);


        //Para los demás puntos, tendremos 3 puntos como mucho
        for (int i=0; i<resultado.size();i++) {

            double[] pixel = imagenEntrada.get((int) resultado.get(i).y, (int) resultado.get(i).x);
            double balanceRojo = (pixel[0] / pixelBlanco[0]) * 255;
            double balanceVerde = (pixel[1] / pixelBlanco[1]) * 255;
            double balanceAzul = (pixel[2] / pixelBlanco[2]) * 255;


            //Encontrar el maximo de esos 3 y comparar con el adecuado

            if(balanceRojo>balanceVerde && balanceRojo>balanceAzul){

                //comprobar rojo
                r+="ROJO | ";
                //continue;

            }

            if(balanceVerde>balanceRojo && balanceVerde>balanceAzul){
                //comprobar verde

                r+="VERDE |";

            }

            if(balanceAzul>balanceRojo && balanceAzul>balanceVerde){
                //comprobar azul
                r+="AZUL |";

            }



            //Log.println(Log.ERROR,"Color ",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));


        }



        return r;

    }

    public String devolverColor(Mat imagenEntrada, MatOfPoint p){

        Mat crop = new Mat(imagenEntrada,Imgproc.boundingRect(p));
        Mat mask = Mat.zeros(crop.rows(),crop.cols(),CvType.CV_8UC1);

        //Imgproc.cvtColor(crop,crop,Imgproc.COLOR_RGB2Lab);


        Point topLeft = Imgproc.boundingRect(p).tl();
        Point minusTopLeft = new Point(-topLeft.x,-topLeft.y);

        List<MatOfPoint> lista = new ArrayList<MatOfPoint>();
        lista.add(p);

        Mat dest = new Mat();

        Imgproc.drawContours(mask,lista,-1,new Scalar(255),Core.FILLED,Imgproc.LINE_AA,dest,1,minusTopLeft);

        Scalar media = Core.mean(crop,mask);




        Log.println(Log.ERROR,"Devolviendo media",String.valueOf(media));



        double[] pixel = media.val;//imagenEntrada.get((int) p.y, (int) p.x);

        //r = pixel[0]
        //g = pixel[1]
        //b = pixel[2]

        List<Double> listaDiferencias = new ArrayList<Double>();


        //Colores en escalar BGR
        int rojo = ColorUtil.argb(255,0,0);
        int verde = ColorUtil.argb(0,255,0);
        int azul = ColorUtil.argb(0,0,255);
        int blanco = ColorUtil.argb(255,255,255);

        int colorMedio = ColorUtil.argb((int)pixel[0],(int)pixel[1],(int)pixel[2],(int)pixel[3]);




        double diffRojo = 0.0;
        double diffVerde = 0.0;
        double diffAzul = 0.0;
        double diffBlanco = 0.0;




        diffRojo = ColorUtil.getColorDifference(media,rojo);
        diffVerde = ColorUtil.getColorDifference(media,verde);
        diffAzul = ColorUtil.getColorDifference(media, azul);
        diffBlanco = ColorUtil.getColorDifference(media,blanco);

        listaDiferencias.add(diffRojo);
        listaDiferencias.add(diffVerde);
        listaDiferencias.add(diffAzul);
        listaDiferencias.add(diffBlanco);

        double minDistancia = Double.MAX_VALUE ;

        for (double d: listaDiferencias) {
            if(minDistancia>d)
                minDistancia=d;
        }

        if (minDistancia == diffBlanco){
            Log.println(Log.ERROR,"Devolviendo blanco",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
            return "BLANCO";
        }

        if(minDistancia == diffRojo){
            Log.println(Log.ERROR,"Devolviendo color rojo",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
            return "ROJO";
        }
        if(minDistancia == diffVerde){
            Log.println(Log.ERROR,"Devolviendo color verde",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
            return "VERDE";
        }

        if(minDistancia == diffAzul){
            Log.println(Log.ERROR,"Devolviendo color azul",String.valueOf(pixel[2]));
            return "AZUL";
        }




        Log.println(Log.ERROR,"Devolviendo sin color",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));
        return "SIN COLOR";


    }

    public String ordenarColoresRect(Mat imagen, List<RotatedRect> vector){


        String resultado = " | ";

        List<Point> puntosContornos = new ArrayList<Point>();


        for(int i=0;i<vector.size();i++){

            puntosContornos.add(vector.get(i).center);

            //resultado+=color+" | ";

        }

        String color = devolverColorRGB(imagen,puntosContornos);

        return color;
    }



    public String ordenarColores(Mat imagen, List<MatOfPoint> vector){

        String resultado = " | ";

        for(int i=0;i<vector.size();i++){
            //Core.mean(vector.get(i));
            String color = devolverColor(imagen,vector.get(i));
            resultado+=color+" | ";

        }

        return resultado;


    }




    public Mat procesaGris(Mat entrada){
        List<Mat> lista = new ArrayList<Mat>(4) ;
        Core.split(entrada,lista);
        Mat maximo = new Mat();

        //lista.get(0).copyTo(maximo);
        Core.max(lista.get(0),lista.get(1),maximo);
        Core.max(maximo,lista.get(2),maximo);
        //for(int i=1;i<lista.size()-1;i++)
        for(int umbral=500;umbral>200;umbral-=100){
            Imgproc.Canny(maximo,bordes,200,umbral,3,true);
            if(Core.norm(bordes, Core.NORM_L1)/255>400)
                break;
        }

        return bordes;
    }

    public Mat procesarImagen(Mat entrada,boolean modoAlternativo) {




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

        //en este metodo obtenemos los contornos candidatos y los guardamos en listaContornos
        optimizarContornos(maximo, contornos, listaContornos);

        //SI TENEMOS MÁS DE 4 CANDIDATOS DEBEMOS REDUCIR EL NÚMERO
        if(!listaContornos.isEmpty() && listaContornos.size()>4){
            //reducimos a los candidatos circulares
            //debemos reducir el número a los 4 candidatos más grandes
            reducirCandidatos(listaContornos);
        }

        //SI TENEMOS 4 CANDIDATOS PASAMOS A ORDENARLOS
        if (!listaContornos.isEmpty() && listaContornos.size()==4){


            //Todos los contornos que forman parte de los rotatedrect, cada mat of point es un contorno
            List<MatOfPoint> puntosContorno = new ArrayList<MatOfPoint>();
            //Los 4 objetos rotated red
            List<RotatedRect> rectContorno = new ArrayList<RotatedRect>();


            listaContornos = ordernaEsquinas(listaContornos);


            //Sacamos de la lista los contornos(MatofPoint) y los RotatedRect ya ordenados
            for (Pair<RotatedRect,MatOfPoint> pair: listaContornos) {
                rectContorno.add(pair.getLeft());
                puntosContorno.add(pair.getRight());
            }


            String res ="";

            //modo false = contorno y espacio de color LAB
            //modo true = punto del centro y el blanco primero

            if(!modoAlternativo){
                res = ordenarColoresRect(entrada,rectContorno);
            }
            else{
                res = ordenarColores(entrada,puntosContorno);

            }



            if(!rectContorno.isEmpty() && rectContorno.size()==4){

                //Extraemos los puntos centrales de los contornos y comprobamos si son convexos
                MatOfPoint2f puntosCentrales2f = new MatOfPoint2f(rectContorno.get(0).center,rectContorno.get(1).center,
                        rectContorno.get(2).center,rectContorno.get(3).center);

                MatOfPoint puntosCentrales = new MatOfPoint(rectContorno.get(0).center,rectContorno.get(1).center,
                        rectContorno.get(2).center,rectContorno.get(3).center);


                //Si forman un contorno convexo
                if(Imgproc.isContourConvex(puntosCentrales)) {


                    MatOfDouble rvec = new MatOfDouble(3,1,CvType.CV_64FC1);
                    MatOfDouble tvec = new MatOfDouble();
                    MatOfDouble mRotation = new MatOfDouble();


                    double fontScale = 200;
                    Scalar color = new Scalar(fontScale);
                    Point p = new Point(200, 200);


                    //EN ESTE PUNTO TENEMOS QUE GUARDAR EL FRAME
                    /*if(!primerFrame){
                        Mat prevImg = entrada;
                    }*/


                    //MatOfByte status = new MatOfByte();
                   // MatOfFloat err = new MatOfFloat();
                    //TODO GUARDAR PUNTOS CENTRALES2F

                    //TODO TENEMOS LOS PUNTOSCENTRALES2F, QUE SON LOS PUNTOS DEL FRAME Y TENEMOS QUE CALCULAR LOS SIGUIENTES
                    //TODO IDEA, DESPUES DE ESTO GUARDAR LOS PUNTOSCENTRALES EN VARIABLE GLOBAL PARA COMPROBAR AL PRINCIPIO DEL METODO
                    //Video.calcOpticalFlowPyrLK(prevImg, entrada, puntosCentrales2f, nextPts, status, err);
                    //trackingPoints(prevImg,entrada,puntosCentrales2f,prevPts);
                    findPose(entrada,puntosCentrales2f,rvec,tvec,mRotation);

                    //En este punto tenemos que dibujar los contornos.
                    for (int k = 0; k < puntosContorno.size(); k++)
                        Imgproc.drawContours(entrada, puntosContorno, k, new Scalar(255, 0, 0), 1);
                    for (int j = 0; j < rectContorno.size(); ++j) {
                        Imgproc.line(entrada, rectContorno.get(j).center, rectContorno.get((j + 1) % rectContorno.size()).center, new Scalar(255, 255, 255));
                    }
                    Imgproc.putText(entrada, res, p, Core.FONT_HERSHEY_PLAIN, 1, color, 1);


                }

            }

        }

        return entrada;


    }

    /*private void trackingPoints(Mat prevFrame,Mat actFrame,MatOfPoint2f puntos,MatOfPoint2f ptsAnteriores){
        *//*Mat prev=new Mat(actFrame.size(),CvType.CV_8UC1);
        Mat curr=new Mat(actFrame.size(),CvType.CV_8UC1);*//*
        //MatOfPoint2f tmpCorners =new MatOfPoint2f();
        MatOfFloat err=new MatOfFloat();
        MatOfByte status = new MatOfByte();
        int x,j,count;
        TermCriteria optical_flow_termination_criteria=new TermCriteria();//=(TermCriteria.MAX_ITER|TermCriteria.EPS,20,.3);//  ( CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, .3 );
        optical_flow_termination_criteria.epsilon=.3;
        optical_flow_termination_criteria.maxCount =20;

        if(prevPts.rows() == 0){
            //primeraVez que entramos

            Imgproc.cvtColor(actFrame, currImg, Imgproc.COLOR_RGBA2GRAY);

            //guardamos los puntos anteriores
            prevPts.fromArray(puntos.toArray());
            prevPts.copyTo(safePts);
        }
        else{
            //ya hemos pasado por ptsAnteriores
            //copiamos la imagenactual a la anterior
            currImg.copyTo(prevImg);

            Imgproc.cvtColor(actFrame, currImg, Imgproc.COLOR_RGBA2GRAY);

            //cogemos los puntos anteriores para usarlos
            safePts.copyTo(prevPts);

            //los puntos actuales nos serviran para la siguiente iteracion
            nextPts.copyTo(prevPts);

            //en ptsAnteriores tenemos los puntos

        }


        //if(puntos.total()>0)
        Video.calcOpticalFlowPyrLK(prevImg, currImg, prevPts, nextPts, status, err);

        List<Point> cornersPrev = prevPts.toList();
        List<Point> cornersThis = nextPts.toList();
        List<Byte> byteStatus = status.toList();
        Point pt,pt2;

        for (x = 0; x < byteStatus.size() - 1; x++) {
            if (byteStatus.get(x) == 1) {
                pt = cornersThis.get(x);
                pt2 = cornersPrev.get(x);

                Imgproc.circle(actFrame, pt, 5, new Scalar(255,0,0), 2);
                Imgproc.line(actFrame, pt, pt2, new Scalar(255,0,0), 1);
            }
        }
        *//*MatOfPoint2f corners2f=new MatOfPoint2f();
        //hp.corners.convertTo(corners2f, CvType.CV_32FC2);
        List<Point> plist = new ArrayList<Point>();
        for (i = 0,j=0; i < 4; i += 1)
        {
            if(i==hp.corners.total())
                break;
            if (status.toList().get(i) == 1)
            {
                corners2f.toList().set(j, tmpCorners.toList().get(i));// .get(i)=tmpCorners.toList().get(i);
                nextPts.toList().set(j, nextPts.toList().get(i));
                j+=1;
            }
        }*//*
    }*/

    private boolean findPose(Mat entrada, MatOfPoint2f puntosCentrales, MatOfDouble rvec, MatOfDouble tvec,MatOfDouble mRotation){
       /* final List<Point3> goodReferencePointsList =
                new ArrayList<Point3>();

        final ArrayList<Point> goodScenePointsList =
                new ArrayList<Point>();*/

        //marcadores son los puntos de referencia


        CameraProjectionAdapter mCameraProjectionAdapter = new CameraProjectionAdapter();


        //todo añadir puntos

        /*final MatOfPoint3f goodReferencePoints = new MatOfPoint3f();
        goodReferencePoints.fromList(goodReferencePointsList);

        final MatOfPoint2f goodScenePoints = new MatOfPoint2f();
        goodScenePoints.fromList(goodScenePointsList);*/

        MatOfDouble projection =
                mCameraProjectionAdapter.getProjectionCV();

        if(Calib3d.solvePnP(marcadores,puntosCentrales,projection,calibracion,rvec,tvec)==false)
            return false;



        double[] rVecArray = rvec.toArray();
        rVecArray[1] *= -1.0;
        rVecArray[2] *= -1.0;
        rvec.fromArray(rVecArray);

        Calib3d.Rodrigues(rvec,mRotation);

        MatOfDouble RT = new MatOfDouble(1,3,CvType.CV_64FC1);
        Core.transpose(mRotation,RT);

        double[] tVecArray = tvec.toArray();

       /* mGLPose[0] = (float)mRotation.get(0, 0)[0];
        mGLPose[1] = (float)mRotation.get(1, 0)[0];
        mGLPose[2] = (float)mRotation.get(2, 0)[0];
        mGLPose[3] = 0f;
        mGLPose[4] = (float)mRotation.get(0, 1)[0];
        mGLPose[5] = (float)mRotation.get(1, 1)[0];
        mGLPose[6] = (float)mRotation.get(2, 1)[0];
        mGLPose[7] = 0f;
        mGLPose[8] = (float)mRotation.get(0, 2)[0];
        mGLPose[9] = (float)mRotation.get(1, 2)[0];
        mGLPose[10] = (float)mRotation.get(2, 2)[0];
        mGLPose[11] = 0f;
        mGLPose[12] = (float)tVecArray[0];
        mGLPose[13] = (float)tVecArray[1];
        mGLPose[14] = (float)tVecArray[2];
        mGLPose[15] = 1f;*/

        mGLPose[0] = (float)mRotation.get(0, 0)[0];
        mGLPose[1] = (float)mRotation.get(1, 0)[0];
        mGLPose[2] = (float)mRotation.get(2, 0)[0];
        mGLPose[3] = (float)mRotation.get(0, 1)[0];
        mGLPose[4] = (float)mRotation.get(1, 1)[0];
        mGLPose[5] = (float)mRotation.get(2, 1)[0];
        mGLPose[6] = (float)mRotation.get(0, 2)[0];
        mGLPose[7] = (float)mRotation.get(1, 2)[0];
        mGLPose[8] = (float)mRotation.get(2, 2)[0];
        mGLPose[9] = (float)tVecArray[0];
        mGLPose[10] = (float)tVecArray[1];
        mGLPose[11] = (float)tVecArray[2];


        //Mat C (Mat_<double>(3, 1));

        MatOfDouble C = new MatOfDouble(3,1,CvType.CV_64FC1);

//multiplica RT con Tvec y lo guarda en C
        Core.gemm(RT,tvec,1,Mat.zeros(3,1,CvType.CV_64FC1),0,C);






        //escalado = escalado * rot1(30.0) ;
        //M = K*Rt*rot1*escalado;

        //añadir a Rt los valores de RT y los de Tvec

        Mat M = new Mat(3,4,CvType.CV_32FC1);

        //Mat Rt = new Mat(3,4,CvType.CV_32FC4);

        MatOfFloat Rt = new MatOfFloat();
        Rt.fromArray(mGLPose);


        /*for(int i=0; i<3;i++){
            double[] data = {0.0,0.0,0.0,0.0};

        }*/
       /* int npixels = (int)RT.total() * (int)RT.elemSize();
        double[] pixels = new double[npixels];
        RT.get(0,0,pixels);

        Rt.put(0,0,pixels);


        /*double[] data = {0.0,0.0,0.0,0.0};
        for(int k=0;k<tVecArray.length;k++){
            data[k]=tVecArray[k];
        }*/

        //Rt.put(3,0,tVecArray);

        Mat resultado = Rt.reshape(1,3);
        //Imgproc.cvtColor(resultado,resultado,CVType.);

        //Core.multiply(K,resultado,M);


       Core.gemm(K,resultado,1,Mat.zeros(3,1,CvType.CV_32FC1),0,M);

        Mat aux = new Mat(9,4,CvType.CV_32FC3);

        //Matriz de 9/3 a 9/4
        //double[] l1 = new ArrayList<float>();

        /*for(int i=0;i<p3d.size().height;i++)
            for(int j=0;j<p3d.size().width;j++){
                double[] a = p3d.get(i,j);
                Point3 p = new Point3(a);
                Log.e("P3D","i j 0 1 |"+i+" " +j+" "+String.valueOf(a[0])+" "+String.valueOf(a[1])+" ");
            }*/

        //TODO HASTA AQUI TODO BIEN, LUEGO SE VUELVEN LOS PUNTOS MUY PEQUEÑOS

        Calib3d.convertPointsToHomogeneous(p3d3,aux);
        //reshape
        Mat objHomo = aux.reshape(1);

        int size = (int) (objHomo.total() * objHomo.channels());
        float[] temp = new float[size];
        objHomo.get(0,0,temp);

        //Core.multiply(objHomo,M.t(),objHomo);
        //objHomo.mul(M.t());
        Core.gemm(objHomo,M.t(),1,Mat.zeros(3,1,CvType.CV_64FC1),0,objHomo);


        Calib3d.convertPointsFromHomogeneous(objHomo,aux);
        Mat GLpose = aux.reshape(1);

        //Mat GLPose = new Mat();

        drawCube(entrada,GLpose);
        return true;

    }

    /**
     * Metodo reducir candidatos. Si tenemos más de 4 contornos en la lista debemos de quedarnos con los más grandes
     * @param listaContornos Lista de pares RotatedRect-Contorno.
     */
    private void reducirCandidatos(List<Pair<RotatedRect, MatOfPoint>> listaContornos) {
        Iterator<Pair<RotatedRect,MatOfPoint>> it = listaContornos.iterator();

        double tam0 = 0.0;
        double tam1 = 0.0;
        double tam2 = 0.0;
        double tam3 = 0.0;


        Log.println(Log.ERROR,"tamano lista",String.valueOf(listaContornos.size()));
        while (it.hasNext()){
            Log.println(Log.ERROR,"tamano lista",String.valueOf(listaContornos.size()));
            Pair<RotatedRect,MatOfPoint> v = it.next();
            if(v.getLeft().angle>10 && v.getLeft().angle<165){
               // it.remove();
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

    /**
     *
     * @param maximo Matriz de entrada que contiene el máximo de los canales RGB de la imagen
     * @param contornos Lista de contornos obtenidos con el método findContours
     * @param listaContornos Lista de pares RotatedRect-Contorno que obtendremos como resultado
     */
    private void optimizarContornos(Mat maximo, List<MatOfPoint> contornos, List<Pair<RotatedRect, MatOfPoint>> listaContornos) {

        //Vector de candidatos
        Vector<RotatedRect> candidatos = new Vector<>();

        for (int c=0; c<contornos.size();c++){

            boolean anadido = false;

            //Log.println(Log.ERROR,"DEBUG contornos:,String.valueOf(contornos.get(c).size().height));
            double tamanoContorno = contornos.get(c).size().height;

            //test de tamaño entre 12 y º10 pixeles
            if((tamanoContorno>=12.0) && (tamanoContorno<=120)){

                //test de area, comprobar area minima
                 double area = Imgproc.contourArea(contornos.get(c));

                if((area>=20.0) && (area<=1100)){

                    MatOfPoint2f temp=new MatOfPoint2f();
                    temp.fromList(contornos.get(c).toList());
                    RotatedRect elip =Imgproc.fitEllipse(temp);

                    double areaTeorica= 3.1416*elip.size.width*elip.size.height/4.0;
                    double ratioForma= (area+4)/areaTeorica;


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



                            double[] pixelInt = maximo.get((int) ptInt.y, (int) ptInt.x);
                            double[] pixelExt = maximo.get((int) ptExt.y, (int) ptExt.x);

                            if(pixelInt!=null && pixelExt != null) {
                                cuentaClaros += (pixelInt[0] - 20 > pixelExt[0]) ? 1 : 0;
                                totalPuntos++;
                            }
                            //Log.println(Log.ERROR,"PtInt, x|y",String.valueOf(ptInt.x)+" | "+String.valueOf(ptInt.y));

                        }
                        Log.println(Log.ERROR,"cuentaClaros, total",String.valueOf(cuentaClaros)+" | "+String.valueOf(totalPuntos));
                        if(cuentaClaros/totalPuntos> 0.95) {

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


                                    anadido= true;
                                    //Imgproc.drawContours(entrada, contornos, c, new Scalar(255,0,0), 1);
                                    Log.println(Log.ERROR,"C añadido | total cands",String.valueOf(c) + " | "+ String.valueOf(candidatos.size()));



                            }


                        }
                    }


                }

            }


        }
    }

    public void barrelDistortion(Mat entrada, Point centro, Point centroI, Point centroD, double d1, double d2){




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


        //matricespara realizar el mapeo,
        mapaX = new Mat(rows, cols, CvType.CV_32FC1 );
        mapaY = new Mat(rows, cols, CvType.CV_32FC1 );

        for( int y = 0; y < rows; y++ ) {
            for( int x = 0; x < cols; x++ ) {

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
