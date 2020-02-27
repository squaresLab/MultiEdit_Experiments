import re
import dateutil.parser as dateparser
from classifications.exception_sets import *
from collections import defaultdict

assertions = defaultdict(list)


def is_int(s):
	try:
		x = int(s)
		return True
	except ValueError:
		return False

def is_float(s):
	try:
		x = float(s)
		return True
	except ValueError:
		return False

def is_date(s):
	try:
		x = dateparser.parse(s)
		return True
	except ValueError:
		return False


"""precondition: err + msg does actually reflect some kind of assertion"""
def classify_assertion(err, msg_lower, error_dict=None, row_dict=None):
	if not error_dict:
		error_dict = defaultdict(int)
	if not row_dict:
		row_dict = defaultdict(bool)

	if msg_lower == "":
		row_dict["other_assert"] = True
		error_dict["other_assert"] += 1
		return "other_assert" 
	else:
		if "null" in msg_lower:
			row_dict["assert_null"] = True
			error_dict["assert_null"] += 1
			return "assert_null"
		else:
			common_pattern = re.compile(".*expected: ?[\[<]?(?P<e>.+?)[]>]? but was: ?[\[<]?(?P<a>.+?)[]>]?")
			match = common_pattern.fullmatch(msg_lower)
			if match:
				expected = match.group('e')
				actual = match.group('a')
				if is_int(expected) or is_float(expected) or expected == "true" or expected == "false":
						row_dict["assert_prim"] = True
						error_dict["assert_prim"] += 1
						return "assert_prim" 

				else:
					row_dict["other_assert"] = True
					return "other_assert" 
					error_dict["other_assert"] += 1
					assertions[err].append(msg_lower)
			else:
				assertions[err].append(msg_lower)
				row_dict["other_assert"] = True
				error_dict["other_assert"] += 1
				return "other_assert" 

def classify(err, msg, error_dict=None, row_dict=None):
	if not error_dict:
		error_dict = defaultdict(int)
	if not row_dict:
		row_dict = defaultdict(bool)

	msg_lower = msg.lower()
	if err in timeout or "timeout" in msg_lower or "timed out" in msg_lower or "time out" in msg_lower:
		error_dict["timeout"] += 1
		row_dict["timeout"] = True
		return "timeout"
	elif err in test_assertions or "expected" in msg_lower:
		# assertions[err].append(msg_lower)
		return classify_assertion(err, msg_lower, error_dict, row_dict)
	elif err in null_pointer:
		error_dict["null_pointer"] += 1
		row_dict["null_pointer"] = True
		return "null_pointer"
	elif err in parsing_conversion or err in serialization:
		error_dict["parsing"] += 1
		row_dict["parsing"] = True
		return "parsing"
	elif err in fields_arguments or err in instantiation_invocation_typing or err in index_out_of_bounds or err in not_found:
		error_dict["access"] += 1
		row_dict["access"] = True
		return "access"
	else:
		error_dict["other"] += 1
		row_dict["other"] = True
		return "other"