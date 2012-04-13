package bric.Image;


public class ImageLayer {

    private int[][] image;
    private int width;
    private int height;
    ColorType color;

    public ImageLayer(int[] src, int w, int h, ColorType c) {
        image = new int[h][w];
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                image[i][j] = src[i * w + j];
            }
        }

        width = w;
        height = h;
        color = c;
    }

    public ImageLayer(int[][] src, int w, int h, ColorType c) {
        image = src;
        width = w;
        height = h;
        color = c;
    }

    public ImageLayer(int w, int h, ColorType c) {
        image = new int[h][w];
        width = w;
        height = h;
        color = c;
    }

    public int[][] getImage() {
        return image;
    }

    public void setImage(int[][] i) {
        image = i;
    }

    public void setImage(int[] src) {
        // TODO: check dimensions
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                image[i][j] = src[i * width + j];
            }
        }
    }
    public void clearImage() {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                image[i][j] = 0;
            }
        }
    }

    public int getPixel(int r, int c) {
        return image[r][c];
    }

    public void setPixel(int r, int c, int val) {
        image[r][c] = val;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getDimension() {
        int[] dim = {width, height};
        return dim;
    }

    public ColorType getColor() {
        return color;
    }

    public void setColor(ColorType c) {
        color = c;
    }

    public int[] toArray() {
        int[] r = new int[width * height];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                r[i * width + j] = image[i][j];
            }
        }

        return r;
    }

    public ImageLayer convolve(Filter f) {
        return f.convolve(this);
    }

    public ImageLayer threshhold(int thresh) {
        ImageLayer tImg = new ImageLayer(width, height, color);
        
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int val = image[i][j];
                if (val > thresh) 
                    tImg.setPixel(i, j, val);   
            }
        }
        
        return tImg;
    }
    
    public void writeImageToFile(String fp) {
        
        ExampleIO e = new ExampleIO(image, "w", fp, color.toString());
        Thread t = new Thread(e);
        t.start();
    }
    
    public void show() {
        System.out.println(color.toString());
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                System.out.printf(" %03d", image[i][j]);
            }
            System.out.println();
        }
    }
}