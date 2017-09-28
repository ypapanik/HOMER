package gr.auth.csd.mlkd.preprocessing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Grigorios Tsoumakas
 * @version 2013.04.16
 */
public class Tokenizer {

    private StanfordCoreNLP pipeline;
    
    public Tokenizer() {
        Properties props = new Properties();
        
        // stanford full
        //props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma");
        props.put("annotators", "tokenize, ssplit");
        //RedwoodConfiguration.empty().capture(System.err).apply();
        pipeline = new StanfordCoreNLP(props);
    }

    public List<String> tokenize(String input) {
        Annotation document = new Annotation(input);
        pipeline.annotate(document);

        //* Stanford full
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> lines = new ArrayList<>();
        StringBuilder builder;
        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            builder = new StringBuilder();
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                builder.append(word.toLowerCase()).append(" ");
                //String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                //String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            }
            lines.add(builder.toString());
        }
        return lines;
    }
}
