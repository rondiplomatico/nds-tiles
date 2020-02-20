package de.rondiplomatico.nds;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A simple WGS84-format bounding box.
 * 
 * @author Daniel Wirtz
 * @since 20.02.2020
 */
@Data
@RequiredArgsConstructor
@ToString
public class WGS84BBox {

    private final double north;
    private final double east;
    private final double south;
    private final double west;

    /**
     * Creates a GeoJSON representation of this bounding box as a "Polygon" feature.
     *
     * @return
     */
    public String toGeoJSON() {
        return "{\"type\": \"Feature\",\r\n" +
                        "      \"properties\": {},\r\n" +
                        "      \"geometry\": {\r\n" +
                        "        \"type\": \"Polygon\",\r\n" +
                        "        \"coordinates\": [[\r\n" +
                        "          [" + west + "," + south + "],\r\n" +
                        "          [" + east + "," + south + "],\r\n" +
                        "          [" + east + "," + north + "],\r\n" +
                        "          [" + west + "," + north + "],\r\n" +
                        "          [" + west + "," + south + "]]\r\n" +
                        "        ]\r\n" +
                        "      }}";
    }

}
