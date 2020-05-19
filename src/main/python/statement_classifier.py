import re
import javalang

basic_method_signature_pattern = re.compile("M*[IB]I\(.*\)\{?")
def is_basic_method_signature(tokens):
	new_str = ""
	for t in tokens:
		if isinstance(t, javalang.tokenizer.Separator):
			new_str += t.value
		else:
			new_str += str(t)[0]
	return basic_method_signature_pattern.match(new_str)



def classify_line(line):
	if not line:
		return "empty"

	if line.startswith("//") or line.startswith("/*") or line.startswith("*/"):
		return "comment"

	try:
		tokens = list(javalang.tokenizer.tokenize(line))
	except (javalang.tokenizer.LexerError, TypeError)  as e:
		if line.startswith("*"): # in comment block; there's something weird in the comment
			return "comment"
		print(repr(e))
		print(line)
		return line

	# if the whole line is just separators, like a closing bracket, for instance
	if all([isinstance(t, javalang.tokenizer.Separator) for t in tokens]):
		return "separators"

	# remove trailing open parens
	if tokens[-1].value in set(['(', '[', '{']):
		tokens = tokens[:-1]

	# remove beginning close parens
	if tokens[0].value in set([')', ']', '}']):
		tokens = tokens[1:]

	# bug(?) for one case where this.field.methodcall() returns a "This" type
	if tokens[0].value == "this" and tokens[1].value == ".":
		tokens = tokens[2:]


	try:
		parser = javalang.parser.Parser(tokens)
		expr = parser.parse_expression()
		# if the tokens parse correctly, but there is a comma at the end, 
		# we will assume that's a parameter in a larger method call
		if tokens[-1].value == ",":
			return "mid_expression"
		classification = type(expr).__name__.split(".")[-1].lower()
		if classification in ["literal", "binaryoperation"]:
			# if the classification is a literal, it's likely the parser is misinterpreting a parameter
			return "mid_expression"
		return classification
	except (javalang.parser.JavaSyntaxError, TypeError, StopIteration):
		try:
			parser = javalang.parser.Parser(tokens)
			expr = parser.parse_statement()
			return type(expr).__name__.split(".")[-1].lower()
		except (javalang.parser.JavaSyntaxError, TypeError, StopIteration) as e:
			token_set = set([t.value for t in tokens])
			if "else" in token_set:
				return "else"
			if "if" in token_set:
				return "if"
			if tokens[0].value == "*": # in comment block
				return "comment"
			if "static" in token_set and tokens[-1].value == ";":
				return "static_field"
			if "import" in token_set or "package" in token_set:
				return "import_package"
			if "@" in token_set:
				return "annotation"
			if "for" in token_set or "while" in token_set:
				return "loop"
			if "case" in token_set:
				return "case"
			if "catch" in token_set:
				return "catch"
			if is_basic_method_signature(tokens):
				return "method_signature"

			print(repr(e))
			print(line)
			return line

