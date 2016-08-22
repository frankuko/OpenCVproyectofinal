package com.tfg.javier.opencvproyectofinal.filtros.ar;

import org.opencv.core.Mat;

/**
 * Created by javier on 12/05/2016.
 */
public interface Filter {
    public abstract void apply(final Mat src, final Mat dst);
}
