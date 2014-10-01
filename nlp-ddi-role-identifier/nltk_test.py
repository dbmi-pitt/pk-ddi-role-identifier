"""
Jeremy Jao
08.13.2014

This is supposed to be the NLP project with Pratibha and Yifan

This is to get the DDIs and Sentences from the DDI project in a 
CSV file to an XML format that the DDI program can read: 
http://bioinformatics.oxfordjournals.org/content/early/2014/09/05/bioinformatics.btu557

Source code is available at:
http://www.biosemantics.org/uploads/DDI.zip

"""

import nltk
import csv
import sys
from nltk.stem.porter import PorterStemmer
import re
import pprint
from prettyXML import prettify

#from nltk.tag.stanford import POSTagger
#from nltk.corpus import stopwords

stemmer = PorterStemmer()


INPUT = 'DDI-Sets-nonexpert1-csv-07212014.csv'
INPUT2 = 'DDI-latest-version-expert2-09182014.csv'
# sets the verbs via stemmer
verbset = set([
		
		stemmer.stem('inhibit'), 
		stemmer.stem('increase'), 
		stemmer.stem('decrease'),
		stemmer.stem('effect'),
		stemmer.stem('affect'),
		stemmer.stem('induce'),
		stemmer.stem('reduce'),
		stemmer.stem('double'),
		stemmer.stem('occur'),
		stemmer.stem('triple')
		
			 ])
#seenStcs = ({}, {})
# qualitative, quantitative



def splitCol(string, delim, index):
	"""
	Simply splits columns from a string and returns the specific column
	
	args:
	string -- specific string to be split
	delim -- the delimiter to split the row
	index -- which column to return after the split
	"""
	return string.split(delim)[index]


def getVars(row):
	"""
	takes a pre-splitted row from the csv input and returns these data 
	about each row in this order as multiple variable returns:
		
		sentence, 
		modality, 
		statementType, 
		drugOne, 
		drugOneType, 
		drugOneRole, 
		drugTwo, 
		drugTwoType, 
		drugTwoRole
	
	args:
	row -- list of info stated from above
	"""
	sentence = row[1]
	modality = splitCol(row[2], ':', 1).lower()
	statementType = splitCol(row[3], ':', 1).lower()

	drugOne = row[4]
	drugOneType = splitCol(row[5],'/', 5).lower()
	drugOneRole = splitCol(row[6], ':', 1).lower()
	
	drugTwo = row[7]
	drugTwoType = splitCol(row[8], '/', 5).lower()
	drugTwoRole = splitCol(row[9], ':', 1).lower()
	
	return sentence, modality, statementType, drugOne, drugOneType, drugOneRole, drugTwo, drugTwoType, drugTwoRole
	
	
def workWithCol(row, ql, qn):
	"""
	Works with the columns inside the csv file to create more of the data structure
	"""
	sentence, modality, statementType, drugOne, drugOneType, drugOneRole, drugTwo, drugTwoType, drugTwoRole = getVars(row)
	seenStcs = ({}, {})
	if statementType == 'qualitative':
		print 'qualitative'
		addStc(ql, sentence, modality, statementType, drugOne, drugOneType, drugOneRole, drugTwo, drugTwoType, drugTwoRole, seenStcs)
	elif statementType == 'quantitative':
		print 'quantitative'
		addStc(qn, sentence, modality, statementType, drugOne, drugOneType, drugOneRole, drugTwo, drugTwoType, drugTwoRole, seenStcs)
		

def addStc(corpus, sentence, modality, statementType, drugOne, drugOneType, drugOneRole, drugTwo, drugTwoType, drugTwoRole, seenStcs):
	"""
	Adds sentences (uniquely) into the ql or qn list by dict and list
	"""
	if statementType == 'qualitative':
		seenStc = seenStcs[0]
	else:
		seenStc = seenStcs[1]
	
	if sentence not in seenStc:
		corpus.append({
			
				'sentence': sentence,
				'modality': [modality],
				'statement type': statementType,
				'drug 1': [{drugOne: {'type': drugOneType, 'role': drugOneRole}}],
				'drug 2': [{drugTwo: {'type': drugTwoType, 'role': drugTwoRole}}],
				'verbs': {},
				'drugs': []})
		tempIndex = len(corpus)-1
		seenStc[sentence] = (corpus[tempIndex], tempIndex)
		print 'added new ' + str(tempIndex)
		#print seenStcs[sentence]
	else:
		tempIndex = seenStc[sentence][1]
		#pprint.pprint(corpus)
		print tempIndex
		print corpus[tempIndex]
		corpus[tempIndex]['drug 1'].append({drugOne: {'type': drugOneType, 'role': drugOneRole}})
		corpus[tempIndex]['drug 2'].append({drugTwo: {'type': drugTwoType, 'role': drugTwoRole}})
		corpus[tempIndex]['modality'].append(modality)
		
