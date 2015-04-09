import codecs
import os
import nltk
import sys
import re
import csv
from operator import itemgetter, attrgetter, methodcaller

col = ['fileName', 'pType', 'p', 'pAnnotator', 'pSpanStart', 'pSpanEnd', 'oType', 'o',
       'oAnnotator', 'metabolite', 'oSpanStart', 'oSpanEnd', 'modality', 'iPhraseType',
       'iPhrase', 'iPhraseAnnotator', 'iPhraseSpanStart', 'iPhraseSpanEnd']
# data is stored in an 'off-by-one' fashion due to the Header column names
# so first element is data[1], in other words -1 to index desired element
# then to retreive key-value is data[1][0][1] where the second index is the 
# column and the the third index is the key or value 0,1
# so data[1][0][1] = package-insert-section-1.txt
data = [] 

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
			row.append(line[i].rstrip().lstrip())
		data.append(zip(col, row))
		ctr = ctr + 1
	csvfile.closed
	return data
	
def read_file(paramFile):
	try:
		packageFile = codecs.open(os.path.join('all-statements-combined', paramFile), 'rb')
	except IOError:
		print "Error: File not found"
	packageText = packageFile.read()
	packageFile.closed
	decoded = packageText.decode('windows-1252')
	encoded = decoded.encode('utf8', errors='strict')

	encoded = re.sub(r'[^\x00-\x7f]',u'',encoded)
	return encoded

def parse_sentences(paramText):
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

def getKey(item):
	return int(item[4][1])
	
def entity_array(paramText): # @param: data[][][]
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


text = read_file('package-insert-section-104.txt')
sentences = parse_sentences(text)

count = 0
for i in range(len(sentences)):
	print i , sentences[i]
print len(text), count
#for i in range(len(sentences)):
#	for j in range(len(sentences[i])):
#		print count, sentences[i][j]
#		count += 1
#for i in range(len(text)):
#	print i, text[i]

























