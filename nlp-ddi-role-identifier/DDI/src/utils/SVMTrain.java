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

/**
 *
 * @author Chinh
 */
import libsvm.*;
import java.io.*;
import java.util.*;

public class SVMTrain {

    private final svm_parameter param;		// set by parse_command_line
    private int tpCount=0,fpCount=0;

    public SVMTrain() {
        param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 500;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        svm.svm_set_print_string_function(new svm_print_interface() { // do nothing
            @Override
            public void print(String string) {
                //
            }
        });
    }

    public svm_parameter getPara(){
        return param ;
    }
    public static void help() {
        System.out.print(
                "Usage: svm_train [options] training_set_file [model_file]\n"
                + "options:\n"
                + "-s svm_type : set type of SVM (default 0)\n"
                + "	0 -- C-SVC		(multi-class classification)\n"
                + "	1 -- nu-SVC		(multi-class classification)\n"
                + "	2 -- one-class SVM\n"
                + "	3 -- epsilon-SVR	(regression)\n"
                + "	4 -- nu-SVR		(regression)\n"
                + "-t kernel_type : set type of kernel function (default 2)\n"
                + "	0 -- linear: u'*v\n"
                + "	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
                + "	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
                + "	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
                + "	4 -- precomputed kernel (kernel values in training_set_file)\n"
                + "-d degree : set degree in kernel function (default 3)\n"
                + "-g gamma : set gamma in kernel function (default 1/num_features)\n"
                + "-r coef0 : set coef0 in kernel function (default 0)\n"
                + "-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
                + "-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
                + "-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
                + "-m cachesize : set cache memory size in MB (default 100)\n"
                + "-e epsilon : set tolerance of termination criterion (default 0.001)\n"
                + "-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
                + "-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
                + "-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
                + "-v n : n-fold cross validation mode\n"
                + "-q : quiet mode (no outputs)\n"
        );
        System.exit(1);
    }


    public static void main(String argv[]) throws IOException {

    }

    private  svm_problem prepareProblem(FeatureData[] instances) {
        svm_problem prob = new svm_problem();
        prob.l = instances.length;
        prob.y = new double[prob.l];
        prob.x = new svm_node[prob.l][];
        for (int i = 0; i < instances.length; i++) {
            prob.y[i] = instances[i].getLabel();
            prob.x[i] = instances[i].getData();
        }
        return prob;
    }

    public  svm_model train(FeatureData[] instances, svm_parameter param) {
        //prepare svm_problem
        svm_problem problem = prepareProblem(instances);

        String error_msg = svm.svm_check_parameter(problem, param);

        if (error_msg != null) {
            System.err.print("ERROR: " + error_msg + "\n");
            System.exit(1);
        }

        return svm.svm_train(problem, param);
    }

    public List<String> evaluate(FeatureData[] instances, svm_parameter param, FeatureData[] test) {
        List<String> ls = new ArrayList<>();
        svm_model model = train(instances, param);
        double predicts[] = predict(test, model);
        int tp = 0;
        int fp = 0;
        int fn = 0;
        int total = 0;
        for (int i = 0; i < test.length; i++) {
            if (predicts[i] == test[i].getLabel() ) {
                if(test[i].getLabel()==1){
                    tp++;
                }
            } else if (test[i].getLabel() == 1) {
                fn++;
            } else if (test[i].getLabel()==0) {
                ls.add(test[i].id);
                fp++;
            }
            total++;
        }
        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);
        double fscore = 2 * precision * recall / (precision + recall);
        System.out.print("Precision: " + precision);
        System.out.print("\tRecall: " + recall);
        System.out.println("\tFScore: " + fscore);
        System.out.println("Total instances:\t" + total+" true positive:\t"+tp+"\tFalse posive:\t"+fp+" False negative:\t"+fn);
        return ls;
    }
    
    public double evaluateF(FeatureData[] instances, svm_parameter param, FeatureData[] test) {
        svm_model model = train(instances, param);
        double predicts[] = predict(test, model);
        int tp = 0;
        int fp = 0;
        int fn = 0;
        int total = 0;
        for (int i = 0; i < test.length; i++) {
            if (predicts[i] == test[i].getLabel() ) {
                if(test[i].getLabel()==1){
                    tp++;
                }
            } else if (test[i].getLabel() == 1) {
                fn++;
            } else if (test[i].getLabel()==0) {
                fp++;
            }
            total++;
        }
        tpCount =tp;
        fpCount =fp;
        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);
        double fscore = 2 * precision * recall / (precision + recall);
        //System.out.println("True positive:\t"+tp+"\tfalse positive:\t"+fp+"\tfscore:\t"+fscore);
        return fscore;
    }
    
    public int getTPCount(){
        return tpCount;
    }
    
    public int getFPCount(){
        return fpCount;
    }
    public  svm_model train(List<FeatureData> instances, svm_parameter param) {
        FeatureData[] array = new FeatureData[instances.size()];
        array = instances.toArray(array);
        return train(array, param);
    }

    public double doInOrderCrossValidation(FeatureData[] instances, svm_parameter param, int nr_fold, boolean binary) {
        int size = instances.length;
        int chunkSize = size / nr_fold;
        int begin = 0;
        int end = chunkSize - 1;
        int tp = 0;
        int fp = 0;
        int fn = 0;
        int total = 0;

        for (int i = 0; i < nr_fold; i++) {
            //System.out.println("Iteration: " + (i+1));
            List<FeatureData> trainingFeatureDatas = new ArrayList<>();
            List<FeatureData> testingFeatureDatas = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if (j >= begin && j <= end) {
                    testingFeatureDatas.add(instances[j]);
                } else {
                    trainingFeatureDatas.add(instances[j]);
                }
            }
            svm_model trainModel = train(trainingFeatureDatas, param);
            double[] predictions = predict(testingFeatureDatas, trainModel);
            for (int k = 0; k < predictions.length; k++) {
                if (predictions[k] == testingFeatureDatas.get(k).getLabel()) {
                    //if (Math.abs(predictions[k] - testingFeatureDatas.get(k).getLabel()) < 0.00001) {
                    if (testingFeatureDatas.get(k).getLabel() == 1) {
                        tp++;
                    }
                } else if (testingFeatureDatas.get(k).getLabel() == 1) {
                    fn++;
                } else if (testingFeatureDatas.get(k).getLabel() == 0) {
                    //System.out.println(testingFeatureDatas.get(k).getData());
                    fp++;
                }
                total++;
            }
            //update
            begin = end + 1;
            end = begin + chunkSize - 1;
            if (end >= size) {
                end = size - 1;
            }
        }
        //System.out.println("True pairs:\t"+(tp)+" Total:\t"+(tp+fn));
        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);
        double fscore = 2 * precision * recall / (precision + recall);
        if (binary) {
            System.out.print("Precision: " + precision);
            System.out.print("\tRecall: " + recall);
            System.out.println("\tFScore: " + fscore);
            System.out.println("Total instances:\t" + total+ "\tTP:\t"+tp+" \tFP:\t"+fp+"\tFN:\t"+fn);
        }
        return fscore;
    }

    public  double[] predict(List<FeatureData> instances, svm_model model) {
        FeatureData[] array = new FeatureData[instances.size()];
        array = instances.toArray(array);
        return predict(array, model);
    }

    
    public  double[] predict(FeatureData[] instances, svm_model model) {
        double values[] = new double[instances.length];
        for (int i = 0; i < instances.length; i++) {
            values[i] = predict(instances[i],model);
        }
        return values;
    }
    
    public  double predict(FeatureData instance, svm_model model) {
         return  svm.svm_predict(model, instance.getData());
    }
    
}
