/*
 * Copyright 2014 Chinh Bui.
 * Email: bqchinh@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nlp;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.util.FastCache;
import com.aliasi.util.Streams;
import edu.mayo.bmi.nlp.tokenizer.Token;
import edu.mayo.bmi.nlp.tokenizer.TokenizerPTB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

/**
 *
 * @author Chinh
 */
public final class ShallowParser {

    public ShallowParser() {
        
    }
    /**
     * @param args the command line arguments
     */
    ChunkerME chunker;
    HmmDecoder tagger;
    public String old_txt = null;
    @SuppressWarnings("CallToThreadDumpStack")
    SenData sen;
    TokenizerPTB PTB = new TokenizerPTB();
    private boolean init =false;
    public void initParser() {
        if(init){
            return ;
        }
        InputStream chunk_modelIn = null;
        try {
            System.out.println("Loading parser data....");
            FastCache<String, double[]> cache = new FastCache<String, double[]>(50000);
            FileInputStream fileIn = new FileInputStream("lib/model/pos-en-bio-genia.HiddenMarkovModel");
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
            Streams.closeQuietly(objIn);
            tagger = new HmmDecoder(hmm, null, cache);
            // chunker
            chunk_modelIn = new FileInputStream("lib/model/en-chunker.bin"); // biomedical model
            ChunkerModel chunk_model = new ChunkerModel(chunk_modelIn);
            chunker = new ChunkerME(chunk_model);
            System.out.println("Loading data ... done!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (chunk_modelIn != null) {
                try {
                    chunk_modelIn.close();
                } catch (IOException e) {
                }
            }
        }
        init =true ;
    }

    public List<String[]> tokenize(String txt) {
        List<Token> ls = PTB.tokenize(txt);
        String token[] = new String[ls.size()];
        String whites[] = new String[ls.size()];
        int prev = 0;
        for (int i = 0; i < ls.size(); i++) {
            Token tk = ls.get(i);
            token[i] = tk.getText();
            if (prev == tk.getStartOffset()) {
                whites[i] = "";
            } else {
                whites[i] = txt.substring(prev, tk.getStartOffset());
            }
            prev = tk.getEndOffset();
        }
        List<String[]> list = new ArrayList<String[]>();
        list.add(token);
        list.add(whites);
        return list;
    }

    public String[] POSTag(String tokens[]) {
        List<String> tokenList = Arrays.asList(tokens);
        Tagging<String> tagging = tagger.tag(tokenList);
        String pos_tags[] = tagging.tags().toArray(new String[tokens.length]);
        return pos_tags;
    }

    
    /**
     * Split text into tokens, assign POS tags
     *
     * @param txt: sentence
     * @return: list of words (tokens)
     */
    public void initSen(SenData sen) {
        List<String[]> ls = tokenize(sen.text);
        sen.tokens = ls.get(0);
        sen.whites = ls.get(1);
        sen.init = true;
        sen.POS = POSTag(sen.tokens);
        this.sen = sen;
    }

    /**
     * Shallow parser: generating chunks (NP,VP, PP) from un-token text
     *
     * @param token: array of word
     * @return: list of chunks
     */
    public List<Chunk> parse(SenData sen) {
        if (!sen.init) {
            initSen(sen);
        }
        return parse(sen.tokens, sen.POS, sen.whites);
    }

