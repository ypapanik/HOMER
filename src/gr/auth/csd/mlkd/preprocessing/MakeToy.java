package gr.auth.csd.mlkd.preprocessing;

import gnu.trove.set.hash.THashSet;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author Yannis Papanikolaou
 */
public class MakeToy {
    
    private static void write(JsonGenerator jGenerator, String pmid, String title, String abstractt, 
            String journal, Iterable<String> mesh) throws IOException {
        jGenerator.writeStartObject();  //each document
                    jGenerator.writeFieldName("pmid");
                    jGenerator.writeString(pmid);
                    jGenerator.writeFieldName("title");
                    jGenerator.writeString(title);
                    jGenerator.writeFieldName("journal");
                    jGenerator.writeString(journal);
                    jGenerator.writeFieldName("abstract");
                    jGenerator.writeString(abstractt);
            jGenerator.writeArrayFieldStart("meshMajor");
                for(String label:mesh) jGenerator.writeString(label);
            jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.writeRaw("\n");
    }
    
    protected static void bipartitionsWrite(String testFile, Labels labels) {
        CorpusJSON corpus = new CorpusJSON(testFile);
        JsonFactory jfactory = new JsonFactory();
        try (JsonGenerator jGenerator = jfactory.createJsonGenerator
                (new File(testFile+".toy"), JsonEncoding.UTF8)) {
            jGenerator.writeStartObject();
            jGenerator.writeFieldName("documents");
            jGenerator.writeStartArray();
            corpus.reset();
            Document doc;
            while((doc = corpus.nextDocument())!=null) {
                THashSet<String> ls = new THashSet<>();
                boolean empty = true;
                for(String label:doc.getLabels()) {
                    if(labels.getLabels().contains(label)) {
                        ls.add(label);
                        empty = false;
                    }
                }
                if(!empty) write(jGenerator, doc.getId(), doc.getTitle(), doc.getAbs(), doc.getJournal(), ls);
            }
            jGenerator.writeEndArray();
            jGenerator.writeEndObject();
        } catch (IOException ex) {
                Logger.getLogger(MakeToy.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    

    
    public static void main(String[] args) {
        String fileLabels = args[0];
        String testFile = args[1];  
        bipartitionsWrite(testFile, Labels.readLabels(fileLabels));
    }
}
