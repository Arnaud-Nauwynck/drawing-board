package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;

/**
 * https://pomax.github.io/BezierInfo-2/#matrixsplit
 */
public class BezierMatrixSplit {

	// Quad Bezier split
	// ------------------------------------------------------------------------
	
	public static void splitQuadBezier(QuadBezier2D resLeftSplit, QuadBezier2D resRightSplit,
			double z, QuadBezier2D src) {
		splitQuadBezier(resLeftSplit, resRightSplit, z, src.startPt, src.controlPt, src.endPt);
	}
	
	public static void splitQuadBezier(QuadBezier2D resLeftSplit, QuadBezier2D resRightSplit,
			double z, Pt2D p1, Pt2D p2, Pt2D p3) {
		// optim (not re-computing twice resLeftSplit.endPt = resRightSplit.startPt
		// leftSplitQuadBezier(resLeftSplit, z, src.startPt, src.controlPt, src.endPt);
		// rightSplitQuadBezier(resRightSplit, z, src.startPt, src.controlPt, src.endPt);
		double zm1 = z - 1;
		resLeftSplit.startPt.set(p1);
		resLeftSplit.controlPt.setLinear(z, p2, -zm1, p1);
		resLeftSplit.endPt.setLinear(z*z, p3, -2*z*zm1, p2, zm1*zm1, p1);
		resRightSplit.startPt.set(resLeftSplit.endPt);
		resRightSplit.controlPt.setLinear(z, p3, -zm1, p2);
		resRightSplit.endPt.set(p3);
	}

	/**
	 * left quad bezier fragment in t=[0,z]
	 * <PRE>
	 * res.p1 =     p1
	 * res.p2 = z   p2 - (z-1)     p1
	 * res.p3 = z^2 p3 - 2 z (z-1) p2  + (z-1)^2 p1
	 * </PRE>
	 */
	public static void leftSplitQuadBezier(QuadBezier2D res, 
			double z, Pt2D p1, Pt2D p2, Pt2D p3) {
		double zm1 = z - 1;
		res.startPt.set(p1);
		res.controlPt.setLinear(z, p2, -zm1, p1);
		res.endPt.setLinear(z*z, p3, -2*z*zm1, p2, zm1*zm1, p1);
	}

	/**
	 * right quad bezier fragment in t=[z,1]
	 * <PRE>
	 * res.p1 = z^2 p3 - 2 z (z-1) p2  + (z-1)^2 p1
	 * res.p2 = z   p3 - (z-1)     p2
	 * res.p3 =     p3
	 * </PRE>
	 */
	public static void rightSplitQuadBezier(QuadBezier2D res, 
			double z, Pt2D p1, Pt2D p2, Pt2D p3) {
		double zm1 = z - 1;
		res.startPt.setLinear(z*z, p3, -2*z*zm1, p2, zm1*zm1, p1);
		res.controlPt.setLinear(z, p3, -zm1, p2);
		res.endPt.set(p3);
	}

	// Cubic Bezier split
	// ------------------------------------------------------------------------
	
	public static void splitCubicBezier(CubicBezier2D resLeftSplit, CubicBezier2D resRightSplit, //
			double z, CubicBezier2D src) {
		splitCubicBezier(resLeftSplit, resRightSplit,
				z, src.startPt, src.p1, src.p2, src.endPt);
	}
	public static void splitCubicBezier(CubicBezier2D resLeftSplit, CubicBezier2D resRightSplit, //
			double z, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		// optim (not re-computing twice resLeftSplit.endPt = resRightSplit.startPt
		// leftSplitCubicBezier(resLeftSplit, z, src.startPt, src.p1, src.p2, src.endPt);
		// rightSplitCubicBezier(resRightSplit, z, src.startPt, src.p1, src.p2, src.endPt);
		double zm1 = z - 1;
		double z2 = z*z, zm12 = zm1*zm1;
		resLeftSplit.startPt.set(p1);
		resLeftSplit.p1.setLinear(z, p2, -zm1, p1);
		resLeftSplit.p2.setLinear(z2, p3, -2*z*zm1, p1, zm12, p3);
		resLeftSplit.endPt.setLinear(z2*z, p4, -3*z2*zm1, p3, 3*z*zm12, p2, zm12, p3);
		resRightSplit.startPt.set(resLeftSplit.endPt);
		resRightSplit.p1.setLinear(z2, p4, -2*z*zm1, p3, zm12, p2);
		resRightSplit.p2.setLinear(z, p4, -zm1, p3);
		resRightSplit.endPt.set(p4);
	}

	/**
	 * left Cubic bezier fragment in t=[0,z]
	 * <PRE>
	 * res.p1 =     p1
	 * res.p2 = z   p2 - (z-1)       p1
	 * res.p3 = z^2 p3 - 2 z (z-1)   p2 + (z-1)^2    p1
	 * res.p4 = z^3 p4 - 3 z^2 (z-1) p3 + 3 z(z-1)^2 p2 + (z-1)^2 p1
	 * </PRE>
	 */
	public static void leftSplitCubicBezier(CubicBezier2D res, 
			double z, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		double zm1 = z - 1;
		double z2 = z*z, zm12 = zm1*zm1;
		res.startPt.set(p1);
		res.p1.setLinear(z, p2, -zm1, p1);
		res.p2.setLinear(z2, p3, -2*z*zm1, p1, zm12, p3);
		res.endPt.setLinear(z2*z, p4, -3*z2*zm1, p3, 3*z*zm12, p2, zm12, p3);
	}

	/**
	 * right Cubic bezier fragment in t=[z,1]
	 * <PRE>
	 * res.p1 = 
	 * res.p2 = z^2 p4 - 2 z (z-1) p3 + (z-1)^2 p2
	 * res.p3 = z   p4 - (z-1)     p3
	 * res.p4 =     p4
	 * </PRE>
	 */
	public static void rightSplitCubicBezier(CubicBezier2D res, 
			double z, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		double zm1 = z - 1;
		double z2 = z*z, zm12 = zm1*zm1;
		res.startPt.setLinear(z2*z, p4, -3*z2*zm1, p3, 3*z*zm12, p2, zm12, p3);
		res.p1.setLinear(z2, p4, -2*z*zm1, p3, zm12, p2);
		res.p2.setLinear(z, p4, -zm1, p3);
		res.endPt.set(p4);
	}

}
