import matplotlib.pyplot as plt
import seaborn as sns

"""
This is the script we used to generate the plots of the coverage categories.
"""
folders = ["data/coverage-experiments/coverage-data-final"]

plt.rcParams.update({'font.size': 20})
# plt.rcParams.update({'font.family': 'serif'})



with open("data/more_than_one_test.txt") as f:
    more_than_one_test = set([x.strip() for x in f.readlines()])

with open("data/multi-location-bugs/multi_location_d4j.data") as f:
    d4j_multi_edit = set([x.strip() for x in f.readlines()])

with open("data/multi-location-bugs/multi_location_bears.data") as f:
    bears_multi_edit = set([x.strip() for x in f.readlines()])

disjoint = 0
same = 0
inBetween = 0

d4j_disjoint = 0
d4j_same = 0
d4j_inBetween = 0

bears_disjoint = 0
bears_same = 0
bears_inBetween = 0

for dir in folders:
    with open(dir+"/disjoint.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in more_than_one_test:

                    if line in d4j_multi_edit:
                        d4j_disjoint += 1
                        disjoint += 1
                        print(f'disjoint {line}')
                    if line in bears_multi_edit:
                        bears_disjoint += 1
                        disjoint += 1
                        print(f'disjoint {line}')


    with open(dir+"/same.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in more_than_one_test:
                    
                    if line in d4j_multi_edit:
                        d4j_same += 1
                        same += 1
                        print(f'same {line}')
                    if line in bears_multi_edit:
                        bears_same += 1
                        same += 1
                        print(f'same {line}')

    with open(dir+"/inBetween.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in more_than_one_test:
                    
                    if line in d4j_multi_edit:
                        d4j_inBetween += 1
                        inBetween += 1
                        print(f'inBetween {line}')
                    if line in bears_multi_edit:
                        bears_inBetween += 1
                        inBetween += 1
                        print(f'inBetween {line}')

def get_percentages(num_disjoint, num_same, num_inBetween):
    sum_bugs = num_disjoint + num_same + num_inBetween

    print(f"Total bugs with multitest/multilocation: {sum_bugs}")
    print(f"Multiple tests disjoint: {num_disjoint}")
    print(f"Multiple tests same: {num_same}")
    print(f"Multiple tests inBetween: {num_inBetween}")

    d_percent = 100 * num_disjoint / sum_bugs
    i_percent = 100 *num_same / sum_bugs
    o_percent = 100 * num_inBetween / sum_bugs

    return d_percent, i_percent, o_percent



disjoint_percent, identical_percent, overlap_percent = get_percentages(disjoint, same, inBetween)
d4j_disjoint_percent, d4j_identical_percent, d4j_overlap_percent = get_percentages(d4j_disjoint, d4j_same, d4j_inBetween)
bears_disjoint_percent, bears_identical_percent, bears_overlap_percent = get_percentages(bears_disjoint, bears_same, bears_inBetween)

x_positions = [0, 1, 2]
width = 0.75

all_disjoint_percent = [disjoint_percent, d4j_disjoint_percent, bears_disjoint_percent]
all_overlap_percent = [overlap_percent, d4j_overlap_percent, bears_overlap_percent]
all_identical_percent = [identical_percent, d4j_identical_percent, bears_identical_percent]

disjoint_plus_overlap = [a+b for a, b in zip(all_disjoint_percent, all_overlap_percent)]

# plt.figure()
# plt.bar(["disjoint", "in between", "same"], [disjoint, inBetween, same])
# plt.title("Distribution of coverage, all multitest patches")
# plt.xlabel("Coverage pattern")
# plt.ylabel("Number patches")

plt.figure()

p_disjoint = plt.bar(x_positions, all_disjoint_percent, width, 
    bottom = [0,0,0],
    edgecolor='black',
    linewidth=5,
    color="darkgray")
p_overlap = plt.bar(x_positions, all_overlap_percent, width, 
    bottom=all_disjoint_percent,
    edgecolor='black',
    linewidth=5,
    color="lightgray")
p_identical = plt.bar(x_positions, all_identical_percent, width, 
    bottom=disjoint_plus_overlap,
    edgecolor='black',
    linewidth=5,
    color="white")


# ax = plt.bar(["disjoint", "overlap", "identical"], [multichunk_disjoint, multichunk_inBetween, multichunk_same])#, color='#e6b8afff')
# plt.title("All multi-location and multi-test:\nDistribution of coverage patterns")
plt.ylim(-5,105)
plt.yticks([], [])
plt.xticks(ticks=[0, 1, 2], labels=['All bugs', "Defects4J", "Bears"])
plt.tick_params(length = 0)
# plt.xlabel("Dataset",labelpad=15)
plt.box(False)
# plt.legend((p_disjoint[0], p_overlap[0], p_identical), ('Contradicts', 'Partially Holds', 'Holds'))
# plt.ylabel("Number of patches")


# Add this loop to add the annotations
for p, raw, percent in zip(p_disjoint.patches, [disjoint, d4j_disjoint, bears_disjoint], all_disjoint_percent):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'Contradicts\n{raw} ({round(percent)}%)', (x + (width/2), y + (height/2)), ha="center", va="center", linespacing=1.5)

for p, raw, percent in zip(p_overlap.patches, [inBetween, d4j_inBetween, bears_inBetween], all_overlap_percent):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'Partially Holds\n{raw} ({round(percent)}%)', (x + (width/2), y + (height/2)), ha="center", va="center", linespacing=1.5)

for p, raw, percent in zip(p_identical.patches, [same, d4j_same, bears_same], all_identical_percent):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'Holds\n{raw} ({round(percent)}%)', (x + (width/2), y + (height/2)), ha="center", va="center", linespacing=1.5)

plt.show()