/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Image;

import bric.Cluster.Cluster;
import bric.Cluster.DBSCAN;
import java.util.HashMap;
import java.util.ArrayList;
import static java.lang.Math.*;

/**
 *
 * @author BrianCarlsen
 */
public class FeatureDetection {
    public static ArrayList<ArrayList<Integer[]>> detectEdges(ImageLayer src, Filter kernel, int thresh) {
        ArrayList<Integer[]> edge = new ArrayList<Integer[]>();
        ImageLayer conv = src.convolve(kernel);
        conv = conv.threshhold(thresh);
        
        int[][] convA = conv.getImage();
        for (int i = 0; i < conv.getHeight(); ++i) {
            for (int j = 0; j < conv.getWidth(); ++j) {
                Integer[] edgePoint = new Integer[2];
                if (abs(convA[i][j]) > thresh) {
                    edgePoint[0] = i;
                    edgePoint[1] = j;
                    edge.add(edgePoint);
                }
            }
        }
        
        //group the edges using DBSCAN
        Double[][] im = new Double[edge.size()][2];
        int i = 0;
        for (Integer[] p : edge) {
            im[i] = new Double[] {new Double(p[0]), new Double(p[1])};
            ++i;
        }
        DBSCAN db = new DBSCAN(im, 1, 4);
        db.cluster();
        Cluster[] cl = db.getClusters();
        
        ArrayList<ArrayList<Integer[]>> groupedEdges = new ArrayList<ArrayList<Integer[]>>();
        ArrayList<Integer[]> edgePoints = new ArrayList<Integer[]>();
        for (Cluster c : cl) {
            ArrayList<double[]> points = c.getData();
            for (double[] d : points)
                edgePoints.add(new Integer[] {new Integer((int)d[0]), new Integer((int)d[1])} );
            groupedEdges.add(edgePoints);
        }
        
        return groupedEdges;
    }
    
    public static ArrayList<Integer[]> detectCorners
            (ImageLayer src, Filter hFilter, Filter vFilter, int thresh) {
        ArrayList<Integer[]> corners = new ArrayList<Integer[]>();
        ImageLayer hBase = src.convolve(hFilter);
        ImageLayer vBase = src.convolve(vFilter);
        
        for (int i = 1; i < src.getHeight() - 1; ++i) {
            for (int j = 1; j < src.getWidth() - 1; ++j) { //loop over image
                int hDiff = 0;
                int vDiff = 0;
                
                hDiff += pow(hBase.getPixel(i, j) - hBase.getPixel(i, j - 1), 2);
                hDiff += pow(hBase.getPixel(i, j) - hBase.getPixel(i, j + 1), 2);
                
                vDiff += pow(vBase.getPixel(i, j) - vBase.getPixel(i - 1, j), 2);
                vDiff += pow(vBase.getPixel(i, j) - vBase.getPixel(i + 1, j), 2);
                
                if (hDiff > thresh && vDiff > thresh) {
                    corners.add(new Integer[] {i, j});
                }
            }
        }
        
        return corners;
    }
    
