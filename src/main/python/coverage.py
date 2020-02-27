import matplotlib.pyplot as plt
import seaborn as sns

# folders = ["data/coverage-experiments/dec16-Chart1-Lang24",
# 			"data/coverage-experiments/dec16-Lang26-Lang41",
# 			"data/coverage-experiments/dec16-Lang43-Lang61",
# 			"data/coverage-experiments/dec16-Lang63-Time11"]

folders = ["data/coverage-experiments/all_bears"]
# folders = ["data/coverage-experiments/jan15"]
# folders = ["data/coverage-experiments/all_bears", "data/coverage-experiments/jan15"]

with open("data/more_than_one_test.txt") as f:
    more_than_one_test = set([x.strip() for x in f.readlines()])

with open("data/multi_edit.txt") as f:
    multi_edit = set([x.strip() for x in f.readlines()])

disjoint = 0
same = 0
inBetween = 0

multichunk_disjoint = 0
multichunk_same = 0
multichunk_inBetween = 0

disjoint_projects = {}
same_projects = {}
inBetween_projects = {}

multichunk_disjoint_projects = {}
multichunk_same_projects = {}
multichunk_inBetween_projects = {}

for dir in folders:
    with open(dir+"/disjoint.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in more_than_one_test:
                    disjoint+=1
                    disjoint_projects[project] = disjoint_projects.get(project, 0) + 1

                    if line in multi_edit:
                        multichunk_disjoint+=1
                        multichunk_disjoint_projects[project] = disjoint_projects.get(project, 0) + 1
                        print(f'disjoint {line}')


    with open(dir+"/same.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in more_than_one_test:
                    same+=1
                    same_projects[project] = same_projects.get(project, 0) + 1

                    if line in multi_edit:
                        multichunk_same+=1
                        multichunk_same_projects[project] = disjoint_projects.get(project, 0) + 1
                        print(f'same {line}')

    with open(dir+"/inBetween.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in more_than_one_test:
                    inBetween+=1
                    inBetween_projects[project] = inBetween_projects.get(project, 0) + 1

                    if line in multi_edit:
                        multichunk_inBetween+=1
                        multichunk_inBetween_projects[project] = disjoint_projects.get(project, 0) + 1
                        print(f'overlap {line}')


print(f"Total projects with multiple tests: {disjoint + same + inBetween}")
print(f"Multiple tests disjoint: {disjoint}")
print(f"Multiple tests same: {same}")
print(f"Multiple tests inBetween: {inBetween}")

print(f"Total projects with multiple tests & multiple chunks: {multichunk_disjoint + multichunk_same + multichunk_inBetween}")
print(f"Multitest/multichunk disjoint: {multichunk_disjoint}")
print(f"Multitest/multichunk same: {multichunk_same}")
print(f"Multitest/multichunk inBetween: {multichunk_inBetween}")


plt.figure()
plt.bar(["disjoint", "in between", "same"], [disjoint, inBetween, same])
plt.title("Distribution of coverage, all multitest patches")
plt.xlabel("Coverage pattern")
plt.ylabel("Number patches")

plt.figure()
plt.bar(["disjoint", "in between", "same"], [multichunk_disjoint, multichunk_inBetween, multichunk_same])
plt.title("Distribution of coverage, patches w/ multiple chunks")
plt.xlabel("Coverage pattern")
plt.ylabel("Number patches")

# fig, axes = plt.subplots(3, 2)
# fig.suptitle("Distribution of coverage, all multitest patches, by project")
# chart = 'CHART'
# axes[0, 0].set_title(chart)
# axes[0, 0].bar(["disjoint", "in between", "same"],
#     [disjoint_projects.get(chart, 0), inBetween_projects.get(chart, 0), same_projects.get(chart, 0)])
# closure = 'CLOSURE'
# axes[0, 1].set_title(closure)
# axes[0, 1].bar(["disjoint", "in between", "same"],
#     [disjoint_projects.get(closure, 0), inBetween_projects.get(closure, 0), same_projects.get(closure, 0)])
# lang = 'LANG'
# axes[1, 0].set_title(lang)
# axes[1, 0].bar(["disjoint", "in between", "same"],
#     [disjoint_projects.get(lang, 0), inBetween_projects.get(lang, 0), same_projects.get(lang, 0)])
# math = 'MATH'
# axes[1, 1].set_title(math)
# axes[1, 1].bar(["disjoint", "in between", "same"],
#     [disjoint_projects.get(math, 0), inBetween_projects.get(math, 0), same_projects.get(math, 0)])
# mockito = 'MOCKITO'
# axes[2, 0].set_title(mockito)
# axes[2, 0].bar(["disjoint", "in between", "same"],
#     [disjoint_projects.get(mockito, 0), inBetween_projects.get(mockito, 0), same_projects.get(mockito, 0)])
# time = 'TIME'
# axes[2, 1].set_title(time)
# axes[2, 1].bar(["disjoint", "in between", "same"],
#     [disjoint_projects.get(time, 0), inBetween_projects.get(time, 0), same_projects.get(time, 0)])


# fig, axes = plt.subplots(3, 2)
# fig.suptitle("Distribution of coverage, patches w/ multiple cunks, by project")
# chart = 'CHART'
# axes[0, 0].set_title(chart)
# axes[0, 0].bar(["disjoint", "in between", "same"],
#     [multichunk_disjoint_projects.get(chart, 0), multichunk_inBetween_projects.get(chart, 0), multichunk_same_projects.get(chart, 0)])
# closure = 'CLOSURE'
# axes[0, 1].set_title(closure)
# axes[0, 1].bar(["disjoint", "in between", "same"],
#     [multichunk_disjoint_projects.get(closure, 0), multichunk_inBetween_projects.get(closure, 0), multichunk_same_projects.get(closure, 0)])
# lang = 'LANG'
# axes[1, 0].set_title(lang)
# axes[1, 0].bar(["disjoint", "in between", "same"],
#     [multichunk_disjoint_projects.get(lang, 0), multichunk_inBetween_projects.get(lang, 0), multichunk_same_projects.get(lang, 0)])
# math = 'MATH'
# axes[1, 1].set_title(math)
# axes[1, 1].bar(["disjoint", "in between", "same"],
#     [multichunk_disjoint_projects.get(math, 0), multichunk_inBetween_projects.get(math, 0), multichunk_same_projects.get(math, 0)])
# mockito = 'MOCKITO'
# axes[2, 0].set_title(mockito)
# axes[2, 0].bar(["disjoint", "in between", "same"],
#     [multichunk_disjoint_projects.get(mockito, 0), multichunk_inBetween_projects.get(mockito, 0), multichunk_same_projects.get(mockito, 0)])
# time = 'TIME'
# axes[2, 1].set_title(time)
# axes[2, 1].bar(["disjoint", "in between", "same"],
#     [multichunk_disjoint_projects.get(time, 0), multichunk_inBetween_projects.get(time, 0), multichunk_same_projects.get(time, 0)])


plt.show()