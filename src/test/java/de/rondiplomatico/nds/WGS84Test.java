package de.rondiplomatico.nds;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Simple tests.
 *
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
public class WGS84Test {

    @Test
    public void testConstructor() {
        try {
            WGS84Coordinate c = new WGS84Coordinate(300, 40);
            fail("IllegalArgumentException expected");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            WGS84Coordinate c = new WGS84Coordinate(-170, -100);
            fail("IllegalArgumentException expected");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        WGS84Coordinate c = new WGS84Coordinate(-170, -50);
        System.out.println(c);
        System.out.println(c.toGeoJSON());
    }

    @Test
    public void testBBox() {
        WGS84BBox c = new WGS84BBox(-170, -50, 20, 75);
        System.out.println(c);
        System.out.println(c.toGeoJSON());
    }
}