    public static ColorType distinguishColor
            (ImageLayer imageRed, ImageLayer imageGreen, ImageLayer imageBlue, ImageLayer imageYellow,
             int colorThresh, int colorDiffThresh) {
        //ColorType color;
        HashMap<Integer[], ColorType> pColor = 
                new HashMap<Integer[], ColorType>();
        
        //get pixel colors
        int[][] redA = imageRed.getImage();
        int[][] greenA = imageGreen.getImage();
        int[][] blueA = imageBlue.getImage();
        
        int height = imageRed.getHeight();
        int width = imageRed.getWidth();

        int tb, tr, tg;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tr = redA[i][j];
                tg = greenA[i][j];
                tb = blueA[i][j];

                ColorType c = getPixelColor(tr, tg, tb, colorThresh, colorDiffThresh);
                Integer[] p = {i, j};
                pColor.put(p, c);
            }
        }
        
        ImageLayer buffRed = new ImageLayer(width, height, ColorType.RED);
        ImageLayer buffGreen = new ImageLayer(width, height, ColorType.GREEN);
        ImageLayer buffBlue = new ImageLayer(width, height, ColorType.BLUE);
        ImageLayer buffYellow = new ImageLayer(width, height, ColorType.YELLOW);
        
        // reconstruct images
        int r = 0;
        int g = 0;
        int b = 0;
        int y = 0;
        
        for (Integer[] Pixel : pColor.keySet()) {
          int i = Pixel[0];
          int j = Pixel[1];
          
          switch ( pColor.get(Pixel) ) {
              case RED:
                  buffRed.setPixel(i, j, 255);
                  ++r;
                  break;
              case GREEN:
                  buffGreen.setPixel(i, j, 255);
                  ++g;
                  break;
              case BLUE:
                  buffBlue.setPixel(i, j, 255);
                  ++b;
                  break;
              case YELLOW:
                  buffYellow.setPixel(i, j, 255);
                  ++y;
                  break;
              case WHITE:
              case BLACK:
              case NONE:
                  buffRed.setPixel(i, j, 0);
                  buffGreen.setPixel(i, j, 0);
                  buffBlue.setPixel(i, j, 0);
                  buffYellow.setPixel(i, j, 0);
                  break;
              default:
                  continue;
          }
      }
        

        imageRed.setImage(buffRed.getImage());
        imageGreen.setImage(buffGreen.getImage());
        imageBlue.setImage(buffBlue.getImage());
        imageYellow.setImage(buffYellow.getImage());
        
        int colorT = getMaxInd(new int[] {r, g, b, y});
        if (r + g + b + y > 0) {
            switch (colorT) {
                case 0:
                    return ColorType.RED;
                case 1:
                    return ColorType.GREEN;
                case 2: 
                    return ColorType.BLUE;
                case 3: 
                    return ColorType.YELLOW;
                default:
                    return ColorType.NONE;       
            }
        }
        else 
            return ColorType.BLACK;
    }

    private static ColorType getPixelColor(int r, int g, int b, int thresh, int diffThresh) {
        int[] c = {r, g, b};
        boolean[] cbDiff = {
            getDiff(r, g, diffThresh),
            getDiff(r, b, diffThresh),
            getDiff(g, b, diffThresh)};
        //      red green         red blue        green blue    

        // eliminate values less than the threshhold
        for (int i = 0; i < c.length; i++) {
            if (c[i] < thresh) {
                c[i] = 0;
            }
        }
        //if all are below threshhold return ColorType.NONE
        if (c[0] == 0 && c[1] == 0 && c[2] == 0) {
            return ColorType.NONE;
        }

        // get most intense color
        ColorType maxC;
        switch (getMaxInd(c)) {
            case 0:
                maxC = ColorType.RED;
                break;
            case 1:
                maxC = ColorType.GREEN;
                break;
            case 2:
                maxC = ColorType.BLUE;
                break;
            default:
                return ColorType.NONE;
        }
        if (cbDiff[0] == false && cbDiff[1] == false && cbDiff[2] == false) {
            return ColorType.WHITE;
        }
        if (cbDiff[0] == true && cbDiff[1] == true && cbDiff[2] == true) {
            return maxC;
        }

        if (maxC == ColorType.BLUE) {
            return ColorType.BLUE;
        }
        if (maxC == ColorType.RED) {
            if (cbDiff[0] == false) {
                return ColorType.YELLOW;
            }
            return ColorType.RED;
        }
        if (maxC == ColorType.GREEN) {
            if (cbDiff[0] == false) {
                return ColorType.YELLOW;
            }
            return ColorType.GREEN;
        }

        return ColorType.NONE;
    }

    private static int getMaxInd(int[] a) {
        int maxI = 0;
        int maxV = a[maxI];

        for (int i = 1; i < a.length; ++i) {
            if (a[i] > maxV) {
                maxV = a[i];
                maxI = i;
            }
        }

        return maxI;
    }

    private static boolean getDiff(int a, int b, int thresh) {
        if (abs(a - b) > thresh) {
            return true;
        }
        return false;
    }

}
