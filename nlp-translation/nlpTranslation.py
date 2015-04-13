################################################################################################
### nlpTranslation
### Richard Mau ALLTEMPS, Richard Boyce PhD
### April 8, 2015
################################################################################################
from xml.etree.ElementTree import Element, SubElement, tostring
from xml.etree import ElementTree
from xml.dom import minidom
import re
import os 
import nltk
import csv
import sys
import codecs
### p is abbreviated for precipitant
### o is abbreviated for object
### i is abbreviated for interaction
col = ['fileName', 'pType', 'p', 'pAnnotator', 'pSpanStart', 'pSpanEnd', 'oType', 'o',
       'oAnnotator', 'metabolite', 'oSpanStart', 'oSpanEnd', 'modality', 'iPhraseType',
       'iPhrase', 'iPhraseAnnotator', 'iPhraseSpanStart', 'iPhraseSpanEnd']
### data is stored in an 'off-by-one' fashion due to the Header column names
### so first element is data[1], in other words -1 to index desired element
### then to retreive key-value is data[1][0][1] where the second index is the 
### column and the the third index is the key or value 0,1
### so data[1][0][1] = package-insert-section-1.txt
data = [] # initiated here for compile_data()

################################################################################################
### 		The Functions
################################################################################################
### @desc: Reads the specific csv file that contains the drug interactions and data[]
### @param: 
### @return: the data[][][] list
def compile_data():
	try:
		csvfile = open('all-consensus-interaction-entities-dumped-05162011.csv', 'rb')
	except IOError:
		print "Error: File not found"
	
	reader = csv.reader(csvfile, delimiter=',')
	ctr = 0
	for line in reader:
		row = []
		for i in range(18):
			row.append(line[i])
		data.append(zip(col, row))
		ctr = ctr + 1
	csvfile.closed
	return data

### @desc: creates a document element for xml 
### @param: paramId = the document id which represents the row in the csv file
### 		paramOrigId = the origId is the section number from the package file
### @return: the document element as document
def create_document(paramId, paramOrigId, paramText):
	paramText = paramText.replace('\n', '')
	document = Element('document', {'id':paramId, 'origId':paramOrigId})
	return document

### @desc: creates a sentence element for xml 
### @param: paramId = the sentence id is a combination of document_id + s[i]
### 		paramText = the entire sentence
###		  	i = the iteration for the sentences [0-(len(text)-1)]
### @return: the sentence element as sentence under document
def create_sentence(paramId, paramText, i):
	paramText = paramText.replace('\n', '\\n')
	sentence = SubElement(document, 'sentence',
		{'id':paramId+'.s'+str(i), 'origId':'s'+str(i), 'text':paramText})
	return sentence

### @desc: creates an entity element for xml
### @param: paramId = combination of sentence_id + e[j]
###	     	paramText = name of the drug
###		    paramOff = the offset in which the drug appears in package text
###		  	i = the sentence id in which the entity appeared
### 		j = the entity id
###  		paramType = the type (Active Ingredient/Drug product/Metabolite)
### @return: the entity element as entity under sentence
def create_entity(paramId, paramText, paramOff, i, j, paramType):
	entity = SubElement(sentence, 'entity',
		{'id':paramId+'.s'+str(i)+'.e'+str(j),
		 'origId':'s'+str(i)+'.e'+str(j),
		 'text': paramText,
		 'charOffset': paramOff + '-' + str(int(paramOff) + len(paramText)),
		 'type':paramType})
	return entity

### @desc: creates a pair element for the xml
### @param: paramId = combination of sentence_id + p[n]
### 		interaction = (true/false), whether there's an interaction
###			modality = (Negative/Positive)
### 		ipt = (Qualitative/Quantitative) interactionPhraseType
###			e1 = acting entity as precipitant
###			e2 = acting entity as object
###			i = sentence number
###			n = pair number
### 		p = displayed text of entity as precipitant
###			o = displayed text of entity as object
### @return: the pair element as pair under sentence 
def create_pair(paramId, interaction, modality, ipt, e1, e2, i, n, p, o):
	pair = SubElement(sentence, 'pair',
		{'id':paramId+'.s'+str(i)+'.p'+str(n),
		 'object':o,
		 'precipitant':p,
		 'e2':paramId+'.s'+str(i)+'.e'+str(e2),
		 'e1':paramId+'.s'+str(i)+'.e'+str(e1),
		 'interaction': interaction, # true or false
		 'modality':modality,
	     'interactionPhraseType':ipt})
	return pair
	
