package projects;

import multiedit.coverage.CoverageSubset;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class BearsPatchTest {

    @Test
    public void testBears1() {
        BearsPatch bp = new BearsPatch(1);
        CoverageSubset patchLocations = bp.getPatchLocations();
        CoverageSubset actual = new CoverageSubset("actual");
        actual.addClass("com/fasterxml/jackson/databind/deser/impl/CreatorCollector", new HashSet<>(Arrays.asList(165, 166, 167, 169, 170, 171, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 299, 300, 301, 302, 309, 310, 313, 335, 344)));
        assertEquals(actual, patchLocations);
    }

    @Test
    public void testBears105() {
        BearsPatch bp = new BearsPatch(105);
        CoverageSubset patchLocations = bp.getPatchLocations();
        CoverageSubset actual = new CoverageSubset("actual");
        actual.addClass("org/traccar/protocol/TelicProtocolDecoder", new HashSet<>(Arrays.asList(57, 86, 87, 135, 136, 137, 138)));
        assertEquals(actual, patchLocations);
    }

    @Test
    public void testBears189() {
        BearsPatch bp = new BearsPatch(189);
        CoverageSubset patchLocations = bp.getPatchLocations();
        CoverageSubset actual = new CoverageSubset("actual");
        actual.addClass("hu/oe/nik/szfmv/Main", new HashSet<>(Arrays.asList(34, 35)));
        actual.addClass("hu/oe/nik/szfmv/automatedcar/AutomatedCar", new HashSet<>(Arrays.asList(10, 65, 66)));
        actual.addClass("hu/oe/nik/szfmv/environment/World", new HashSet<>(Arrays.asList(17, 30, 31, 32, 33)));
        assertEquals(actual, patchLocations);
    }

    @Test
    public void testBears39() {
        BearsPatch bp = new BearsPatch(39);
        CoverageSubset patchLocations = bp.getPatchLocations();
        CoverageSubset actual = new CoverageSubset("actual");

    }

}