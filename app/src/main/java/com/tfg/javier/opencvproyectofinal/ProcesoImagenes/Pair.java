package com.tfg.javier.opencvproyectofinal.ProcesoImagenes;

/**
 * Created by FranciscoJavier on 12/08/2016.
 */
public class Pair<L,R> {

    private final L left;
    private final R right;
    private boolean nulo;

    public Pair(){
        this.left = null;
        this.right = null;
        this.nulo = true;
    }

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
        this.nulo = false;
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        if(this.nulo && pairo.nulo) return true;
        return this.left.equals(pairo.getLeft()) &&
                this.right.equals(pairo.getRight());
    }

}