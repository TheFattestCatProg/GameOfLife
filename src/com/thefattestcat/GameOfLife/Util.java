package com.thefattestcat.GameOfLife;

public class Util {
    public static double floorAlign(double v, double a) {
        return Math.floor( v / a ) * a;
    }

    public static double ceilAlign(double v, double a) {
        return Math.ceil( v / a ) * a;
    }
}
