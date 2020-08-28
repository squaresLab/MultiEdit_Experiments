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

with open("data/patch_locs.json") as f:
	patch_json = json.load(f)

d4jnames = {"CHART": "Chart ", 
"CLOSURE": "Closure ", 
"LANG": "Lang ", 
"MATH": "Math ", 
"MOCKITO": "Mockito ", 
"TIME": "Time ", 
"JSOUP":"Jsoup ",
"JACKSONCORE":"JacksonCore ",
"CLI":"Cli ",
"JACKSONDATABIND":"JacksonDatabind ",
"COMPRESS": "Compress ",
"JXPATH":"JxPath ",
"GSON":"Gson ",
"CODEC": "Codec ",
"CSV":"Csv ",
"JACKSONXML":"JacksonXml ",
"COLLECTIONS": "Collections "}

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


for fname in raw_coverage_files:
	with open(fname) as f:
		parse_cov(f)

print(all_cov)
print(len(all_cov))
print(num)

def get_chunk_ranges(bugId):
	for b in patch_json:
		if b["bugId"] == bugId:
			patch = b["patch"]

			chunk_map = {}
			chunk_num = 0
			for c in patch.keys():
				lines = patch[c]
				c = c[6:-5]
				if c.startswith("src/"):
					c = c[4:]
				if c.startswith("source/"):
					c = c[7:]
				if c.startswith("main/"):
					c = c[5:]
				if c.startswith("java/"):
					c= c[5:]
				print("class: " + c)

				chunks = {}
				prev_number = -1
				for l in lines:
					l = int(l)
					if l != prev_number + 1:
						chunk_num += 1
					chunks[l] = chunk_num
					prev_number = l

				chunk_map[c] = chunks
			return chunk_map, chunk_num
	return {}, 0

with open("data/multitest_multiedit.txt") as f:
	multitest_multichunk = [x.strip() for x in f]

print(len(multitest_multichunk))


classify = {}

for bug in multitest_multichunk:
	if not bug.startswith("CLOSURE"):
		continue
		
	if bug == "JSOUP:071":
		continue
	name, num = bug.split(":")
	if name in d4jnames:
		chunkmap, num_chunks = get_chunk_ranges(f'{d4jnames[name]}{int(num)}')
	else:
		if num == "192":
			continue
		chunkmap, num_chunks = get_chunk_ranges(f'Bears-{int(num)}')
	print(bug)
	print(chunkmap)
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
				try:
					intersect_chunks.add(chunkmap[c][l])
				except KeyError:
					print(f"diff weirdness: {c} {l}")

		for c, arr in union_cov.items():
			for l in arr:
				try:
					union_chunks.add(chunkmap[c][l])
				except KeyError:
					print(f"diff weirdness: {c} {l}")

		# are they the same chunks?
		if intersect_chunks == union_chunks:
			classify[bug] = "same" #, len(union_chunks), num_chunks
		# do they overlap chunks?
		elif len(intersect_chunks.intersection(union_chunks)) > 0:
			classify[bug] = "overlap" #, len(union_chunks), num_chunks
		else:
			classify[bug] = "disjoint" #, len(union_chunks), num_chunks

print(json.dumps(classify, indent=3))

