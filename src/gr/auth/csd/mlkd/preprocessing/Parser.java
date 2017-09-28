/*
 * Copyright (C) 2015 user
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
public abstract class Parser {

    public abstract void createDocList(JsonGenerator jGenerator) throws Exception;
    public static void write(JsonGenerator jGenerator, String pmid, String title, String abstractt,
            String year, String journal, THashSet<String> mesh, String fullText) throws IOException {
        jGenerator.writeStartObject();  //each document
        jGenerator.writeFieldName("pmid");
        jGenerator.writeString(pmid);
        jGenerator.writeFieldName("title");
        jGenerator.writeString(title);
        jGenerator.writeFieldName("abstract");
        jGenerator.writeString(abstractt);
        jGenerator.writeFieldName("full_text");
        jGenerator.writeString(fullText);
        jGenerator.writeFieldName("year");
        jGenerator.writeString(year);
        jGenerator.writeFieldName("journal");
        jGenerator.writeString(journal);
        jGenerator.writeArrayFieldStart("meshMajor");
        for (String label : mesh) {
            jGenerator.writeString(label);
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.writeRaw("\n");
    }

    public static void write(JsonGenerator jGenerator, String pmid, THashSet<String> mesh, 
            String fullText) throws IOException {
        jGenerator.writeStartObject();  //each document
        jGenerator.writeFieldName("pmid");
        jGenerator.writeString(pmid);
        jGenerator.writeFieldName("full_text");
        jGenerator.writeString(fullText);
        jGenerator.writeArrayFieldStart("meshMajor");
        for (String label : mesh) {
            jGenerator.writeString(label);
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.writeRaw("\n");
    }
    
    
    public void parse(String outputFile) {
        JsonFactory jfactory = new JsonFactory();
        try (JsonGenerator jGenerator = jfactory.createJsonGenerator(new File(outputFile), JsonEncoding.UTF8)) {
            jGenerator.writeStartObject();
            jGenerator.writeFieldName("documents");
            jGenerator.writeStartArray();

            createDocList(jGenerator);
            jGenerator.writeEndArray();
            jGenerator.writeEndObject();
        } catch (Exception ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
