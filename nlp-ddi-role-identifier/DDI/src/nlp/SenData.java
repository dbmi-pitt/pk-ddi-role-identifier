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
import java.util.List;
import relation.DDIPair;

/**
 * Data related to a given sentence:
 * Tokens, white spaces, POS, offset, len, text
 * @author Chinh
 */
public class SenData implements Serializable{
   public String senID;
   public String text ;// sentence
   public String long_text;
   public int offset=0, len=0 ;// offset and length of the sentence
   public String [] tokens, whites ;// token and white space list
   public String [] POS ;// POS tags
   public boolean init =false ;
   public boolean skip =false ;
   public List<Chunk> chunks = null ;
   public List<Word> drugList=null;
   public List<Word> prepList=null;
   public List<Word> negList =null;
   public List<Word> connector = null;
   public List<DDIPair> ddiList =null;
   public List<Word> relList =null;
   public List<Word> ccList =null;
   public List<Word> commaList =null;
   public SenData(String txt, int off, String ID){
       text = txt;
       offset = off;
       len = text.length();
       senID = ID ;
   }
   
}
