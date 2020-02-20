package de.rondiplomatico.nds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A simple class containing coordinates in WGS84 format.
 * 
 * @see https://en.wikipedia.org/wiki/World_Geodetic_System
 * @see https://earth-info.nga.mil/GandG/update/index.php?dir=wgs84&action=wgs84
 * 
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
@EqualsAndHashCode
@ToString
@Getter
public class WGS84Coordinate {

    private final double longitude;
    private final double latitude;

    /**
     * Instantiates a new WGS 84 coordinate.
     *
     * @param longitude
     *                      the longitude
     * @param latitude
     *                      the latitude
     * 
     */
    public WGS84Coordinate(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("The longitude value " + longitude + " exceeds the valid range of [-180; 180]");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("The latitude value " + latitude + " exceeds the valid range of [-90; 90]");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Creates a GeoJSON "Point" feature representation of this coordinate
     *
     * @return
     */
    public String toGeoJSON() {
        return "{\"type\": \"Feature\",\r\n" +
                        "      \"properties\": {},\r\n" +
                        "      \"geometry\": {\r\n" +
                        "        \"type\": \"Point\",\r\n" +
                        "        \"coordinates\": [\r\n" +
                        "          " + longitude + "," + latitude + "\r\n" +
                        "        ]\r\n" +
                        "      }},";
    }
}