def itRows(csvinp):
	"""
	Iterates through every row in the csv and instantiates corpuses for
	qualitative and quantitative sentences
	"""
	ql = []
	qn = []
	qlindex = 0
	qnindex = 0
	for row in csvinp:
		workWithCol(row, ql, qn)
	return ql, qn
	
def workWithSentenceList(ql, qn):
	"""
	initializes the working of sentences using ie_process for now... 
	and makes the drug list
	"""
	drugList = process_drugList()
	ie_process(ql, drugList)
	ie_process(qn, drugList)
	#pprint.pprint(ql[0])
	#pprint.pprint(ql[11])
	#pprint.pprint(ql[2])
	#pprint.pprint(ql[3])
	
	
	
	
def process_drugList():
	"""
	takes the drug list in the folder and turns this into a dict of
	drug: drug-type
	
	ex... (and it's wrong):
	'9-hydroxyrisperidone': 'Active Ingredient'
	
	There are cases where the value of the dict is incorrect! the above
	should be a Metabolite but we would have to change the string itself
	"""
	with open("drug-rxcui-type.txt", 'r') as fil:
		drugList = {}
		for line in fil:
			drugs = line.strip("\n").split(',')
			if drugs[2] == 'NAN':
				continue
			drugList[drugs[0]] = drugs[2]
		return drugList
	return None
	
def tokStc(stc):
	"""
	returns a word-tokenized stc as a list via nltk
	"""
	return nltk.word_tokenize(stc)
	
def ie_process(corpus, drugList):
	"""
	starts the process to add drugs to the corpus (whether quant or qual)
	
	"""
	stopwords = nltk.corpus.stopwords.words('english') #deprecated I think
	stc2 = []
	for data in corpus:
		#processOne(data, drugList, stopwords)
		processOne(data)
		eliminateSubDrugs(data['drugs'])
		#stc2.append(data['new sentence'])
	
	pprint.pprint(corpus[0])
	pprint.pprint(corpus[1])
	pprint.pprint(corpus[2])
	pprint.pprint(corpus[3])

def processOne(data):
	"""
	This will get the relationships from drug 1 and drug 2...
	"""
	for i in xrange(0, len(data['drug 1'])):
		drug1 = data['drug 1'][i]
		drug2 = data['drug 2'][i]
		appendDrug(drug1, data['sentence'], data)
		appendDrug(drug2, data['sentence'], data)

def appendDrug(drug, sentence, data):
	for k, v in drug.iteritems():
		d = k
		info = v
		allstartingindexes = [m.start() for m in re.finditer(d.lower(), sentence.lower())]
		if len(allstartingindexes) > 0:
			for index in allstartingindexes:
				endIndex = index + (len(d)-1)
				data['drugs'].append({'drug':d, 'type': info['type'], 'start index': index, 'end index': endIndex})

def eliminateSubDrugs(drugs):
	"""
	Naive algorithm (pure O(N^2) runtime) that will drugs that are substrings
	of their bigger drug counterparts.
	"""
	for i in xrange(0, len(drugs)):
		for j in xrange(0, len(drugs)):
			#j drug is in i drug, remove j drug
			if j == i:
				continue
			if j >= len(drugs) or i >= len(drugs):
				break
			if (drugs[i]['drug'].find(drugs[j]['drug']) != -1) and ((drugs[i]['start index'] <= drugs[j]['start index']) and (drugs[j]['end index'] <= drugs[i]['end index'])):
				del drugs[j]
			
#def processOne(data, drugList, stopwords):
	"""
	deprecated....
	supposed to tokenize the sentences and get the verbs and drugs by 
	list index...
	"""
	#oneStc = []
	#index = 0
	#for w in tokStc(data['sentence']):
		#try:
			#word = w
			#if word not in stopwords:
				#if word.lower() not in drugList:
					#word = stemmer.stem(word.lower())
					#if word in verbset:
						#if word in data['verbs']:
							#data['verbs'][word] = [index]
						#else:
							#data['verbs'][word] = index
				#else:
					#if word not in data['drugs']:
						#data['drugs'][word.lower()] = [index]
					#else:
						#data['drugs'][word.lower()].append(index)
				#oneStc.append(word)
			#index += 1
		#except UnicodeDecodeError:
			#print w
	#data['new sentence'] = oneStc
	
