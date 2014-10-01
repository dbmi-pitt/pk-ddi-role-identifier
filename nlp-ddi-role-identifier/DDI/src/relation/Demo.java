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

import corpora.XML2Object;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import libsvm.svm_model;
import libsvm.svm_parameter;
import nlp.SenData;
import utils.Data;
import utils.FeatureData;
import utils.SVMTrain;

/**
 * This file demos the performance of the system against three test datasets: DB2011, DB2013, and ML2013.
 * To run the main method with one of the following parameters: DB2011, DB2013,ML2013
 * @author Chinh
 */
public class Demo {

    XML2Object converter = new XML2Object();
    FeatureGenerator fg = new FeatureGenerator();
    /**
     * Convert DB2013 XML files into objects and parse sentences with a shallow parser
     */
    private void load_DB_2013_Data(){
      String train_source[] = {"./DDI_corpora/Train2013/DrugBank"};
      String test_source[] ={"./DDI_corpora/Test2013/DrugBank"};
        System.out.println("---> Reading xml files ...");
        // Saving training data
        converter.saveData(Data.Train_DB2013_path, train_source,true);
        // Saving testing data
        converter.saveData(Data.Test_DB2013_path, test_source,true);
        System.out.println("---> Saving ...done");
    }
    
    
    /**
     * Use both DB and ML for training
     */
    private void load_Mix_2013_Data(){
      String train_source[] = {"./DDI_corpora/Train2013/DrugBank", "./DDI_corpora/Train2013/MedLine"};
      String test_source[] ={ "./DDI_corpora/Test2013/MedLine"};
        System.out.println("---> Reading xml files ...");
        // Saving training data
        converter.saveData(Data.Train_MIX2013_path, train_source,true);
        // Saving testing data
        converter.saveData(Data.Test_ML2013_path, test_source,true);
        System.out.println("---> Saving ...done");
    }
    /**
     * Convert DB2011 training dataset into objects
     */
    private  void load_DB_2011_Data(){
      converter.setTriggers(Data.Trigger_2011_path);
      String train_source[] = {"./DDI_corpora/Train2011/DrugDDI_Unified"};
        System.out.println("---> Reading xml files ...");
        // Saving training data
        converter.saveData(Data.Train_DB2011_path, train_source,true);
        // load test DB2011, this requires a special code since this dataset have a separate answer file.
        load_DB_2011_Test();
        System.out.println("---> Saving ...done");
    }
    /**
     * Convert Db2011 test dataset into objects
     */
    private void load_DB_2011_Test(){
        String path =Data.Test_DB2011_path;
        String data_pah[]={"./DDI_corpora/Test2011"}; // path to the test corpus
        String answers_file="./DDI_corpora/resultUnified.txt"; //path to the Test 2011 answer file
        Map<String, Boolean> answers = new HashMap<>();
        try {
         BufferedReader reader = new BufferedReader(new FileReader(answers_file));
         String line;
         String st[];
         while ((line = reader.readLine()) != null) {
             st = line.split("\t");
             if(st==null || st.length<2){
                 break;
             }
             answers.put(st[0]+st[1], st[2].equals("1"));
         }
         reader.close();
         converter.saveTestData(path, data_pah, false,answers);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Evaluate DB2013 test dataset with the best parameters
     */
    private  void evaluateTestDB2013() {
        try {
            fg.featureGenerator(Data.Train_DB2013_path, true, false,Data.Train_DB2013Pairs);
            fg.featureGenerator(Data.Test_DB2013_path, false, false,Data.Test_DB2013Pairs);
            Map<String, FeatureData[]> all_data, test_data;
            all_data = (Map<String, FeatureData[]>) Data.read(Data.Train_DB2013Pairs);
            test_data = (Map<String, FeatureData[]>) Data.read(Data.Test_DB2013Pairs);
            double c[]={2,4,1,5,1}; //best C
            double v[] = {0.25, 0.05, 0.15, 0.15, 0.25}; // best gamma
            int true_pairs = countTruePairs(Data.Test_DB2013_path);
            evaluate(all_data, test_data, c, v, true_pairs,"DB2013");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Evaluate ML test dataset using mix training data (DB2013 & ML2013)
     * @param output 
     */
    private  void evaluateTestML2013Mix() {
        try {
            fg.featureGenerator(Data.Train_MIX2013_path, true, false,Data.Train_MIX2013Pairs);
            fg.featureGenerator(Data.Test_ML2013_path, false, false,Data.Test_ML2013Pairs);
            Map<String, FeatureData[]> all_data, test_data;
            all_data = (Map<String, FeatureData[]>) Data.read(Data.Train_MIX2013Pairs);
            test_data = (Map<String, FeatureData[]>) Data.read(Data.Test_ML2013Pairs);
            int true_pairs = countTruePairs(Data.Test_ML2013_path);
            double c[]={1,4,4,2,2}; //best C
            double v[] = {0.1, 0.05, 0.05, 0.05, 0.05}; // best gamma
            evaluate(all_data, test_data, c, v, true_pairs, "ML2013");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Count positive DDI pairs in order to calculate recall
     * @param path: path to test database
     * @return: number of true pairs 
     */
    private int countTruePairs(String path) {
        int count = 0;
        // Count unprocess cases due to text processing errors
        try {
            Map<String, SenData> senMap = (Map<String, SenData>) Data.read(path);
            SenData currSen;
            for (Map.Entry<String, SenData> entry : senMap.entrySet()) {
                currSen = entry.getValue();
                for (DDIPair pair : currSen.ddiList) {
                    if(pair.ddi){
                        count++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count;

    }
    private void evaluate(Map<String, FeatureData[]> trainset, Map<String, FeatureData[]> testset, double C_values[], 
            double G_values[], int pos_pairs,String db_name){
        try {
            SVMTrain trainer = new SVMTrain();
            svm_parameter para = trainer.getPara();
            para.kernel_type = svm_parameter.RBF;
            para.nr_weight = 1;
            double ww[] = {2};
            para.weight = ww;
            int lb[] = {1};
            para.weight_label = lb;
            double c[]=C_values;
            double v[] = G_values;
            FileWriter wrt = new FileWriter("./output/"+db_name+"_error_output.csv");
            int i = 0;
            int tp = 0;
            int fp = 0;
            int total = pos_pairs;
            for (String type : fg.out_type) {
                int ltp=0,lfp=0;
                para.C = (double) c[i];
                para.gamma = v[i];
                FeatureData[] data = trainset.get(type);
                FeatureData[] test = testset.get(type);
                svm_model model = trainer.train(data, para);
                for (FeatureData dt : test) {
                    double val = trainer.predict(dt, model);
                    if (dt.getLabel() == 1) {
                        if (val == 1) {
                            ltp++; // true positive
                        } else {
                            wrt.append(dt.id + ",false negative,"+type+"\n");
                        }
                    } else { // true negative
                        if (val == 1) {
                            lfp++; // false positive
                            wrt.append(dt.id + ",false positive,"+type+"\n");
                        }
                    }
                }
                i++;
                System.out.println(type+"\t\tTP:\t"+ltp+"\tFP:\t"+lfp+"\tPrecision:\t"+(ltp)*1f/(ltp+lfp)+"\tRecall:\t"+(ltp*1f)/fg.typeCounter.get(type));
                tp+=ltp;
                fp+=lfp;
            }
            System.out.println("True positives:\t"+tp);
            System.out.println("False positives:\t"+fp);
            double precision = (double) tp / (double) (tp + fp);
            double recall = (double) tp / (double) total;
            double f_score = (2 * precision * recall) / (precision + recall);
            System.out.println("Precision:\t"+precision+"\tRecall:\t"+recall+"\tFscore:\t"+f_score);
            wrt.close();
        }catch (Exception ex) {
            
        }
    }
    private void evaluateTest2011() {
        try {
            System.out.println("Evaluation results:\n");
            fg.featureGenerator(Data.Train_DB2011_path, true, true,Data.Train_DB2011Pairs);
            fg.featureGenerator(Data.Test_DB2011_path, false, true,Data.Test_DB2011Pairs);
            Map<String, FeatureData[]> train_data, test_data;
            train_data = (Map<String, FeatureData[]>) Data.read(Data.Train_DB2011Pairs);
            test_data = (Map<String, FeatureData[]>) Data.read(Data.Test_DB2011Pairs);
            double c[]={3,6,2,3,5}; //best C
            double v[] = {0.05, 0.15, 0.1, 0.05, 0.05}; // best gamma
            int pos_pairs= countTruePairs(Data.Test_DB2011_path);
            evaluate(train_data, test_data, c, v, pos_pairs, "DB2011");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * DrugBank 2013 demo
     * - Load datasets (Note: path need to be modified) 
     * - Evaluate test set
     */
    public void demoDB2013(){
        long t1= System.currentTimeMillis();
        load_DB_2013_Data();
        evaluateTestDB2013(); //  path to output errors
        long t2= System.currentTimeMillis();
        System.out.println("Total time:\t"+(t2-t1)/1000);
    }
    /**
     * Train on DB2013 and ML2013
     * Test on ML2013
     */
    public void demoMix2013(){
        load_Mix_2013_Data();
        evaluateTestML2013Mix();
    }
    
    public void demoDB2011(){
        load_DB_2011_Data();
        evaluateTest2011();
    }
    
    public void printHelp(){
        System.out.println("Run the system with one of the folling parameters: DB2011, DB2013, ML2013");
            System.out.println("DB2011 -> to evaluate the DB2011 test dataset ");
            System.out.println("DB2013 -> to evaluate the DB2013 test dataset ");
            System.out.println("ML2013 -> to evaluate the ML2013 test dataset ");
    }
    public static void main(String[] args) {
        Demo demo = new Demo();
        if(args.length==0 || args.length>1){
           demo.printHelp();
           
        }else if (args[0].equals("DB2011")){
            demo.demoDB2011();
        }else if(args[0].equals("DB2013")){
            demo.demoDB2013();
        }else if(args[0].equals("ML2013")){
            demo.demoMix2013();
        }else {
            demo.printHelp();
        }
    }
    
}
