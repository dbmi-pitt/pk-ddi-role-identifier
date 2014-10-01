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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Feature index
 * @author Chinh
 */
public class FeatureIndex implements Serializable{
  private int index=0;
  private final TreeMap<String, Integer> values = new TreeMap<>();
  /**
   * Add (new) feature into the feature list
   * @param ft
   * @return: index of feature 
   */
  public int add(String ft){
      String val = ft.toLowerCase();
      if(values.containsKey(val)){
          return values.get(val);
      }else {
          index++;
          values.put(val, index);
          return index;
      }
  }
  public int getIndex(String ft){
      String val = ft.toLowerCase();
      if(values.containsKey(val)){
          return values.get(val);
      }else {
          return -1;
      }
  }
  public int getSize(){
      return index;
  }
  
  public void saveFeatures(String path){
      try {
          PrintWriter out   = new PrintWriter(new BufferedWriter(new FileWriter(path)));
          for(String s: values.keySet()){
              Integer i = values.get(s);
              out.println(i+"\t"+s);
          }
          out.close();
      } catch (Exception ex) {
          
      }
  }
  public Map<String,Integer> readFeatures(String path){
      try{
          Map<String, Integer> map = new HashMap<>();
          BufferedReader reader = new BufferedReader(new FileReader(path)); 
          String txt, st[];
          
          while ((txt=reader.readLine())!=null){
              st= txt.split("\t");
              map.put(st[1], Integer.parseInt(st[0]));
          }
          return map;
 
      } catch (Exception ex) {
          
      }
      return null;
  }
}
