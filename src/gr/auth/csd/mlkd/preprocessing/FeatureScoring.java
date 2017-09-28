package gr.auth.csd.mlkd.preprocessing;


import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Document;
import gr.auth.csd.mlkd.preprocessing.Labels;
import gr.auth.csd.mlkd.preprocessing.NGram;
import gr.auth.csd.mlkd.utils.Utils;
import org.apache.commons.lang3.text.WordUtils;
/**
 *
 * @author user0
 * pos stores the number of instances that have this label
 * tp stores the number of positive instances that have the features
 * fp stores the number of negative instances that have the feature
 * fn = pos -tp
 * tn = neg - fp
 */
public abstract class FeatureScoring {

    protected static int corpusSize;
    protected static Dictionary dictionary;
    protected final boolean zoning;
    
    public FeatureScoring(Dictionary dict, boolean zoning) {
        dictionary = dict;
        corpusSize = dictionary.getCorpusSize();
        this.zoning = zoning;
    }
    public abstract double fsMethod(NGram feature, Document doc, int wf, Labels labels);
    protected double zoning(NGram feature, Document doc, double wf, Labels labels) {
        double wf_temp = wf;
        String token = Utils.concatenateArrayOfStrings(feature.getList()).toLowerCase();
        String title = doc.getTitle().toLowerCase();
        String [] titletokens = title.split(" ");
        for(String tok:titletokens) {
            if(token.contentEquals(tok)) {
                wf_temp*=2;
                break;
            }
        }
        if(labels.getIndex(WordUtils.capitalize(token))!=-1) {
            wf_temp*=1.25;
        }
        return wf_temp;
    }

    public abstract void read(int label, int pos);

    public double fsMethod(int i, int featureFrequency) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}