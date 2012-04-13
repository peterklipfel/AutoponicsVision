package bric.Image;

public class Filter {
  private int[][] filter;
  private int width;
  private int height;
  
  public Filter(int[][] k, int x, int y) {
    filter = k;
    width = x;
    height = y;
  }
  
  public int getWidth() {
      return width;
  }
  public int getHeight() {
      return height;
  }
  
  public int getWeight() {
      int w = 0;
      
      for (int i = 0; i < height; ++i) {
          for (int j = 0; j < width; ++j) {
              w += filter[i][j];
          }
      }
      return w;
  }
  
  public ImageLayer convolve(ImageLayer src, boolean weighted) {
    int[][] s = src.getImage();
    int sW = src.getWidth();
    int sH = src.getHeight();
    int[][] r = new int[sH][sW];  //convolved image; returned
    
    for (int i = 0; i < sH; ++i) { //row
      for (int j = 0; j < sW; ++j) {  //col //loop over image
      
        int sum = 0;
        
        for (int h = 0; h < height; ++h) { //row
          for (int w = 0; w < width; ++w) {  //col // loop over filter
            if ( (i + h - 1) >= 0  && //check upper boundary
                 (i + h - 1) < sH && //check lower boundary
                 (j + w - 1) >= 0  && //check left boundary
                 (j + w - 1) < sW ){ //check right boundary
                
                sum += filter[h][w] * s[i + h - 1][j + w - 1];
            }
            else {
                sum += s[i][j];
            }
          }
        }  //end filter loop
        
        int w = this.getWeight();
        if (weighted && w != 0)
            r[i][j] = sum/w;
        else
            r[i][j] = sum;
      }
    }
    
    return new ImageLayer(r, sW, sH, src.getColor());
  }
  public ImageLayer convolve(ImageLayer src) {
      return convolve(src, true);
  }

}