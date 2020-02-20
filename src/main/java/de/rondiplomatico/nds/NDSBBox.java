package de.rondiplomatico.nds;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Utility class for bounding boxes in NDS coordinates.
 * 
 * For efficiency, this is implemented as separate class instead of including this into NDSTile.
 *
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
@Data
@RequiredArgsConstructor
@ToString
public class NDSBBox {
    /**
     * The western hemisphere part of the world map corresponds to NDS Tile number 0 on level 0
     */
    public static final NDSBBox WEST_HEMISPHERE = new NDSBBox(NDSCoordinate.MAX_LATITUDE, 0, NDSCoordinate.MIN_LATITUDE, NDSCoordinate.MIN_LONGITUDE);
    /**
     * The eastern hemisphere part of the world map corresponds to NDS Tile number 1 on level 0
     */
    public static final NDSBBox EAST_HEMISPHERE = new NDSBBox(NDSCoordinate.MAX_LATITUDE, NDSCoordinate.MAX_LONGITUDE, NDSCoordinate.MIN_LATITUDE, 0);

    private final int north;
    private final int east;
    private final int south;
    private final int west;

    /**
     * 
     * Gets the south west corner of the bounding box
     *
     * @return
     */
    public NDSCoordinate southWest() {
        return new NDSCoordinate(west, south);
    }

    /**
     * Gets the south east corner of the bounding box
     *
     * @return NDSCoordinate
     */
    public NDSCoordinate southEast() {
        return new NDSCoordinate(east, south);
    }

    /**
     * Gets the north west corner of the bounding box
     *
     * @return NDSCoordinate
     */
    public NDSCoordinate northWest() {
        return new NDSCoordinate(west, north);
    }

    /**
     * Gets the north east corner of the bounding box
     *
     * @return NDSCoordinate
     */
    public NDSCoordinate northEast() {
        return new NDSCoordinate(east, north);
    }

    /**
     * Returns the center of the bounding box
     * 
     * @return NDSCoordinate
     */
    public NDSCoordinate center() {
        return new NDSCoordinate((east + west) / 2, (north + south) / 2);
    }

    /**
     * 
     * Converts this bounding box to a WGS84-coordinate based bounding box.
     *
     * @return WGS84BBox
     */
    public WGS84BBox toWGS84() {
        WGS84Coordinate ne = northEast().toWGS84();
        WGS84Coordinate sw = southWest().toWGS84();
        return new WGS84BBox(ne.getLatitude(), ne.getLongitude(), sw.getLatitude(), sw.getLongitude());
    }

    /**
     * Creates a GeoJSON representation of this bounding box as a "Polygon" feature.
     * 
     * @return String
     */
    public String toGeoJSON() {
        return toWGS84().toGeoJSON();
    }

}
