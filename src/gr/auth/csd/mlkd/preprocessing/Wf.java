package gr.auth.csd.mlkd.preprocessing;

import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Document;
import gr.auth.csd.mlkd.preprocessing.Labels;
import gr.auth.csd.mlkd.preprocessing.NGram;



/**
 * @author Ioannis Papanikolaou
 */
public class Wf extends FeatureScoring {

    public Wf(Dictionary dict) {
        super(dict, true);
        
    }
    
    @Override
    public double fsMethod(NGram feature, Document doc, int wf, Labels labels) {
        return 1;
    }

    @Override
    public void read(int label, int pos) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}