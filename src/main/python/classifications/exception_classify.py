import sys
from classifications.exception_sets import *

def classify(err, msg, error_dict, row_dict):
	msg_lower = msg.lower()
	if err in timeout or "timeout" in msg_lower or "timed out" in msg_lower or "time out" in msg_lower:
		error_dict["timeout"] += 1
		row_dict["timeout"] = True
	elif err in test_assertions or "expected" in msg_lower:
		error_dict["test_assertions"] += 1
		row_dict["test_assertions"] = True
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