package multiedit.coverage;

import java.util.Collection;

public class CoverageUtils {

    public static CoverageSubset aggregate(String desc, Collection<CoverageSubset> coll) {
        CoverageSubset aggr = new CoverageSubset(desc);
        for (CoverageSubset cov : coll) {
            aggr.addAllClasses(cov.getClassCoverageMap());
        }
        return aggr;
    }

    public static CoverageSubset intersect(String desc, Collection<CoverageSubset> coll) {
        CoverageSubset intersection = null;

        for (CoverageSubset cov : coll) {
            if (intersection == null) {
                intersection = cov;
            } else {
                intersection = intersection.intersection(cov, desc);
            }
        }

        return intersection;
    }

}
