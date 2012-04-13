/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bric.Image;

import bric.LogisticRegression.TrainingExample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author BrianCarlsen
 */
public class TrainingExAdapter {
    private static TrainingExample loadToTrainingEx(String in) {
        String[] inputS = in.split("[,;] ");
        if (inputS.length <= 2)
            return null;
        
        String ansS = inputS[inputS.length - 1];
        double[] input = new double[inputS.length - 1];
        int ans;
        
        for (int i = 0; i < inputS.length - 1; ++i) {
            input[i] = new Double(inputS[i]);
        }
        ans = new Integer(ansS);
        
        return new TrainingExample(input, ans);
    }
    
    public static ArrayList<TrainingExample> toTrainingEx (File f) {
        ArrayList<TrainingExample> tex = new ArrayList<TrainingExample>();
        String s;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while ((s = reader.readLine()) != null) {
                TrainingExample t = loadToTrainingEx(s);
                if (t != null)
                    tex.add(t);
            }            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return tex;
    }
    
    
    public static void main(String... args) {
        String fp = System.getProperty("user.dir");
        File f = new File(fp + "\\webotsTestFile.txt");
        ArrayList<TrainingExample> t  = TrainingExAdapter.toTrainingEx(f);
        for (TrainingExample e : t)
            e.show();
    } 
}
