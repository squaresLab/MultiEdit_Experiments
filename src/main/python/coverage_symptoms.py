import pandas
from collections import defaultdict


symptoms = pandas.read_csv("data/symptoms/assertion-symptoms.csv")

same = []
overlap = []
disjoint = []
with open("data/coverage-experiments/coverage-pattern.txt") as f:
	for line in f:
		pattern, name = line.strip().split()
		if pattern == "same":
			same.append(name)
		elif pattern == "overlap":
			overlap.append(name)
		elif pattern == "disjoint":
			disjoint.append(name)
		else:
			print(f'err: {line}')

same_symptoms = defaultdict(int)
overlap_symptoms = defaultdict(int)
disjoint_symptoms = defaultdict(int)

non_symptom_cols = set(["index", "name", "multi"])
for b in same:
	b_row = symptoms.loc[symptoms['name'] == b]
	for column in symptoms:
		if column not in non_symptom_cols:
			if b_row[column].iat[0]:
				same_symptoms[column] += 1
print(same_symptoms)
for b in overlap:
	b_row = symptoms.loc[symptoms['name'] == b]
	for column in symptoms:
		if column not in non_symptom_cols:
			if b_row[column].iat[0]:
				overlap_symptoms[column] += 1
print(overlap_symptoms)
for b in disjoint:
	b_row = symptoms.loc[symptoms['name'] == b]
	for column in symptoms:
		if column not in non_symptom_cols:
			if b_row[column].iat[0]:
				disjoint_symptoms[column] += 1
print(disjoint_symptoms)