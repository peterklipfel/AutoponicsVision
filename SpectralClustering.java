/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Cluster;

//IMORTS
import Jama.Matrix;
import Jama.EigenvalueDecomposition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import static java.lang.Math.*;

/**
 *
 * @author BrianCarlsen
 */
public class SpectralClustering {
    private ArrayList<double[]> data;
    private Matrix affinity;
    private Matrix laplacian;
    private PriorityQueue<Eigenvector> eVectors;
    private Cluster[] clusters;
    
    public SpectralClustering (double[][] d) {
        data = new ArrayList<double[]>(d.length);
        data.addAll(Arrays.asList(d));
        
        affinity = new Matrix(data.size(), data.size());
        updateAffinity();
        
        eVectors = new PriorityQueue<Eigenvector>();
        updateEigenvectors();
            
        laplacian = new Matrix
                (affinity.getRowDimension(), affinity.getRowDimension());
        updateLaplacian();
    }
    
    private void updateAffinity(double sigma) {
        //the affinity matrix is symmetric along the diaganol so 
        // we only have to do the comutations for the lower half
        for (int i = 0; i < affinity.getRowDimension(); ++i) {
            for (int j = 0; j < i; ++j) {   //lower diaganol
                //double dist = getEuclideanDistance(data.get(i), data.get(j));
                double v = getGaussianDistance(data.get(i), data.get(j), sigma);
                affinity.set(i, j, v);
                affinity.set(j, i, v);   //symmetric matrix
            }
        }
        
        //the diaganol entries are always 0 becuase it is measuring 
        // the distance from a point to itself
        for (int i = 0; i < affinity.getRowDimension(); ++i) {
            affinity.set(i, i, 0);
        }
    }
    private void updateAffinity() {
        updateAffinity(1);
    }
    
    /**
     * 
     * @param p1 row vector
     * @param p2 row vector
     * @return Euclidean distance between p1 and p2
     */
    private static double getEuclideanDistance(double[] p1, double[] p2) {
        double dist = 0;
        
        for (int i = 0; i < p1.length; ++i) {
            dist += pow( p1[i] - p2[i], 2);
        }
        
        return sqrt(dist);
    }
    
    private static double getGaussianDistance(double[] p1, double[] p2, double sigma) {
        double dist = getEuclideanDistance(p1, p2);
        return exp( -pow(dist, 2) / (2*pow(sigma, 2)) );
    }
    
    private Matrix getDegreeMatrix() {
        Matrix d = new Matrix
                (affinity.getRowDimension(), affinity.getRowDimension());
        
        for (int i = 0; i < affinity.getRowDimension(); ++i) {
            double val = 0;
            for (int j = 0; j < affinity.getColumnDimension(); ++j) {
                val += affinity.get(i, j);
            }
            d.set(i, i, val);
        }
        
        return d;
    }
    
    private void updateLaplacian() {
        Matrix degree = getDegreeMatrix();
        for (int i = 0; i < degree.getRowDimension(); ++i) {
           degree.set(i, i, sqrt(degree.get(i, i)));
        }
        degree = degree.inverse();
        //construct Symmetric Laplacian Matrix
        // D^(-1/2) W D^(-1/2)
        laplacian = degree.times( affinity.times(degree) );
    }
    
    private void updateEigenvectors() {
        EigenvalueDecomposition e = affinity.eig();
        Matrix eVec = e.getV();
        Matrix eVal = e.getD();  //assume all eigenvalues are real
        
        for (int i = 0; i < eVec.getColumnDimension(); ++i) {
            double[] vec = eVec.getMatrix(0, eVec.getRowDimension() - 1, i, i)
                    .getColumnPackedCopy();
            double val = eVal.get(i, i);
            eVectors.add( new Eigenvector(vec, val) );
        }
    }
    
