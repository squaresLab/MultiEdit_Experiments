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
		row_dict["test_assertions"] = True 
		error_dict["test_assertions"] += 1
	else:
		common_pattern = re.compile(".*expected: ?[\[<]?(?P<e>.+?)[]>]? but was: ?[\[<]?(?P<a>.+?)[]>]?")
		match = common_pattern.fullmatch(msg_lower)
		if match:
			expected = match.group('e')
			actual = match.group('a')
			if is_int(expected):
				if int(expected) == 0:
					row_dict["expect_0"] = True 
					error_dict["expect_0"] += 1
				elif is_int(actual) and int(actual) == 0:
					row_dict["actual_0"] = True 
					error_dict["actual_0"] += 1
				else:
					row_dict["assert_int"] = True 
					error_dict["assert_int"] += 1
			elif is_float(expected):
				if float(expected) == 0.0:
					row_dict["expect_0"] = True 
					error_dict["expect_0"] += 1
				elif is_float(actual) and float(actual) == 0.0:
					row_dict["actual_0"] = True 
					error_dict["actual_0"] += 1
				else:
					row_dict["assert_float"] = True 
					error_dict["assert_float"] += 1
			elif expected == "null":
				row_dict["assert_null"] = True
				error_dict["assert_null"] += 1
			elif actual == "null":
				row_dict["actual_null"] = True 
				error_dict["actual_null"] += 1
			elif expected == "true" or expected == "false":
				row_dict["assert_bool"] = True 
				error_dict["assert_bool"] += 1
			elif is_date(expected):
				row_dict["assert_date"] = True
				error_dict["assert_date"] += 1
			elif expected[0] == "[" and expected[-1] == "]":
				row_dict["assert_arr"] = True
				error_dict["assert_arr"] += 1
			else:
				addr_pattern = re.compile("[A-Za-z0-9$_]+@[A-Za-z0-9]+")
				if (common_pattern.fullmatch(expected)):
					row_dict["assert_obj"] = True 
					error_dict["assert_obj"] += 1
				else:
					row_dict["test_assertions"] = True 
					error_dict["test_assertions"] += 1
					assertions[err].append(msg_lower)
		else:
			if "exception" in msg_lower or "warning" in msg_lower or "error" in msg_lower:
				row_dict["error_expected"] = True 
				error_dict["error_expected"] += 1

			else:
				assertions[err].append(msg_lower)
				row_dict["test_assertions"] = True 
				error_dict["test_assertions"] += 1

def classify(err, msg, error_dict, row_dict):
	msg_lower = msg.lower()
	if err in timeout or "timeout" in msg_lower or "timed out" in msg_lower or "time out" in msg_lower:
		error_dict["timeout"] += 1
		row_dict["timeout"] = True
	elif err in test_assertions or "expected" in msg_lower:
		# assertions[err].append(msg_lower)
		classify_assertion(err, msg_lower, error_dict, row_dict)
	elif err in timeout:
		error_dict["timeout"] += 1
		row_dict["timeout"] = True
	elif err in null_pointer:
		error_dict["null_pointer"] += 1
		row_dict["null_pointer"] = True
	elif err in index_out_of_bounds:
		error_dict["index_out_of_bounds"] += 1
		row_dict["index_out_of_bounds"] = True
	elif err in math:
		error_dict["math"] += 1
		row_dict["math"] = True
	elif err in runtime_exception:
		error_dict["runtime_exception"] += 1
		row_dict["runtime_exception"] = True
	elif err in parsing_conversion:
		error_dict["parsing_conversion"] += 1
		row_dict["parsing_conversion"] = True
	elif err in serialization:
		error_dict["serialization"] += 1
		row_dict["serialization"] = True
	elif err in fields_arguments:
		error_dict["fields_arguments"] += 1
		row_dict["fields_arguments"] = True
	elif err in instantiation_invocation_typing:
		error_dict["instantiation_invocation_typing"] += 1
		row_dict["instantiation_invocation_typing"] = True
	elif err in not_found:
		error_dict["not_found"] += 1
		row_dict["not_found"] = True
	else:
		error_dict["other"] += 1
		row_dict["other"] = True