    private void groupVerbChunk(List<Chunk> ls, String[] pos_tags) {
        int i = 0;
        Chunk c, prev = null, next;
        List<Chunk> remove = new ArrayList<Chunk>();
        while (i < ls.size()) {
            c = ls.get(i);
            if (c.type.equals("VP") && c.begin == c.end) {
                if (!(pos_tags[c.begin].startsWith("VB") || pos_tags[c.begin].startsWith("MD"))) { // not a VP, merger with previous chunk
                    if (prev != null && prev.type.equals("NP")) {
                        prev.merge(c);
                        remove.add(c);
                        i++;
                        if (i < ls.size()) {
                            c = ls.get(i);
                            if (c.type.equals("NP")) {
                                prev.merge(c);
                                remove.add(c);
                            }
                            i++;
                            if (i < ls.size()) {
                                c = ls.get(i);
                            }
                        }
                    } else if (i + 1 < ls.size()) {
                        next = ls.get(i + 1);
                        if (next.type.equals("NP")) {
                            c.merge(next);
                            c.type = "NP";
                            remove.add(next);
                            i++;
                            if (i + 1 < ls.size()) {
                                c = ls.get(i + 1);
                            }
                        } else if (pos_tags[c.begin].startsWith("NN")) {
                            c.type = "NP";
                        } else {
                            c.type = "O";
                        }
                    } else {
                        System.out.println("PARSER: Fixing verb phrase: ----->Unknown case: " + c.getText());
                    }
                }
            } else if (c.type.equals("NP") && c.begin == c.end) {
                if (pos_tags[c.begin].startsWith("VB")) {
                    c.type = pos_tags[c.begin];
                }
            }
            // now remove ADVP in front of VP
            if (c.type.equals("VP")) {
                if (i < ls.size() - 1) { // behind
                    next = ls.get(i + 1);
                    if (next.type.equals("ADJP")) {
                        c.merge(next);
                        remove.add(next);
                        i++;
                    } else if (next.type.equals("VP") && next.txt.startsWith("to ")) {
                        c.merge(next);
                        remove.add(next);
                        i++;
                    }else if(next.type.startsWith("AD")){
                        if(i< ls.size()-2){
                            Chunk next2 = ls.get(i+2);
                            if(next2.type.startsWith("AD")||
                               next2.type.equals("VP") && next2.txt.startsWith("to ")){
                                c.merge(next);
                                remove.add(next);
                                c.merge(next2);
                                remove.add(next2);
                                i+=2;
                            }
                        }
                    }
                }
            }
            prev = c;
            i++;
        }
        for (Chunk ch : remove) {
            ls.remove(ch);
        }

    }

