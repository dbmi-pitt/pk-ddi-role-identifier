NLP DDI Role Identifier
=======================

Authors:
- Richard Boyce, PhD
- Jeremy Jao
- Yifan Ning
- Pratibha Shambhangoudr


The goal of this project is to develop a classifier for identifying
precipitant and object drug products within pharmacokinetic drug-drug
interaction mentions located in drug product labels. At the prsent
time, we are just getting familiar with using an NLP pipeline from the
following paper to extract features from the sentences with the main
class DDISets from the DDI folder:
http://bioinformatics.oxfordjournals.org/content/early/2014/09/05/bioinformatics.btu557

# CURRENT USAGE:
(NOTE: as of 11/18/2014 this simply runs the pipeline using the SemEval DDI corpus. See the following issue <https://github.com/dbmi-pitt/pk-ddi-role-identifier/issues/1>)

1. python nltk_test.py (takes the nonexpert and expert files and turns them into xml)

2. cd DDI

3. ant DDISets (the main file that evaluates our sentences)

##Folders:
- DDI -> source code of the paper, with an added class to run (DDISets)
	- from here: http://www.biosemantics.org/uploads/DDI.zip

- DDIXML - subfolder that contains the output of nltk_test.py (separated by train and test folders)

- python_input - data that contains the DDIs for nonexpert1 and expert2 from the Domeo DDI project

##Files:
- nltk_test.py - file that creates the input for DDISets
	- train set - nonexpert1
	- test set - expert1
	- output
		- DDI/data/train.ser -> hashmap of the training set (nonexpert1)
		- DDI/data/test.ser -> hashmap of the test set (expert2)
- DDI/edu/pitt/dbmi/ddisets/DDISets.java - evaluator of our test and train sets
	- output:
		- DDI/data/trainpairs.ser -> feature set for the training set
		- DDI/data/testpairs.ser -> feature set for the test set

How the XML output works:
- Assign each drug entity chosen an entity ID
- For each drug chosen by the person
	- return a list of the starting indexes of the drug found (re.iteritem() method)
	- end index = (the length of the selected drug – starting index)
	- The drug used for the entity is the last found in the sentence (Incomplete algorithm!!!)
		- Only way I can see fixing this is finding the location of the verb inreference to the drug... (but I took out the algorithm for that)
