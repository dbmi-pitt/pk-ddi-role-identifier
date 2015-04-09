import nltk
#paramText = 'In four patients with cardiovascular disease, coadministration of propafenone 150 mg t.i.d.  with immediate-release metoprolol 50 mg t.i.d.  resulted in two- to five-fold increases in the steady-state concentration of metoprolol. These increases in plasma concentration would decrease the cardioselectivity of metoprolol.'
#paramText = 'Cimetidine: Five studies in healthy volunteers investigated the impact of multiple cimetidine doses on the single or multiple dose pharmacokinetics of nifedipine. Two studies investigated the impact of coadministered cimetidine on blood pressure in hypertensive subjects on nifedipine. In normotensive subjects receiving single doses of 10 mg or multiple doses of up to 20 mg nifedipine t.i.d. alone or together with cimetidine up to 1000 mg/day, the AUC values of nifedipine in the presence of cimetidine were between 1.52 and 2.01 times those in the absence of cimetidine. The Cmax values of nifedipine in the presence of cimetidine were increased by factors ranging between 1.60 and 2.02. The increase in exposure to nifedipine by cimetidine was accompanied by relevant changes in blood pressure or heart rate in normotensive subjects. Hypertensive subjects receiving 10 mg q.d. nifedipine alone or in combination with cimetidine 1000 mg q.d. also experienced relevant changes in blood pressure when cimetidine was added to nifedipine. The interaction between cimetidine and nifedipine is of clinical relevance and blood pressure should be monitored and a reduction of the dose of nifedipine considered.'
#paramText = 'In a study assessing disposition of sertraline (100 mg) on the second of 8 days of cimetidine administration (800 mg daily), there were significant increases in sertraline mean AUC (50%), C max (24%) and half-life (26%) compared to the placebo group. The clinical significance of these changes is unknown.'
paramText = 'Cimetidine: Five studies in healthy volunteers investigated the impact of multiple cimetidine doses on the single or multiple dose pharmacokinetics of nifedipine. Two studies investigated the impact of coadministered cimetidine on blood pressure in hypertensive subjects on nifedipine. In normotensive subjects receiving single doses of 10 mg or multiple doses of up to 20 mg nifedipine t.i.d. alone or together with cimetidine up to 1000 mg/day, the AUC values of nifedipine in the presence of cimetidine were between 1.52 and 2.01 times those in the absence of cimetidine. The Cmax values of nifedipine in the presence of cimetidine were increased by factors ranging between 1.60 and 2.02. The increase in exposure to nifedipine by cimetidine was accompanied by relevant changes in blood pressure or heart rate in normotensive subjects. Hypertensive subjects receiving 10 mg q.d. nifedipine alone or in combination with cimetidine 1000 mg q.d. also experienced relevant changes in blood pressure when cimetidine was added to nifedipine. The interaction between cimetidine and nifedipine is of clinical relevance and blood pressure should be monitored and a reduction of the dose of nifedipine considered.'
abbreviations = ['t.i.d.', 'b.i.d.', 'p.o.', 'q.d.']
paramText = paramText.decode('utf8')
sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
sentences = sent_detector.tokenize(paramText)

retval = []

for s in range(len(sentences)):
	sent = sentences[s].split()
	if sent[0][0].islower():
		retval.append(retval.pop(len(retval)-1) + ' ' + sentences[s])
	else:
		retval.append(sentences[s])
		
for i in range(len(retval)):
	print i, retval[i]
