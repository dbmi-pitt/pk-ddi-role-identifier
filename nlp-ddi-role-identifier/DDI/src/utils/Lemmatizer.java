

package utils;

/**
 *
 * @author Chinh
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lemmatizer{
  
  public final Set<String> constantTags = new HashSet<String>(Arrays.asList("NNP","NP00000"));
  private HashMap<List<String>,String> dictMap;

  
  public Lemmatizer(String dictionary) {
        dictMap = new HashMap<List<String>,String>();
        try {
            BufferedReader breader = new BufferedReader(new FileReader(dictionary));
            String line;
            while ((line = breader.readLine()) != null) {
                String[] elems = line.split("\t");
                dictMap.put(Arrays.asList(elems[0],elems[1]),elems[2]);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        
  }
  
  private List<String> getDictKeys(String word, String postag) {
        List<String> keys = new ArrayList<String>();
        if (constantTags.contains(postag)) { 
            keys.addAll(Arrays.asList(word,postag));
        }
        else {
            keys.addAll(Arrays.asList(word.toLowerCase(),postag));
        }
        return keys;
    }
     
  public String lemmatize(String word, String postag) {
    List<String> keys = getDictKeys(word, postag);
    //lookup lemma as value of the map
    String keyValue = dictMap.get(keys);
    if (keyValue != null) { 
        return  keyValue;
    }else if(Character.isDigit(word.charAt(0))){
        return "NUMBER";
    }else if(word.startsWith("DRUG")){
        return "ARG";
    }else if(Character.isLetter(word.charAt(0))){
        return "UNKNOWN";
    }else {
        return word ;
    }
    
  }

}