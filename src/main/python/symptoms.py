import json
import matplotlib.pyplot as plt
import pandas
import seaborn as sns

sns.set_style("whitegrid")
sns.set_palette("hls", 2)

multi_classes = {}
single_classes = {}
multi_errors = {}
single_errors = {}

with open("data/multi_edit.txt") as f:
    multi_edit = set([x.strip() for x in f.readlines()])

with open("data/defects4j-bugs.json") as f:
	d4jbugs = json.load(f)

for b in d4jbugs:
	bug_name = f'{b["project"].upper()}:{int(b["bugId"]):03}'
	for test in b["failingTests"]:
		error = test["error"]
		if bug_name in multi_edit:
			classes = multi_classes.get(bug_name, set())
			classes.add(test["className"])
			multi_classes[bug_name] = classes

			multi_errors[error] = multi_errors.get(error, 0) + 1

		else:
			classes = single_classes.get(bug_name, set())
			classes.add(test["className"])
			single_classes[bug_name] = classes

			single_errors[error] = single_errors.get(error, 0) + 1

with open("data/bears-bugs.json") as f:
	bearsbugs = json.load(f)

for b in bearsbugs:
	_, num = b["bugId"].split("-")
	bug_name = f'BEARS:{int(num):03}'
	for test in b["tests"]["failureDetails"]:
		error = test["failureName"]
		if bug_name in multi_edit:
			classes = multi_classes.get(bug_name, set())
			classes.add(test["testClass"])
			multi_classes[bug_name] = classes

			multi_errors[error] = multi_errors.get(error, 0) + 1
			# print("multi: " + bug_name)

		else:
			classes = single_classes.get(bug_name, set())
			classes.add(test["testClass"])
			single_classes[bug_name] = classes

			single_errors[error] = single_errors.get(error, 0) + 1

df1 = pandas.DataFrame.from_dict(multi_errors, orient='index', columns=["multi"])
df2 = pandas.DataFrame.from_dict(single_errors, orient='index', columns=["single"])
df = pandas.merge(df2, df1, left_index=True, right_index=True, sort=False, how="outer")

df = df[(df["multi"] > 1) | (df["single"] > 1)]

df.sort_values(["multi", "single"], inplace=True, ascending=False)
df.plot(kind='barh')
# plt.tight_layout()
# plt.xticks(rotation=45)
plt.show()