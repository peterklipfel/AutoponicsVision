/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Image;

/**
 *
 * @author BrianCarlsen
 */
public class Pixel implements Comparable<Pixel> {
    private int[] position;  //row, col
    private int red;
    private int green;
    private int blue;
    
    public Pixel (int[] p, int r, int g, int b) {
        position = p;
        red = r;
        green = g;
        blue = b;
    }
    public Pixel (int[] p, int... rgb) {
        position = p;
        //TODO throw exception
        if (rgb.length != 3) {
            red = 0; 
            green = 0;
            blue = 0;
        }
        else {
            red = rgb[0];
            green = rgb[1];
            blue = rgb[2];
        }     
    }
    public Pixel(int[] p) {
        this(p, 0, 0, 0);
    }
    
    public int[] getPosition() {
        return position;
    }
    public void setPosition(int r , int c) {
        position[0] = r;
        position[1] = c;
    }
    public void setPosition(int[] rc) {
        position = rc;
    }
    
    public int getRow() {
        return position[0];
    }
    public void setRow(int r) {
        position[0] = r;
    }
    
    public int getColumn() {
        return position[1];
    }
    public void setColumn(int c) {
        position[1] = c;
    }
    
    public int getRed() {
        return red;
    }
    public void setRed(int r) {
        red = r;
    }
    
    public int getGreen() {
        return green;
    }
    public void setGreen(int g) {
        green = g;
    }
    
    public int getBlue() {
        return blue;
    }
    public void setBlue(int b) {
        blue = b;
    }
    
    public int getColor(ColorType c) {
         if (c == ColorType.RED)
            return red;
        else if (c== ColorType.GREEN)
            return green;
        else if (c == ColorType.BLUE)
            return blue;
        else if (c == ColorType.YELLOW) 
            return (red + green)/2;
        else if (c == ColorType.BLACK || c == ColorType.WHITE) 
            return getGreyScale();
        else 
            return -1;
    }
    public int[] getColors() {
        return new int[] {red, green, blue};
    }
    public void setColor(ColorType c, int val) {
        if (c == ColorType.RED)
            red = val;
        else if (c== ColorType.GREEN)
            green = val;
        else if (c == ColorType.BLUE)
            blue = val;
        else if (c == ColorType.YELLOW) {
            red += val;
            green += val;
        }
        else if (c == ColorType.BLACK || c == ColorType.WHITE) {
            red = val;
            green = val;
            blue = val;
        }
        else  //TODO: throw error
            System.out.println("No Color");
    }
    
    public int getGreyScale() {
        return (red + green + blue) / 3;
    }
    
    public int toInt() {
        int out = (red << 16) & 0x00ff0000;
        out |= (green << 8) & 0x0000ff00;
        out |= blue & 0x000000ff;
        
        out |= 0xff000000;
        return out;
    }
    
    public void show() {
        System.out.print("Position: " + position[0] + ", " + position[1]);
        System.out.println("\tColors: " + red + ", " + green + ", " + blue);
    }
    @Override
    public int compareTo(Pixel other) {
        //sort by row then column
        int[] oPos = other.getPosition();
        
        //rows equal
        if (position[0] == oPos[0]) {
            if (position[1] < oPos[1])
                return -1;
            else if (position [1] > oPos[1])
                return 1;
            else 
                return 0;
        }
        else {   //rows not equal
            if (position[0] < oPos[0])
                return -1;
            else
                return 1;
        }
    }
    
    public boolean equals(Pixel other) {
        if (position[0] == other.getRow() &&
            position[1] == other.getColumn() &&
            red == other.getRed() &&
            green == other.getGreen() &&
            blue == other.getBlue()) 
                return true;
        return false;
    }
    
    
    
    public static void main(String... args) {
        Pixel p1 = new Pixel(new int[] {2, 1});
        Pixel p2 = new Pixel(new int[] {1, 2});
        
        System.out.println(p1.compareTo(p2));
        
        System.out.println("NEXT");
        p1.setColor(ColorType.RED, 255);
        p1.setGreen(255/2);
        p1.show();
        
        System.out.println("\n" + Integer.toHexString(p1.toInt()));
    }
}
