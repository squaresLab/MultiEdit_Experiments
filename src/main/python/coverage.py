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

multichunk_disjoint = 0
multichunk_same = 0
multichunk_inBetween = 0

bears_multichunk_same = 0
bears_multichunk_disjoint = 0
bears_multichunk_overlap = 0

d4j_multichunk_same = 0
d4j_multichunk_disjoint = 0
d4j_multichunk_overlap = 0

bugs = set()

for dir in folders:
    with open(dir+"/disjoint.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")


                if line in multi_edit_test:
                    if project == "BEARS":
                        print(f'disjoint bears {line}')
                        bears_multichunk_disjoint += 1
                    else:
                        print(f'disjoint d4j {line}')

                    d4j_multichunk_disjoint += 1

                    multichunk_disjoint+=1


    with open(dir+"/same.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in multi_edit_test:
                    if project == "BEARS":
                        bears_multichunk_same += 1
                    else:
                        d4j_multichunk_same += 1
                    multichunk_same+=1
                    print(f'same {line}')

    with open(dir+"/inBetween.data") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                project, bugnum = line.split(":")

                if line in multi_edit_test:
                    if project == "BEARS":
                        bears_multichunk_overlap += 1
                    else:
                        d4j_multichunk_overlap += 1
                    multichunk_inBetween+=1
                    print(f'overlap {line}')


def plot(num_disjoint, num_overlap, num_identical, maxy=100):
    sum_mchunk = num_disjoint + num_identical + num_overlap

    print(f"Total bugs with multiple tests & multiple chunks: {sum_mchunk}")
    print(f"Multitest/multichunk disjoint: {num_disjoint}")
    print(f"Multitest/multichunk identical: {num_identical}")
    print(f"Multitest/multichunk overlap: {num_overlap}")

    disjoint_percent = round(100 * multichunk_disjoint / sum_mchunk)
    identical_percent = round(100 *multichunk_same / sum_mchunk)
    overlap_percent = round(100 * multichunk_inBetween / sum_mchunk)

    plt.figure()
    ax = plt.bar(["disjoint", "overlap", "identical"], [num_disjoint, num_overlap, num_identical])#, color='#e6b8afff')
    # plt.title("All multi-location and multi-test:\nDistribution of coverage patterns")
    plt.ylim(0, maxy)
    plt.xlabel("Coverage pattern")
    plt.ylabel("Number of patches")


    # Add this loop to add the annotations
    for p, percent, raw in zip(ax.patches, [disjoint_percent, overlap_percent, identical_percent], [num_disjoint, num_overlap, num_identical]):
        width, height = p.get_width(), p.get_height()
        x, y = p.get_xy()
        plt.annotate(f'{raw} ({percent}%)', (x + (width/2) - 0.25, y + height + 0.75))

    plt.show()

plot(multichunk_disjoint, multichunk_inBetween, multichunk_same, maxy=100)
plot(bears_multichunk_disjoint, bears_multichunk_overlap, bears_multichunk_same, maxy=20)
plot(d4j_multichunk_disjoint, d4j_multichunk_overlap, d4j_multichunk_same, maxy=80)