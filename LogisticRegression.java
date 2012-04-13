/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.LogisticRegression;

//IMPORT
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.Serializable;
import Jama.Matrix;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import static java.lang.Math.*;

/**
 * A <code>LogisticRegression</code> object is used to perform logistic 
 * regression.  Each object consists of a cost function, and a set of hypothesis
 * used for classification.  You must supply a training set of 
 * <code>TrainingExample</code>s to tune the hypothesis.
 * 
 * @author BrianCarlsen
 */

public class LogisticRegression implements Serializable{
    //INSTANCE FIELDS
    private Method costFunction;
    private Hypothesis[] hypothesis;
    private TrainingExample[] trainingSet;
    private double learningRate;
    private double regularizationParam;
     
    //CONSTRUCTORS
    /**
     * 
     * @param lr is the learning rate.
     * @param ts is the training set. 
     */
    public LogisticRegression(double lr, double rp, TrainingExample[] ts) {
        learningRate = lr;
        regularizationParam = rp;
        trainingSet = ts;
       
        // Construct the correct number of hypothesis given 
        // the number of features in the trainingSet
        // and the number of different classifications
        ArrayList<Integer> classifications = new ArrayList<Integer>();
        for (TrainingExample t : trainingSet) {
            Integer c = t.getAnswer();
            if ( !classifications.contains(c) )
                classifications.add(c);
        }

        hypothesis = new Hypothesis[classifications.size() - 0];
        int s = trainingSet[0].getInput().length + 1;

        for (int i = 0; i < classifications.size() - 0; ++i) {
            hypothesis[i] = new Hypothesis(s, classifications.get(i));

        }
                       
        try {
            costFunction = this.getClass().getMethod
                    ("defaultCostFunction", Hypothesis.class);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
    public LogisticRegression(double lr, TrainingExample[] ts) {
        this(lr, 0, ts);
    }
    public LogisticRegression(TrainingExample[] ts) {
        this(1, 0, ts);
    }
    
    /**
     * Prints each hypothesis along with its classification to stdout.
     */
    public void show() {
        for (Hypothesis h: hypothesis) {
            System.out.print(h.getClassification() + ": ");
            h.show();
        }
    }
    
    public double getLearningRate() {
        return learningRate;
    }
    public void setLearningRate(double newRate) {
        learningRate = newRate;
    }
    
    public double getRegularizationParam() {
        return regularizationParam;
    }
    public void setRegularizationParam(double rp) {
        regularizationParam = rp;
    }
    
    /**
     * Classifies the input.  
     * 
     * @param input the values for each feature.
     * @return the classification with the highest probability.
     */
    public int classifyDefinitive(double[] input) {
        HashMap<Integer, Double> p = classify(input);
        double pVal, max = -1;
        int c = -1;
   
        for (Integer i : p.keySet()) {
            pVal = p.get(i);

            if ( pVal > max ) {
                max = pVal;
                c = i;
            }
        }
       
        return c;
    }
    
     /** Classifies the input.
     * 
     * @param input the value for each feature.
     * @return the probability associated with each classification.
     */
    public HashMap<Integer, Double> classify(double[] input) {
        HashMap<Integer, Double> p = new HashMap<Integer, Double>();
        double tProb = 0;
        for (Hypothesis h : hypothesis) {
            double prob = h.predict(input);
            tProb += prob;
            p.put(h.getClassification(), prob);
        }
        for (Integer c : p.keySet()) 
            p.put(c, p.get(c)/tProb);
        
        return p;
    }
    
    /**
     * Runs gradient decent to tune the parameters of each hypothesis.
     * 
     * @param iterations the number of times to run gradient decent
     */
    public void tune(int iterations) {
        for(Hypothesis h : hypothesis) {
            //construct a new training set using One vs. Rest
            // if the training example has the same value as the
            // hypothesis then set the answer to 1
            // otherwise set the answer to 0.
            TrainingExample[] tSet = new TrainingExample[trainingSet.length];
            int answer;
            int i = 0;
            for (TrainingExample t : trainingSet) {
                if (t.getAnswer() == h.getClassification())
                    answer = 1;
                else
                    answer = 0;
                
                tSet[i] = new TrainingExample(t.getInput(), answer);
                ++i;
            }
            
            for(i = 0; i < iterations; ++i) {
                h.gradientDecent(tSet);
            }
        }       
    }
    
    /**
     * Calculates the cost of the <code>trainingSet</code>.
     * 
     * @param hyp the hypothesis to use in calculating the cost.
     * @return the cost associated with the hypothesis.
     */
    public double defaultCostFunction(Hypothesis hyp) {
        double error = 0;
        double h;
        int answer;
        for (TrainingExample t : trainingSet) {
            try {
                 h = (Double) hyp.predict(t.getInput());
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            answer = t.getAnswer();
            error -= answer*log(h) + (1-answer)*log(1-h);
        }
        
        double regError = 0;
        for (int i = 0; i < hyp.getNumFeatures(); ++i) {
            regError += pow( hyp.getParameter(i), 2);
        }
        error += regError/regularizationParam;
        
        return error/(2*trainingSet.length);
    }
    
    public void writeToFile(File f) {
        for (Hypothesis h : hypothesis) {
            h.writeToFile(f);
        }
    }
    public void loadFromFile(File f) {
        ArrayList<Hypothesis> hl = new ArrayList<Hypothesis>();
        String s;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            Hypothesis hyp;
            while ((s = reader.readLine()) != null ) {
                if ( s.equals("") )
                    continue;
                hyp = new Hypothesis(0, 0);
                hyp.loadFromString(s);
                hl.add(hyp);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        hypothesis = new Hypothesis[hl.size()];
        int i = 0;
        for (Hypothesis h : hl) {
            hypothesis[i] = h;
            ++i;
        }
    }
   
    private class Hypothesis implements Serializable{
        //INSTANCE FIELDS
        private Matrix parameter;
        private Method function;
        private int numFeatures;
        private int classification;
        
        //CONSTRUCTOR
        public Hypothesis(int nF, int c) {
            numFeatures = nF;
            classification = c;
            parameter = new Matrix(numFeatures, 1);
            for (int i = 0; i < numFeatures; i++) 
                parameter.set(i, 0, 1);
            
            try {
                function = this.getClass().getMethod
                        ("defaultHypothesis", double[].class);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public int getClassification() {
            return classification;
        }
        
        public int getNumFeatures() {
            return numFeatures;
        }
        
        public double getParameter(int i) {
            return parameter.get(i, 0);
        }
      
        /**
         * Prints the values of the hypothesis' parameters.
         */
        public void show() {
            parameter.print(5, 3);
        }
        
        /**
         * 
         * @param input is an array of matrices, each of which represents
         * the values to input into that feature
         *  
         * @return Returns the probability that the input is of this class 
         */
        public double predict(double[] input) {
            Double p;
            try {
                p = (Double) function.invoke(this, input);
            }
            catch(Exception e) {
                e.printStackTrace();
                p = new Double(-1);
            }

            return p;
        }
        
        /**
         * Runs gradient decent on the hypothesis
         * @param tSet the training set to be used
         */
        private void gradientDecent(TrainingExample[] tSet) {
            double h, val, newVal;
            int answer;
            double lm = LogisticRegression.this.learningRate/ tSet.length;
            
            for (int i = 0; i < numFeatures; ++i) {
                val = 0;
                for( TrainingExample t : tSet) {
                    answer = t.getAnswer();
                    h = predict(t.getInput());
                    if (i == 0) 
                        val += (h - answer);
                    else
                        val += (h - answer)*t.getInput()[i - 1];
                }
               
                newVal = parameter.get(i, 0) * 
                        (1 - lm *LogisticRegression.this.getRegularizationParam());
                newVal -= lm * val;
           
                parameter.set(i, 0, newVal);            
            }       
        }
        
        public double defaultHypothesis(Matrix input) 
                throws IllegalArgumentException {
            double z = ((parameter.transpose()).times(input)).get(0,0);
            return 1/(1 + exp(-z) );
        }
        public double defaultHypothesis(double[] input) {
            Matrix in = createParamMatrix(input);
            return defaultHypothesis(in);
        }
        
        
        private Matrix createParamMatrix(double[] input) {
            double[][] t = new double[input.length + 1][1];
            t[0][0] = 1;
            for(int j = 0; j < input.length; ++j)
                t[j + 1][0] = input[j]; 
            
            return new Matrix(t);
        }
        
        public void writeToFile(File f) {
            try {
                PrintWriter fw = new PrintWriter(new
                        FileWriter(f, true));
                String s = Integer.toString(numFeatures) + ", ";
                s += Integer.toString(classification) + "; ";
                for (int i = 0; i < parameter.getRowDimension(); ++i)
                    s += parameter.get(i, 0) + ", ";        
                fw.println(s +"\n");

                fw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void loadFromFile(File f) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String s = reader.readLine();
                String[] ins = s.split("[,;] ");
                numFeatures = new Integer(ins[0]);
                classification = new Integer(ins[1]);
                
                double[][] d = new double[ins.length - 2][1];
                for (int i = 2; i < ins.length; ++i) 
                    d[i - 2][0] = new Double(ins[i]);
                Matrix m = new Matrix(d);
                parameter = m;                    
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
         public void loadFromString(String s) {
            try {
                String[] ins = s.split("[,;] ");
                ArrayList<String> inl = new ArrayList<String>();
                for (String in : ins) {
                    in.trim();
                    if (! in.isEmpty())
                        inl.add(in);
                }
         
                numFeatures = new Integer(inl.get(0));
                classification = new Integer(inl.get(1));
                
                double[][] d = new double[inl.size() - 2][1];
                for (int i = 0; i < numFeatures; ++i) 
                    d[i][0] = new Double(inl.get(i+ 2));
                Matrix m = new Matrix(d);
                parameter = m;                    
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        int n = 3;
        int f = 1;
 
        TrainingExample[] t = new TrainingExample[n];  
        
        double[] i0 = {0, 0};
        t[0] = new TrainingExample(i0, 0);
        
        double[] i1 = {1, 0};
        t[1] = new TrainingExample(i1, 1);
        
        double[] i2 = {2, 1};
        t[2] = new TrainingExample(i2, 3);
        
        LogisticRegression l = new LogisticRegression(1, .1, t);
        l.tune(100);
        System.out.println(l.classifyDefinitive(i1));    
        l.show();
        
        try {
            File file = new File("C:\\Users\\BrianCarlsen\\Documents\\testExample.txt");
            l.writeToFile(file);
            
            LogisticRegression l2 = new LogisticRegression(new TrainingExample[] {t[0]});
            l2.loadFromFile(file);
            
            l2.show();
             System.out.println(l2.classifyDefinitive(i1));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
