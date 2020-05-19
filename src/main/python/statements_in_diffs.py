import json
from collections import defaultdict
from statement_classifier import classify_line
from parse_raw_coverage import read_raw_coverage

"""
This parses the git diffs that are provided in the data files for Bears and Defects4J [dissection] to identify different kinds of statements
"""

#############################################################################################

calculated_coverage_buggy = {}
calculated_coverage_patched = {}

def get_coverage_dict(list_entries):
    coverage = {}
    for line in list_entries:
        classname, loc_array = line.split(": ")
        loc_array = loc_array.strip("][").split(", ")
        coverage[classname] = set([int(x) for x in loc_array])
    return coverage

for fname in ["data/coverage-experiments/buggy-versions/rawCoverage.data", "data/coverage-experiments/buggy-versions-only-bears/rawCoverage.data"]:
    bug_coverage = read_raw_coverage(fname)
    for bugname, intersect, aggregate in bug_coverage:
        calculated_coverage_buggy[bugname] = get_coverage_dict(aggregate)

bug_coverage = read_raw_coverage("data/coverage-experiments/coverage-data-final/rawCoverage.data")
for bugname, intersect, aggregate in bug_coverage:
    calculated_coverage_patched[bugname] = get_coverage_dict(aggregate)

# these have no coverage whatsoever
calculated_coverage_buggy["BEARS:191"] = {}
calculated_coverage_buggy["BEARS:192"] = {}
calculated_coverage_patched["BEARS:191"] = {}
calculated_coverage_patched["BEARS:192"] = {}

with open("data/multitest_multiedit.txt") as f:
    multiedit_multitest = set([x.strip() for x in f.readlines()])

#############################################################################################

classified_lines = defaultdict(int)
num_deleted_lines = 0
num_added_lines = 0
num_classified_lines = 0
num_uncovered_modifications_deletions = 0
num_modified_deleted_chunks = 0
num_chunks = 0
uncovered_deleted_modified_chunks = defaultdict(list)


def classify_added_line(line):
    global classified_lines, num_classified_lines
    num_classified_lines += 1
    classification = classify_line(line)
    # if classification == "arraycreator":
    #     print("check", classification, line)
    classified_lines[classification] += 1
    return classification

def classify_deleted_line(line):
    global classified_lines, num_classified_lines
    print(line)
    num_classified_lines += 1
    classification = classify_line(line)
    if classification == "classcreator" or classification == "memberreference":
        print("check", classification, line)
    # classified_lines[classification] += 1
    return classification


def parse_diff(diff, bug):
    global num_deleted_lines, num_added_lines, num_modified_deleted_chunks, num_uncovered_modifications_deletions, num_chunks, calculated_coverage_buggy
    current_class = ""
    current_orig_line_no = 0
    current_patch_line_no = 0
    prev_op = " "
    curr_chunk_covered = False

    coverage_for_buggy = calculated_coverage_buggy[bug]
    class_coverage_buggy = set()

    coverage_for_patched = calculated_coverage_patched[bug]
    class_coverage_patched = set()

    uncovered_classification_for_deleted_chunk = defaultdict(int)

    print(bug)
    for line in diff:
        if line.startswith("---") or line.startswith("+++"):
            current_class = line[:-5] # strip .java
            class_found = False
            for k in coverage_for_buggy.keys():
                if current_class.endswith(k):
                    class_coverage_buggy = coverage_for_buggy[k]
                    class_found = True
            if not class_found:
                class_coverage_buggy = set()
            class_found = False
            for k in coverage_for_patched.keys():
                if current_class.endswith(k):
                    class_coverage_patched = coverage_for_patched[k]
                    class_found = True
            if not class_found:
                class_coverage_patched = set()
                print("MISSING: " + current_class)


        elif "diff --git" in line or line.startswith("index"):
            continue
        elif "@@" in line:
            splitting = line.split(" ")
            current_orig_line_no = int(splitting[1][1:].split(",")[0])
            current_patch_line_no = int(splitting[2][1:].split(",")[0])
            prev_op = " "
        else:
            if len(line) == 0:
                op_char = " "
            else:
                op_char = line[0]

            line = line.lstrip("+-").strip()

            if op_char == " ":
                # end of deletion
                if prev_op == "-": 
                    if not curr_chunk_covered:
                        num_uncovered_modifications_deletions += 1
                        uncovered_deleted_modified_chunks[bug].append(uncovered_classification_for_deleted_chunk)
                        uncovered_classification_for_deleted_chunk = defaultdict(int)
                current_orig_line_no += 1
                current_patch_line_no += 1
                prev_op = " "

            if op_char == "+":
                num_added_lines += 1

                # start new chunk
                if prev_op == " ":
                    num_chunks += 1
                    curr_chunk_covered = False
                # modification
                elif prev_op == "-":
                    if not curr_chunk_covered:
                        num_uncovered_modifications_deletions += 1
                        uncovered_deleted_modified_chunks[bug].append(uncovered_classification_for_deleted_chunk)
                        uncovered_classification_for_deleted_chunk = defaultdict(int)

                prev_op = "+"
                if current_patch_line_no not in class_coverage_patched:
                    print(current_patch_line_no)
                    classified = classify_added_line(line)
                current_patch_line_no += 1

            if op_char == "-":
                num_deleted_lines += 1

                # start new chunk
                if prev_op == " ":
                    num_chunks += 1
                    num_modified_deleted_chunks += 1
                    curr_chunk_covered = False
                    uncovered_classification_for_deleted_chunk = defaultdict(int)

                prev_op = "-"
                if current_orig_line_no in class_coverage_buggy:
                    curr_chunk_covered = True
                else:
                    print(current_orig_line_no)
                    classification = classify_deleted_line(line)
                    uncovered_classification_for_deleted_chunk[classification] += 1


                current_orig_line_no += 1


#############################################################################################

with open("/home/serenach/bears-benchmark/docs/data/bears-bugs.json") as f:
    bears_data = json.load(f)

with open("data/defects4j-bugs.json") as f:
    d4j_data = json.load(f)


for b in bears_data:
    bug_name = b["bugId"]
    bugnum = int(bug_name.split("-")[1])
    bug_name = f'BEARS:{bugnum:03}'
    diff = b["diff"].split("\n")
    if bug_name in multiedit_multitest:
        parse_diff(diff, bug_name)

for d in d4j_data:
    bug_name = f'{d["project"].upper()}:{d["bugId"]:03}'
    diff = d["diff"].split("\n")
    if bug_name in multiedit_multitest:
        parse_diff(diff, bug_name)

with open("data/uncovered_chunk_classification.json", "w") as f:
    json.dump(uncovered_deleted_modified_chunks, f, indent=3)

results = [ (k, classified_lines[k]) for k in classified_lines.keys() ]

for k, v in sorted(results, key=lambda x: x[1]):
    print(k, v)

print()

print("num_deleted_lines", num_deleted_lines)
print("num_added_lines", num_added_lines)
print("num_classified_lines", num_classified_lines)
print("num_uncovered_modifications_deletions", num_uncovered_modifications_deletions)
print("num_modified_deleted_chunks", num_modified_deleted_chunks)
print("num_chunks", num_chunks)

