import json

"""
This parses the git diffs that are provided in the data files for Bears and Defects4J [dissection] to extract the patch locations.
"""

def parse_diff(diff):
    patch = {}
    chunks = {}

    current_class = ""
    current_orig_line_no = 0
    current_patch_line_no = 0
    prev_op = " "
    num_chunks = 0
    for line in diff:
        if line.startswith("---") or line.startswith("+++"):
            if num_chunks > 0:
                chunks[current_class] = num_chunks
            current_class = line
            num_chunks = 0
        elif "@@" in line:
            splitting = line.split(" ")
            current_orig_line_no = int(splitting[1][1:].split(",")[0])
            current_patch_line_no = int(splitting[2][1:].split(",")[0])
            prev_op = " "
        elif "diff --git" in line or line.startswith("index"):
            continue
        else:
            if len(line) == 0:
                op_char = " "
            else:
                op_char = line[0]
            line_nums = set(patch.get(current_class, []))
            if op_char == " ":
                if prev_op == "-": # a deletion, not a modification
                    line_nums.add(current_patch_line_no)
                current_orig_line_no += 1
                current_patch_line_no += 1
                prev_op = " "
            if op_char == "+":
                if prev_op == " ":
                    num_chunks += 1
                prev_op = "+"
                line_nums.add(current_patch_line_no)
                current_patch_line_no += 1
            if op_char == "-":
                if prev_op == " ":
                    num_chunks += 1
                prev_op = "-"
                current_orig_line_no += 1
            patch[current_class] = sorted(line_nums)
    chunks[current_class] = num_chunks

    return patch, chunks

#############################################################################################

with open("data/bears-bugs.json") as f:
    bears_data = json.load(f)

with open("data/defects4j-bugs.json") as f:
    d4j_data = json.load(f)

all_bug_patches = []

for b in bears_data:
    bug_name = b["bugId"]
    diff = b["diff"].split("\n")
    patch, chunks = parse_diff(diff)
    all_bug_patches.append({"bugId" : bug_name, "patch" : patch, "total_chunks" : chunks})

for d in d4j_data:
    bug_name = f'{d["project"]} {d["bugId"]}'
    diff = d["diff"].split("\n")
    patch, chunks = parse_diff(diff)
    all_bug_patches.append({"bugId" : bug_name, "patch" : patch, "total_chunks" : chunks})

with open("data/patch_locs.json", "w") as f:
    json.dump(all_bug_patches, f, indent=3)


