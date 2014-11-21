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

public class DumpChunksPK {

	public static void main(String[] args) {
	    FeatureGenerator fg = new FeatureGenerator();
	    fg.printGroupDataPK("./output/bioin2120_HW_successful_PK_output.csv");
	}
}
