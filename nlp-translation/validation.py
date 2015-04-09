import csv

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

for m in range(len(data)):
	current = data[m]
	for n in range(len(data)):
		compareTo = data[n]
		if current != compareTo:
			print compareTo[0][1]
		
