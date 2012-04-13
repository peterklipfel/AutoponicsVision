/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.LogisticRegression;

/**
 * A <code>TrainingExample</code> is used to tune the hypothesis of a
 * <code>LogisticeRegression</code>.  Each object consists of inputs 
 * for each feature and the correct classification.
 * Note: The classifications must be consecutive starting from 0;
 * @author BrianCarlsen
 */
public class TrainingExample {
    //INSTANCE FIELDS
    private double[] input;
    private int answer;
    
    //CONSTRUCTOR
    public TrainingExample(double[] i, int a) {
        input = i;
        answer = a;
    }
    
    public double[] getInput() {
        return input;
    }
    public void setInput(double[] i) {
        input = i;
    }
    
    public int getAnswer() {
        return answer;
    }
    
    public void show() {
        System.out.print("Answer: " + answer + "\t");
        for (double d : input)
            System.out.print(d + ", ");
        System.out.println();
    }
    
    @Override
    public String toString() {
        String s = "Answer: " + answer + "\t";
        for (double d : input)
             s += (d + ", ");
         s = s.substring(0, s.length() - 2);
        return s;
    }
    
    
    public static void main(String...args) {
        
    }
}
