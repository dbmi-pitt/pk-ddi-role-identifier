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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Chinh
 */
public class Data {
    // setup local files to store training and test data
    public static final String default_path=".";
    public static  final String Train_DB2013_path =default_path+"/data/ddi_db_2013.ser";
    public static  final String Train_DB2011_path =default_path+ "/data/ddi_db_2011.ser";
    public static  final String Train_ML2013_path =default_path+"/data/ddi_ml_2013.ser";
    public static  final String Train_MIX2013_path =default_path+"/data/ddi_mix_2013.ser";
    public static  final String Test_DB2013_path =default_path+"/data/ddi_testdb2013.ser";
    public static  final String Test_DB2011_path =default_path+"/data/ddi_testdb2011.ser";
    public static  final String Test_ML2013_path =default_path+"/data/ddi_testML2013.ser";
    public static final String Trigger_path =default_path+"/data/triggers.txt";
    public static final String Trigger_2011_path =default_path+"/data/triggers_ddi2011.txt";
    public static final String Dictionary_path =default_path+"/data/EngDic.txt";
    public static final String FeatureMaps =default_path+"/data/featureMaps.ser";
    public static final String Train_DB2013Pairs =default_path+"/data/trainDB2013DDIs.ser";
    public static final String Train_ML2013Pairs =default_path+"/data/trainML2013DDIs.ser";
    public static final String Train_MIX2013Pairs =default_path+"/data/trainMIX2013DDIs.ser";
    public static final String Train_DB2011Pairs =default_path+"/data/trainDB2011DDIs.ser";
    public static final String Test_DB2011Pairs =default_path+"/data/testDB2011DDIs.ser";
    public static final String Test_DB2013Pairs =default_path+"/data/testDB2013DDIs.ser";
    public static final String Test_ML2013Pairs =default_path+"/data/testML2013DDIs.ser";
    // prepositions and negative modifiers
    public static final Set<String> prepSet = new HashSet<>();
    public static final Set<String> negSet = new HashSet<>();
    static final String prepList[] = {"to", "with", "from", "of", "on", "in", "upon", "by", "for", "after", "through", "between", "via","before","during","than"};
    static final String negList[] = {"no", "not", "neither","nor","litle","less","without"};
    public static final String[] connect_list={"when","if","because","since","until","where","while","although"};
    public static final Set<String> connnectSet = new HashSet<>();
    public static final String ccList[] = {"and", "or", "but not", "as well as"};
    public static final String ccShared[] = {"and", "or", "but not", "but"};
    static final String appoList[] = {"like", "such as", "including", "includes", "containing", "include"};
    public static final Set<String> appoSet = new HashSet<>(5);
    public static final Set<String> ccSet = new HashSet<>(10);
    public static final Set<String> sharedVerb = new HashSet<>(10);
    static final String to_be[] = {"be", "is", "are", "was", "were", "been"};
    static final String have[] = {"have", "has", "had"};
    static final String do_verb[] = {"did", "do", "does"};
    static final String relative[] = {"that", "which", "who","whom","those"}; //+ tobe
    public static final Set<String> subclauseSet = new HashSet<>(10);
    public static final Set<String> beSet = new HashSet<>(10);
    public static final Set<String> adviseSet = new HashSet<>(10);
    public static final String[] advise_trg={"advise","advised","advisable","contraindicated"};
    public static final Set<String>haveSet = new HashSet<>();
    public static final Set<String>aux_set = new HashSet<>();
    public static final String ddi_type[]={"advise","mechanism","effect","int"};
    
    public static final Map<String,Integer> typeMap = new HashMap<>();
    static {
        connnectSet.addAll(Arrays.asList(connect_list));
        prepSet.addAll(Arrays.asList(prepList));
        negSet.addAll(Arrays.asList(negList));
        ccSet.addAll(Arrays.asList(ccList));
        sharedVerb.addAll(Arrays.asList(ccShared));
        appoSet.addAll(Arrays.asList(appoList));
        subclauseSet.addAll(Arrays.asList(relative));
        beSet.addAll(Arrays.asList(to_be));
        //beSet.addAll(Arrays.asList(have));
        adviseSet.addAll(Arrays.asList(advise_trg));
        haveSet.addAll(Arrays.asList(have));
        for(int i=0;i<ddi_type.length;i++){
            typeMap.put(ddi_type[i],i);
        }
        aux_set.addAll(haveSet);
        aux_set.addAll(beSet);
        aux_set.addAll(Arrays.asList(do_verb));
    }
    
    
    /**
     * serializes the given objects to the specified file.
     *
     * @param filename	the file to write the object to
     * @param o	the objects to serialize
     * @throws Exception	if serialization fails
     */
    public static void writeAll(String filename, Object[] o) throws Exception {
        writeAll(new FileOutputStream(filename), o);
    }

    /**
     * serializes the given objects to the specified stream.
     *
     * @param stream	the stream to write the object to
     * @param o	the objects to serialize
     * @throws Exception	if serialization fails
     */
    public static void writeAll(OutputStream stream, Object[] o) throws Exception {
        ObjectOutputStream oos;
        int i;

        if (!(stream instanceof BufferedOutputStream)) {
            stream = new BufferedOutputStream(stream);
        }

        oos = new ObjectOutputStream(stream);
        for (i = 0; i < o.length; i++) {
            oos.writeObject(o[i]);
        }
        oos.flush();
        oos.close();
    }

    /**
     * serializes the given object to the specified file.
     *
     * @param filename	the file to write the object to
     * @param o	the object to serialize
     * @throws Exception	if serialization fails
     */
    public static void write(String filename, Object o) throws Exception {
        write(new FileOutputStream(filename), o);
    }

    /**
     * serializes the given object to the specified stream.
     *
     * @param stream	the stream to write the object to
     * @param o	the object to serialize
     * @throws Exception	if serialization fails
     */
    public static void write(OutputStream stream, Object o) throws Exception {
        ObjectOutputStream oos;

        if (!(stream instanceof BufferedOutputStream)) {
            stream = new BufferedOutputStream(stream);
        }

        oos = new ObjectOutputStream(stream);
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }


    public static Object read(String filename) throws Exception {
        return read(new FileInputStream(filename));
    }


    public static Object read(InputStream stream) throws Exception {
        ObjectInputStream ois;
        Object result;

        if (!(stream instanceof BufferedInputStream)) {
            stream = new BufferedInputStream(stream);
        }

        ois = new ObjectInputStream(stream);
        result = ois.readObject();
        ois.close();

        return result;
    }


    public static Object[] readAll(String filename) throws Exception {
        return readAll(new FileInputStream(filename));
    }


    public static Object[] readAll(InputStream stream) throws Exception {
        ObjectInputStream ois;
        List<Object> result;

        if (!(stream instanceof BufferedInputStream)) {
            stream = new BufferedInputStream(stream);
        }

        ois = new ObjectInputStream(stream);
        result = new ArrayList<Object>();
        try {
            while (true) {
                result.add(ois.readObject());
            }
        } catch (IOException e) {
            // ignored
        }
        ois.close();

        return result.toArray(new Object[result.size()]);
    }
}
