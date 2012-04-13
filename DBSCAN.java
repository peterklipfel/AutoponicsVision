/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Cluster;

//IMPORTS
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import static java.lang.Math.*;

/**
 *
 * @author BrianCarlsen
 */
public class DBSCAN {
    private ArrayList<Cluster> clusters;
    private HashSet<Double[]> noise;
    private Double[][] points;
    private double nHoodDist;
    private int minPoints;

    public DBSCAN(Double[][] data, double epsNhood, int minP) {
        points = data;
        nHoodDist = epsNhood;
        minPoints = minP;
        
        clusters = new ArrayList<Cluster>();
        noise = new HashSet<Double[]>();
    }
    public DBSCAN(double[][] data, double epsNhood, int minP) {
        Double[][] d = new Double[data.length][data[0].length];
        int i = 0;
        for (double[] p : data) {
            Double[] dp = toDoubleArray(p);
            d[i] = dp;
            ++i;
        }
            
        points = d;
        nHoodDist = epsNhood;
        minPoints = minP;
        
        clusters = new ArrayList<Cluster>();
        noise = new HashSet<Double[]>();
    }
    public Cluster[] getClusters() {
        Cluster[] t = new Cluster[clusters.size()];
        clusters.toArray(t);
        return t;
    }
    
    public HashSet<Double[]> getNoise() {
        return noise;
    }
    
    public Double[] getPoint(int i) {
        return points[i];
    }
    
    public HashSet<Double[]> getReachable(Double[] p, HashSet<Double[]> visited) {
        HashSet<Double[]> group = new HashSet<Double[]>();
        group.add(p);
        visited.add(p);
        
        HashSet<Double[]> nhood = getEpNeighborhood(p);
        for (Double[] d : nhood) 
            if (!visited.contains(d))
                group.addAll(getReachable(d, visited));
        
        return group;
    }
    
    public static Cluster[] cluster(double[][] data, double epsNhood, int minPoints) {
        DBSCAN db = new DBSCAN(data, epsNhood, minPoints);
        db.cluster();
        return db.getClusters();
    }
    public void cluster() {
        HashSet<Double[]> visited = new HashSet<Double[]>();

        for (Double[] p : points) {
            if (!visited.contains(p)) {
                visited.add(p);

                HashSet<Double[]> group = new HashSet<Double[]>   //get points reachable from p
                        (getReachable(p, new HashSet<Double[]>()));

                if (group.size() < minPoints) 
                    noise.addAll(group);
                else {
                    Cluster c = new Cluster(points[0].length);
                    for (Double[] d : group)
                        c.addData(toDoubArray(d));
                    clusters.add(c);
                }
                
                visited.addAll(group);
            }     
        }  
    }
    
    private HashSet<Double[]> getEpNeighborhood(Double[] p) {
        HashSet<Double[]> nhood = new HashSet<Double[]>();
        for (Double[] op : points) {
            if (inEpNeighborhood(p, op))
                nhood.add(op);
        }
        
        return nhood;
    }
    
    private boolean inEpNeighborhood(Double[] p1, Double[] p2) {
        if (getDistance(p1, p2) <= nHoodDist)
            return true;
        else 
            return false;
    }
    private static double getDistance(Double[] p1, Double[] p2) {
        if (p1.length != p2.length) {
            return 0;
        }
        
        double dist = 0;
        int i = 0;
        
        for (double v : p1) {
            dist += pow(p1[i]- p2[i], 2);
            ++i;
        }
        
        return sqrt(dist);
    }
    
    private static Double[] toDoubleArray(double[] p) {
        Double[] t = new Double[p.length];
        int i = 0;

        for (double v : p) {
            t[i] = v;
            ++i;
        }
        
        return t;
    }
    private static double[] toDoubArray(Double[] p) {
        double[] t = new double[p.length];
        int i = 0;

        for (Double v : p) {
            t[i] = v;
            ++i;
        }
        
        return t;
    }

    public void show() {
        int i = 0;
        
        for (Cluster c : clusters) {
            System.out.println("Cluster " + (i++) + ":");
            c.show();
        }
        
        System.out.println("\nNoise:");
        for (Double[] p : noise) 
            System.out.println(Arrays.deepToString(p));
    }

    public static void main(String[] args) {
        double[] p1 = {-1,0,000};
        double[] p2 = {0,0,0};
        double[] p3 = {1,0,0};

        double[] p4 = {9,0,000};
        double[] p5 = {10,0,0};
        double[] p6 = {11,0,0};
        double[] p7 = {11.1,0,0};
        double[] p8 = {11.2,0,0};
        double[] p9 = {11,.1,0};

        double[][] data = {p1,p2,p3,p4,p5,p6,p7,p8,p9};
        DBSCAN db = new DBSCAN(data, 1, 4);
        db.cluster();
        db.show();
    }
}