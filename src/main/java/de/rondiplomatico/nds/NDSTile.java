package de.rondiplomatico.nds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Implementation of the NDS Tile scheme.
 * It follows the NDS Format Specification, Version 2.5.4, §7.3.1.
 * 
 * No warranties for correctness, use at own risk.
 *
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
@Getter
@ToString
@EqualsAndHashCode
public class NDSTile {

    /**
     * The maximum Tile level within the NDS specification.
     */
    public static final int MAX_LEVEL = 15;

    /*
     * The tile level
     */
    private int level = -1;

    /*
     * The tile number.
     * 
     * The tile number is identical to the (2*level+1) most-significant bits of
     * the Morton code of the south-west corner of the tile.
     */
    private int tileNumber;

    /*
     * Transient center coordinate
     */
    private transient NDSCoordinate center;

    /**
     * Creates a new {@link NDSTile} instance from a packed Tile id.
     *
     * @param packedId
     * @see NDSSpecification 2.5.4: 7.3.3 Generating Packed Tile IDs
     */
    public NDSTile(int packedId) {
        level = extractLevel(packedId);
        if (level < 0) {
            throw new IllegalArgumentException("Invalid packed Tile ID " + packedId + ": No Level bit present.");
        }
        int level_bit = 1 << (16 + level);
        tileNumber = packedId ^ level_bit;
    }

    /**
     * Creates a new {@link NDSTile} instance for a given id and level.
     *
     * @param level
     *                  Must be in range 0..15
     * @param nr
     *                  An admissible tile number w.r.t to the specified level.
     * 
     * @see NDSSpecification 2.5.4: 7.3.3 Generating Packed Tile IDs
     */
    public NDSTile(int level, int nr) {
        if (level < 0) {
            throw new IllegalArgumentException("The Tile level " + level + " exceeds the range [0, 15].");
        }
        this.level = level;
        if (nr < 0) {
            throw new IllegalArgumentException("The Tile id " + level + " must be positive (Max length is 31 bits).");
        }
        if (nr > (1 << 2 * level + 1) - 1) {
            throw new IllegalArgumentException("Invalid Tile number for level " + level + ", numbers 0 .. " + (Math.pow(2, 2 * level + 1) - 1)
                            + " are allowed");
        }
        this.tileNumber = nr;
    }

    /**
     * 
     * Creates a new {@link NDSTile} instance of the specified level, containing the specified coordinate
     *
     * @param level
     * @param coord
     */
    public NDSTile(int level, NDSCoordinate coord) {
        /*
         * Getting the NDS tile for a NDS coordinate amount to shifting the morton code of the coordinate by the necessary
         * amount. Each NDSTile can be represented by the level and morton code of the lower left / south west corner.
         */
        this(level, (int) (coord.getMortonCode() >> 32 + (MAX_LEVEL - level) * 2));
    }

    /**
     * Creates a new {@link NDSTile} instance of the specified level, containing the specified coordinate
     *
     * @param level
     *                  the level
     * @param coord
     *                  the coord
     */
    public NDSTile(int level, WGS84Coordinate coord) {
        this(level, new NDSCoordinate(coord.getLongitude(), coord.getLatitude()));
    }

    /**
     * Checks if the current Tile contains a certain coordinate.
     *
     * @param c
     *              the coordinate
     * @return true, if successful
     */
    public boolean contains(NDSCoordinate c) {
        /*
         * Checks containment via verifying if the coordinates' tile number for the current tile level matches.
         * 
         * The tile number is identical to the (2*level+1) most-significant bits of
         * the Morton code of the south-west corner of the tile.
         */
        return tileNumber == (int) (c.getMortonCode() >> 32 + (MAX_LEVEL - level) * 2);
    }

    /**
     * Returns the packed Tile ID for this tile. Contains the level and (partial) morton code (bitwise)
     * 
     * @see NDSFormatSpecification: 7.3.3 Generating Packed Tile IDs
     *
     * @return
     */
    public int packedId() {
        return tileNumber + (1 << (16 + level));
    }

    /**
     * Returns the center of this tile as NDSCoordinate
     *
     * @return NDSCoordinate The center of this tile
     */
    public NDSCoordinate getCenter() {
        if (center == null) {
            if (level == 0) {
                return tileNumber == 0 ? new NDSCoordinate(NDSCoordinate.MAX_LONGITUDE / 2, 0)
                                : new NDSCoordinate(NDSCoordinate.MIN_LONGITUDE / 2, 0);
            }
            NDSCoordinate sw = new NDSCoordinate(southWestAsMorton());
            // Same computation as for bounding box, but for the next lower level
            int clat = (int) (sw.latitude + Math.floor(NDSCoordinate.LATITUDE_RANGE / (1L << level + 1))) + (sw.latitude < 0 ? 1 : 0);
            int clon = (int) (sw.longitude + Math.floor(NDSCoordinate.LONGITUDE_RANGE / (1L << level + 2))) + (sw.longitude < 0 ? 1 : 0);
            center = new NDSCoordinate(clon, clat);
        }
        return center;
    }

    /**
     * Creates a bounding box for the current tile.
     *
     * @see NDSFormatSpecification: 7.3.3 Generating Packed Tile IDs
     * 
     * @return
     */
    public NDSBBox getBBox() {
        /*
         * For level 0 there are two tiles.
         */
        if (level == 0) {
            return tileNumber == 0 ? NDSBBox.EAST_HEMISPHERE : NDSBBox.WEST_HEMISPHERE;
        }
        long southWestCornerMorton = southWestAsMorton();
        NDSCoordinate sw = new NDSCoordinate(southWestCornerMorton);
        int north = (int) (sw.latitude + Math.floor(NDSCoordinate.LATITUDE_RANGE / (1L << level))) + (sw.latitude < 0 ? 1 : 0);
        int east = (int) (sw.longitude + Math.floor(NDSCoordinate.LONGITUDE_RANGE / (1L << level + 1))) + (sw.longitude < 0 ? 1 : 0);
        return new NDSBBox(north, east, sw.latitude, sw.longitude);
    }

    /**
     * Computes a GeoJSON representation of the NDS Tile as GeoJSON "Polygon" feature.
     *
     * @return String
     */
    public String toGeoJSON() {
        return getBBox().toWGS84().toGeoJSON();
    }

    private long southWestAsMorton() {
        int shift = 32 + (MAX_LEVEL - level) * 2;
        return (long) tileNumber << shift;
    }

    private int extractLevel(int packedId) {
        for (int lvl = MAX_LEVEL; lvl > -1; lvl--) {
            int lvl_bit = 1 << 16 + lvl;
            if ((packedId & lvl_bit) > 0) {
                return lvl;
            }
            // The 32nd bit can not be checked by java's native integer, as the sign bit is hidden
            if (packedId < 0 && lvl == MAX_LEVEL)
                return MAX_LEVEL;
        }
        return -1;
    }
}
