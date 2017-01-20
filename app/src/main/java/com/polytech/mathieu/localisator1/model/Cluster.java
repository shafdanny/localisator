package com.polytech.mathieu.localisator1.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by shafiq on 20/01/2017.
 */
public class Cluster {

    public List<Point> points;
    public Point centroid;
    public int id;

    public Cluster() {}

    public Cluster(List<Point> points, Point centroid, int id) {
        this.points = points;
        this.centroid = centroid;
        this.id = id;
    }

    //Creates a new Cluster
    public Cluster(int id) {
        this.id = id;
        this.points = new ArrayList();
        this.centroid = null;
    }

    public List getPoints() {
        return points;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void setPoints(List points) {
        this.points = points;
    }

    public Point getCentroid() {
        return centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        points.clear();
    }

    public void plotCluster() {
        System.out.println("[Cluster: " + id+"]");
        System.out.println("[Centroid: " + centroid + "]");
        System.out.println("[Points: \n");
        for(Point p : points) {
            System.out.println(p);
        }
        System.out.println("]");
    }

}

