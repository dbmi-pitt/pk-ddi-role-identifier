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
package relation;

import java.io.Serializable;
import nlp.Word;

/**
 *
 * @author Chinh
 */
public class DDIPair implements Comparable, Serializable{
    public String id ;
    public Word rel=null; // trigger word / relation word
    public Word arg1, arg2; // argument 1 and 2
    public boolean skip = false;// skip this relation if it does not belong to a single sentence
    public boolean init =false ;
    String type ;
    public boolean ddi =false;
    
    // This constructor should be removed later since it is not applicable for DDI
    public DDIPair(Word r, Word a1, Word a2) {
        rel = r;
        arg1 = a1;
        arg2 = a2;
    }

    public DDIPair(String id, Word a1, Word a2,String type, boolean ddi) {
        this.id =id ;
        arg1 = a1;
        arg2 = a2;
        this.type = type ;
        this.ddi = ddi;
    }
    
    /**
     * Note: arg1 and arg2 need to be sorted so arg1 < arg2
     * @return 
     */
    
    
   @Override
    public int compareTo(Object o) {
        DDIPair obj =((DDIPair)o);
        if(arg1.pos==obj.arg1.pos){
            return arg2.pos-obj.arg2.pos;
        }else {
            return arg1.pos-obj.arg1.pos;
        }
    }
    @Override
    public String toString() {
        return arg1.name+"-"+arg1.type+" "+arg2.name+"-"+arg2.type+" "+ddi+" "+type ;
    }
}
