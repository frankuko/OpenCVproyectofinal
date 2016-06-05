package com.tfg.javier.opencvproyectofinal.filtros;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.lang.ref.WeakReference;

/**
 * Created by javier on 05/06/2016.
 */
public class BitmapWorkerAsync extends AsyncTask <Integer, Void, Bitmap> {

    private int data = 0;
    private Bitmap mBitmap;
    private Size mSize;
    private WeakReference<Mat> matWeakReference;


    public BitmapWorkerAsync(Mat matriz){
        matWeakReference = new WeakReference<Mat>(matriz);
        mSize = new Size(matriz.width(),matriz.height());
        mBitmap = Bitmap.createBitmap(matriz.width(),matriz.height(), Bitmap.Config.ARGB_8888);
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {

        data = params[0];

        Utils.matToBitmap(matWeakReference.get(),mBitmap);

        return mBitmap;
    }
}
