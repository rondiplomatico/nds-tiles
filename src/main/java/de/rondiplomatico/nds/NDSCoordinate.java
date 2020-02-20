package de.rondiplomatico.nds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Implementation of a NDS coordinate, according to the NDS Format Specification, Version 2.5.4, §7.2.1.
 * 
 * The NDS coordinate encoding divides the 360° range into 2^32 steps.
 * Consequently, each coordinate is represented by a pair of signed integers, where
 * a coordinate unit corresponds to 360/2^32 = 90/2^30 degrees longitude/latitude
 * (with their respective longitude [-180°,180°] and latitude [-90°,90°] ranges).
 * 
 * Note: The integer range is not fully used encoding latitude values due to the half degree range.
 * But this is done in favor of equally sized coordinate units along longitude/latitude.
 * 
 * No warranties for correctness, use at own risk.
 * 
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
@Getter
@ToString
@EqualsAndHashCode
public class NDSCoordinate {

    public static final int MAX_LONGITUDE = Integer.MAX_VALUE;
    public static final int MIN_LONGITUDE = Integer.MIN_VALUE;
    public static final int MAX_LATITUDE = MAX_LONGITUDE / 2;
    public static final int MIN_LATITUDE = MIN_LONGITUDE / 2;

    public static final long LONGITUDE_RANGE = (long) MAX_LONGITUDE - MIN_LONGITUDE;
    public static final long LATITUDE_RANGE = (long) MAX_LATITUDE - MIN_LATITUDE;

    public final int latitude;
    public final int longitude;

    /**
     * Creates a new {@link NDSCoordinate} instance
     *
     * @param longitude
     * @param latitude
     */
    public NDSCoordinate(int longitude, int latitude) {
        verify(longitude, latitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Instantiates a new NDS coordinate from WGS84 coordinates.
     *
     * @param lon
     *                the longitude within [-180, 180]
     * @param lat
     *                the latitude within [-90, 90]
     */
    public NDSCoordinate(double lon, double lat) {
        if (lon < -180 || lon > 180) {
            throw new IllegalArgumentException("The longitude value " + lon + " exceeds the valid range of [-180; 180]");
        }
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("The latitude value " + lat + " exceeds the valid range of [-90; 90]");
        }
        latitude = (int) Math.floor(lat / 180.0 * LATITUDE_RANGE);
        longitude = (int) Math.floor(lon / 360.0 * LONGITUDE_RANGE);
    }

    /**
     * Creates a new NDSCoordinate from a morton code.
     * 
     * @param ndsMortonCoordinates
     * @return
     */
    public NDSCoordinate(long ndsMortonCoordinates) {
        int lat = 0;
        int lon = 0;
        for (int pos = 0; pos < 32; ++pos) {
            if (pos < 31 && (ndsMortonCoordinates & 1L << (pos * 2 + 1)) != 0L) {
                lat |= 1 << pos;
            }
            if ((ndsMortonCoordinates & 1L << (pos * 2)) != 0L) {
                lon |= 1 << pos;
            }
        }
        /*
         * with NDS, the latitude value is considered a 31-bit signed integer.
         * hence, if the 31st bit is 1, this means we have a negative integer, requiring
         * to set the 32st bit to 1 for native java 32bit signed integers.
         */
        if ((lat & 1 << 30) > 0) {
            lat |= 1 << 31;
        }
        verify(lon, lat);
        latitude = lat;
        longitude = lon;
    }

    private void verify(int lon, int lat) {
        if (lat < MIN_LATITUDE || MAX_LATITUDE < lat) {
            throw new IllegalArgumentException("Latitude value " + lat + " exceeds allowed range [-2^30; 2^30] [" + MIN_LATITUDE + "," + MAX_LATITUDE + "].");
        }
    }

    /**
     * Adds an offset specified by two int values to the coordinate.
     * Useful for NDS coordinate decoding using tile offsets.
     *
     * @param deltaLongitude
     * @param deltaLatitude
     * @return NDSCoordinate
     */
    public NDSCoordinate add(int deltaLongitude, int deltaLatitude) {
        return new NDSCoordinate(longitude + deltaLongitude, latitude + deltaLatitude);
    }

    /**
     * Returns the unique morton code for this NDSCoordinate.
     * 
     * @see NDS Format Specification, Version 2.5.4, §7.2.1.
     *
     * @return long
     */
    public long getMortonCode() {
        long res = 0L;
        for (int pos = 0; pos < 31; pos++) {
            if ((longitude & 1 << pos) > 0) {
                res |= 1L << (2 * pos);
            }
            if (pos < 31 && (latitude & 1 << pos) > 0) {
                res |= 1L << (2 * pos + 1);
            }
        }
        if (longitude < 0) {
            res |= 1L << 62;
        }
        // For 31-bit signed integers the 32st bit needs to be copied to the 31st bit in case of negative numbers.
        if (latitude < 0) {
            res |= 1L << 61;
        }
        return res;
    }

    /**
     * 
     * Converts this coordinate to a WGS84 coordinate (using the "usual" longitude/latitude degree ranges)
     *
     * @return
     */
    public WGS84Coordinate toWGS84() {
        double lon = longitude >= 0 ? (double) longitude / (double) MAX_LONGITUDE * 180.0D
                        : (double) longitude / (double) MIN_LONGITUDE * -180.0D;
        double lat = latitude >= 0 ? (double) latitude / (double) MAX_LATITUDE * 90.0D
                        : (double) latitude / (double) MIN_LATITUDE * -90.0D;
        return new WGS84Coordinate(lon, lat);
    }

    /**
     * Creates a GeoJSON "Point" feature representation of this coordinate
     *
     * @return
     */
    public String toGeoJSON() {
        return toWGS84().toGeoJSON();
    }
}