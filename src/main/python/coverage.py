import matplotlib.pyplot as plt
import seaborn as sns

"""
This is the script we used to generate the plots of the coverage categories.
"""

# folders = ["data/coverage-experiments/mar4-bears"]
# folders = ["data/coverage-experiments/mar4-d4j", "data/coverage-experiments/mar4-mockito"]
# folders = ["data/coverage-experiments/mar4-bears", "data/coverage-experiments/mar4-d4j", "data/coverage-experiments/mar4-mockito"]
folders = ["data/coverage-experiments/coverage-data-final"]

plt.rcParams.update({'font.size': 20})
# plt.rcParams.update({'font.family': 'serif'})



with open("data/more_than_one_test.txt") as f:
    more_than_one_test = set([x.strip() for x in f.readlines()])

multi_edit = []

with open("data/multi-location-bugs/multi_location_d4j.data") as f:
    multi_edit = [x.strip() for x in f.readlines()]

with open("data/multi-location-bugs/multi_location_bears.data") as f:
    multi_edit.extend([x.strip() for x in f.readlines()])

multi_edit = set(multi_edit)


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

bugs = set()

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
                        multichunk_same_projects[project] = same_projects.get(project, 0) + 1
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
                        multichunk_inBetween_projects[project] = inBetween_projects.get(project, 0) + 1
                        print(f'overlap {line}')


print(f"Total bugs with multiple tests: {disjoint + same + inBetween}")
print(f"Multiple tests disjoint: {disjoint}")
print(f"Multiple tests same: {same}")
print(f"Multiple tests inBetween: {inBetween}")

sum_mchunk = multichunk_disjoint + multichunk_same + multichunk_inBetween

print(f"Total bugs with multiple tests & multiple chunks: {sum_mchunk}")
print(f"Multitest/multichunk disjoint: {multichunk_disjoint}")
print(f"Multitest/multichunk same: {multichunk_same}")
print(f"Multitest/multichunk inBetween: {multichunk_inBetween}")

disjoint_percent = round(100 * multichunk_disjoint / sum_mchunk)
identical_percent = round(100 *multichunk_same / sum_mchunk)
overlap_percent = round(100 * multichunk_inBetween / sum_mchunk)


# plt.figure()
# plt.bar(["disjoint", "in between", "same"], [disjoint, inBetween, same])
# plt.title("Distribution of coverage, all multitest patches")
# plt.xlabel("Coverage pattern")
# plt.ylabel("Number patches")

plt.figure()
ax = plt.bar(["disjoint", "overlap", "identical"], [multichunk_disjoint, multichunk_inBetween, multichunk_same])#, color='#e6b8afff')
# plt.title("All multi-location and multi-test:\nDistribution of coverage patterns")
plt.ylim(0, 85)
plt.xlabel("Coverage pattern")
plt.ylabel("Number of patches")


# Add this loop to add the annotations
for p, percent, raw in zip(ax.patches, [disjoint_percent, overlap_percent, identical_percent], [multichunk_disjoint, multichunk_inBetween, multichunk_same]):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'{raw} ({percent}%)', (x + (width/2) - 0.25, y + height + 0.75))

plt.show()