    /**
     * Check CONJ in a chunk
     *
     * @param p1
     * @param p2
     * @param tokens
     * @return
     */
    private int hasConj(int p1, int p2, String[] tokens) {
        for (int i = p2 - 1; i > p1; i--) {
            if (map.contains(tokens[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check CONJ in a chunk
     *
     * @param p1
     * @param p2
     * @param tokens
     * @return
     */
    private int hasComma(int p1, int p2, String[] tokens, String tags[]) {
        int c_count = 0;
        int a_count = 0;
        int c_pos = 0;
        int a_pos = 0;
        for (int i = p1 + 1; i <= p2; i++) {
            if (tokens[i].equals(",")) {
                c_count++;
                c_pos = i;
            } else if (map.contains(tokens[i])) {
                a_count++;
                a_pos = i;
                if (a_pos - c_pos > 1 && c_count == 1 && a_count == 1) {
                    if (c_pos > p1 && a_pos < p2) {
                        if (tags[c_pos - 1].startsWith("NN") && tags[a_pos - 1].startsWith("NN") && tags[a_pos + 1].startsWith("NN")) {
                            return c_pos;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private int hasComma(int p1, int p2, String[] tokens) {
        int c_count = 0;
        int pos = -1;
        for (int i = p1 + 1; i <= p2; i++) {
            if (tokens[i].equals(",")) {
                c_count++;
                pos = i;
            }
        }
        if (c_count == 1) {
            return pos;
        }
        return -1;
    }

    private String formTxt(int start, int end, String[] tokens, String whites[]) {
        String txt = tokens[start];
        for (int i = start + 1; i <= end; i++) {
            txt += whites[i] + tokens[i];
        }
        return txt;
    }

    private void fixConj(List<Chunk> ls, String tokens[], String whites[], String tags[]) {
        int pos;
        int j = 0;
        String new_txt;
        try {
            while (j < ls.size() - 2) {
                Chunk c = ls.get(j);
                Chunk c1 = ls.get(j + 1);
                Chunk c2 = ls.get(j + 2);
                if ((c.txt.equals("of") && c1.type.equals("NP") && c2.type.equals("VP"))) {
                    pos = hasComma(c1.begin, c1.end, tokens, tags);
                    if (pos != -1 && !c1.txt.contains("(") && !c1.txt.contains(")")) {
                        new_txt = formTxt(pos + 1, c1.end, tokens, whites);
                        c1.txt = formTxt(c1.begin, pos - 1, tokens, whites);
                        Chunk con_chunk = new Chunk("O");
                        con_chunk.begin = pos;
                        con_chunk.end = pos;
                        con_chunk.txt = tokens[pos];
                        Chunk new_chunk = new Chunk("NP");
                        new_chunk.begin = pos + 1;
                        new_chunk.end = c1.end;
                        new_chunk.txt = new_txt;
                        c1.end = pos - 1;
                        ls.add(j + 2, new_chunk);
                        ls.add(j + 2, con_chunk);
                        j += 3;
                        //debug = true;
                        continue;
                    }
                } else if ((c.type.equals("VP") && c1.type.equals("NP") && c2.type.equals("VP"))) {
                    pos = hasConj(c1.begin, c1.end, tokens);
                    int comma = hasComma(c1.begin, c1.end, tokens);
                    if (pos != -1 && comma == -1) {
                        System.out.println("--> "+c1);
                        new_txt = formTxt(pos + 1, c1.end, tokens, whites);
                        c1.txt = formTxt(c1.begin, pos - 1, tokens, whites);
                        Chunk con_chunk = new Chunk("O");
                        con_chunk.begin = pos;
                        con_chunk.end = pos;
                        con_chunk.txt = tokens[pos];
                        Chunk new_chunk = new Chunk("NP");
                        new_chunk.begin = pos + 1;
                        new_chunk.end = c1.end;
                        new_chunk.txt = new_txt;
                        c1.end = pos - 1;
                        ls.add(j + 2, new_chunk);
                        ls.add(j + 2, con_chunk);
                        j += 3;
                        debug =true ;
                        continue;
                    } 
                }
                j++;
            }
        } catch (Exception ex) {
            System.out.println("Error at: " + j);
            printChunk(ls);
            System.out.println(ex.getLocalizedMessage());
            System.exit(0);
        }
    }

    public List<Chunk> parse(String[] token, String tags[], String whites[]) {
        List<Chunk> result = new ArrayList<>();
        String chunks[] = chunker.chunk(token, tags);
        String previous = "";
        Chunk chunk = null;
        int i = 0;
        for (String s : chunks) {
            if (s.startsWith("B-")
                    || (s.startsWith("I-") && !previous.endsWith(s.substring(2)))) {
                if (chunk != null) {
                    chunk.end = i - 1;
                    result.add(chunk);
                }
                chunk = new Chunk(s.substring(2));
                chunk.begin = i;
                chunk.addWord(token[i],whites[i]);
            }else if(s.startsWith("I-") && chunk!=null){
                chunk.addWord(token[i],whites[i]);
            }else if(s.startsWith("O")){
                if(chunk!=null){
                    chunk.end = i - 1;
                    result.add(chunk);
                }
                chunk = new Chunk("O");
                chunk.begin = i;
                chunk.end = i;
                chunk.addWord(token[i], whites[i]);
                result.add(chunk);
                chunk =null ;
            }else {
                System.out.println("Unknown cases: ");
            }
            previous = s;
            i++;
        }
        if(chunk!=null){
            chunk.end =i-1;
            result.add(chunk);
        }
//        System.out.println("-----------");
//        printChunk(result);
//        System.out.println("-----------");
        groupVerbChunk(result, tags); // fix verb chunk problems
        fixParenthesis(result, whites); // fix parentheses problems
        return result;
    }

    private void fixParenthesis(List<Chunk> ls, String whites[]) {
        int i = 0;
        Chunk tmp, prev = null;
        while (i < ls.size() - 1) {
            tmp = ls.get(i);
            String close;
            if (tmp.txt.contains("(") || tmp.txt.contains("[")) {
                List<Chunk> remove = new ArrayList<>();
                boolean found = false;
                int j = i + 1;
                if (tmp.txt.contains("(")) {
                    close = ")";
                } else {
                    close = "]";
                }
                while (j < ls.size()) {
                    Chunk c = ls.get(j);
                    if (c.txt.contains(close)) {
                        found = true;
                        break;
                    } else {
                        j++;
                    }
                }
                if (prev != null && prev.type.equals("NP") && found) {
                    for (int k = i; k <= j; k++) {
                        Chunk c = ls.get(k);
                        int idx = c.begin;
                        if (idx < whites.length) {
                            prev.txt = prev.txt + whites[c.begin] + c.txt;
                            prev.end = c.end;
                            prev.is_merged =true;
                            remove.add(c);
                        } else {
                            System.out.println(prev.txt + "Index: " + idx + " " + c.txt + " Error at: " + k + " Chunk len: " + ls.size() + " space: " + whites.length);
                            System.exit(1);
                        }
                    }
                    for (Chunk c : remove) {
                        ls.remove(c);
                    }
                } else if (found) {
                    for (int k = i + 1; k <= j; k++) {
                        Chunk c = ls.get(k);
                        tmp.txt = tmp.txt + whites[c.begin] + c.txt;
                        tmp.end = c.end;
                        tmp.is_merged =true;
                        remove.add(c);
                    }
                    for (Chunk c : remove) {
                        ls.remove(c);
                    }
                }
                if (found) {
                    i++;
                    continue;
                }
            } else if (tmp.txt.equals(")") || tmp.txt.equals("]")) {
                if (prev != null) {
                    prev.txt = prev.txt + whites[tmp.begin] + tmp.txt;
                    prev.end = tmp.end;
                    prev.is_merged =true;
                    ls.remove(tmp);
                    i++;
                    continue;
                } else {
                    System.out.println("--->" + old_txt);
                    printChunk(ls);
                    System.out.println("--BUG- here:" + tmp.getText() + " POS: " + tmp.begin);
                    System.exit(1);
                }

            } else if (prev != null && prev.txt.equals("but")) {
                boolean found = false;
                if (tmp.txt.equals("not")) {
                    prev.txt = prev.txt + whites[tmp.begin] + tmp.txt;
                    prev.end = tmp.end;
                    ls.remove(tmp);
                    found = true;
                } else if (tmp.type.equals("NP") && tmp.txt.startsWith("not ")) {
                    prev.txt = prev.txt + " " + "not";
                    prev.end += 1;
                    tmp.txt = tmp.txt.substring(4);
                    tmp.begin += 1;
                    found = true;
                }
                if (found) {
                    continue;
                }
            } else if (prev != null && (prev.txt.equals("as well")
                    || (prev.txt.equals("as") && tmp.txt.startsWith("well")))) {
                if (prev.txt.equals("as well") && tmp.txt.equals("as")) {
                    prev.type = "CONJP";
                    prev.txt = "as well as";
                    prev.end += 1;
                    ls.remove(tmp);
                    continue;
                } else if (prev.txt.equals("as") && tmp.txt.equals("well") && ls.get(i + 1).txt.equals("as")) {
                    prev.type = "CONJP";
                    prev.txt = "as well as";
                    prev.end += 2;
                    ls.remove(i);
                    ls.remove(i);
                    continue;
                } else if (prev.txt.equals("as") && tmp.txt.equals("well as")) {
                    prev.type = "CONJP";
                    prev.txt = "as well as";
                    prev.end += 2;
                    ls.remove(i);
                    continue;
                } else if (prev.txt.equals("as") && tmp.txt.startsWith("well as") && tmp.type.equals("NP")) {
                    prev.type = "CONJP";
                    prev.txt = "as well as";
                    prev.end += 2;
                    tmp.txt = tmp.txt.substring(8);
                    tmp.begin += 2;
                }
            }else if(prev!=null && prev.type.equals("ADVJ") && tmp.type.equals("PP")){
                prev.type = "PP";
                prev.txt += " "+tmp.txt;
                prev.end = tmp.end;
                ls.remove(i);
                continue;
            }
            prev = tmp;
            i++;
        }
    }
    public int total = 0;

    public void printChunk(List<Chunk> ls) {
        Chunk c;
        for (int k = 0; k < ls.size(); k++) {
            c = ls.get(k);
            System.out.print("[" + c.type + " " + c.txt + "] ");
        }
        System.out.println("");
    }
    public String id;
    public int senid;
    public static final Set<String> map = new HashSet<>();
    private static final String conj_list[] = {"and", "or", "but not", "as well as"};
    static {
        map.addAll(Arrays.asList(conj_list));
    }
    boolean debug = false;

    public void test(String txt) {
        debug = true;
        SenData sendata = new SenData(txt, 0, "ID");
        List<Chunk> chunks = parse(sendata);
        for(int i=0;i<sendata.tokens.length;i++){
            System.out.print(sendata.tokens[i]+"-"+sendata.POS[i]+"  ");
        }
        printChunk(chunks);
    }

    public void testALL() {
        try {
            File file = new File("D:/Output/true_sentences.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String txt;
            while ((txt = reader.readLine()) != null) {
                if (txt.length() > 10) {
                    test(txt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // TODO code application logic here
        ShallowParser parser = new ShallowParser();
        parser.initParser();
        String s = "Concurrent use of DRUG1 with DRUG2 may increase the effect of DRUG3.";
        System.out.println(s);
        List<String[]> ls = parser.tokenize(s);
        
        StringBuilder sb = new StringBuilder();
        String tokens[]=ls.get(0);
        String whites[] =ls.get(1);
        for(int i=0;i<tokens.length;i++){
            sb.append(whites[i]);
            sb.append(tokens[i]);
        }
        if(sb.toString().length()!=s.length()){
            System.out.println("BUG");
        }
        parser.test(s);
    }
}
