import json
import matplotlib.pyplot as plt
from matplotlib.patches import Patch
from collections import defaultdict

with open("data/uncovered_chunk_classification.json") as f:
    uncovered_chunks = json.load(f)

all_classifications = defaultdict(int)
num_chunks_with_all_non_semantic_lines = 0
num_chunks_with_only_separators = 0
num_chunks_only_imports = 0
num_other_not_covered = 0
num_other_covered = 0

non_semantic_classifications = set(["comment", "empty"])
separator_classification = "separators"
import_package_classification = "import_package"


covered_by_jacoco = set(["catch",
"throwstatement",
"loop",
"assignment",
"methodinvocation",
"if",
"returnstatement",
"classcreator",
"memberreference" # sometimes, in the patched version, it can be a field declaration which is covered
])

no_covered_by_jacoco = set(["else",
"comment",
"empty",
"separators",
"continuestatement",
"method_signature",
"import_package",
"static_field",
"mid_expression",
"breakstatement",
"field_declaration",
"case",
"annotation"]) # plus try

for chunk_list in uncovered_chunks.values():
	for chunk in chunk_list:
		for k in chunk.keys():
			all_classifications[k] += chunk[k]

		if all([x in non_semantic_classifications \
				for x in chunk.keys()]):
			num_chunks_with_all_non_semantic_lines += 1
		elif all(x in non_semantic_classifications or x == separator_classification \
				for x in chunk.keys()):
			num_chunks_with_only_separators += 1

		elif all(x in non_semantic_classifications or x == import_package_classification \
				for x in chunk.keys()):
			num_chunks_only_imports += 1
		elif any(x in covered_by_jacoco or "return" in x \
				for x in chunk.keys()):
			print(chunk)
			num_other_covered += 1
		else:
			num_other_not_covered += 1

print(json.dumps(all_classifications, indent=3))

ax = plt.gca()
labels = ["Only comments/whitespace", "Only brackets and punctuation", 
	"Only imports and package declarations", "Other un-coverable code constructs", 
	"Contains potentially covered line"]
data = [num_chunks_with_all_non_semantic_lines, num_chunks_with_only_separators, num_chunks_only_imports, num_other_not_covered, num_other_covered]
bar = plt.bar(labels, data,
	color=["#4285f4", "#4285f4", "#4285f4", "#4285f4", "#a0c1f8"])
plt.xlabel("Composition of deleted or modified code at a location")
plt.ylabel("Number of deleted or\nmodified buggy locations")
plt.ylim([0, 60])
plt.title("How many un-covered buggy locations\nwere due to Jacoco's limitations?", fontsize="x-large")
ax.set_xticklabels(labels = labels, rotation=30, ha="right", fontsize="small")
# legend_elements = [Patch(facecolor="#980000", edgecolor="#980000",
#                          label='Unable to be covered by Jacoco'),
# 					Patch(facecolor="#e6b8af", edgecolor="#e6b8af",
#                          label='Contains potentially coverable line')]

legend_elements = [Patch(facecolor="#4285f4", edgecolor="#4285f4",
                         label='Unable to be covered by Jacoco'),
					Patch(facecolor="#a0c1f8", edgecolor="#a0c1f8",
                         label='Contains potentially coverable line')]
ax.legend(handles=legend_elements, loc='upper left')

for p, val in zip(bar.patches, data):
    width, height = p.get_width(), p.get_height()
    x, y = p.get_xy() 
    plt.annotate(f'{val}', (x + (width/2) - 0.05, y + height + 0.5))

plt.show()

