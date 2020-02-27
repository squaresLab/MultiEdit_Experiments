import json
import matplotlib.pyplot as plt
import pandas
import seaborn as sns
import sys
from collections import defaultdict


### Choose one
# from classifications.exception_classify import classify
# from classifications.assertion_classify import classify
# from classifications.assertion_lessgranular_classify import classify
# from classifications.smaller_classify import classify
# from classifications.smallerer_classify import classify
from classifications.assert_only_classify import classify

sns.set_style("whitegrid")


multi_errors = defaultdict(list)
single_errors = defaultdict(list)

with open("data/multi_edit.txt") as f:
    multi_edit = set([x.strip() for x in f.readlines()])

with open("data/defects4j-bugs.json") as f:
	d4jbugs = json.load(f)


with open("data/bears-bugs.json") as f:
	bearsbugs = json.load(f)


for b in d4jbugs:
	bug_name = f'{b["project"].upper()}:{int(b["bugId"]):03}'

	for test in b["failingTests"]:
		error = test["error"]
		msg = test["message"]
		if bug_name in multi_edit:
			multi_errors[bug_name].append(classify(error, msg))

		else:
			single_errors[bug_name].append(classify(error, msg))

for b in bearsbugs:
	_, num = b["bugId"].split("-")
	bug_name = f'BEARS:{int(num):03}'

	for test in b["tests"]["failureDetails"]:
		error = test["failureName"]
		msg = test.get("detail", "")
		if bug_name in multi_edit:
			multi_errors[bug_name].append(classify(error, msg))

		else:
			single_errors[bug_name].append(classify(error, msg))


with open("data/multi_symptoms.csv", 'w') as f:
	for name, arr in multi_errors.items():
		f.write(f'{name},{",".join(arr)}\n')

with open("data/single_symptoms.csv", 'w') as f:
	for name, arr in single_errors.items():
		f.write(f'{name},{",".join(arr)}\n')