### @desc: creates a sentencespan element for xml 
### @param: paramId = the sentence id is a combination of document_id + s[i]
### 		paramText = the entire sentence
###		  	i = the iteration for the sentencespan [0-(len(text)-1)]
### @return: the sentencespan element as sentencespan under document
def create_sentence_span(paramId, paramText, i):
	paramText = paramText.replace('\n', '\\n')
	sentence_span = SubElement(document, 'sentencespan',
		{'id':paramId+'.sp'+str(i),
		 'origId':'sp' + str(i),
		 'text':paramText})
	return sentence_span

### @desc: creates an entity element for xml under sentencespan
### @param: paramId = combination of sentencespan id + e[j]
###	     	paramText = name of the drug
###		    paramOff = the offset in which the drug appears in package text
###		  	i = the sentencespan id in which the entity appeared
### 		j = the entity id
###  		paramType = the type (Active Ingredient/Drug product/Metabolite)
### @return: the entity element as entity under sentencespan
def create_entity_span(paramId, paramText, paramOff, i, j, paramType):
	entity = SubElement(sentencespan, 'entity',
		{'id':paramId+'.sp'+str(i)+'.e'+str(j),
		 'origId':'sp'+str(i)+'.e'+str(j),
		 'text': paramText,
		 'charOffset': paramOff + '-' + str(int(paramOff) + len(paramText)),
		 'type':paramType})
	return entity

### @desc: creates a pair element for the xml under sentencespan
### @param: paramId = combination of sentencespan id+ p[n]
### 		interaction = (true/false), whether there's an interaction
###			modality = (Negative/Positive)
### 		ipt = (Qualitative/Quantitative) interactionPhraseType
###			e1 = acting entity as precipitant
###			e2 = acting entity as object
###			i = sentence number
###			n = pair number
### 		p = displayed text of entity as precipitant
###			o = displayed text of entity as object
### @return: the pair element as pair under sentencespan
def create_pair_span(paramId, interaction, modality, ipt, e1, e2, i, n, p, o):
	pair = SubElement(sentencespan, 'pair',
		{'id':paramId+'.sp'+str(i)+'.p'+str(n),
		 'object':o,
		 'precipitant':p,
		 'e2':paramId+'.sp'+str(i)+'.e'+str(e2),
		 'e1':paramId+'.sp'+str(i)+'.e'+str(e1),
		 'interaction': interaction, # true or false
		 'modality':modality,
	     'interactionPhraseType':ipt})
	return pair
	
### @desc: prints out xml in a readable format
### @param: elem = the element in which to format
### @return: the formatted result
def prettify(elem):
	roughString = ElementTree.tostring(elem, 'utf-8')
	reparsed = minidom.parseString(roughString)
	result = reparsed.toprettyxml(indent='\t', encoding='UTF-8')
	result = result.replace('<?xml version="1.0" encoding="UTF-8"?>', '')
	return result
		
### @desc: parses package-insert-section-xxx.txt for text attribute in sentence
### @param: the file name 
### @return: an array of the sentences as sentences
def parse_sentences(paramText):
	# parse the sentences and put into an array
	paramText = paramText.decode('utf8')
	sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
	sentences = sent_detector.tokenize(paramText)
	
	retval = []
	for s in range(len(sentences)):
		sent = sentences[s].split()
		if sent[0][0].islower():
			retval.append(retval.pop(len(retval)-1) + sentences[s])
		else:
			retval.append(sentences[s])

	return retval
	
### @desc: reads the package-insert-section-xxx.txt from the all-statements-combined directory
### @param: the package-insert-section-xxx.txt file
### @return: the text from the file 
def read_file(paramFile):
	try:
		packageFile = codecs.open(os.path.join('all-statements-combined', paramFile), 'rb')
	except IOError:
		print "Error: File not found"
	packageText = packageFile.read()
	packageFile.closed
	return packageText.decode('ascii', 'ignore').encode('utf8', 'replace')

