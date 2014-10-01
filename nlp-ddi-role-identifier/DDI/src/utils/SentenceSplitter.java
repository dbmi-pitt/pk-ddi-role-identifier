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
package utils;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import edu.mayo.bmi.nlp.tokenizer.Token;
import edu.mayo.bmi.nlp.tokenizer.TokenizerPTB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Chinh
 * @Date: Oct 28, 2010
 */
public class SentenceSplitter {

    
    static final TokenizerFactory TOKENIZER = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
    static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER, SENTENCE_MODEL);
    static final TokenizerPTB PTB = new TokenizerPTB();
   
    public static List<String> senSpliter(String str) {
        char cc[] = str.toCharArray();
        List<String> list = new ArrayList<String>();
        Chunking chunks = SENTENCE_CHUNKER.chunk(cc, 0, cc.length);
        Set<Chunk> ls = chunks.chunkSet();
        String sub;
        if (ls.size() < 1) {
            System.out.println("No sentence found.");
            return list;
        }
        String sub_sen = chunks.charSequence().toString();
        for (Iterator<Chunk> it = ls.iterator(); it.hasNext();) {
            Chunk sentence = it.next();
            int start = sentence.start();
            int end = sentence.end();
            sub = sub_sen.substring(start, end);
            //st = sub.split("\n");                    
            list.add(sub);
        }
        return list;
    }

    public static List<String> splitWords(String txt){
        List<String> list = new ArrayList<String>();
        List<Token> ls = PTB.tokenize(txt);
        for(Token ob: ls){
            list.add(ob.getText());
        }
        return list ;
    }
    
   
    
    public static List<String>[] wordSpliter(String txt) {
        List<String> ls[] = new ArrayList[2];
        ls[0] = new ArrayList<String>();
        ls[1] = new ArrayList<String>();
        char cc[] = txt.toCharArray();
        Tokenizer tk = TOKENIZER.tokenizer(cc, 0, cc.length);
        tk.tokenize(ls[0], ls[1]);
        return ls;
    }

    public static String[] tokenize(String txt) {
        char cc[] = txt.toCharArray();
        Tokenizer tk = TOKENIZER.tokenizer(cc, 0, cc.length);
        return tk.tokenize();
        
    }
    public static void main(String[] args) {
        String txt = "DRUG1 administration to three cancer patients over a dose range of 0.025 mg to 2.2 mg led to a dose-dependent inhibition of DRUG2 elimination.14 The effect of alternate-day administration of 0.25 mg of DRUG3 on drug metabolism in MS patients is unknown.";
        List<String> sen = SentenceSplitter.senSpliter(txt);
        System.out.println(sen);
    }
}