    private Matrix getNormalEigenMatrix(int numClusters) {
        Matrix X = new Matrix(laplacian.getRowDimension(), numClusters);
        for (int i = 0; i < numClusters; ++i) {
            double[][] t = {eVectors.poll().getVector()};
            Matrix tm = new Matrix(t);
            X.setMatrix
                    (0, X.getRowDimension() - 1, i, i, tm.transpose() );
        }
        
        //normalize rows
        for (int i = 0; i < X.getRowDimension(); ++i) {
            double norm = 0;
            for (int j = 0; j < X.getColumnDimension(); ++j) {  //calculate norm
                norm += pow( X.get(i, j), 2);
            }
            for (int j = 0; j < X.getColumnDimension(); ++j) { //normalize each element
                X.set(i, j, X.get(i, j)/ sqrt(norm));
            }
        }
        
        return X;
    }
    
    public static Cluster[] cluster(double[][] d, int numClusters) {
         SpectralClustering sc = new SpectralClustering(d);
         sc.cluster(numClusters);
         return sc.getClusters();
    }
    public void cluster(int numClusters) {
        Matrix Y = getNormalEigenMatrix(numClusters);

        //cluster using kMeans
        KMeans km = new KMeans
                (Y.getArray(), numClusters);
       km.cluster(100);
       Cluster[] clust = km.getClusters();

       ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
       
       ArrayList<double[]> dataClone = new ArrayList<double[]>(data.size());
       dataClone.addAll(data);
        
        for (Cluster c : clust) {
            Cluster tc = new Cluster(data.get(0).length);
            for (int i = 0; i < Y.getRowDimension(); ++i) {
                double[] eVec = Y.getArray()[i];

                //assign original point si to cluster j if row i in X
                // was assigned to cluster j
                for (double[] r : c.getData()) {
                    if (Arrays.asList(r).equals(Arrays.asList(eVec))) {
                        double[] g = data.get(i);
                        tc.addData(data.get(i));
                    }
                }
            }
            tc.centerOnData();
            clusterList.add(tc);
        }
        
        clusters = new Cluster[clusterList.size()]; 
        clusterList.toArray(clusters);
    }

    /*  TODO
     * 
     public void smartCluster(int start, int end) {
        ArrayList<Cluster> bestCluster = new ArrayList<Cluster>();
        double bestCost = Double.POSITIVE_INFINITY;
        for (int i = start; i <= end; ++i) {
            cluster(i);
            double cCost = cost();
            if (cCost < bestCost) {
                bestCost = cCost;
                bestCluster.clear();
                bestCluster.addAll(Arrays.asList(clusters));
            }
        }

        bestCluster.toArray(clusters);
    }
   
    private double cost() {
        double[] distError = new double[clusters.length];
        int i = 0;
        for (Cluster c : clusters) {  //get distance between each point and its associated points
            ArrayList<double[]> cData = c.getData();
            double[] cPos = c.getPosition();
            for (double[] d : cData)
                distError[i] *= getGaussianDistance(cPos, d, 1);
            ++i;
        }
        
        double error = 0;
        for (double de : distError) 
            error += de;
            
        double cost = 10/error + 10*clusters.length;
        System.out.println(error);

        return cost;
    }
   */
    
    public Cluster[] getClusters() {
        return clusters;
    }
    
     public void show() {
        int i = 0;
        
        for (Cluster c : clusters) {
            System.out.println("Cluster " + (i++) + ":");
            c.show();
        }
    }


    private class Eigenvector implements Comparable<Eigenvector>{
        private double[] vector;
        private double value;   // eigenvalue

        public Eigenvector(double[] vec, double val) {
            vector = vec;
            value = val;
        }

        public double[] getVector() {
            return vector;
        }
        public void setVector(double[] vec) {
            vector = vec;
        }

        public double getValue() {
            return value;
        }
        public void setValue(double val) {
            value = val;
        }

        @Override
        public int compareTo(Eigenvector e) {
            Double v = new Double(value);
            Double v2 = new Double(e.getValue());
            return -v.compareTo(v2);
        }

        public int equals(Double oVal) {
            return this.equals(oVal);
        }
    }
    
    
    
    
    
     public static void main(String[] args) {
         int N = 10;
         double[][] points = new double[N][2];
        for (int i = 0; i < N; ++i) {
            double val = i;
            if(i > N/2)
                val +=50;
            
            points[i][0]= random()*100;
            points[i][1] = points[i][0];
        }
    
        SpectralClustering sp = new SpectralClustering(points);

        sp.cluster(5);
        sp.show();
    }
}