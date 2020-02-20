NDS Tiles Toolset
=================
A simple java implementation of the NDS Tiling Scheme along with some utility classes and functions.
Short feature list:
- Create NDSTiles from coordinates, level+nr, packedId
- Access NDS Tile properties and bounding boxes
- Convert between WGS84 and NDS coordinate formats
- Get Morton codes for NDS Coordinates
- GeoJSON output of all classes

Usage
=====

Compiling
---------
This is a simple maven project. You can build the current jar with
	
	$ mvn package

Development
-----------
I used the Lombok java agent for easy code generation of class' default methods etc.
See https://projectlombok.org/ for setup instructions if you want to include the source code in your projects with e.g. Eclipse.

References
==========
- https://nds-association.org/
- https://en.wikipedia.org/wiki/Navigation_Data_Standard
- NDS Format Specification, Version 2.5.4
 * NDS Tiles: [1, §7.3.1] 
 * Morton codes: [1, §7.2.1]