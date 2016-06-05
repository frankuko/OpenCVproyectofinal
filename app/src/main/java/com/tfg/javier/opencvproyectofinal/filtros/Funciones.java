package com.tfg.javier.opencvproyectofinal.filtros;

/**
 * Created by javier on 04/06/2016.
 */

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Funciones {
    float xscale;
    float yscale;
    float xshift;
    float yshift;
    int [] s;
    private String TAG = "Filters";

    public Funciones(){

        Log.e(TAG, "***********inside constructor");
    }

    public Bitmap barrel (Bitmap input, float k){
        Log.e(TAG, "***********inside barrel method ");
        float centerX=input.getWidth()/2; //center of distortion
        float centerY=input.getHeight()/2;

        int width = input.getWidth(); //image bounds
        int height = input.getHeight();

        Bitmap dst = Bitmap.createBitmap(width, height,input.getConfig() ); //output pic
        Log.e(TAG, "***********dst bitmap created ");
        xshift = calc_shift(0,centerX-1,centerX,k);
        float newcenterX = width-centerX;
        float xshift_2 = calc_shift(0,newcenterX-1,newcenterX,k);

        yshift = calc_shift(0,centerY-1,centerY,k);
        float newcenterY = height-centerY;
        float yshift_2 = calc_shift(0,newcenterY-1,newcenterY,k);

        xscale = (width-xshift-xshift_2)/width;
        yscale = (height-yshift-yshift_2)/height;
        Log.e(TAG, "***********about to loop through bm");
          /*for(int j=0;j<dst.getHeight();j++){
              for(int i=0;i<dst.getWidth();i++){
                float x = getRadialX((float)i,(float)j,centerX,centerY,k);
                float y = getRadialY((float)i,(float)j,centerX,centerY,k);
                sampleImage(input,x,y);
                int color = ((s[1]&0x0ff)<<16)|((s[2]&0x0ff)<<8)|(s[3]&0x0ff);
    //            System.out.print(i+" "+j+" \\");

                dst.setPixel(i, j, color);

              }
            }*/

        int origPixel; // the pixel in orig image

        for(int j=0;j<dst.getHeight();j++){
            for(int i=0;i<dst.getWidth();i++){
                origPixel= input.getPixel(i,j);
                float x = getRadialX((float)i,(float)j,centerX,centerY,k);
                float y = getRadialY((float)i,(float)j,centerX,centerY,k);
                sampleImage(input,x,y);
                int color = ((s[1]&0x0ff)<<16)|((s[2]&0x0ff)<<8)|(s[3]&0x0ff);
                //            System.out.print(i+" "+j+" \\");


// check whether a pixel is within the circle bounds of 150

                if( Math.sqrt( Math.pow(i - centerX, 2) + ( Math.pow(j - centerY, 2) ) ) <= 150 ){
                    dst.setPixel(i, j, color);
                }else{
                    dst.setPixel(i,j,origPixel);
                }
            }
        }
        return dst;
    }

    float calc_shift(float x1,float x2,float cx,float k)
    {
        float thresh = 1;
        float x3 = x1+(x2-x1)*(float)0.5;
        float res1 = x1+((x1-cx)*k*((x1-cx)*(x1-cx)));
        float res3 = x3+((x3-cx)*k*((x3-cx)*(x3-cx)));


        if(res1>-thresh && res1 < thresh)
            return x1;
        if(res3<0){
            return calc_shift(x3,x2,cx,k);
        }else{
            return calc_shift(x1,x3,cx,k);
        }
    }


    float getRadialX(float x,float y,float cx,float cy,float k){
        x = (x*xscale+xshift);
        y = (y*yscale+yshift);
        float res = x+((x-cx)*k*((x-cx)*(x-cx)+(y-cy)*(y-cy)));
        return res;
    }

    float getRadialY(float x,float y,float cx,float cy,float k){
        x = (x*xscale+xshift);
        y = (y*yscale+yshift);
        float res = y+((y-cy)*k*((x-cx)*(x-cx)+(y-cy)*(y-cy)));
        return res;
    }


    void sampleImage(Bitmap arr, float idx0, float idx1)
    {
        s = new int [4];
        if(idx0<0 || idx1<0 || idx0>(arr.getHeight()-1) || idx1>(arr.getWidth()-1)){
            s[0]=0;
            s[1]=0;
            s[2]=0;
            s[3]=0;
            return;
        }

        float idx0_fl=(float) Math.floor(idx0);
        float idx0_cl=(float) Math.ceil(idx0);
        float idx1_fl=(float) Math.floor(idx1);
        float idx1_cl=(float) Math.ceil(idx1);

        int [] s1 = getARGB(arr,(int)idx0_fl,(int)idx1_fl);
        int [] s2 = getARGB(arr,(int)idx0_fl,(int)idx1_cl);
        int [] s3 = getARGB(arr,(int)idx0_cl,(int)idx1_cl);
        int [] s4 = getARGB(arr,(int)idx0_cl,(int)idx1_fl);

        float x = idx0 - idx0_fl;
        float y = idx1 - idx1_fl;

        s[0]= (int) (s1[0]*(1-x)*(1-y) + s2[0]*(1-x)*y + s3[0]*x*y + s4[0]*x*(1-y));
        s[1]= (int) (s1[1]*(1-x)*(1-y) + s2[1]*(1-x)*y + s3[1]*x*y + s4[1]*x*(1-y));
        s[2]= (int) (s1[2]*(1-x)*(1-y) + s2[2]*(1-x)*y + s3[2]*x*y + s4[2]*x*(1-y));
        s[3]= (int) (s1[3]*(1-x)*(1-y) + s2[3]*(1-x)*y + s3[3]*x*y + s4[3]*x*(1-y));
    }

    int [] getARGB(Bitmap buf, int x, int y){
        int rgb = buf.getPixel(y, x); // Returns by default ARGB.
        int [] scalar = new int[4];
        scalar[0] = (rgb >>> 24) & 0xFF;
        scalar[1] = (rgb >>> 16) & 0xFF;
        scalar[2] = (rgb >>> 8) & 0xFF;
        scalar[3] = (rgb >>> 0) & 0xFF;
        return scalar;
    }

    // Cx and Cy specify the coordinates from where the distorted image will have as initial point and
// k specifies the distortion factor
    void fishEye(Mat _src, Mat _dst, float Cx, float Cy, float k, boolean scale)
    {
        // die if distortion parameters are not correct

        if (Cx >= 0 && Cy >= 0 && k >= 0) return;

        Mat src = _src.clone();
        // die if sample image is not the correct type
        //CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3);


        Mat mapx = new Mat(src.size(), CvType.CV_32FC1);
        Mat mapy = new Mat(src.size(), CvType.CV_32FC1);

        int w = src.cols();
        int h = src.rows();

        float[] props;

        float xShift = calc_shift(0 , Cx - 1, Cx, k);
        //props[0] = xShift;
        float newCenterX = w - Cx;
        float xShift2 = calc_shift(0, newCenterX - 1, newCenterX, k);

        float yShift = calc_shift(0, Cy - 1, Cy, k);
        //props[1] = yShift;
        float newCenterY = w - Cy;
        float yShift2 = calc_shift(0, newCenterY - 1, newCenterY, k);

        float xScale = (w - xShift - xShift2) / w;
       // props[2] = xScale;
        float yScale = (h - yShift - yShift2) / h;
        //props[3] = yScale;

       /* float *p = mapx.ptr<float>(0);

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                *p++ = getRadialX((float)x, (float)y, Cx, Cy, k, scale, props);
            }
        }

        p = mapy.ptr<float>(0);
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                *p++ = getRadialY((float)x, (float)y, Cx, Cy, k, scale, props);
            }
        }

        Imgproc.remap(src, _dst, mapx, mapy, Imgproc.INTER_LINEAR, Imgproc.BORDER_CONSTANT);
*/
//    Mat(src).copyTo(_dst);
    }

}
