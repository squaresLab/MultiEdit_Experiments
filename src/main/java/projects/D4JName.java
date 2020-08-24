package projects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum D4JName {
    CHART("Chart", getBugLists(pair(1, 26))),
    CLOSURE("Closure", getBugLists(pair(1, 62), pair(64, 92), pair(94, 176))),
    LANG("Lang", getBugLists(pair(1, 1), new ImmutablePair<>(3, 65))),
    MATH("Math", getBugLists(pair(1, 106))),
    MOCKITO("Mockito", getBugLists(pair(1, 38))),
    TIME("Time", getBugLists(pair(1, 20), pair(22, 27))),
    JSOUP("Jsoup", getBugLists(pair(1, 93))),
    JACKSONCORE("JacksonCore", getBugLists(pair(1, 26))),
    CLI("Cli", getBugLists(pair(1,5), pair(7,40))),
    JACKSONDATABIND("JacksonDatabind", getBugLists(pair(1, 112))),
    COMPRESS("Compress", getBugLists(pair(1, 47))),
    JXPATH("JxPath", getBugLists(pair(1, 22))),
    GSON("Gson", getBugLists(pair(1, 18))),
    CODEC("Codec", getBugLists(pair(1, 18))),
    CSV("Csv", getBugLists(pair(1, 16))),
    JACKSONXML("JacksonXml", getBugLists(pair(1, 6))),
    COLLECTIONS("Collections", getBugLists(pair(25, 28)));


    public final List<Integer> bugs;
    public final String id;

    private D4JName(String id, List<Integer> bugs) {
        this.bugs = Collections.unmodifiableList(bugs);
        this.id = id;
    }

    private static List<Integer> getBugLists(Pair<Integer, Integer>... rangesInclusive) {
        return Stream
                .of(rangesInclusive)
                .flatMap(x ->
                        IntStream.rangeClosed(x.getLeft(), x.getRight())
                                .boxed())
                .collect(Collectors.toList());
    }

    private static Pair<Integer, Integer> pair(int a, int b) {
        return new ImmutablePair<>(a, b);
    }
}
