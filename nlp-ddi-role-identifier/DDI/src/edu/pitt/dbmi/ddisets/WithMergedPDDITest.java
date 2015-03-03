package edu.pitt.dbmi.ddisets;

import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import nlp.SenData;
import nlp.Word;

import corpora.Sentence;
import corpora.Document;

import relation.DDIPair;

import libsvm.svm_model;
import libsvm.svm_parameter;
import relation.DDIPair;
import relation.FeatureGenerator;
import utils.Data;
import utils.FeatureData;
import utils.SVMTrain;
import corpora.XML2Object;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;

public class WithMergedPDDITest {
	
	XML2Object converter;
	FeatureGenerator fg;
    //static HashMap<String, DDIPair> ddiPairMap = new HashMap<String, DDIPair>();
    //static Connection conn = null;       

	private static String mainlocation = ".";
	private static String input_location = mainlocation + "/DDI_corpora";
	private static String train_path = "data/train.ser";
	private static String test_path = "data/test.ser";

       // Config for DrugBank 2013 testing 
       // private static String train_source_string = "/Test2013/DrugBank";
       // private static String test_source_string = "/Test2013/DrugBank"; 
       // private static String train_pairs_data_path = Data.Train_DB2013_path; 
       // private static String train_pairs_data = Data.Train_DB2013Pairs;
       // private static String test_pairs_path = Data.Test_DB2013_path;
       // private static String test_pairs_data = Data.Test_DB2013Pairs;

       // Config for DrugBank 2011 testing 
       // private static String train_source_string = "/Train2011/DrugDDI_Unified";
       // private static String test_source_string = "/Test2011";
       // private static String train_pairs_data_path = Data.Train_DB2011_path; 
       // private static String train_pairs_data = Data.Train_DB2011Pairs;
       // private static String test_pairs_path = Data.Test_DB2011_path;
       // private static String test_pairs_data = Data.Test_DB2011Pairs;

       // Config for MedLine 2013 testing 
       private static String train_source_string = "/Train2013/CombinedDrugBankMedLine";
       private static String test_source_string = "/Test2013/MedLine";       
       private static String train_pairs_data_path = Data.Train_MIX2013_path; 
       private static String train_pairs_data = Data.Train_MIX2013Pairs;
       private static String test_pairs_path = Data.Test_ML2013_path;
       private static String test_pairs_data = Data.Test_ML2013Pairs;
    
	public WithMergedPDDITest() {
		converter = new XML2Object();
		fg = new FeatureGenerator();
	}
	
	public void run() {
		load();
		evaluateStart();
	}
	
	private void load() {
	        String[] train_source = {input_location + train_source_string};
		String[] test_source = {input_location + test_source_string};
		System.out.println("---> Reading xml files ...");
		// Saving training data
		converter.saveData(train_path, train_source,true);
		converter.saveData(test_path, test_source,true);

		System.out.println("---> Saving ...done");

		//System.out.println("---> Reading xml files to cache DDI pairs ...");
		// File f = new File(input_location + test_source_string);
		// File[] files = f.listFiles();
                // for (File file : files) {
                //     Document doc = converter.loadCorpus(file);
                //     List<Sentence> sens = doc.getSentence();
                //     for (Sentence sen : sens) {
		// 	SenData senData = converter.preparedData(sen, false);
		// 	for (DDIPair ddiPair : senData.ddiList){
		// 	    ddiPairMap.put(ddiPair.id, ddiPair);
		// 	}
                //     }
                // }
		
		// try {
		//     conn = DriverManager.getConnection("jdbc:mysql://localhost/merged_DDIs?user=mergedPddi&password=pddi");
		// } catch (SQLException ex) {
		//     // handle any errors
		//     System.out.println("SQLException: " + ex.getMessage());
		//     System.out.println("SQLState: " + ex.getSQLState());
		//     System.out.println("VendorError: " + ex.getErrorCode());
		//     System.exit(1);
		// }

		evaluateStart();
	}
	