#def processOne(data, drugList, stopwords):
	#"""
	#Finds the index locations of all possible drugs found inside 
	#drug-rxcui-type.txt using re.finditer which will also see substrings
	#Possible that it will get substrings and might have some right or wrong 
	#results, but that's mostly on drug-rxcui-type.txt. 
	#"""
	#sentence = data['sentence']
	##stopwords not used...
	#for drug in drugList:
		#allstartingindexes = [m.start() for m in re.finditer(drug.lower(), sentence.lower())]
		#if len(allstartingindexes) > 0:
			#for index in allstartingindexes:
				#endIndex = index + (len(drug)-1)
				#data['drugs'].append({'drug':drug, 'type': drugList[drug], 'start index': index, 'end index':endIndex})

#def testStcs():
	#st = POSTagger()
	#for sentence in seenStcs:
		#st.tag(sentence.split())
		#sys.exit(0)
	
def createXML(corpus, name, typ):
	"""
	Creates an XML file... Proposed Format:
	<document id="quantitative | qualitative">
		<sentence id="" text="">
				<entity id="" text="" charOffset="" /> <!-- the drug from the drug list and multiple of this-->
				<pair id="" e1="" e2="" ddi="" /> <!-- the e1-e2 ddi pair-->
		</sentence>
		....
	</document>
	The printing is not pretty however (sad...)
	
	Issues:
	- there's no way for me right now to pick the best entity to choose. so it chooses the first one.
	"""
	#with open(folder+name+'.xml', 'w') as fil:
	import xml.etree.cElementTree as et
	document = et.Element(u'document') #root
	document.set(u'id', name) #set id of root
	i = 0
	#iterate through the sentences
	for a in xrange(0, len(corpus)):
		value = corpus[a]
		sid = u'.s'+str(i).decode('utf-8')
		sentence = None
		try:
			sentence = et.SubElement(document, u'sentence',
				{
				u'id': (name+sid).decode('utf-8'),
				u'text': value['sentence'].decode('utf-8')
				})
			#set the entities (drugs)
			j = 0
			sid_full = (name+sid).decode('utf-8')
			drugs = {}
			for drugv in value['drugs']:
				drugs[drugv['drug']] = (sid_full+'.e'+str(j)).decode('utf-8')
				entity = et.SubElement(sentence, u'entity',
					{
					u'id': drugs[drugv['drug']],
					u'charOffset': (str(drugv['start index'])+'-'+str(drugv['end index'])).decode('utf-8'),
					u'type': drugv['type'].decode('utf-8'),
					u'text': drugv['drug'].decode('utf-8')
					}
				)
				j += 1
			for h in xrange(0, len(value['drug 1'])):
				for k, v in value['drug 1'][h].iteritems():
					drug1 = k.decode('utf-8')
				for k, v in value['drug 2'][h].iteritems():
					drug2 = k.decode('utf-8')
				modality = setModality(value['modality'][h]).decode('utf-8')
				pair = et.SubElement(sentence, 'pair',
					{
					'id': sid_full+'.p'+str(h),
					'e1': drugs[drug1],
					'e2': drugs[drug2],
					'ddi': modality
					}
				)
		
			i += 1
		except KeyError:
			print 'a drug was not found... removing the sentence for now.'
			if sentence != None:
				document.remove(sentence)
	et.ElementTree(document).write('DDIXML/'+typ+'/'+name + '.xml', encoding='UTF-8')
	#with open('DDIXML/'+name + '.xml', 'w') as fil:
		#fil.write(prettify(document))
		
def setModality(modality):
	if modality == 'positive':
		return 'true'
	return 'false'

def runMain(inp, typ):
	csvinp = csv.reader(inp, dialect='excel')
	next(csvinp)
	qualitative, quantitative = itRows(csvinp)
	workWithSentenceList(qualitative, quantitative)
	#testStcs()
	#with open(folder+'qualitative.xml', 'w') as fil:
	createXML(qualitative, u'qualitative', typ)
	#with open(folder+'quantitative.xml', 'w') as fil:
	createXML(quantitative, u'quantitative', typ)

def main():
	with open(INPUT, 'r') as inp:
		runMain(inp, 'train')
	#seenStcs = ({}, {})
	with open(INPUT2, 'r') as inp:
		runMain(inp, 'test')
		

if __name__ == '__main__':
	main()
