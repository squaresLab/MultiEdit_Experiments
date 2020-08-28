import matplotlib.pyplot as plt
import seaborn as sns
from parse_raw_coverage import read_raw_coverage
import scipy

buggy_coverage_files = ["data/coverage-experiments/buggy-coverage-final/rawCoverage.data"]
exp_1_folder_name = "data/coverage-experiments/coverage-data-final"

def count_num_lines(list_entries):
	num_lines = 0
	for line in list_entries:
		loc_array = line.split(": ")[1]
		num_lines += len(loc_array.split(", "))
	return num_lines

original_results = {}

with open(exp_1_folder_name + "/disjoint.data") as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            original_results[line] = "disjoint"


with open(exp_1_folder_name + "/same.data") as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            original_results[line] = "identical"

with open(exp_1_folder_name + "/inBetween.data") as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            original_results[line] = "overlap"


originally_disjoint = []
originally_identical = []
originally_overlap = []

for fname in buggy_coverage_files:
	bug_coverage = read_raw_coverage(fname)

	for bugname, intersect, aggregate in bug_coverage:
		num_intersect = count_num_lines(intersect)
		num_aggregate = count_num_lines(aggregate)
		
		percentage = float(num_intersect) / num_aggregate

		if original_results[bugname] == "disjoint":
			originally_disjoint.append(percentage)
		elif original_results[bugname] == "identical":
			originally_identical.append(percentage)
		elif original_results[bugname] == "overlap":
			originally_overlap.append(percentage)

print(len(originally_disjoint) + len(originally_overlap) + len(originally_identical))

print("disjoint", originally_disjoint)
print("identical", originally_identical)
print("overlap", originally_overlap)

print(scipy.stats.mannwhitneyu(originally_overlap, originally_disjoint, use_continuity=True, alternative='two-sided'))
print(scipy.stats.mannwhitneyu(originally_identical, originally_disjoint, use_continuity=True, alternative='greater'))
print(scipy.stats.mannwhitneyu(originally_identical, originally_overlap, use_continuity=True, alternative='greater'))

plt.rcParams.update({'font.size': 20})
# plt.rcParams.update({'font.family': 'serif'})

prop = {'linewidth':3}

bp = plt.boxplot([originally_disjoint, originally_overlap, originally_identical], labels=["Contradicts", "Partially Holds", "Holds"],
	boxprops=prop,
	capprops=prop,
	whiskerprops=prop,
	flierprops=prop,
	medianprops=prop,
	meanprops=prop)
plt.ylabel("Percent coverage by all failing tests",labelpad=15)
plt.yticks(ticks=[0.0, 0.2, 0.4, 0.6, 0.8, 1.0], labels=['0%', '20%', '40%', '60%', '80%', '100%'])
plt.xlabel("Coverage pattern",labelpad=15)

for medline in bp['medians']:
    linedata = medline.get_ydata()
    median = linedata[0]
    print(median)
# plt.title("Do the failing tests of multi-edit and multi-test bugs\nexecute the same lines of code?", pad=40, fontdict={'fontsize': "xx-large",})
plt.show()