        // TODO: add in the SPL PK Corpus after nltk_test is corrected
        // to fix how the training and test set is being generated
	private void evaluateStart() {
		try {
			System.out.println("Evaluation results:\n");
			fg.featureGenerator(train_pairs_data_path, true, false, train_pairs_data);
			fg.featureGenerator(test_pairs_path, false, false, test_pairs_data);
			
			Map<String, FeatureData[]> train_data, test_data;
			train_data = (Map<String, FeatureData[]>) Data.read(train_pairs_data);
			test_data = (Map<String, FeatureData[]>) Data.read(test_pairs_data);

			// Configure the SVM 
			double c[] = null;
			double v[] = null;
			if (test_source_string == "/Test2011/DrugBank"){
			    System.out.println("INFO: Using DrugBank 2011 SVM parameter configuration");
			    double temp_c[]={3,6,2,3,5}; //best C
			    c = temp_c;
			    double temp_v[] = {0.05, 0.15, 0.1, 0.05, 0.05}; // best gamma
			    v = temp_v;
			} else if (test_source_string == "/Test2013/DrugBank") {
			    System.out.println("INFO: Using DrugBank 2013 SVM parameter configuration");
			    double temp_c[]={2,4,1,5,1}; //best C
			    c = temp_c;
			    double temp_v[] = {0.25, 0.05, 0.15, 0.15, 0.25}; // best gamma
			    v = temp_v;
			} else if (test_source_string == "/Test2013/MedLine"){
			    System.out.println("INFO: Using MedLine 2013 SVM parameter configuration");
			    double temp_c[]={1,4,4,2,2}; //best C
			    c = temp_c;
			    double temp_v[] = {0.1, 0.05, 0.05, 0.05, 0.05}; // best gamma
			    v = temp_v;
			} else {
			    System.out.println("ERROR: no test_source_string specified - unable to configure the SVM parameters so exiting");
			    System.exit(1);
			}
			int true_pairs = countTruePairs(test_pairs_path);

			evaluate(train_data, test_data, c, v, true_pairs, "test-pk-ddi-bioinf2120");
		} catch (Exception e) {
			e.printStackTrace();
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
            FileWriter errWrt = new FileWriter("./output/WithMergedPddiTest_error_PK_output.csv");
	    FileWriter okWrt = new FileWriter("./output/WithMergedPddiTest_successful_PK_output.csv");
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
		    // TODO: if the value == 0, use the id to pull the
		    // sentence node from the test data XML. Then,
		    // test if there is an exact case insensitive
		    // match between two entities and a drug pair in
		    // the merged PDDI dataset (excluding DrugBank and
		    // PK DDI sources). If so, classify this as a "1"
		    // (i.e., val = 1). The rest of the code will
		    // calculate the performance. 
		    //System.out.println("FeatureData id:" + dt.id);
		    // if ((type == "clause") && (val == 0)){
		    // 	if (ddiPairMap.containsKey(dt.id)){
		    // 	    DDIPair ddiPair = (DDIPair) ddiPairMap.get(dt.id);
		    // 	    System.out.println(ddiPair.id);
		    // 	    System.out.println(ddiPair.arg1.id);
		    // 	    System.out.println(ddiPair.arg2.id);
		    // 	    System.out.println(ddiPair.arg1.word);
		    // 	    System.out.println(ddiPair.arg2.word);
		    // 	    System.out.println(ddiPair.ddi);

		    // 	    String query = "SELECT object, precipitant FROM DDI WHERE source NOT IN ('DDI-Corpus-2011', 'DDI-Corpus-2013') AND (LOWER(object) = \'" + ddiPair.arg1.word.toLowerCase() + "\' AND LOWER(precipitant) = \'" + ddiPair.arg2.word.toLowerCase() + "\') OR (LOWER(object) = \'" + ddiPair.arg2.word.toLowerCase() + "\' AND LOWER(precipitant) = \'" + ddiPair.arg1.word.toLowerCase() + "\');";
		    // 	    System.out.println(query);

		    // 	    Statement st = conn.createStatement();
		    // 	    try {						    				
		    // 		ResultSet rs = null;
		    // 		if (st.execute(query)){
		    // 		    System.out.println("QUERY SUCCEEDED");
		    // 		    rs = st.getResultSet();
		    // 		} else {
		    // 		    System.out.println("QUERY FAILED");
		    // 		    System.exit(1);
		    // 		}

		    // 		if (rs.first()){
		    // 		    System.out.println("QUERY RETURNED RESULT");
		    // 		    String object = rs.getString("object");
		    // 		    String precipitant = rs.getString("precipitant");
		    // 		    System.out.format("%s, %s\n", object, precipitant);			      
		    // 		    while (rs.next()){
		    // 		     	object = rs.getString("object");
		    // 		     	precipitant = rs.getString("precipitant");
		    // 		     	System.out.format("%s, %s\n", object, precipitant);
		    // 		     }
		    // 		    System.out.println("SETTING VAL = 1");
		    // 		    val = 1;
		    // 		} else {
		    // 		    System.out.println("NO RESULT");
		    // 		}
				
		    // 		if (rs != null) {
		    // 		    try {
		    // 			rs.close();
		    // 		    } catch (SQLException sqlEx) { } // ignore
		    // 		    rs = null;
		    // 		}
		    // 		if (st != null) {
		    // 		    try {
		    // 			st.close();
		    // 		    } catch (SQLException sqlEx) { } // ignore
		    // 		    st = null;
		    // 		}
		    // 	    } catch (SQLException ex) {
		    // 	    	// handle any errors
		    // 	    	System.out.println("SQLException: " + ex.getMessage());
		    // 	    	System.out.println("SQLState: " + ex.getSQLState());
		    // 	    	System.out.println("VendorError: " + ex.getErrorCode());
		    // 	    }
		    // 	}
		    // }
                    if (dt.getLabel() == 1) {
                        if (val == 1) {
                            ltp++; // true positive
			    okWrt.append(dt.id + ",true positive,"+type+"\n");
                        } else {
			    //System.out.println("Feature data for false negative node:\n\t" + dt.toString());
                            errWrt.append(dt.id + ",false negative,"+type+"\n");
                        }
                    } else { // true negative
                        if (val == 1) {
                            lfp++; // false positive
                            errWrt.append(dt.id + ",false positive,"+type+"\n");
                        } else {
			    okWrt.append(dt.id + ",true negative,"+type+"\n");
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
            errWrt.close();
	    okWrt.close();
        }catch (Exception ex) {
            
        }
    }
    
	public static void main(String[] args) {
	    WithMergedPDDITest ddi = new WithMergedPDDITest();
	    ddi.run();
	}
}
