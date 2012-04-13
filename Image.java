package bric.Image;

import java.util.Arrays;
import java.util.TreeSet;


public class Image {
    TreeSet<Pixel> image;
    private int width;
    private int height;
    
    public Image(ImageLayer r, ImageLayer g, ImageLayer b, int w, int h) {
        //initialize image
        image = new TreeSet<Pixel>();
        Pixel p;
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) {
                p = new Pixel(new int[] {i, j},
                r.getPixel(i, j),     
                g.getPixel(i, j),
                b.getPixel(i, j));
                
                image.add(p);
            }
        
        width = w;
        height = h;
    }
    public Image(ImageLayer r, ImageLayer g, ImageLayer b) {
        width = r.getWidth();   //TODO: Check to make sure dimensions match
        height = r.getHeight();
        
        //initialize image
        image = new TreeSet<Pixel>();
        Pixel p;
        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j) {
                p = new Pixel(new int[] {i, j},
                r.getPixel(i, j),     
                g.getPixel(i, j),
                b.getPixel(i, j));
                
               image.add(p);
            }
    }

    public ImageLayer getLayer(ColorType c) {
        int[][] src = new int[height][width];
        for (Pixel p : image) 
            src[p.getRow()][p.getColumn()] = p.getColor(c);
        
        return new ImageLayer(src, width, height, c);
    }
    public void setLayer(ColorType c, int[][] src) {
        for (Pixel p : image) 
            p.setColor(c, src[p.getRow()][p.getColumn()]);
    }
    
    public ImageLayer getRedLayer() {
        return getLayer(ColorType.RED);
    }
    public void setRedLayer(ImageLayer r) {
        setLayer(ColorType.RED, r.getImage());
    }

    public ImageLayer getGreenLayer() {
        return getLayer(ColorType.GREEN);
    }
    public void setGreenLayer(ImageLayer g) {
        setLayer(ColorType.GREEN, g.getImage());
    }

    public void setBlueLayer(ImageLayer b) {
        setLayer(ColorType.BLUE, b.getImage());
    }
    public ImageLayer getBlueLayer() {
        return getLayer(ColorType.BLUE);
    }

    public void setYellowLayer(ImageLayer y) {
        setLayer(ColorType.YELLOW, y.getImage());
    }
    public ImageLayer getYellowLayer() {
        return getLayer(ColorType.YELLOW);
    }

    public ImageLayer getGreyScaleImage() {
        return getLayer(ColorType.BLACK);
    }
/*    
    public void writeImageToFile(String filePath, ColorType c) {
        ExampleIO e = new ExampleIO(image, "w", fp, c.toString());
        Thread t = new Thread(e);
        t.start();
    } 
    public void writeImageToFile(String filePath) {
        writeImageToFile(filePath, ColorType.NONE);
    }
    public void writeImageToFile() {
        writeImageToFile(System.getProperty("user.dir"));
    }

*/
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] toArray() {
        int[] out = new int[width*height];
        for (Pixel p : image) 
            out[p.getRow() * width + p.getColumn()] = p.toInt();
        
        return out;
    }
    public int[][] toArray2D() {
        int[][] t = new int[height][width];
        for (Pixel p : image) 
            t[p.getRow()][p.getColumn()] = p.toInt();
        
        return t;
    }
    public void show() {
        for (Pixel p : image) 
          p.show();
    }
    
    
    
    public static void main(String... args) {
        int h = 9;
        int w = 9;
        int[][] t = new int[h][w];
        int[][] t2 = new int[h][w];
        for (int i = 0; i < h; ++i) 
            for (int j = 0; j < w; ++j) {
                t[i][j] = (int) (Math.random()*255);
                t2[i][j] = 256 - t[i][j];
            }
        
        ImageLayer r = new ImageLayer(t, w, h, ColorType.RED);
        ImageLayer g = new ImageLayer(t, w, h, ColorType.GREEN);
        ImageLayer b = new ImageLayer(t, w, h, ColorType.BLUE);
        ImageLayer y = new ImageLayer(t, w, h, ColorType.YELLOW);
        
        ImageLayer r2 = new ImageLayer(t2, w, h, ColorType.RED);
        
        Image im = new Image(r, g, b, w, h);
        System.out.println("TEST:");
        im.show();
        System.out.println("Change");
        im.setRedLayer(r2);
        im.show();
        
        System.out.println("NEXT");
        im.setRedLayer(r2);
        im.setGreenLayer(r2);
        im.setBlueLayer(r2);
        im.show();
        
        System.out.println("NEXT");
        im.setGreenLayer(r);
        ImageLayer rl = im.getLayer(ColorType.RED);
        rl.show();
        ImageLayer gl = im.getLayer(ColorType.GREEN);
        gl.show();
        ImageLayer yl = im.getLayer(ColorType.YELLOW);
        yl.show();
        
        System.out.println("NEXT");
        ImageLayer rlT = rl.threshhold(100);
        rlT.show();

        System.out.println("NEXT");
        //ImageLayer rlC = rlT.convolve(new Filter(new int[][] {{1,1,1},{1,1,1},{1,1,1}}, 3, 3));
        //rlC.show();
        //ImageLayer ylC = yl.convolve(new Filter(new int[][] {{1,1,1},{1,1,1},{1,1,1}}, 3, 3));
        //ylC.show();
        int[] tA = new int[w * h];
        for (int i = 0; i < w*h; ++i)
            tA[i] = (int)(Math.random()*2) *255;
        ImageLayer temp = new ImageLayer(tA, w, h, ColorType.NONE);
        ImageLayer tempC = temp.convolve(new Filter(new int[][] {{1,1,1},{1,1,1},{1,1,1}}, 3, 3));
        temp.show();
        tempC.show();
    }
}
