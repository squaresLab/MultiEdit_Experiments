import json

"""
This script automatically generates a file containing the multi-test and multi-edit bugs, for coverage experiments.
"""
with open("data/bears-bugs.json") as f:
    bears_bugs = json.load(f)

with open("data/defects4j-bugs.json") as f:
    d4j_bugs = json.load(f)

multi_edit = set()
with open("data/multi-location-bugs/multi_location_bears.data") as f:
    multi_edit.update([s.strip() for s in f])

with open("data/multi-location-bugs/multi_location_d4j.data") as f:
    multi_edit.update([s.strip() for s in f])

multi_test = []
both = []

for bug in d4j_bugs:
    bugname = f'{bug["project"].upper()}:{int(bug["bugId"]):03}' #check if this is correct
    tests = bug["failingTests"]
    if len(tests) > 1:
        multi_test.append(bugname)
        if bugname in multi_edit:
            both.append(bugname)

for bug in bears_bugs:
    bugname = f'BEARS:{int(bug["bugId"].split("-")[1]):03}' #check this is correct
    tests = bug["tests"]["failureDetails"]
    if len(tests) > 1:
        multi_test.append(bugname)
        if bugname in multi_edit:
            both.append(bugname)

print(multi_test)
print(len(multi_test))
print(both)
print(len(both))

with open("data/more_than_one_test.txt", "w") as f:
    for b in multi_test:
        f.write(b + "\n")

with open("data/multitest_multiedit.txt", "w") as f:
    for b in both:
        f.write(b + "\n")