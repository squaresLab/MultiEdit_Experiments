import json
import matplotlib.pyplot as plt
import pandas
import seaborn as sns

sns.set_style("whitegrid")

multi_classes = {}
single_classes = {}
multi_errors = {}
single_errors = {}

num_multi_bugs = 0
num_single_bugs =0

with open("data/multi_edit.txt") as f:
    multi_edit = set([x.strip() for x in f.readlines()])

with open("data/defects4j-bugs.json") as f:
	d4jbugs = json.load(f)

for b in d4jbugs:
	bug_name = f'{b["project"].upper()}:{int(b["bugId"]):03}'
	if bug_name in multi_edit:
		num_multi_bugs += 1
	else:
		num_single_bugs += 1

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
	if bug_name in multi_edit:
		num_multi_bugs += 1
	else:
		num_single_bugs += 1
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

print("multi", num_multi_bugs, "single", num_single_bugs)

df1.sort_values("multi", inplace=True, ascending=True)
df2.sort_values("single", inplace=True, ascending=True)

df1.plot(kind="pie", y="multi", legend=False)
df2.plot(kind="pie", y="single", legend=False)

df = pandas.merge(df2, df1, left_index=True, right_index=True, sort=False, how="outer")
df = df.fillna(0)

df_cleaned = df[(df["multi"] > 1) | (df["single"] > 1)]
others = df[(df["multi"] <= 1) & (df["single"] <= 1)]

df_cleaned.sort_values(["multi", "single"], inplace=True, ascending=False)
sns.set_palette("hls", 2)
df_cleaned.plot(kind='barh')
# plt.tight_layout()
# plt.xticks(rotation=45)
# plt.show()


##### chi-square test
# hold the distribution for single edit as expected, since there are more bugs in there

# dist
symptoms_sums = df_cleaned.sum(axis=0, skipna=True)
other_sums = others.sum(axis=0, skipna=True)

multi_sum = symptoms_sums["multi"] + other_sums["multi"]
single_sum = symptoms_sums["single"] + other_sums["single"]

print("sums, multi, single")
print(multi_sum, single_sum)

actual_v_expected = df_cleaned.copy()
actual_v_expected["multiExpected"] = (actual_v_expected["single"].div(single_sum).mul(multi_sum))#.replace(0, 0.01)

expected_other = other_sums["single"] / single_sum * multi_sum
actual_v_expected.loc["other"] = [other_sums["single"], other_sums["multi"], expected_other]

actual_v_expected.index.name = "symptom"
pandas.set_option('display.max_colwidth', -1)
print(actual_v_expected.to_csv())

chi2 = 0
dof = -1

for index, row in actual_v_expected.iterrows():
	print(chi2)
	chi2 += ((row["multi"]-row["multiExpected"])**2)/row["multiExpected"]
	dof += 1

# print(chi2)
# chi2 += ((other_sums["multi"]-other_sums["single"])**2)/other_sums["single"]

print(f'x2: {chi2}')
print(f'dof: {dof}')
##### df for bogdan

bool_df = pandas.DataFrame(columns=["name", "multi"] + list(df_cleaned.index.values) + ["other"])

for b in d4jbugs:
	row = {}

	bug_name = f'{b["project"].upper()}:{int(b["bugId"]):03}'
	row["name"] = bug_name
	if bug_name in multi_edit:
		row["multi"] = True
	else:
		row["multi"] = False

	for test in b["failingTests"]:
		error = test["error"]
		if error in list(df_cleaned.index.values):
			row[error] = True
		else:
			row["other"] = True

	bool_df = bool_df.append(row, ignore_index=True)

for b in bearsbugs:
	row = {}

	_, num = b["bugId"].split("-")
	bug_name = f'BEARS:{int(num):03}'

	row["name"] = bug_name
	if bug_name in multi_edit:
		row["multi"] = True
	else:
		row["multi"] = False

	for test in b["tests"]["failureDetails"]:
		error = test["failureName"]
		if error in list(df_cleaned.index.values):
			row[error] = True
		else:
			row["other"] = True

	bool_df = bool_df.append(row, ignore_index=True)


bool_df = bool_df.fillna(False)
bool_df.to_csv("data/symptoms.csv")