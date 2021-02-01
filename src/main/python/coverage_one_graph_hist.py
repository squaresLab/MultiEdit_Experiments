import matplotlib.pyplot as plt
import seaborn as sns

"""
This is the script we used to generate the plots of the coverage categories.
"""
folders = ["data/coverage-experiments/coverage-data-final"]

plt.rcParams.update({'font.size': 20})
# plt.rcParams.update({'font.family': 'serif'})



with open("data/multitest_multiedit.txt") as f:
    multi_edit_test = set([x.strip() for x in f.readlines()])

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

                if line in multi_edit_test:

                    if line.startswith("BEARS"):
                        bears_disjoint += 1
                        disjoint += 1
                        print(f'disjoint {line}')
                    else:
                        d4j_disjoint += 1
                        disjoint += 1
                        print(f'disjoint {line}')

    with open(dir+"/same.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in multi_edit_test:

                    if line.startswith("BEARS"):
                        bears_same += 1
                        same += 1
                        print(f'same {line}')
                    else:
                        d4j_same += 1
                        same += 1
                        print(f'same {line}')

    with open(dir+"/inBetween.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in multi_edit_test:
                    if line.startswith("BEARS"):
                        bears_inBetween += 1
                        inBetween += 1
                        print(f'inBetween {line}')
                    else:
                        d4j_inBetween += 1
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

    return d_percent, o_percent, i_percent


all_raw = [disjoint, inBetween, same]
d4j_raw = [d4j_disjoint, d4j_inBetween, d4j_same]
bears_raw = [bears_disjoint, bears_inBetween, bears_same]

all_percent = get_percentages(disjoint, same, inBetween)
d4j_percent = get_percentages(d4j_disjoint, d4j_same, d4j_inBetween)
bears_percent = get_percentages(bears_disjoint, bears_same, bears_inBetween)

x_positions_all = [0, 1, 2]
x_positions_d4j = [5, 6, 7]
x_positions_bears = [10, 11, 12]

width = 0.75
colors = ["black", "gray", "white"]

# plt.figure()
# plt.bar(["disjoint", "in between", "same"], [disjoint, inBetween, same])
# plt.title("Distribution of coverage, all multitest patches")
# plt.xlabel("Coverage pattern")
# plt.ylabel("Number patches")

plt.figure()


p_all = plt.bar(x_positions_all, all_raw, width,
    edgecolor='black',
    linewidth=1,
    color=colors)
p_d4j = plt.bar(x_positions_d4j, d4j_raw, width, 
    edgecolor='black',
    linewidth=1,
    color=colors)
p_bears = plt.bar(x_positions_bears, bears_raw, width, 
    edgecolor='black',
    linewidth=1,
    color=colors)


# ax = plt.bar(["disjoint", "overlap", "identical"], [multichunk_disjoint, multichunk_inBetween, multichunk_same])#, color='#e6b8afff')
# plt.title("All multi-location and multi-test:\nDistribution of coverage patterns")

plt.xticks(ticks=[1, 6, 11], labels=['All bugs', "Defects4J", "Bears"])
plt.tick_params(length = 0)
# plt.xlabel("Dataset",labelpad=15)
plt.legend((p_all[0], p_all[1], p_all[2]), ('Contradicts', 'Partially Holds', 'Holds'))
plt.ylabel("Number of patches", labelpad=15)
plt.ylim(0,100)
plt.grid(True, 'major', 'y')
ax = plt.gca()
ax.spines['right'].set_visible(False)
ax.spines['left'].set_visible(False)
ax.spines['top'].set_visible(False)
ax.set_axisbelow(True)




# Add this loop to add the annotations
for p, percent, raw in zip(p_all.patches, all_percent, all_raw):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'{raw} ({round(percent)}%)', (x + (width/2), y + height + 2), ha='center', rotation=90)

for p, percent, raw in zip(p_d4j.patches, d4j_percent, d4j_raw):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'{raw} ({round(percent)}%)', (x + (width/2), y + height + 2), ha='center', rotation=90)

for p, percent, raw in zip(p_bears.patches, bears_percent, bears_raw):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'{raw} ({round(percent)}%)', (x + (width/2), y + height + 2), ha='center', rotation=90)


plt.show()