package de.rondiplomatico.nds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.rondiplomatico.nds.NDSCoordinate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Tests for the NDSCoordinate class.
 * 
 * Test values are taken from the NDS Format Specification, Version 2.5.4.
 * 
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
@Getter
@ToString
@EqualsAndHashCode
public class NDSCoordinateTest {

    @Test
    public void testConstructor() {
        try {
            NDSCoordinate c = new NDSCoordinate(300D, 40D);
            fail("IllegalArgumentException expected");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            NDSCoordinate c = new NDSCoordinate(-170D, -100D);
            fail("IllegalArgumentException expected");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        NDSCoordinate c = new NDSCoordinate(-170D, -50D);
        System.out.println(c);
        System.out.println(c.toGeoJSON());

        NDSCoordinate c2 = new NDSCoordinate(-170D, -50D);
        assertTrue(c.equals(c2));
    }

    @Test
    public void testNDSCoordinateCorners() {
        assertEquals(new NDSCoordinate(180.0, 90.0), new NDSCoordinate(NDSCoordinate.MAX_LONGITUDE, NDSCoordinate.MAX_LATITUDE));
        assertEquals(new NDSCoordinate(-180.0, -90.0), new NDSCoordinate(NDSCoordinate.MIN_LONGITUDE, NDSCoordinate.MIN_LATITUDE));
        assertEquals(new NDSCoordinate(0.0, 0.0), new NDSCoordinate(0, 0));
    }

    /**
     * 
     * Verifies the values of Table 8-1 in Section "7.2.1 Coding of Coordinates", NDS Spec 2.5.4
     * 
     * Deviations -+1 are to be expected due to different floating point arithmetics:
     * 
     * <pre>
     * Due to the floating point arithmetic, the result values can differ in
       the least significant bits between different implementations. Because
       the data precision in NDS is usually higher than the data precision
       provided by map suppliers, this has no negative impact on the overall
       data quality. Nevertheless it is important to ensure a consistent
       conversion.
     * </pre>
     *
     */
    @Test
    public void testNDSCoordinateSpecCasesWGSToInt() {
        NDSCoordinate eiffel = new NDSCoordinate(2.2945, 48.858222);
        assertEquals(27374451, eiffel.getLongitude());
        assertEquals(582901293, eiffel.getLatitude());

        NDSCoordinate liberty = new NDSCoordinate(-74.044444, 40.689167);
        assertEquals(-883384626, liberty.getLongitude());
        assertEquals(485440670, liberty.getLatitude()); // NDS 2.5.4 Spec: 485440671

        NDSCoordinate Sugarloaf = new NDSCoordinate(-43.157444, -22.948658);
        assertEquals(-514888363, Sugarloaf.getLongitude()); // NDS 2.5.4 Spec: -514888362
        assertEquals(-273788155, Sugarloaf.getLatitude()); // NDS 2.5.4 Spec: -273788154

        NDSCoordinate Sydney = new NDSCoordinate(151.214189, -33.857529);
        assertEquals(1804055545, Sydney.getLongitude());
        assertEquals(-403936055, Sydney.getLatitude()); // NDS 2.5.4 Spec: -403936054

        NDSCoordinate dome = new NDSCoordinate(0.0, 51.503);
        assertEquals(0, dome.getLongitude());
        assertEquals(614454723, dome.getLatitude()); // NDS 2.5.4 Spec: 614454724

        NDSCoordinate quito = new NDSCoordinate(-78.45, 0.0);
        assertEquals(-935944957, quito.getLongitude()); // NDS 2.5.4 Spec: -935944956
        assertEquals(0, quito.getLatitude());
    }

    /**
     * 
     * Verifies the morton code values of Table 8-2, Section "7.2.1 Coding of Coordinates", NDS Spec 2.5.4
     * 
     */
    @Test
    public void testNDSCoordinateMortonCodeComputationSpecCases() {
        // Eiffel
        NDSCoordinate c = new NDSCoordinate(27374451, 582901293);
        assertEquals(579221254078012839L, c.getMortonCode());

        // Liberty
        c = new NDSCoordinate(-883384626, 485440671);
        assertEquals(5973384896724652798L, c.getMortonCode());

        // Sugarloaf
        c = new NDSCoordinate(-514888362, -273788154);
        assertEquals(8983442095026671932L, c.getMortonCode());

        // Sydney
        c = new NDSCoordinate(1804055545, -403936054);
        assertEquals(4354955230616876489L, c.getMortonCode());

        // Dome
        c = new NDSCoordinate(0, 614454724);
        assertEquals(585611620934393888L, c.getMortonCode());

        // Quito
        c = new NDSCoordinate(-935944956, 0);
        assertEquals(5782627506097029136L, c.getMortonCode());
    }

    /**
     * Verifies additional corner cases
     */
    @Test
    public void testNDSCoordinateMortonCodeComputationCornerCases() {
        NDSCoordinate c = new NDSCoordinate(NDSCoordinate.MAX_LONGITUDE, NDSCoordinate.MAX_LATITUDE);
        // 0001111111111111111111111111111111111111111111111111111111111111
        assertEquals(2305843009213693951L, c.getMortonCode());
        System.out.println(Long.toBinaryString(c.getMortonCode()));

        c = new NDSCoordinate(NDSCoordinate.MIN_LONGITUDE, NDSCoordinate.MIN_LATITUDE);
        // 0110000000000000000000000000000000000000000000000000000000000000
        assertEquals(6917529027641081856L, c.getMortonCode());
        System.out.println(Long.toBinaryString(c.getMortonCode()));

        c = new NDSCoordinate(NDSCoordinate.MIN_LONGITUDE, NDSCoordinate.MAX_LATITUDE);
        // 0100101010101010101010101010101010101010101010101010101010101010
        assertEquals(5380300354831952554L, c.getMortonCode());
        System.out.println(Long.toBinaryString(c.getMortonCode()));

        c = new NDSCoordinate(NDSCoordinate.MAX_LONGITUDE, NDSCoordinate.MIN_LATITUDE);
        // 0011010101010101010101010101010101010101010101010101010101010101
        assertEquals(3843071682022823253L, c.getMortonCode());
        System.out.println(Long.toBinaryString(c.getMortonCode()));

        c = new NDSCoordinate(0, 0);
        assertEquals(0L, c.getMortonCode());
    }
}