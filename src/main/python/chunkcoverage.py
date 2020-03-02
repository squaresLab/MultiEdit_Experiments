import json
import re

with open("data/patch_locs.json") as f:
	patch_json = json.load(f)


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



with open("data/coverage-experiments/all_bears/rawCoverage.data") as f:
	parse_cov(f)

with open("data/coverage-experiments/mar2-d4j/rawCoverage.data") as f:
	parse_cov(f)

with open("data/coverage-experiments/mockito/rawCoverage.data") as f:
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

multitest_multichunk = ["traccar-traccar-207561899-207563891:103", "2018swecapstone-h2ms-356638992-356666847:140", "FasterXML-jackson-databind-195752461-195777970:007", "FasterXML-jackson-databind-215111320-215799347:012", "INRIA-spoon-191511078-191595944:031", "INRIA-spoon-270437105-270439051:079", "DmitriiSerikov-money-transfer-service-446104441-446106577:209", "vkostyukov-la4j-414793864-436911083:250", "INRIA-spoon-201940544-203101555:040", "INRIA-spoon-204567691-207361743:041", "INRIA-spoon-239871875-239928671:062", "INRIA-spoon-270439051-271649592:080", "traccar-traccar-265439859-265542197:123", "2018swecapstone-h2ms-363210218-363627522:141", "INRIA-spoon-431601111-431664501:216", "CHART:002", "CHART:014", "CHART:019", "CHART:022", "CHART:025", "LANG:020", "LANG:030", "LANG:041", "LANG:047", "LANG:050", "MATH:001", "MATH:004", "MATH:029", "MATH:035", "MATH:036", "MATH:037", "MATH:098", "MATH:099", "TIME:012", "LANG:022", "LANG:034", "MATH:021", "MATH:043", "MATH:046", "MATH:047", "MATH:068", "MATH:076", "MATH:086", "MATH:102", "TIME:005", "TIME:021", "TIME:022", "CHART:016", "CHART:018", "LANG:012", "LANG:015", "LANG:019", "LANG:036", "MATH:016", "TIME:006", "MOCKITO:004", "MOCKITO:035", "MOCKITO:003", "MOCKITO:011", "MOCKITO:020"]

print(len(multitest_multichunk))

d4jnames = {"CHART": "Chart ", "CLOSURE": "Closure ", "LANG": "Lang ", "MATH": "Math ", "MOCKITO": "Mockito ", "TIME": "Time "}

classify = {}

for bug in multitest_multichunk:
	name, num = bug.split(":")
	if name in d4jnames:
		chunkmap, num_chunks = get_chunk_ranges(f'{d4jnames[name]}{int(num)}')
	else:
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
			classify[bug] = "same"
		# do they overlap chunks?
		elif len(intersect_chunks.intersection(union_chunks)) > 0:
			classify[bug] = "overlap"
		else:
			classify[bug] = "disjoint"

print(json.dumps(classify, indent=3))

