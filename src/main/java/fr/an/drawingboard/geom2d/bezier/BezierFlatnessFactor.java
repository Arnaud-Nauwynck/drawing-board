package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import lombok.val;

/**
 * http://www.malinc.se/m/DeCasteljauAndBezier.php
 * |P0P1|+|P1P2|+|P2P3|<"flatness factor"⋅|P0P3|
 * 
 * http://hcklbrrfnn.files.wordpress.com/2012/08/bez.pdf
 */
public class BezierFlatnessFactor {

	public static double flatnessFactor(CubicBezier2D bezier) {
		return flatnessFactor(bezier.startPt, bezier.p1, bezier.p2, bezier.endPt); 
	}
	
	// http://hcklbrrfnn.files.wordpress.com/2012/08/bez.pdf
	public static double flatnessFactor(Pt2D p0, Pt2D p1, Pt2D p2, Pt2D p3) {
	    val ux = 3 * p1.x - 2 * p0.x - p3.x,
	        uy = 3 * p1.y - 2 * p0.y - p3.y,
	        vx = 3 * p2.x - 2 * p3.x - p0.x,
	        vy = 3 * p2.y - 2 * p3.y - p0.y;
	    return Math.max(ux * ux, vx * vx) + Math.max(uy * uy, vy * vy);
	}

}
