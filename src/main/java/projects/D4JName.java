package projects;

public enum D4JName {
    CHART(26),
    CLOSURE(133),
    LANG(65),
    MATH(106),
    MOCKITO(38),
    TIME(27);


    public final int numBugs;
    private D4JName(int numBugs) {
        this.numBugs = numBugs;
    }

    public String getID() {
        String name = this.toString();
        return name.substring(0, 1) + name.substring(1).toLowerCase();
    }
}
