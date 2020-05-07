import re
import javalang

def classify_line(line):
	if not line:
		return "empty"

	if line.startswith("//") or line.startswith("/*") or line.startswith("*/"):
		return "comment"

	try:
		tokens = list(javalang.tokenizer.tokenize(line))
	except (javalang.tokenizer.LexerError, TypeError)  as e:
		print(repr(e))
		return line

	if all([isinstance(t, javalang.tokenizer.Separator) for t in tokens]):
		return "separators"

	# remove trailing open parens
	if tokens[-1].value in set(['(', '[', '{']):
		tokens = tokens[:-1]

	# remove beginning close parens
	if tokens[0].value in set([')', ']', '}']):
		tokens = tokens[1:]

	parser = javalang.parser.Parser(tokens)

	try:
		expr = parser.parse_expression()
		return type(expr)
	except (javalang.parser.JavaSyntaxError, TypeError) as e:
		token_set = set([t.value for t in tokens])
		if "else" in token_set:
			return "else"
		if "return" in token_set:
			return ""
		if tokens[0].value == "*": # in comment block
			return "comment"
		

		print(repr(e))
		return line

