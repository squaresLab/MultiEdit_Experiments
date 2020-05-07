import json
from collections import defaultdict
from statement_classifier import classify_line
from parse_raw_coverage import read_raw_coverage

"""
This parses the git diffs that are provided in the data files for Bears and Defects4J [dissection] to identify different kinds of statements
"""

#############################################################################################

calculated_coverage_patched = {}

def get_coverage_dict(list_entries):
    coverage = {}
    for line in list_entries:
        classname, loc_array = line.split(": ")
        loc_array = loc_array.strip("][").split(", ")
        coverage[classname] = [int(x) for x in loc_array]
    return coverage

for fname in ["data/coverage-experiments/buggy-versions/rawCoverage.data", "data/coverage-experiments/buggy-versions-only-bears/rawCoverage.data"]:
    bug_coverage = read_raw_coverage(fname)
    for bugname, intersect, aggregate in bug_coverage:
        calculated_coverage_patched[bugname] = get_coverage_dict(aggregate)

calculated_coverage_patched["BEARS:191"] = {}
calculated_coverage_patched["BEARS:192"] = {}

with open("data/multitest_multiedit.txt") as f:
    multiedit_multitest = set([x.strip() for x in f.readlines()])

#############################################################################################

classified_lines = defaultdict(int)
num_deleted_lines = 0
num_added_lines = 0
num_uncovered_modifications_deletions = 0
num_modified_deleted_chunks = 0
num_chunks = 0


def classify_added_line(line):
    print(classify_line(line))
    print()
    return True

def classify_deleted_line(line):
    print(classify_line(line))
    print()
    return True

def parse_diff(diff, bug):
    global num_deleted_lines, num_added_lines, num_modified_deleted_chunks, num_uncovered_modifications_deletions, num_chunks, calculated_coverage_patched
    current_class = ""
    current_orig_line_no = 0
    current_patch_line_no = 0
    prev_op = " "
    curr_chunk_covered = False
    coverage_for_bug = calculated_coverage_patched[bug]
    class_coverage = {}

    for line in diff:
        if line.startswith("---") or line.startswith("+++"):
            current_class = line[:-5] # strip .java
            class_found = False
            for k in coverage_for_bug.keys():
                if current_class.endswith(k):
                    class_coverage = coverage_for_bug[k]
                    class_found = True
            if not class_found:
                print(f'MISSING CLASS {bug} {current_class}')


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

                prev_op = "+"
                classified = classify_added_line(line)

            if op_char == "-":
                num_deleted_lines += 1

                # start new chunk
                if prev_op == " ":
                    num_chunks += 1
                    num_modified_deleted_chunks += 1
                    curr_chunk_covered = False

                prev_op = "-"
                classified = classify_deleted_line(line)

                if classified:
                    curr_chunk_covered = True

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

# with open("data/patch_locs.json", "w") as f:
#     json.dump(all_bug_patches, f, indent=3)


