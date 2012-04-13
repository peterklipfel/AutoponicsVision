/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Cluster;

//IMPORTS
import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.Math.*;

/**
 *
 * @author BrianCarlsen
 */
public class KMeans {
    ArrayList<double[]> data;
    Cluster[] clusters;
    
    public KMeans(ArrayList<double[]> d, int numClusters) {
        data = d;
        
        clusters = new Cluster[numClusters];
        for (int i = 0; i < numClusters; ++i) 
            clusters[i] = new Cluster(data.get(0).length);        
    }
    public KMeans(double[][] d, int numClusters) {
        data = new ArrayList<double[]>();
        data.addAll(Arrays.asList(d));
        
        clusters = new Cluster[numClusters];
        for (int i = 0; i < numClusters; ++i) 
            clusters[i] = new Cluster(data.get(0).length);
    }
    
    public void setNumberOfClusters(int numClusters) {
        clusters = new Cluster[numClusters];
        for (int i = 0; i < numClusters; ++i) 
            clusters[i] = new Cluster(data.get(0).length);  
    }
    public int getNumberOfClusters() {
        return clusters.length;
    }
    
    public Cluster[] getClusters() {
        return clusters;
    }
    
    public ArrayList<ArrayList<double[]>> getClustersAsList() {
        ArrayList<ArrayList<double[]>> rc = new ArrayList<ArrayList<double[]>>();
        for (Cluster c : clusters) {
            rc.add(c.getData());
        }
        
        return rc;
    }
    
    public void addData(double[] d) {
        data.add(d);
    }
 /*   public boolean removeData(Matrix pos) {
        for (Matrix d : data) {
            for (int i = 0; i < d.getRowDimension(); ++i) {
                for (int j = 0; j < d.getColumnDimension(); ++j) {
                    if (d.get(i, j) != pos.get(i, j)) 
                        break;
                }
            }
            data.remove(d);
            return true;
        }
    }
  */

    private void associateData() {
        for (double[] p : data) {
            Double min = Double.POSITIVE_INFINITY;
            Cluster best = clusters[0];

            for (Cluster c : clusters) {
                double dist = distanceBetween(p, c.getPosition() );
                if (dist < min) {
                    min = dist;
                    best = c;
                }
            }
            
            best.addData(p);
        }
    }
    
    private static double distanceBetween(double[] p1, double[] p2) {
        double dist = 0;
        for (int i = 0; i < p1.length; ++i) 
            dist += pow(p1[i] - p2[i], 2);
        
        return sqrt(dist);
    }
    
    private void centerClusters() {
        for (Cluster c : clusters) 
            c.centerOnData();
    }
    
    public static Cluster[] cluster(double[][] d, int numClusters, int iterations) {
        KMeans km = new KMeans(d, numClusters);
        km.cluster(iterations);
        return km.getClusters();
    }
    public void cluster(int iterations) {
        for (int i = 0; i < iterations; ++i) {
            for (Cluster c : clusters) {
                if (c.getData().isEmpty()) //if no data is associated with c
                    c.restart();
                
                c.resetData();
            }
            
            associateData();
            centerClusters();
        }
    }
    
    public void smartCluster(int iterations, int start, int end) {
        ArrayList<Cluster> bestCluster = new ArrayList<Cluster>();
        double bestCost = Double.POSITIVE_INFINITY;
        for (int i = start; i <= end; ++i) {
            setNumberOfClusters(i);
            cluster(iterations);
            double cCost = cost();
            if (cCost < bestCost) {
                bestCost = cCost;
                bestCluster.clear();
                bestCluster.addAll(Arrays.asList(clusters));
            }
        }
        
        setNumberOfClusters(bestCluster.size());
        bestCluster.toArray(clusters);
    }
   
    private double cost() {
        double distError = 0;
        for (Cluster c : clusters) {  //get distance between each point and its associated points
            ArrayList<double[]> cData = c.getData();
            double[] cPos = c.getPosition();
            for (double[] d : cData)
                distError += distanceBetween(cPos, d);
        }
        
        double cost = distError + 10*clusters.length;

        return cost;
    }
    
    public void show() {
        int i = 0;
        
        for (Cluster c : clusters) {
            System.out.println("Cluster " + (i++) + ":");
            c.show();
        }
    }
    
    
    
    
    public static void main(String[] args) {
        double[] p1 = {-1,0,000};
        double[] p2 = {0,0,0};
        double[] p3 = {1,0,0};

        double[] p4 = {9,0,000};
        double[] p5 = {10,0,0};
        double[] p6 = {11,0,0};
        
        double[] p11 = {-1,0,10};
        double[] p21 = {0,0,10};
        double[] p31 = {1,0,10};

        double[] p41= {9,0,10};
        double[] p51 = {10,0,10};
        double[] p61 = {11,0,10};

        double[][] data = {p1,p2,p3,p4,p5,p6};
        for (Cluster c : KMeans.cluster(data, 2, 10)) {
            System.out.println("Cluster:");
            c.show();
        }
        
        KMeans km = new KMeans(data, 2);
        km.smartCluster(100, 1, 10);

        km.show();
    }

}