### @desc: key for sorting
### @param: item = the sort by option
### @return: the sort by precipitant offset
def getKey(item):
	return item[4][1]

### @desc: groups interactions from the csv by section number into a list
### @param: the data[][][] after it has been compiled_data()
### @return: a 4D list that is keyed by section number
def entity_array(paramText): 
	entityArray = [0] * len(paramText)	
	prevSecNo = 0
	temp = []
	
	for i in range(1,len(paramText)):
		secNo = int(re.search('\d+', paramText[i][0][1]).group(0))
		if i == 1 or prevSecNo == secNo:
			temp.append(paramText[i])
		else:
			prevSecNo = secNo
			temp = []
			temp.append(paramText[i])
		entityArray[secNo] = temp
	return entityArray
		
### @desc: creates the header for the xml file
### @param: the xml file 
### @return: 
def header_xml(paramFile):
	xmlFile = codecs.open(paramFile, 'wb')
	xmlFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
	xmlFile.write('<documents>')
	xmlFile.closed

### @desc: creates the footer for the xml file
### @param: the xml file
### @return:
def closer_xml(paramFile):
	xmlFile = codecs.open(paramFile, 'ab')
	xmlFile.write('</documents>')
	xmlFile.closed

### @desc: appends to the xml file
### @param: the xml file
### @return:
def append_xml(paramString, paramFile):
	xmlFile = codecs.open(paramFile, 'ab')
	xmlFile.write(paramString)
	xmlFile.closed
	
################################################################################################
###			Main
################################################################################################
data = compile_data() # stores all the information from csv file
fileName = sys.argv[1]
docId = 'DBMI.pac'
entityArray = entity_array(data) # stores entites by package file number

### Creates the xml file for argv[1] and erase nlpTranslation
header_xml(fileName)
with open('nlpTranslation.csv', 'wb') as csvfile:
	spamwriter = csv.writer(csvfile, delimiter=',')
	spamwriter.writerow(['FileName', 'Precipitant Type', 'Precipitant', 'Precipitant Annotator', 'Precipitant Span Start', 'Precipitant SpanEnd', 'Object Type', 'Object',
       'Object Annotator', 'Metabolite', 'Object Span Start', 'Object Span End', 'Modality', 'Interaction Phrase Type',
       'Interaction Phrase', 'Interaction Phrase Annotator', 'Interaction Phrase Span Start', 'Interaction Phrase Span End'])
