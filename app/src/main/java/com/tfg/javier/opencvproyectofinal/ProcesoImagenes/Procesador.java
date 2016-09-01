package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

import android.util.Log;

import com.tfg.javier.opencvproyectofinal.CameraActivity;
import com.tfg.javier.opencvproyectofinal.adapters.CameraProjectionAdapter;
import com.tfg.javier.opencvproyectofinal.filtros.ar.ARCubeRenderer;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private MatOfPoint3f p3d3;
    private MatOfPoint3f p3d3Grande;

    private Mat K;

    private MatOfDouble calibracion;

    private float[] mGLPose;
    private float[] mGLPose1;
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
        mGLPose1 = new float[16];



        /*prevImg = new Mat();
        currImg = new Mat();
        primerFrame = true;
        nextPts = new MatOfPoint2f();
        prevPts = new MatOfPoint2f();
        safePts = new MatOfPoint2f();*/

        //MARCADORES ES EL TAMAÑO DEL MARCADOR QUE QUEREMOS RECONOCES

        marcadores = new MatOfPoint3f(new Point3(0,0,0),new Point3(1,0,0),new Point3(1,1,0),new Point3(0,1,0));
        int r = 0;
        int c = 0;
        //marcadores.put(r,c,0,0,0,20,0,0,0,20,0,20,20,0);

        //P3D ES LO QUE QUEREMOS DIBUJAR

        p3d = new Mat(9,3,CvType.CV_32FC3);

        p3d.put(r,c,1,1,5,1,2,5,2,2,5,2,1,5,1,1,6,1,2,6,2,2,6,2,1,6,2.5,1.5,5.5);

        p3d3 = new MatOfPoint3f(new Point3(1,1,5),
                new Point3(1,2,5),
                new Point3(2,2,5),
                new Point3(2,1,5),
                new Point3(1,1,6),
                new Point3(1,2,6),
                new Point3(2,2,6),
                new Point3(2,1,6),
                new Point3(2.5,1.5,5.5));

        p3d3Grande = new MatOfPoint3f(new Point3(10,10,50),
                new Point3(10,20,50),
                new Point3(20,20,50),
                new Point3(20,10,50),
                new Point3(10,10,60),
                new Point3(10,20,60),
                new Point3(20,20,60),
                new Point3(20,10,60),
                new Point3(25,15,55));


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

    void drawCube(Mat dst, List<Point> object){
        int [] array = {1,2,3,4,1,5,6,7,8,5,6,2,6,7,3,7,8,4,9,3,7,9,8};

        for(int i = 1; i<array.length-1; ++i){

            Point p1 = object.get(array[i-1]-1);
            Point p2 = object.get(array[i]-1);

            if(p1.x < 0)
                p1.x = -1 * p1.x;
            if(p2.x < 0 )
                p2.x = -1 * p2.x;

            Imgproc.line(dst,p1,p2,new Scalar(0,255,0));

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

        //Obtener el punto central del rectangulo que envuelve a los puntos centrales
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

        //puntos cogidos de la parte izquierda, getLeft, el rectangulo
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

        }

        return result;

    }

   /* public MatOfPoint2f ordernaEsquinas(MatOfPoint2f corners,Point centro){

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

    }*/

    public String devolverColorRGB(Mat imagenEntrada, List<Point> pp){

        String r = " | ";

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

                r+="VERDE | ";

            }

            if(balanceAzul>balanceRojo && balanceAzul>balanceVerde){
                //comprobar azul
                r+="AZUL | ";

            }



            //Log.println(Log.ERROR,"Color ",String.valueOf(pixel[0])+" "+String.valueOf(pixel[1])+" "+String.valueOf(pixel[2]));


        }



        return r;

    }

    public String devolverColorLab(Mat imagenEntrada, MatOfPoint p){

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

    public String ordenarColoresRGB(Mat imagen, List<RotatedRect> vector){


        String resultado = " | ";

        List<Point> puntosContornos = new ArrayList<Point>();


        for(int i=0;i<vector.size();i++){

            puntosContornos.add(vector.get(i).center);

            //resultado+=color+" | ";

        }

        String color = devolverColorRGB(imagen,puntosContornos);

        return color;
    }



    public String ordenarColoresLab(Mat imagen, List<MatOfPoint> vector){

        String resultado = " | ";

        for(int i=0;i<vector.size();i++){
            //Core.mean(vector.get(i));
            String color = devolverColorLab(imagen,vector.get(i));
            resultado+=color+" | ";

        }

        return resultado;


    }

    private String obtenerCodigoPatron(String resultado){

        for(int i=0; i<4; i++){

            resultado = resultado.replace("BLANCO","0");

            resultado =  resultado.replace("ROJO","1");

            resultado = resultado.replace("VERDE","2");

            resultado = resultado.replace("AZUL","3");

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

    public Mat procesarImagen(Mat entrada,boolean modoAlternativo, Mat image) {




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
                res = ordenarColoresRGB(entrada,rectContorno);
            }
            else{
                res = ordenarColoresLab(entrada,puntosContorno);

            }


            res = obtenerCodigoPatron(res);

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
                    Point puntoCentro = new Point(entrada.cols()/2,entrada.rows()/2);











                    findPose3(entrada,puntosCentrales2f,image);

                    //Mat H = findPose2(puntosCentrales2f,rvec,tvec);
                    //renderer.GLpose = pos;
                    if(res.equals(" | 0 | 3 | 2 | 1 | ")){
                        List<Point> pos = findPose2(entrada,puntosCentrales2f,rvec,tvec,mRotation,res);
                        dibujarCubo(entrada, pos);
                    }


                    //En este punto tenemos que dibujar los contornos.
                    for (int k = 0; k < puntosContorno.size(); k++)
                        Imgproc.drawContours(entrada, puntosContorno, k, new Scalar(255, 0, 0), 1);
                    for (int j = 0; j < rectContorno.size(); ++j) {
                        Imgproc.line(entrada, rectContorno.get(j).center, rectContorno.get((j + 1) % rectContorno.size()).center, new Scalar(255, 255, 255));
                    }
                    Imgproc.putText(entrada, res, puntoCentro, Core.FONT_HERSHEY_PLAIN, 1, color, 1);


                    //Imgproc.warpPerspective(entrada,entrada,H,entrada.size());
                }

            }

        }
        /*if(glPose!=null)
            Log.e("Valor de glpose 0 :",String.valueOf(glPose[0])) ;*/
        return entrada;


    }

    private void dibujarCubo(Mat entrada, List<Point> pos) {
        int [] array = {1,2,3,4,1,5,6,7,8,5,6,2,6,7,3,7,8,4,9,3,7,9,8};

        for(int i = 1; i<array.length-1; ++i){

            Point p1 = pos.get(array[i-1]-1);
            Point p2 = pos.get(array[i]-1);
/*
            Imgproc.line(entrada,p1,p2,new Scalar(0,255,0));*/

            /*Point p1 = pos.get(array[i]-1);
            Point p2 = pos.get((array[i+1] % array.length)-1);*/

            Imgproc.line(entrada,p1,p2,new Scalar(0,255,0));

        }
    }

    private Mat calibrarCamara(MatOfPoint2f puntos){

        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();

        //Calib3d.calibrate()
        return null;
    }

    private void findPose3(Mat entrada, MatOfPoint2f puntosCentrales,  Mat image){


        int imageWidth = image.width();
        int imageHeight = image.height();

        List<Point> lista = puntosCentrales.toList();
        Range colRange = new Range((int)lista.get(0).x,(int)lista.get(1).x);
        Range rowRange = new Range((int)lista.get(1).y,(int)lista.get(2).y);

        //Creamos una submatriz a partir de los puntos de los marcadores
        Mat submatEntrada = entrada.submat(rowRange,colRange);

        //Imgproc.resize(submatEntrada,submatEntrada,image.size());

        //Redimensionamos la imagen al tamaño de la submatriz
       // Imgproc.resize(image,image,submatEntrada.size());

        //Calculamos la homografia con los puntos
        MatOfPoint2f listaM = new MatOfPoint2f(new Point(0,0),new Point(imageWidth,0),new Point(imageWidth,imageHeight),new Point(0,imageHeight));



        Mat C = Imgproc.getPerspectiveTransform(listaM,puntosCentrales);

        //Mat H = Calib3d.findHomography(listaM,puntosCentrales);



        //Llamamos a la función warpPerspective con la imagen y la homografia, guardamos resultado en temp
        Mat temp = new Mat();
        //Imgproc.warpPerspective(image,entrada,C,entrada.size());

        Imgproc.warpPerspective(image,temp,C,entrada.size(),Imgproc.INTER_LINEAR,Core.BORDER_TRANSPARENT,new Scalar(255,0,0));


       /* Mat mask = new Mat( new Size( entrada.cols(), entrada.rows() ), CvType.CV_8UC1 );
        mask.setTo( new Scalar( 0.0 ) );*/

        temp.copyTo(entrada/*,mask*/);







    }

    private List<Point> findPose2(Mat entrada, MatOfPoint2f puntosCentrales,MatOfDouble rvec, MatOfDouble tvec, MatOfDouble mRotation,
                                  String patron){

        CameraProjectionAdapter mCameraProjectionAdapter = new CameraProjectionAdapter();

        MatOfDouble projection =
                mCameraProjectionAdapter.getProjectionCV();

        MatOfPoint2f listaM = new MatOfPoint2f(new Point(0,0),new Point(1,0),new Point(1,1),new Point(0,1));


        //Mat H = Calib3d.findHomography(listaM,puntosCentrales);
        if(!Calib3d.solvePnP(marcadores,puntosCentrales,projection,calibracion,rvec,tvec))
            return null;

        MatOfPoint2f output = new MatOfPoint2f();


        //Calib3d.projectPoints(p3d3,rvec,tvec,projection,calibracion,output);

        if (patron.equals(" | 0 | 1 | 2 | 3 | "))
            Calib3d.projectPoints(p3d3,rvec,tvec,projection,calibracion,output);
        else if (patron.equals(" | 0 | 3 | 2 | 1 | "))
            Calib3d.projectPoints(p3d3Grande,rvec,tvec,projection,calibracion,output);

        List<Point> listaOut = output.toList();


        return listaOut;


    }


    private List<Point> findPose(Mat entrada, MatOfPoint2f puntosCentrales, MatOfDouble rvec, MatOfDouble tvec,MatOfDouble mRotation,
                                 String patron){
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


        if(!Calib3d.solvePnP(marcadores,puntosCentrales,projection,calibracion,rvec,tvec))
            return null;



        double[] rVecArray = rvec.toArray();
        MatOfDouble rvec2 = new MatOfDouble();

        rVecArray[1] *= -1.0;
        rVecArray[2] *= -1.0;
        rvec2.fromArray(rVecArray);

        Calib3d.Rodrigues(rvec2,mRotation);

        MatOfDouble RT = new MatOfDouble(1,3,CvType.CV_64FC1);
        Core.transpose(mRotation,RT);

        double[] tVecArray = tvec.toArray();

        mGLPose1[0] = (float)mRotation.get(0, 0)[0];
        mGLPose1[1] = (float)mRotation.get(1, 0)[0];
        mGLPose1[2] = (float)mRotation.get(2, 0)[0];
        mGLPose1[3] = 0f;
        mGLPose1[4] = (float)mRotation.get(0, 1)[0];
        mGLPose1[5] = (float)mRotation.get(1, 1)[0];
        mGLPose1[6] = (float)mRotation.get(2, 1)[0];
        mGLPose1[7] = 0f;
        mGLPose1[8] = (float)mRotation.get(0, 2)[0];
        mGLPose1[9] = (float)mRotation.get(1, 2)[0];
        mGLPose1[10] = (float)mRotation.get(2, 2)[0];
        mGLPose1[11] = 0f;
        mGLPose1[12] = (float)tVecArray[0];
        mGLPose1[13] = (float)tVecArray[1];
        mGLPose1[14] = (float)tVecArray[2];
        mGLPose1[15] = 1f;

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



        Mat resultado = Rt.reshape(1,3);



       Core.gemm(K,resultado,1,Mat.zeros(3,1,CvType.CV_32FC1),0,M);

        Mat aux = new Mat(9,4,CvType.CV_32FC3);

        //Matriz de 9/3 a 9/4




        Calib3d.convertPointsToHomogeneous(p3d3,aux);
        Mat resul = new Mat();

        Calib3d.convertPointsFromHomogeneous(aux,resul);


       /* List<Point3> puntos3d = resul.toList();
        for (Point3 punto : puntos3d ) {
            Point3 pp = punto;
        }*/

        /*int size1 = (int) (resul.total() * resul.channels());
        float[] temp1 = new float[size1];
        resul.get(0,0,temp1);*/


        //reshape
        Mat objHomo = aux.reshape(1);



        //Core.multiply(objHomo,M.t(),objHomo);
        //objHomo.mul(M.t());
        Mat multi = new Mat(3,4,CvType.CV_32FC1);
        Mat puntos = new Mat(9,2,CvType.CV_32FC1);

        Core.gemm(objHomo,M.t(),1,Mat.zeros(3,1,CvType.CV_64FC1),0,multi);



        Calib3d.convertPointsFromHomogeneous(multi,puntos);

        Mat GLpose = aux.reshape(1);

        //Mat GLPose = new Mat();
        //Calib3d.pr
        MatOfPoint2f output = new MatOfPoint2f();


        Calib3d.projectPoints(p3d3,rvec,tvec,projection,calibracion,output);

        List<Point> listaOut = output.toList();

        // drawCube(entrada,listaOut);
        return listaOut;

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

        Pair<RotatedRect,MatOfPoint> par0 = new Pair<RotatedRect,MatOfPoint>();
        Pair<RotatedRect,MatOfPoint> par1 = new Pair<RotatedRect,MatOfPoint>();
        Pair<RotatedRect,MatOfPoint> par2 = new Pair<RotatedRect,MatOfPoint>();
        Pair<RotatedRect,MatOfPoint> par3 = new Pair<RotatedRect,MatOfPoint>();

        List<Pair<RotatedRect,MatOfPoint>> listaBorrar = new ArrayList<Pair<RotatedRect,MatOfPoint>>();



        Log.println(Log.ERROR,"tamano lista",String.valueOf(listaContornos.size()));
        while (it.hasNext()){
            Log.println(Log.ERROR,"tamano lista",String.valueOf(listaContornos.size()));
            Pair<RotatedRect,MatOfPoint> v = it.next();

            if(listaContornos.size()>4) {

                    if(v.getLeft().size.area()>tam0){
                        //tenemos un nuevo valor mayor
                        tam3=tam2;
                        tam2=tam1;
                        tam1=tam0;
                        tam0=v.getLeft().size.area();

                        //borramos el contorno de la lista.
                        if(!par3.equals(new Pair<RotatedRect,MatOfPoint>()))
                            listaBorrar.add(par3);
                        par3=par2;
                        par2=par1;
                        par1=par0;
                        par0=v;

                    }
                    else
                        if(v.getLeft().size.area()>tam1){
                            tam3=tam2;
                            tam2=tam1;
                            tam1=v.getLeft().size.area();

                            if(!par3.equals(new Pair<RotatedRect,MatOfPoint>()))
                                listaBorrar.add(par3);
                            par3=par2;
                            par2=par1;
                            par1=v;
                        }

                        else
                            if(v.getLeft().size.area()>tam2){
                                tam3=tam2;
                                tam2 = v.getLeft().size.area();

                                if(!par3.equals(new Pair<RotatedRect,MatOfPoint>()))
                                    listaBorrar.add(par3);
                                par3=par2;
                                par2 = v;
                            }

                            else
                                if(v.getLeft().size.area()>tam3){
                                    tam3 = v.getLeft().size.area();

                                    if(!par3.equals(new Pair<RotatedRect,MatOfPoint>()))
                                        listaBorrar.add(par3);
                                    par3=v;
                                }

                                /*else{
                                    Log.println(Log.ERROR,"borramos lista",String.valueOf(listaContornos.size()));
                                    it.remove();
                                }*/

                  }



        }

        for (Pair<RotatedRect,MatOfPoint> par :listaBorrar) {
            if(!par.equals(new Pair<RotatedRect,MatOfPoint>()))
                listaContornos.remove(par);
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
