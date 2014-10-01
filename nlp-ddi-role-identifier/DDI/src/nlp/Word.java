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

/**
 *
 * @author Chinh
 * @Revise: Oct 3,2012
 */
public class Word implements Comparable, Serializable{
    public String id;
    public String word;
    public int pos = -1; // order of this word in the sentence
    public int [] locs =null; // real position related to full abstract/paragraph
    public String posTag =null; //POS tag for trigger (if two-word trigger, use main word)
    public String type =null; // for drug type
    public String name =null;
    public int c_pos =-1 ; // chunk pos
    public int level=10; // Number of PP between the first NP to the NP containng this word
    public Word(String w, int pos, int[] locs){
        word = w;
        this.pos = pos ;
        this.locs = locs ;
    }

    /**
     * Key for trigger word
     * @return: Trigger + POS  
     */
    public String getKey(){
        return word.toLowerCase()+"_"+posTag;
    }
    /**
     * Get type (int) for trigger.
     * Should not use for drug type(string)
     * @return 
     */
    public int getType(){
        return Integer.parseInt(type);
    }
    //for Drug entity
    public Word(String id, String w, int[] locs){
        this.id =id ;
        word = w;
        this.locs = locs ;
    }
    
    @Override
    public int compareTo(Object o) {
        int ob_locs[] = ((Word)o).locs ;
        if(locs[1]==ob_locs[1]){
            return locs[0]-ob_locs[0];
        }else {
            return locs[1]-ob_locs[1];
        }
    }
    
    @Override
    public String toString(){
        if(name!=null){
           return name+" "+type ; 
        }else {
            return word +" " +type ;
        }
    }
}