### Iterates through each package-insert-section in the 'all-statements-combined' file 
for m in range(1,len(data)):
	secNo = re.search('\d+', data[m][0][1]).group(0)
	docOrigId = 'pac' + secNo 
	packageText = read_file(data[m][0][1])
	text = parse_sentences(packageText) # stores the entire text from file as a list of sentences
	newText = [] # store the entire text from file as a list of sentence spans of 3 sentences
	
	### if same document from the csv file then continue under current document element
	if m == 1 or secNo != prevSecNo:
		document = create_document(docId + secNo, docOrigId, ''.join(map(str, text)))
		entityLen = len(entityArray[int(secNo)])
		head = 0
		nextHead = 0
		### Iterates through each sentence in text
		for i in range(len(text)): 
			sentence = create_sentence(docId + secNo, text[i], i)
			entCount = 0
			entList = []
			entPList = []
			entOList = [] 
			nextHead += len(text[i]) + 1
			
			### Append to the newText for sentence span
			if ((i+1) % 3) == 0 and i != 0: # sentences broken into 3s
				newText.append(text[i-2]+ ' ' + text[i-1]+ ' ' +text[i])
			elif len(text) == 1 and i == 0:
				newText.append(text[i])
			elif len(text) < 3 and i == 1: # text is 2 in length
				newText.append(text[i-1] + ' ' + text[i])
			elif (len(text) > 3) and (len(text) % 3 == len(text) - i):
				if len(text) % 3 == 1:
					newText.append(text[i])
				elif len(text) % 3 == 2:
					newText.append(text[i] + ' ' + text[i+1])
			
			### Iterate through each interaction in the package-insert-section
			sortedEntArray  = sorted(entityArray[int(secNo)], key=getKey) # sort before entering entity array
			for j in range(entityLen):
				curEnt = sortedEntArray[j]
				currentO = int(curEnt[10][1])
				currentP = int(curEnt[4][1])
				
				### Measure if the interaction is in the current sentence and create entity if not created
				if (head <= currentP and currentP < nextHead):
					entPList.append(curEnt)
					if curEnt[2][1] not in entList:
						entList.append(curEnt[2][1])
						entity = create_entity(docId + secNo, curEnt[2][1], curEnt[4][1], i, entCount, curEnt[1][1])
						entCount += 1
					if curEnt[7][1] not in entList:
						entList.append(curEnt[7][1])
						entity = create_entity(docId + secNo, curEnt[7][1], curEnt[10][1], i, entCount, curEnt[6][1])
						entCount += 1
					else:
						pass
				else:
					pass
			
			### Cartesian Product of all the entities created and create pairs
			pair = 0
			for n in range(entCount):
				e1 = entList[n]
				for p in range(entCount):
					match = 0
					e2 = entList[p]
					for q in range(len(entPList)):
						e3 = entPList[q]
						if e1 == e3[2][1] and e2 == e3[7][1] and n != p:
							match = 1
							create_pair(docId+secNo, 'true', e3[12][1], e3[13][1], n, p, i, pair, e3[2][1], e3[7][1])
							pair += 1
							### Write to csv file
							with open('nlpTranslation.csv', 'ab') as csvfile:
								spamwriter = csv.writer(csvfile, delimiter=',')
								spam = [e3[0][1], e3[1][1], e3[2][1], e3[3][1], e3[4][1], e3[5][1], e3[6][1], 
								e3[7][1], e3[8][1], e3[9][1], e3[10][1], e3[11][1], e3[12][1], e3[13][1], text[i], e3[15][1], e3[16][1], e3[17][1]]
								spamwriter.writerow(spam)
								
					if match == 0  and n != p:
						create_pair(docId+secNo, 'false', '', '', n, p, i, pair, '', '')
						pair +=1
			head = nextHead - 1
			### End of sentence loop
			
		### Beginning of sentence span. Iterate through each sentence span
		head = 0
		nextHead = 0
		for i in range(len(newText)): 
			sentencespan = create_sentence_span(docId + secNo, newText[i].replace('\n', '\\n'), i)
			entCount = 0
			entList = []
			entPList = []
			entOList = []
			nextHead += len(newText[i]) + 2
				
			### Iterate through each interaction in the package-insert-section
			sortedEntArray  = sorted(entityArray[int(secNo)], key=getKey) # sort before entering entity array
			for j in range(entityLen):
				curEnt = sortedEntArray[j]
				currentO = int(curEnt[10][1])
				currentP = int(curEnt[4][1])
				
				### Measure if the interaction is in the current sentence and create entity if not created
				if (head <= currentP and currentP < nextHead):
					entPList.append(curEnt)
					if curEnt[2][1] not in entList:
						entList.append(curEnt[2][1])
						entity = create_entity_span(docId + secNo, curEnt[2][1], curEnt[4][1], i, entCount, curEnt[1][1])
						entCount += 1
					if curEnt[7][1] not in entList:
						entList.append(curEnt[7][1])
						entity = create_entity_span(docId + secNo, curEnt[7][1], curEnt[10][1], i, entCount, curEnt[6][1])
						entCount += 1
					else: 
						pass
				else:
					pass
			
			### Cartesian Product of all the entities created and create pairs
			pair = 0
			for n in range(len(entList)):
				e1 = entList[n]
				for p in range(len(entList)):
					match = 0
					e2 = entList[p]
					for q in range(len(entPList)):
						e3 = entPList[q]
						if e1 == e3[2][1] and e2 == e3[7][1] and n != p:
							match = 1
							create_pair_span(docId+secNo, 'true', e3[12][1], e3[13][1], n, p, i, pair, e3[2][1], e3[7][1])
							pair += 1
					if match == 0 and n != p:
						create_pair_span(docId+secNo, 'false', '', '', n, p, i, pair, '', '')
						pair +=1
			head = nextHead - 2
			### End of sentence span loop	
		prevSecNo = secNo		
		append_xml(prettify(document), fileName)
		del(document)	
		### End of document 
		
	# If the same document, then skip
	else: 
		prevSecNo = secNo
closer_xml(fileName)
### End of Main
