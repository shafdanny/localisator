package com.polytech.mathieu.localisator1.model;

/**
 * Created by shafiq on 20/01/2017.
 */
public class Point {

    private double x = 0;
    private double y = 0;
    private int clusterNumber = 0;

    public Point() {
    }

    public Point(double x, double y, int clusterNumber) {
        this.x = x;
        this.y = y;
        this.clusterNumber = clusterNumber;
    }

    public Point(double x, double y)
    {
        this.setX(x);
        this.setY(y);
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX()  {
        return this.x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return this.y;
    }

    public int getClusterNumber() {
        return clusterNumber;
    }

    public void setClusterNumber(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    public String toString() {
        return ""+x+","+y+" Cluster nb: " + clusterNumber;
    }
}
