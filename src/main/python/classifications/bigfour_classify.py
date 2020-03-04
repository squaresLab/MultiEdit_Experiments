import re
import dateutil.parser as dateparser
from classifications.exception_sets import *
from collections import defaultdict

def classify(err, msg, error_dict=None, row_dict=None):
	if not error_dict:
		error_dict = defaultdict(int)
	if not row_dict:
		row_dict = defaultdict(bool)

	if err == "junit.framework.AssertionFailedError":
		error_dict["assertionfailederror"] += 1
		row_dict["assertionfailederror"] = True
		return "assertionfailederror"
	elif err == "java.lang.AssertionError":
		error_dict["assertionerror"] += 1
		row_dict["assertionerror"] = True
		return "assertionfailederror"
	elif err == "java.lang.NullPointerException":
		error_dict["nullpointer"] += 1
		row_dict["nullpointer"] = True
		return "nullpointer"
	elif err == "junit.framework.ComparisonFailure":
		error_dict["comparisonfailure"] += 1
		row_dict["comparisonfailure"] = True
		return "comparisonfailure"
	else:
		error_dict["other"] += 1
		row_dict["other"] = True
		return "other"