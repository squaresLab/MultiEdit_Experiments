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
def classify_assertion(err, msg_lower, error_dict, row_dict):
	if msg_lower == "":
		row_dict["other_assert"] = True 
		error_dict["other_assert"] += 1
	elif "null" in msg_lower:
		row_dict["assert_null"] = True
		error_dict["assert_null"] += 1
	else:
		common_pattern = re.compile(".*expected: ?[\[<]?(?P<e>.+?)[]>]? but was: ?[\[<]?(?P<a>.+?)[]>]?")
		match = common_pattern.fullmatch(msg_lower)
		if match:
			expected = match.group('e')
			actual = match.group('a')
			if is_int(expected):
				row_dict["assert_int"] = True 
				error_dict["assert_int"] += 1
			elif is_float(expected):
				row_dict["assert_float"] = True 
				error_dict["assert_float"] += 1
			else:
				addr_pattern = re.compile("[A-Za-z0-9$._]+@[A-Za-z0-9]+")
				if addr_pattern.fullmatch(expected) or is_date(expected) or (expected[0] == "[" and expected[-1] == "]"):
					row_dict["assert_obj_arr_date"] = True 
					error_dict["assert_obj_arr_date"] += 1
				else:
					row_dict["other_assert"] = True 
					error_dict["other_assert"] += 1
					assertions[err].append(msg_lower)
		else:
			if "exception" in msg_lower or "warning" in msg_lower or "error" in msg_lower:
				row_dict["error_expected"] = True 
				error_dict["error_expected"] += 1

			else:
				assertions[err].append(msg_lower)
				row_dict["other_assert"] = True 
				error_dict["other_assert"] += 1

def classify(err, msg, error_dict, row_dict):
	msg_lower = msg.lower()
	if err in timeout or "timeout" in msg_lower or "timed out" in msg_lower or "time out" in msg_lower:
		error_dict["timeout"] += 1
		row_dict["timeout"] = True
	elif err in test_assertions or "expected" in msg_lower:
		# assertions[err].append(msg_lower)
		classify_assertion(err, msg_lower, error_dict, row_dict)
	else:
		error_dict["other"] += 1
		row_dict["other"] = True