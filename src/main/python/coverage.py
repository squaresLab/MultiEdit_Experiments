import json
import matplotlib.pyplot as plt
import seaborn as sns

folders = ["data/coverage-experiments/dec16-Chart1-Lang24", 
			"data/coverage-experiments/dec16-Lang26-Lang41", 
			"data/coverage-experiments/dec16-Lang43-Lang61", 
			"data/coverage-experiments/dec16-Lang63-Time11"]

with open("data/more_than_one_test.json") as f:
	more_than_one_test = set(json.load(f))

disjoint = 0
same = 0
inBetween = 0

multitest_disjoint = 0
multitest_same = 0
multitest_inBetween = 0

disjoint_projects = {}
same_projects = {}
inBetween_projects = {}

multitest_disjoint_projects = {}
multitest_same_projects = {}
multitest_inBetween_projects = {}

for dir in folders:
	with open(dir+"/disjoint.data") as f:
		for line in f:
			line = line.strip()
			if len(line) > 0:
				project, bugnum = line.split(":")

				disjoint+=1
				disjoint_projects[project] = disjoint_projects.get(project, 0) + 1
				if line in more_than_one_test:
					multitest_disjoint += 1
					multitest_disjoint_projects[project] = multitest_disjoint_projects.get(project, 0) + 1
	with open(dir+"/same.data") as f:
		for line in f:
			line = line.strip()			
			if len(line) > 0:
				project, bugnum = line.split(":")

				same+=1
				same_projects[project] = same_projects.get(project, 0) + 1
				if line in more_than_one_test:
					multitest_same += 1
					multitest_same_projects[project] = multitest_same_projects.get(project, 0) + 1
	with open(dir+"/inBetween.data") as f:
		for line in f:
			line = line.strip()
			if len(line) > 0:
				project, bugnum = line.split(":")

				inBetween+=1
				inBetween_projects[project] = inBetween_projects.get(project, 0) + 1
				if line in more_than_one_test:
					multitest_inBetween += 1
					multitest_inBetween_projects[project] = multitest_inBetween_projects.get(project, 0) + 1

print(f"Total projects: {disjoint + same + inBetween}")
print(f"Total projects with multiple tests: {multitest_disjoint + multitest_same + multitest_inBetween}")
print(f"Multiple tests disjoint: {multitest_disjoint}")
print(f"Multiple tests same: {multitest_same}")
print(f"Multiple tests inBetween: {multitest_inBetween}")


plt.figure()
plt.bar(["disjoint", "in between", "same"], [disjoint, inBetween, same])
plt.title("Distribution of coverage, all patches")
plt.xlabel("Coverage pattern")
plt.ylabel("Number patches")

plt.figure()
plt.bar(["disjoint", "in between", "same"], [multitest_disjoint, multitest_inBetween, multitest_same])
plt.title("Distribution of coverage, patches w/ multiple failing tests")
plt.xlabel("Coverage pattern")
plt.ylabel("Number patches")

fig, axes = plt.subplots(3, 2)
fig.suptitle("Distribution of coverage, all patches, by project")
chart = 'CHART'
axes[0, 0].set_title(chart)
axes[0, 0].bar(["disjoint", "in between", "same"],
	[disjoint_projects[chart], inBetween_projects[chart], same_projects[chart]])
closure = 'CLOSURE'
axes[0, 1].set_title(closure)
axes[0, 1].bar(["disjoint", "in between", "same"],
	[disjoint_projects[closure], inBetween_projects[closure], same_projects[closure]])
lang = 'LANG'
axes[1, 0].set_title(lang)
axes[1, 0].bar(["disjoint", "in between", "same"],
	[disjoint_projects[lang], inBetween_projects[lang], same_projects[lang]])
math = 'MATH'
axes[1, 1].set_title(math)
axes[1, 1].bar(["disjoint", "in between", "same"],
	[disjoint_projects[math], inBetween_projects[math], same_projects[math]])
mockito = 'MOCKITO'
axes[2, 0].set_title(mockito)
axes[2, 0].bar(["disjoint", "in between", "same"],
	[disjoint_projects[mockito], inBetween_projects[mockito], same_projects[mockito]])
time = 'TIME'
axes[2, 1].set_title(time)
axes[2, 1].bar(["disjoint", "in between", "same"],
	[disjoint_projects[time], inBetween_projects[time], same_projects[time]])

fig, axes = plt.subplots(3, 2)
fig.suptitle("Distribution of coverage, patches w/ multiple failing tests, by project")
chart = 'CHART'
axes[0, 0].set_title(chart)
axes[0, 0].bar(["disjoint", "in between", "same"],
	[multitest_disjoint_projects[chart], multitest_inBetween_projects[chart], multitest_same_projects[chart]])
closure = 'CLOSURE'
axes[0, 1].set_title(closure)
axes[0, 1].bar(["disjoint", "in between", "same"],
	[multitest_disjoint_projects[closure], multitest_inBetween_projects[closure], multitest_same_projects[closure]])
lang = 'LANG'
axes[1, 0].set_title(lang)
axes[1, 0].bar(["disjoint", "in between", "same"],
	[multitest_disjoint_projects[lang], multitest_inBetween_projects[lang], multitest_same_projects[lang]])
math = 'MATH'
axes[1, 1].set_title(math)
axes[1, 1].bar(["disjoint", "in between", "same"],
	[multitest_disjoint_projects[math], multitest_inBetween_projects[math], multitest_same_projects[math]])
mockito = 'MOCKITO'
axes[2, 0].set_title(mockito)
axes[2, 0].bar(["disjoint", "in between", "same"],
	[multitest_disjoint_projects[mockito], multitest_inBetween_projects[mockito], multitest_same_projects[mockito]])
# time = 'TIME'
# axes[2, 1].set_title(time)
# axes[2, 1].bar(["disjoint", "in between", "same"],
# 	[multitest_disjoint_projects[time], multitest_inBetween_projects[time], multitest_same_projects[time]])


plt.show()