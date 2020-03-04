library(readr)
library(car)
library(pscl)

# Assertions

symptoms = read_csv("~/MultiEdit_Experiments/data/symptoms/assertion-symptoms.csv")

# View(symptoms)
names(symptoms)
str(symptoms)

m = glm(multi ~ assert_obj_arr_date
                + assert_int
                + assert_float
                + error_expected
                + timeout
                + assert_null
                + other_assert
                + other
        , data = symptoms
        , family = "binomial")

vif(m)
summary(m)
pR2(m)
anova(m)


# Grouping 1

symptoms = read_csv("~/MultiEdit_Experiments/data/symptoms/grouping1-symptoms.csv")

# View(symptoms)
names(symptoms)
str(symptoms)

m = glm(multi ~ access
        + assert_prim
        + null_pointer
        + timeout
        + assert_null
        + parsing
        + other_assert
        + other
        , data = symptoms
        , family = "binomial")

vif(m)
summary(m)
pR2(m)
anova(m)



# Grouping 2

symptoms = read_csv("~/MultiEdit_Experiments/data/symptoms/grouping2-symptoms.csv")

# View(symptoms)
names(symptoms)
str(symptoms)

m = glm(multi ~ assert_equal
        + access
        + null_pointer
        + parsing
        + other_assert
        + other
        , data = symptoms
        , family = "binomial")

vif(m)
summary(m)
pR2(m)
anova(m)

# Big Four

symptoms = read_csv("~/MultiEdit_Experiments/data/symptoms/bigfour-symptoms.csv")

# View(symptoms)
names(symptoms)
str(symptoms)

m = glm(multi ~ assertionfailederror
        + comparisonfailure
        + nullpointer
        + assertionerror
        + other
        , data = symptoms
        , family = "binomial")

vif(m)
summary(m)
pR2(m)
anova(m)

# assert + parsing

symptoms = read_csv("~/MultiEdit_Experiments/data/symptoms/assertparsing-symptoms.csv")

# View(symptoms)
names(symptoms)
str(symptoms)

m = glm(multi ~ assert_obj_arr_date
        + assert_int
        + assert_float
        + error_expected
        + timeout
        + assert_null
        + parsing
        + other_assert
        + other
        , data = symptoms
        , family = "binomial")

vif(m)
summary(m)
pR2(m)
anova(m)

### OLD BELOW


symptoms_exceptions[symptoms_exceptions$test_assertions == "1.0",]$test_assertions = TRUE
symptoms_exceptions[symptoms_exceptions$test_assertions == "False",]$test_assertions = FALSE

symptoms_exceptions[symptoms_exceptions$fields_arguments == "1.0",]$fields_arguments = TRUE
symptoms_exceptions[symptoms_exceptions$fields_arguments == "False",]$fields_arguments = FALSE

symptoms_exceptions[symptoms_exceptions$index_out_of_bounds == "1.0",]$index_out_of_bounds = TRUE
symptoms_exceptions[symptoms_exceptions$index_out_of_bounds == "False",]$index_out_of_bounds = FALSE

symptoms_exceptions[symptoms_exceptions$null_pointer == "1.0",]$null_pointer = TRUE
symptoms_exceptions[symptoms_exceptions$null_pointer == "False",]$null_pointer = FALSE

symptoms_exceptions[symptoms_exceptions$timeout == "1.0",]$timeout = TRUE
symptoms_exceptions[symptoms_exceptions$timeout == "False",]$timeout = FALSE

symptoms_exceptions[symptoms_exceptions$runtime_exception == "1.0",]$runtime_exception = TRUE
symptoms_exceptions[symptoms_exceptions$runtime_exception == "False",]$runtime_exception = FALSE

symptoms_exceptions[symptoms_exceptions$parsing_conversion == "1.0",]$parsing_conversion = TRUE
symptoms_exceptions[symptoms_exceptions$parsing_conversion == "False",]$parsing_conversion = FALSE

symptoms_exceptions[symptoms_exceptions$serialization == "1.0",]$serialization = TRUE
symptoms_exceptions[symptoms_exceptions$serialization == "False",]$serialization = FALSE

symptoms_exceptions[symptoms_exceptions$other == "1.0",]$other = TRUE
symptoms_exceptions[symptoms_exceptions$other == "False",]$other = FALSE

symptoms_exceptions[symptoms_exceptions$math == "1.0",]$math = TRUE
symptoms_exceptions[symptoms_exceptions$math == "False",]$math = FALSE

symptoms_exceptions[symptoms_exceptions$instantiation_invocation_typing == "1.0",]$instantiation_invocation_typing = TRUE
symptoms_exceptions[symptoms_exceptions$instantiation_invocation_typing == "False",]$instantiation_invocation_typing = FALSE

symptoms_exceptions[symptoms_exceptions$not_found == "1.0",]$not_found = TRUE
symptoms_exceptions[symptoms_exceptions$not_found == "False",]$not_found = FALSE


bugs = read_csv("~/Downloads/serena/serena.csv")
View(bugs)

# summary(bugs)

summary(bugs$single)
summary(bugs$multi)
summary(bugs$multiExpected)

boxplot(list(single = bugs$single+1,
             multi = bugs$multi+1), 
        log="y")

wilcox.test(bugs$single, bugs$multi, paired=TRUE, conf.int = TRUE, alternative = "g")




table(symptoms$org.junit.ComparisonFailure)
table(symptoms$junit.framework.AssertionFailedError)
table(symptoms$java.lang.ArrayIndexOutOfBoundsException)

m = glm(multi ~ 
                junit.framework.AssertionFailedError
        + junit.framework.ComparisonFailure
        + java.lang.NullPointerException
        + java.lang.ArrayIndexOutOfBoundsException
        + java.lang.Exception
        + java.lang.UnsupportedOperationException
        + java.lang.ClassCastException
        + java.lang.IllegalArgumentException
        + java.lang.StringIndexOutOfBoundsException
        + java.lang.IndexOutOfBoundsException
        + java.lang.NumberFormatException
        + java.lang.OutOfMemoryError
        + java.lang.AssertionError
        + java.lang.RuntimeException
        + java.lang.IllegalStateException
        + java.lang.StackOverflowError
        + java.lang.ArrayStoreException
        # + org.junit.ComparisonFailure
        # + org.jfree.data.UnknownKeyException
        # + org.joda.time.IllegalFieldValueException
        # + java.io.NotSerializableException
        # + spoon.compiler.ModelBuildingException
        # + spoon.SpoonException
        # + org.opentest4j.AssertionFailedError
        # + com.fasterxml.jackson.databind.JsonMappingException
        # + io.debezium.text.ParsingException
        # + org.testng.TestException
        # + dk.alexandra.fresco.framework.TestFrameworkException
        # + org.mockito.exceptions.base.MockitoException
        # + org.mockito.exceptions.verification.junit.ArgumentsAreDifferent
        # + com.fasterxml.jackson.databind.exc.InvalidFormatException
        # + com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
        # + java.text.ParseException
        # + java.time.format.DateTimeParseException
        # + org.apache.commons.math.MathRuntimeException
        # + org.apache.commons.math.exception.TooManyEvaluationsException
        # + org.apache.commons.math3.fraction.FractionConversionException
        # + org.spockframework.runtime.WrongExceptionThrownError
        # + org.apache.kafka.connect.errors.ConnectException
        # + other
        ,data = symptoms
        ,family = "binomial")

vif(m)
summary(m)
pR2(m)

