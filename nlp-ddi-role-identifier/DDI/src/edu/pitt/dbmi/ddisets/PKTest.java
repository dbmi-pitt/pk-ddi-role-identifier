package edu.pitt.dbmi.ddisets;

import java.io.FileWriter;
import java.util.Map;

import nlp.SenData;
import libsvm.svm_model;
import libsvm.svm_parameter;
import relation.DDIPair;
import relation.FeatureGenerator;
import utils.Data;
import utils.FeatureData;
import utils.SVMTrain;
import corpora.XML2Object;

public class PKTest {
	
	XML2Object converter;
	FeatureGenerator fg;
	
	private static String mainlocation = "..";
	private static String input_location = mainlocation + "/DDIXML";
	private static String train_path = "data/train.ser";
	private static String test_path = "data/test.ser";
	private static String mixtrainpairs = "data/mixtrainpairs.ser";
	private static String pktestpairs = "data/pktestpairs.ser";
	
	//	private static String train_pairs = mainlocation + 
	
	public PKTest() {
		converter = new XML2Object();
		fg = new FeatureGenerator();
	}
	
	public void run() {
		load();
		evaluateStart();
	}
	
	private void load() {
		String[] train_source = {input_location + "/train"};
		String[] test_source = {input_location + "/test"};
		System.out.println("---> Reading xml files ...");
        // Saving training data
        converter.saveData(train_path, train_source,true);
        converter.saveData(test_path, test_source,true);

        System.out.println("---> Saving ...done");
        
        evaluateStart();
	}
	
        // TODO: add in the SPL PK Corpus after nltk_test is corrected
        // to fix how the training and test set is being generated
	private void evaluateStart() {
		try {
			System.out.println("Evaluation results:\n");
			// For now, use the SemEval 2013 corpus but
			// write the feature data locally so we can
			// play with various components of the system
			fg.featureGenerator(Data.Train_MIX2013_path, true, false, mixtrainpairs);
			fg.featureGenerator(test_path, false, false, pktestpairs);
			
			// TODO: this is where the SPL PK DDI corpus
			// would be read for feature generation in
			// place of the one we are currently using
			// (see above)
			//fg.featureGenerator(train_path, true, false, trainpairs);
			//fg.featureGenerator(test_path, false, false, testpairs);

			Map<String, FeatureData[]> train_data, test_data;
			train_data = (Map<String, FeatureData[]>) Data.read(mixtrainpairs);
			test_data = (Map<String, FeatureData[]>) Data.read(pktestpairs);

			// TODO: this is where the SPL PK DDI corpus
			// would be read for evaluation in place of
			// the one we are currently using (see above)
			//train_data = (Map<String, FeatureData[]>) Data.read(trainpairs);
			//test_data = (Map<String, FeatureData[]>) Data.read(testpairs);

			// Configure the SVM 
			double c[]={2,4,1,5,1}; //best C
			double v[] = {0.25, 0.05, 0.15, 0.15, 0.25}; // best gamma

			int true_pairs = countTruePairs(Data.Test_DB2013_path);

			// TODO: this is where the SPL PK DDI corpus
			// true results would be read for evaluation
			// in place of the one we are currently using
			// (see above)
			//int true_pairs = countTruePairs(test_path);

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
            FileWriter errWrt = new FileWriter("./output/bioin2120_HW_error_PK_output.csv");
	    FileWriter okWrt = new FileWriter("./output/bioin2120_HW_successful_PK_output.csv");
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
		    // TODO: if the value = 0, use the id to pull the
		    // sentence node from the test data XML. Then,
		    // test if there is an exact case insensitive
		    // match between two entities and a drug pair in
		    // the merged PDDI dataset (excluding DrugBank and
		    // PK DDI sources). If so, classify this as a "1"
		    // (i.e., val = 1). The rest of the code will
		    // calculate the performance. 
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
		PKTest ddi = new PKTest();
		ddi.run();
	}
}
