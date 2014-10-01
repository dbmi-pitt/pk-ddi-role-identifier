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

public class DDISets {
	
	XML2Object converter;
	FeatureGenerator fg;
	
	private static String mainlocation = "..";
	private static String input_location = mainlocation + "/DDIXML";
	private static String train_path = "data/train.ser";
	private static String test_path = "data/test.ser";
	private static String trainpairs = "data/trainpairs.ser";
	private static String testpairs = "data/testpairs.ser";
	
	//	private static String train_pairs = mainlocation + 
	
	public DDISets() {
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
	
	private void evaluateStart() {
		try {
			System.out.println("Evaluation results:\n");
			fg.featureGenerator(train_path, true, false, trainpairs);
			fg.featureGenerator(test_path, false, false, testpairs);
			Map<String, FeatureData[]> train_data, test_data;
            train_data = (Map<String, FeatureData[]>) Data.read(Data.Train_DB2013Pairs);
            test_data = (Map<String, FeatureData[]>) Data.read(Data.Test_DB2013Pairs);
			double c[]={2,4,1,5,1}; //best C
            double v[] = {0.25, 0.05, 0.15, 0.15, 0.25}; // best gamma
            int true_pairs = countTruePairs(test_path);
            evaluate(train_data, test_data, c, v, true_pairs, "test-ddi-rdb20");
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
    
	public static void main(String[] args) {
		DDISets ddi = new DDISets();
		ddi.run();
	}
}