NLP Translation CSV-XML

Richard Mau ALLTEMP, Richard Boyce PhD, Yifan Ning
April 9, 2015

DESCRIPTION:
Translate the all-consensus-interaction-entities-dumped-05162011.csv file
into an XML document for the gold standard and follow on use.

FILES:
	-nlpTranslation.xml
	-all-statements-combined' directory
	
	output: argv[1] # writes the argv[1] as the filename and over writes
			if it already exists. Need the .xml extension.
	input:  working directory contains the 'all-statements-combined' directory

USAGE: 
	Generate the XML document
	1. Ensure 'all-statements-combined' directory is in working directory
	2. Input into terminal: python nlpTranslation.py [output_filename].xml

VALIDATION:
	COMMANDS:
		-- Get the number of documents in nlpTranslation.xml
		grep -wc '<document' nlpTranslation.xml

		-- Get the number of documents in csv per document
		grep 'section-[0-9]*' all-consensus-interaction-entities-dumped-05162011.csv | cut -d, -f1| sort | uniq -c | wc -l
		
		-- Get Modality
		grep 'Negative' all-consensus-interaction-entities-dumped-05162011.csv | wc -l
		grep $'Negative\",\"Quantitative' all-consensus-interaction-entities-dumped-05162011.csv | wc -l
		grep $'Negative\",\"Quantitative' all-consensus-interaction-entities-dumped-05162011.csv | wc -l
		
		grep 'Positive' all-consensus-interaction-entities-dumped-05162011.csv | wc -l
		grep $'Positive\",\"Quantitative' all-consensus-interaction-entities-dumped-05162011.csv | wc -l
		grep $'Positive\",\"Quantitative' all-consensus-interaction-entities-dumped-05162011.csv | wc -l


		-- Do the next commands in xmllint
		xmllint --shell task1-2.xml

			-- Get the number of interactions in the task1-2.xml
			xpath //document/sentence/pair[@interaction='true']

			-- Get the number of negative modalities
			xpath //document/sentence/pair[@modality='Negative']
	
			-- Get the number of positive modalities
			xpath //document/sentence/pair[@modality='Positive']

			-- to Add interactionPhraseType
			xpath //document/sentence/pair[@modality='Negative' and @interactionPhraseType='Qualitative']
			xpath //document/sentence/pair[@modality='Negative' and @interactionPhraseType='Quantitative']
	
			xpath //document/sentence/pair[@modality='Positive' and @interactionPhraseType='Qualitative']
			xpath //document/sentence/pair[@modality='Positive' and @interactionPhraseType='Quantitative']
	
	OUTPUT:
		Documentation:
		- interactions = 592
		- negative modality = 204
		- negative qualitative modality = 202 
		- negative quantitative modality = 2 
		- positive modality = 388
		- positive qualitative modality = 186
		- positive quantitative modality = 202
		
		CSV File:
		- interactions = 608
		- negative modality = 220 
		- negative qualitative modality = 213
		- negative quantitative modality = 7
		- positive modality = 388
		- positive qualitative modality = 186
		- positive quantitative modality = 202
		
		XML: (by sentence pairs)
		- interactions = 608
		- negative modality = 219 
		- negative qualitative modality = 212
		- negative quantitative modality = 7
		- positive modality = 389
		- positive qualitative modality = 187
		- positive quantitative modality = 202
		
		
NOTES:
The script does not use a learning mechanism to determine the drug interactions (i.e. interactions cross files). The script parses each package file and
only identifies drugs listed in the csv file and its interactions. For example in package-insert-section-123.txt, the annotated interactions take place
in offsets 9050-11699 so any drugs mentioned before that are not parsed. The script recognizes when a precipitant and object are BOTH mentioned in a 
sentence and parses it, however, if there is no mention of the BOTH precipitant and object then the sentence is not parsed. This may create gaps as the
script does not account for implied drugs in the context of the entire package file. In order to close this gap, the sentencespan element was added
to provide an adhoc solution by grouping sentences into three sentences. 

There are duplicates in the CSV and therefore duplicates in the XML. Duplicates reflect multiple interaction phrase types and modalities between two drugs. 
for example:
FileName						Precipitant Type	Precipitant	Precipitant Annotator			Precipitant Span Start	Precipitant Span End	Object Type	Object	Object Annotator	Metabolite Active Ingredient	Object Span Start	Object Span End	Modality		Interaction Phrase Type	Interaction Phrase	Interaction Phrase Annotator	Interaction Phrase Span Start	Interaction Phrase Span End
package-insert-section-150.txt	Active ingredient	felbamate 	consensus set annotator team	35						44						Active ingredient	phenobarbital 		consensus set annotator team	50					63	Negative	Qualitative		increase in phenobarbital plasma concentrations 
package-insert-section-150.txt	Active ingredient	felbamate 	consensus set annotator team	359						368						Active ingredient	phenobarbital 		consensus set annotator team	219					232	Positive	Quantitative	Cmin concentration increased to 17.8 micrograms/mL 




		

























