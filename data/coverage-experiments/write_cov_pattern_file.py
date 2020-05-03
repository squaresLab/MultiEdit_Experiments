
with open("data/coverage-experiments/coverage-pattern.txt", "w") as outputfile:

	with open("data/coverage-experiments/coverage-data-final/disjoint.data") as f:
		for line in f:
			outputfile.write(f'disjoint {line}')

	with open("data/coverage-experiments/coverage-data-final/inBetween.data") as f:
		for line in f:
			outputfile.write(f'overlap {line}')

	with open("data/coverage-experiments/coverage-data-final/same.data") as f:
		for line in f:
			outputfile.write(f'same {line}')

