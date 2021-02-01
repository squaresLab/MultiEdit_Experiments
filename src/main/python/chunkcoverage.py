import json
import re

"""
This script is used to identify bugs for which the coverage category identified by the experiment based on Jacoco differs
from the coverage category that would be assigned based on looking at whether the tests covered any patched line at a location.

For example, this script identifies bugs where the patch looks like:

location 1:
```
x = foo();

if (x > 0) {
	bar();
}
```

location 2:
```
baz();
```

If the failing tests execute both locations, but one test fails the if statement and the other enters the if statement,
the program would have classified this as "overlap."

However, it should have been classified as "same," since both locations were executed.
"""

raw_coverage_files = ["data/coverage-experiments/coverage-data-final/rawCoverage.data"]

d4jnames = {"CHART": "Chart", 
"CLOSURE": "Closure", 
"LANG": "Lang", 
"MATH": "Math", 
"MOCKITO": "Mockito", 
"TIME": "Time", 
"JSOUP":"Jsoup",
"JACKSONCORE":"JacksonCore",
"CLI":"Cli",
"JACKSONDATABIND":"JacksonDatabind",
"COMPRESS": "Compress",
"JXPATH":"JxPath",
"GSON":"Gson",
"CODEC": "Codec",
"CSV":"Csv",
"JACKSONXML":"JacksonXml",
"COLLECTIONS": "Collections"}

mode = "PATCH"
patch_pattern = re.compile("(.+): \[(.+)]")
all_cov = {}

num = 0

def parse_cov(f):
	global mode, num, all_cov
	name = ""
	intersect = {}
	union = {}
	for line in f:
		if line.startswith("PATCH"):
			num += 1
			all_cov[name] = { "INTERSECT": intersect, "UNION": union }
			mode = "PATCH"
			name = line.split(" : ")[1].strip()
			proj, bugnum = name.split(":")
			if proj not in d4jnames:
				name = f"BEARS:{bugnum}"
			intersect = {}
			union = {}

		elif line.startswith("INTERSECT"):
			mode = "INTERSECT"
		elif line.startswith("UNION"):
			mode = "UNION"
		else:
			match = patch_pattern.fullmatch(line.strip())
			if match:
				class_name = match.group(1)
				cov = [int(x) for x in match.group(2).split(", ")]
				if mode == "INTERSECT":
					intersect[class_name] = cov
				elif mode == "UNION":
					union[class_name] = cov
				else:
					print("err: " + line)
			else:
				print("err: " + line)
	num += 1
	all_cov[name] = { "INTERSECT": intersect, "UNION": union }


# # patch locations based on Java diff tool that I use for the other experiments
# def parse_patches(f):
# 	name = ""
# 	patches = {} # {bugname : {class: {line no : chunk index}}}
# 	num_chunks = 0
# 	for line in f:
# 		line = line.strip()
# 		if line == "Patch" or line == "" or line.endswith('patch'):
# 			continue
# 		elif line == "----------------":
# 			continue
# 		else:
# 			match = patch_pattern.fullmatch(line.strip())
# 			if match:
# 				class_name = match.group(1)
# 				cov = sorted([int(x) for x in match.group(2).split(", ")])
# 				patch_locs = {}
# 				prev_line = cov[0]-1
# 				for i in cov:
# 					if i != prev_line + 1:
# 						num_chunks += 1
# 					patch_locs[i] = num_chunks
# 					prev_line = i
# 				patches[name][class_name] = patch_locs
# 			else:
# 				# probably a bug number
# 				print(line)
# 				proj, num = line.split(" ")
# 				name = f'{proj.upper()}:{int(num):03}'
# 				patches[name] = {}
# 				num_chunks = 0
# 	return patches


for fname in raw_coverage_files:
	with open(fname) as f:
		parse_cov(f)

print(all_cov)
print(len(all_cov))
print(num)


# Patch locations based on bash diff tool
# I'm using this one because the way I count locations is slightly more accurate here
def get_chunk_ranges(fname):
	# print(fname)
	with open(fname) as f:
		chunk_map = {} # {class: {line no : chunk index}}
		chunk_num = 0
		current_class = ""

		for line in f:
			# print(line)

			if line.startswith("diff"): # class
				tokens = line.split()
				current_class = tokens[-1]
				chunk_map[current_class] = {}
			elif line.startswith("Only in"):
				tokens = line.split()
				path = tokens[-2].strip(":")
				current_class = f'{path}/{tokens[-1]}'
				chunk_map[current_class] = {i: 0 for i in range(1000)}
			elif re.match("[0-9,]+[acd][0-9,]+", line): # new chunk
				diff_ranges = re.split("[acd]", line)
				patch_range = diff_ranges[1].split(",")
				if len(patch_range) == 1:
					begin = int(patch_range[0])
					end = begin
				else:
					begin, end = int(patch_range[0]), int(patch_range[1])

				if 'd' in line:
					end += 1

				for i in range(begin, end+1):
					chunk_map[current_class][i] = chunk_num

				chunk_num += 1
			else:
				continue
		return chunk_map, chunk_num
	return {}, 0

# This method is necessary since the file path I get from the diff is different than the classpath
def get_chunk_num(class_name, line_no, chunkmap):
	for k in chunkmap.keys():
		if k.endswith(class_name+".java"):
			try:
				return chunkmap[k][line_no]
			except:
				pass
	return -1

# with open("data/patch_locations.txt") as f:
# 	patches = parse_patches(f)

with open("data/multitest_multiedit.txt") as f:
	multitest_multichunk = [x.strip() for x in f]

with open("data/bug_id_and_branch.txt") as f:
	bears_branches = {int(num) : branch.strip() for num, branch in map(lambda x:x.split(","), f) }

classify = {}

for bug in multitest_multichunk:
	if bug == 'BEARS:192' or bug == 'BEARS:244':
		continue # engineering challenges
	name, num = bug.split(":")
	if name in d4jnames:
		chunkmap, num_chunks = get_chunk_ranges(f'data/diffs/{d4jnames[name]}{int(num)}')
	elif name == "BEARS":
		chunkmap, num_chunks = get_chunk_ranges(f'data/diffs/{bears_branches[int(num)]}')
	else:
		print("MISSING: " + name)
		continue
	# chunkmap = patches[bug]
	# print(bug)
	# print(chunkmap)
	intersect_cov = all_cov[bug]["INTERSECT"]
	union_cov = all_cov[bug]["UNION"]

	if intersect_cov == union_cov:
		classify[bug] = "same"
	elif len(intersect_cov) == 0:
		classify[bug] = "disjoint"
	else:
		# get chunks for intersect
		# get chunks for union
		intersect_chunks = set()
		union_chunks = set()

		for c, arr in intersect_cov.items():
			for l in arr:
				cn = get_chunk_num(c, l, chunkmap)
				if cn >= 0:
					intersect_chunks.add(cn)
				else:
					pass
					# print(f"diff weirdness: {c} {l}")

		for c, arr in union_cov.items():
			for l in arr:
				cn = get_chunk_num(c, l, chunkmap)
				if cn >= 0:
					union_chunks.add(cn)
				else:
					pass
					# print(f"diff weirdness: {c} {l}")

		# are they the same chunks?
		if intersect_chunks == union_chunks:
			classify[bug] = "same" #, len(union_chunks), num_chunks
		# do they overlap chunks?
		elif len(intersect_chunks.intersection(union_chunks)) > 0:
			classify[bug] = "overlap" #, len(union_chunks), num_chunks
		else:
			classify[bug] = "disjoint" #, len(union_chunks), num_chunks

print(json.dumps(classify, indent=3))

