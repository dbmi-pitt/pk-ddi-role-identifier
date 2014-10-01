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

import java.io.Serializable;
import relation.DDIPair;
import utils.Data;

/**
 *
 * @author Chinh
 */
public class Chunk implements Serializable{

    public String type = null; //NP ; VP ; PP ; SBAR ; CC ...
    public String txt = null;
    public int begin = 0; // position of begin chunk
    public int end = 0;// end chunk
    public boolean is_merged = false; // 
    

    public Chunk(String type) {
        this.type = type;
    }

    public void addWord(String w, String space) {
        if (txt == null) {
            txt = w;
        } else {
            txt = txt + space + w;
        }
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return txt;
    }

    /**
     * Merge two chunks: appending a chunk to the current chunk
     *
     * @param c :chunk
     *
     */
    public void merge(Chunk c) {
        end = c.end;
        txt += " " + c.txt;
    }

    public boolean contains(Word key) {
        if (key.pos >= begin && key.pos <= end) {
            return true;
        }
        return false;
    }


    public boolean contains(DDIPair r){
        return contains(r.arg1)&&contains(r.arg2);
    }
    
    
    private boolean hasConj(int pos, String[] tokens) {
        if (pos > 0 && pos < tokens.length) {
            if (Data.ccSet.contains(tokens[pos - 1])) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return   "["+type + " " + txt + "] ";
    }
}
