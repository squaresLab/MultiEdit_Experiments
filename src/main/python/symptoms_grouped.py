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


multi_errors = defaultdict(int)
single_errors = defaultdict(int)

num_multi_bugs = 0
num_single_bugs =0

bool_df = pandas.DataFrame(columns=["name", "multi"] + list(multi_errors.keys()))

with open("data/multi_edit.txt") as f:
	multi_edit = set([x.strip() for x in f.readlines()])

with open("data/defects4j-bugs.json") as f:
	d4jbugs = json.load(f)


with open("data/bears-bugs.json") as f:
	bearsbugs = json.load(f)


for b in d4jbugs:
	bug_name = f'{b["project"].upper()}:{int(b["bugId"]):03}'

	row = {}
	row["name"] = bug_name

	if bug_name in multi_edit:
		num_multi_bugs += 1
		row["multi"] = True
	else:
		num_single_bugs += 1
		row["multi"] = False

	for test in b["failingTests"]:
		error = test["error"]
		msg = test["message"]
		if bug_name in multi_edit:
			classify(error, msg, multi_errors, row)

		else:
			classify(error, msg, single_errors, row)

	bool_df = bool_df.append(row, ignore_index=True)

for b in bearsbugs:
	branch_name = b["bugName"]
	_, num = b["bugId"].split("-")
	bug_name = f'{branch_name}:{int(num):03}'

	row = {}
	row["name"] = bug_name

	if bug_name in multi_edit:
		num_multi_bugs += 1
		row["multi"] = True
	else:
		num_single_bugs += 1
		row["multi"] = False

	for test in b["tests"]["failureDetails"]:
		error = test["failureName"]
		msg = test.get("detail", "")
		if bug_name in multi_edit:
			classify(error, msg, multi_errors, row)

		else:
			classify(error, msg, single_errors, row)

	bool_df = bool_df.append(row, ignore_index=True)

df1 = pandas.DataFrame.from_dict(multi_errors, orient='index', columns=["multi"])
df2 = pandas.DataFrame.from_dict(single_errors, orient='index', columns=["single"])

print("multi", num_multi_bugs, "single", num_single_bugs)

df1.sort_values("multi", inplace=True, ascending=True)
df2.sort_values("single", inplace=True, ascending=True)

df = pandas.merge(df2, df1, left_index=True, right_index=True, sort=False, how="outer")
df = df.fillna(0)
df.sort_values(["multi", "single"], inplace=True, ascending=False)

df.index.name = "symptom"
pandas.set_option('display.max_colwidth', -1)
print(df)

bool_df.replace(to_replace=1.0, value=True, inplace=True)
bool_df = bool_df.fillna(False)
bool_df.index.name = "index"
bool_df.to_csv("data/symptoms.csv")