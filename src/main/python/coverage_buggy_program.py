import matplotlib.pyplot as plt
import seaborn as sns
from parse_raw_coverage import read_raw_coverage

def count_num_lines(list_entries):
	num_lines = 0
	for line in list_entries:
		loc_array = line.split(": ")[1]
		num_lines += len(loc_array.split(", "))
	return num_lines

original_results = {}

with open("data/coverage-experiments/coverage-data-final/disjoint.data") as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            original_results[line] = "disjoint"


with open("data/coverage-experiments/coverage-data-final/same.data") as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            original_results[line] = "identical"

with open("data/coverage-experiments/coverage-data-final/inBetween.data") as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            original_results[line] = "overlap"


originally_disjoint = []
originally_identical = []
originally_overlap = []

for fname in ["data/coverage-experiments/buggy-versions/rawCoverage.data", "data/coverage-experiments/buggy-versions-only-bears/rawCoverage.data"]:
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

plt.rcParams.update({'font.size': 17})
# plt.rcParams.update({'font.family': 'serif'})

plt.boxplot([originally_disjoint, originally_overlap, originally_identical], labels=["disjoint", "overlap", "identical"])
plt.ylabel("Percentage of executed lines were\ncovered by all failing tests",labelpad=30)
plt.xlabel("The coverage category we originally identified",labelpad=30)
plt.title("Do the failing tests of multi-edit and multi-test bugs\nexecute the same lines of code?", pad=40, fontdict={'fontsize': "xx-large",})
plt.show()



