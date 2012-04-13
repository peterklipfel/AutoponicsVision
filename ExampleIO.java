/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Image;

//IMPORTS
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;

import java.util.ArrayList;

import bric.Cluster.KMeans;
import bric.Cluster.DBSCAN;
import bric.Cluster.SpectralClustering;
import bric.Cluster.Cluster;
import java.io.FileWriter;

/**
 *
 * @author BrianCarlsen
 */
public class ExampleIO implements Runnable{
    private String name;
    private String filePath;
    private String op;
    private int[][] image;
    
    public ExampleIO (int[][] i, String rw, String fp, String n) {
        rw = rw.toLowerCase();
        image = i;
        filePath = fp;
        name = n;

        if (rw.equals("read") || rw.equals("r") )
            op = "r";         
        else if (rw.equals("write") || rw.equals("w") )
            op = "w";                  
        else 
            op = "other";  
    }
    
    public void setImage(int[][] i) {
        image = i;
    }
    public void setFilePath(String fp) {
        filePath = fp;
    }
    public void setName(String n) {
        name = n;
    }
            
    public int[][] getImage() {
        return image;
    }
    public String getFilePath() {
        return filePath;
    }
    public String getName() {
        return name;
    }
    
     public void writeImageToFile(String filePath) throws IOException{
       
        try {
            File f = File.createTempFile(name + "_EX_", "", new File(filePath));
            PrintWriter out = new PrintWriter(f);
             
            int i = 0;
            int j = 0;
            for (int [] r : image) {
                for (int c : r) {
                    out.println(i + ", " + j + ", " + c);
                    ++j;
                }
                ++i;
            }
            
            out.close();
        }
        catch (IOException e) {
            throw e;
        }
    }
     
     public int[][] readImageFromFile (File f) {
         HashMap<Integer[], Integer> t = new HashMap<Integer[], Integer>();
         int width = 0;
         int height = 0;
             
         try {
             Scanner in = new Scanner(f);

             String line;
             while ( (line = in.nextLine()) != null ) {
                 String[] tokens = line.split(", ");
                 Integer[] p = new Integer[2];
                 
                 p[0] = Integer.parseInt(tokens[0]); 
                 p[1] = Integer.parseInt(tokens[1]); 
                 Integer v = new Integer(Integer.parseInt(tokens[2]));
                 
                 if (p[0] > height) 
                     height = ++p[0];
                 if (p[1] > width)
                     width = ++p[1];
                 
                 t.put(p, v);
            }
             
         }
         catch (FileNotFoundException e) {
             System.out.println(e.toString());
         }

         int[][] im = new int[height][width];
         for (Integer[] p : t.keySet()) {
             im[p[0]][p[1]] = t.get(p);
         }
      
         return im;
     }
     public void readImageFromFile(String fp) {
         File f = new File(filePath + name);
         readImageFromFile(f);
     }
     
    public void makeExample() throws IOException{
        int cornerT = 10000;
        int edgeT = 100;
        
        ImageLayer il = new ImageLayer(image, image[0].length, image.length, ColorType.RED);
        Filter sobelVertical = new Filter(new int[][]{{-1, 1},
                                                      {-2, 2},
                                                      {-1, 1}},
                                                        2, 3);
        Filter sobelHorizontal = new Filter(new int[][]{{-1, -2, -1},
                                                         {1, 2, 1}},
                                                        3, 2);
        
        int numCorners = FeatureDetection.detectCorners
                (il, sobelHorizontal, sobelVertical, cornerT).size();
        int numEdges = FeatureDetection.detectEdges(il, sobelVertical, edgeT).size();
        numEdges += FeatureDetection.detectEdges(il, sobelHorizontal, edgeT).size();
        Cluster[] kMeansCluster;
        Cluster[] spectralCluster;
        Cluster[] dbCluster;       
        ArrayList<double[]> activePixels = getActivePixels(100);
        double[][] activePixelsArray = new double[activePixels.size()][2];
        activePixels.toArray(activePixelsArray);
        
        KMeans km = new KMeans(activePixels, 0);
        km.smartCluster(100, 1, 10);
        kMeansCluster = km.getClusters();
/*        
        SpectralClustering sc = new SpectralClustering(activePixelsArray);
        sc.cluster(km.getNumberOfClusters());
        spectralCluster = sc.getClusters();
*/
       
        DBSCAN db = new DBSCAN(activePixelsArray, 1.5, 9);
        db.cluster();
        dbCluster = db.getClusters();

         try {
            //File f = File.createTempFile(name + "_EX_", "", new File(filePath));
            File f = new File(filePath);
            FileWriter out = new FileWriter(f, true);
             
            String s = numCorners + ", ";
            s += numEdges + ", ";
            s += kMeansCluster.length + ", ";
            //out.println(spectralCluster.length);
            s += dbCluster.length;
            
            out.write(s);
            out.write("\r\n");
            out.close();
        }
        catch (IOException e) {
            throw e;
        }
    } 
    
    private ArrayList<double[]> getActivePixels(double thresh) {
        ArrayList<double[]> active = new ArrayList<double[]>();
        int i = 0;
        for (int[] r : image) {
            int j = 0;
            for (int c : r) {
                if (c >= thresh) {
                    double[] t = {(double) i, (double) j};
                    active.add(t);
                }
                ++j;
            }
            ++i;
        }
        
        return active;
    }
    
    @Override
     public void run() {
        try {
         if (op.equals("r"))
             readImageFromFile(filePath);
         else if (op.equals("w"))
             writeImageToFile(filePath);
         else {
             makeExample();}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
     }
    
    public static void main(String... args) {
        int[][] im = new int[10][10];
        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 10; ++j) 
                im[i][j] = (int)(Math.random() * 255);
        for (int i = 0; i < 10; ++i) {
            im[2][i] = 255;
            im[i][4] = 255;
        }
        String fp = System.getProperty("user.dir");
        ExampleIO eio = new ExampleIO(im, "e", fp + "\\webotsTestFile.txt", "");
        
        Thread t = new Thread(eio);
        t.start();
        
        
    }
        
}
