
def get_line_numbers(f):
	text = []
	curr_input = f.readline().strip()
	while curr_input:
		text.append(curr_input)
		curr_input = f.readline().strip()
	return text


def read_raw_coverage(fname):
	bug_coverage = []
	with open(fname) as f:
		bugname = f.readline().strip()
		while bugname:
			bugname = bugname.split(" : ")[1]
			f.readline() # INTERSECT
			f.readline() # intersect all failing tests

			intersect = get_line_numbers(f)

			f.readline() # UNION
			f.readline() # aggregate all failing tests

			aggregate = get_line_numbers(f)

			bug_coverage.append((bugname, intersect, aggregate))

			bugname = f.readline().strip()

	return bug_coverage