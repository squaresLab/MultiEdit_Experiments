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
	elif "null" in msg_lower:
		row_dict["assert_null"] = True
		error_dict["assert_null"] += 1
		return "assert_null"
	else:
		common_pattern = re.compile(".*expected: ?[\[<]?(?P<e>.+?)[]>]? but was: ?[\[<]?(?P<a>.+?)[]>]?")
		match = common_pattern.fullmatch(msg_lower)
		if match:
			expected = match.group('e')
			actual = match.group('a')
			if is_int(expected):
				row_dict["assert_int"] = True 
				error_dict["assert_int"] += 1
				return "assert_int"
			elif is_float(expected):
				row_dict["assert_float"] = True 
				error_dict["assert_float"] += 1
				return "assert_float"
			else:
				addr_pattern = re.compile("[A-Za-z0-9$._]+@[A-Za-z0-9]+")
				if addr_pattern.fullmatch(expected) or is_date(expected) or (expected[0] == "[" and expected[-1] == "]"):
					row_dict["assert_obj_arr_date"] = True 
					error_dict["assert_obj_arr_date"] += 1
					return "assert_obj_arr_date"
				else:
					row_dict["other_assert"] = True 
					error_dict["other_assert"] += 1
					assertions[err].append(msg_lower)
					return "other_assert"
		else:
			if "exception" in msg_lower or "warning" in msg_lower or "error" in msg_lower:
				row_dict["error_expected"] = True 
				error_dict["error_expected"] += 1
				return "error_expected"

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
	else:
		error_dict["other"] += 1
		row_dict["other"] = True
		return "other"