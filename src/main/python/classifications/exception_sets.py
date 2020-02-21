

test_assertions = set(['org.spockframework.runtime.WrongExceptionThrownError',
'org.testng.TestException', 
'dk.alexandra.fresco.framework.TestFrameworkException',
'org.junit.ComparisonFailure',
'junit.framework.ComparisonFailure',
'junit.framework.AssertionFailedError',
'java.lang.AssertionError',
'org.opentest4j.AssertionFailedError',
'org.mockito.exceptions.verification.junit.ArgumentsAreDifferent'])

timeout = set(['org.junit.runners.model.TestTimedOutException',
'java.lang.OutOfMemoryError',
'java.lang.StackOverflowError',
'org.apache.commons.math.MaxIterationsExceededException',
'org.apache.commons.math.exception.TooManyEvaluationsException',
'org.apache.commons.math3.exception.MaxCountExceededException'])

not_found = set(['org.springframework.security.core.userdetails.UsernameNotFoundException',
'java.util.NoSuchElementException'])

instantiation_invocation_typing = set(['spoon.reflect.declaration.ParentNotInitializedException',
'spoon.support.SpoonClassNotFoundException',
'Wanted but not invoked',
'java.lang.NoSuchMethodError', 
'org.mockito.exceptions.misusing.MissingMethodInvocationException',
'org.mockito.internal.creation.instance.InstantationException',
'spoon.compiler.InvalidClassPathException',
'java.lang.AbstractMethodError', 
'java.lang.UnsupportedOperationException'])

fields_arguments = set(['com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException',
'org.joda.time.IllegalFieldValueException',
'java.lang.IllegalArgumentException',
'org.jfree.data.UnknownKeyException',
'spoon.template.TemplateException'])

index_out_of_bounds = set(['java.lang.StringIndexOutOfBoundsException',
'java.lang.ArrayIndexOutOfBoundsException',
'java.lang.IndexOutOfBoundsException'])

null_pointer = set(['java.lang.NullPointerException'])

math = set(['org.apache.commons.math.ConvergenceException',
'org.apache.commons.math.MathException',
'org.apache.commons.math.exception.NotStrictlyPositiveException',
'org.apache.commons.math.optimization.OptimizationException',
'org.apache.commons.math.optimization.direct.BOBYQAOptimizer$PathIsExploredException',
'org.apache.commons.math3.exception.ConvergenceException',
'java.lang.ArithmeticException'])


runtime_exception = set(['org.mockito.exceptions.base.MockitoException',
'ro.pippo.core.PippoRuntimeException',
'java.lang.RuntimeException',
'org.apache.commons.math.MathRuntimeException$4',
'org.apache.commons.math.MathRuntimeException$6',
'java.lang.Exception',
'java.lang.IllegalStateException',
'spoon.SpoonException'])

parsing_conversion = set(['java.text.ParseException',
'java.time.format.DateTimeParseException',
'com.fasterxml.jackson.databind.exc.InvalidFormatException',
'java.lang.NumberFormatException', 
'io.debezium.text.ParsingException',
'org.springframework.core.convert.ConversionFailedException',
'org.openmrs.module.webservices.rest.web.response.ConversionException',
'org.apache.commons.math3.fraction.FractionConversionException',
'com.shapesecurity.shift.es2017.parser.JsError',
'java.lang.ClassCastException'])

serialization = set(['org.apache.commons.lang.SerializationException',
'org.apache.commons.lang3.SerializationException',
'java.io.NotSerializableException'])