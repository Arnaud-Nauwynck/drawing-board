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
	
	public static void splitQuadBezierIn2(QuadBezier2D resLeftSplit, QuadBezier2D resRightSplit,
			QuadBezier2D src) {
		splitQuadBezierIn2(resLeftSplit, resRightSplit, src.startPt, src.controlPt, src.endPt);
	}
	
	public static void splitQuadBezierIn2(QuadBezier2D resLeftSplit, QuadBezier2D resRightSplit, //
			Pt2D p1, Pt2D p2, Pt2D p3) {
		splitQuadBezier(resLeftSplit, resRightSplit, 0.5, p1, p2, p3);
	}
	
	public static void splitQuadBezier(QuadBezier2D resLeftSplit, QuadBezier2D resRightSplit,
			double z, QuadBezier2D src) {
		splitQuadBezier(resLeftSplit, resRightSplit, z, src.startPt, src.controlPt, src.endPt);
	}
	
	public static void splitQuadBezier(QuadBezier2D resLeftSplit, QuadBezier2D resRightSplit,
			double z, Pt2D p1, Pt2D p2, Pt2D p3) {
		// optim (not re-computing twice resLeftSplit.endPt = resRightSplit.startPt
		// leftSplitQuadBezier(resLeftSplit, z, src.startPt, src.controlPt, src.endPt);
		// rightSplitQuadBezier(resRightSplit, z, src.startPt, src.controlPt, src.endPt);
		double zm1 = 1.0-z;
		resLeftSplit.startPt.set(p1);
		resLeftSplit.controlPt.setLinear(z, p2, zm1, p1);
		resLeftSplit.endPt.setLinear(z*z, p3, 2*z*zm1, p2, zm1*zm1, p1);
		
		resRightSplit.startPt.set(resLeftSplit.endPt);
		resRightSplit.controlPt.setLinear(z, p3, zm1, p2);
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
		double zm1 = 1.0-z;
		res.startPt.setLinear(z*z, p3, 2*z*zm1, p2, zm1*zm1, p1);
		res.controlPt.setLinear(z, p3, zm1, p2);
		res.endPt.set(p3);
	}

	// Cubic Bezier split
	// ------------------------------------------------------------------------
	
	public static void splitCubicBezierIn2(CubicBezier2D resLeftSplit, CubicBezier2D resRightSplit, //
			CubicBezier2D src) {
		splitCubicBezierIn2(resLeftSplit, resRightSplit, src.startPt, src.p1, src.p2, src.endPt);
	}

	public static void splitCubicBezierIn2(CubicBezier2D resLeftSplit, CubicBezier2D resRightSplit, //
			Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		// equivalent to splitCubicBezier(resLeftSplit, resRightSplit, 0.5, p1, p2, p3, p4);
		double z = 0.5, z2 = z*z, z3 = z2*z;
		resLeftSplit.startPt.set(p1);
		resLeftSplit.p1.setLinear(z, p2, z, p1);
		resLeftSplit.p2.setLinear(z2, p3, 2*z2, p2, z2, p1);
		resLeftSplit.endPt.setLinear(z3, p4, 3*z3, p3, 3*z3, p2, z3, p1);
		resRightSplit.startPt.set(resLeftSplit.endPt);
		resRightSplit.p1.setLinear(z2, p4, 2*z2, p3, z2, p2);
		resRightSplit.p2.setLinear(z, p4, z, p3);
		resRightSplit.endPt.set(p4);
	}
	
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
		double zm1 = 1.0-z;
		double z2 = z*z, zm12 = zm1*zm1, zzm1 = z*zm1;
		resLeftSplit.startPt.set(p1);
		resLeftSplit.p1.setLinear(z, p2, zm1, p1);
		resLeftSplit.p2.setLinear(z2, p3, 2*zzm1, p2, zm12, p1);
		resLeftSplit.endPt.setLinear(z2*z, p4, 3*z2*zm1, p3, 3*z*zm12, p2, zm12*zm1, p1);
		
		resRightSplit.startPt.set(resLeftSplit.endPt);
		resRightSplit.p1.setLinear(z2, p4, 2*zzm1, p3, zm12, p2);
		resRightSplit.p2.setLinear(z, p4, zm1, p3);
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
		double zm1 = 1-z;
		double z2 = z*z, zm12 = zm1*zm1, zzm1 = z*zm1;
		res.startPt.set(p1);
		res.p1.setLinear(z, p2, zm1, p1);
		res.p2.setLinear(z2, p3, 2*zzm1, p2, zm12, p1);
		res.endPt.setLinear(z2*z, p4, 3*z2*zm1, p3, 3*z*zm12, p2, zm12*zm1, p1);
	}

	/**
	 * right Cubic bezier fragment in t=[z,1]
	 * <PRE>
	 * res.p1 = z^3 p4 - 3 z^2 (z-1) p3 + 3 z(z-1)^2 p2 + (z-1)^3 p1
	 * res.p2 = z^2 p4 - 2 z (z-1) p3 + (z-1)^2 p2
	 * res.p3 = z   p4 - (z-1)     p3
	 * res.p4 =     p4
	 * </PRE>
	 */
	public static void rightSplitCubicBezier(CubicBezier2D res, 
			double z, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		double zm1 = 1-z;
		double z2 = z*z, zm12 = zm1*zm1, zzm1 = z*zm1;
		res.startPt.setLinear(z2*z, p4, 3*z2*zm1, p3, 3*z*zm12, p2, zm12*zm1, p1);
		res.p1.setLinear(z2, p4, 2*zzm1, p3, zm12, p2);
		res.p2.setLinear(z, p4, zm1, p3);
		res.endPt.set(p4);
	}

	public static void middleSplitCubicBezier(CubicBezier2D res, 
			double start, double end, CubicBezier2D bezier) {
		middleSplitCubicBezier(res, start, end, bezier.startPt, bezier.p1, bezier.p2, bezier.endPt);
	}

	public static void middleSplitCubicBezier_check(CubicBezier2D res, 
			double start, double end, CubicBezier2D bezier) {
		middleSplitCubicBezier_check(res, start, end, bezier.startPt, bezier.p1, bezier.p2, bezier.endPt);
	}

	/**
	 * middle Cubic bezier fragment in t=[sStart,sEnd]
	 * 
	 * internally:
	 * .. first apply right t=[sStart,1], then  left t=[0,sEnd'], where 
	 * 
	 *  0  +---------+      1
	 *     sStart    sEnd
	 * 
	 *     +----------------+
	 *     0                1
	 *              sEnd'
	 *          
	 *  sEnd'/1 = (sEnd-sStart)/(1-sStart)
	 * 
	 * <PRE>
	 * leftB(z,p1,p2,p3,p4)= [ p1, z*p2+(1-z)*p1, z^2 * p3 + 2*z*(1-z)*p2 + (1-z)^2 * p1, (z^3 * p4 + 3 * z^2 * (1-z) * p3 + 3 * z * (1-z)^2 * p2 + (1-z)^2 * p1) ]
	 * rightB(z,p1,p2,p3,p4)= [ (z^3 * p4 + 3 * z^2 * (1-z) * p3 + 3 * z * (1-z)^2 * p2 + (1-z)^3 * p1), (z^2 * p4 + 2 * z *(1-z) * p3 + (1-z)^2 * p2),  (z  * p4 + (1-z) * p3), p4 ]
	 * 
	 * leftB0(z,p1,p2,p3,p4)= p1
     * leftB1(z,p1,p2,p3,p4)= z*p2+(1-z)*p1
     * leftB2(z,p1,p2,p3,p4)= z^2 * p3 + 2*z*(1-z)*p2 + (1-z)^2 * p1
     * leftB3(z,p1,p2,p3,p4)= z^3 * p4 + 3 * z^2 * (1-z) * p3 + 3 * z * (1-z)^2 * p2 + (1-z)^3 * p1
     * 
	 * middleB0(start,ep, p1,p2,p3,p4)= leftB0(ep, rightB(start,p1,p2,p3,p4)[0], rightB(start,p1,p2,p3,p4)[1], rightB(start,p1,p2,p3,p4)[2], rightB(start,p1,p2,p3,p4)[3])
     * middleB1(start,ep, p1,p2,p3,p4)= leftB1(ep, rightB(start,p1,p2,p3,p4)[0], rightB(start,p1,p2,p3,p4)[1], rightB(start,p1,p2,p3,p4)[2], rightB(start,p1,p2,p3,p4)[3])
     * middleB2(start,ep, p1,p2,p3,p4)= leftB2(ep, rightB(start,p1,p2,p3,p4)[0], rightB(start,p1,p2,p3,p4)[1], rightB(start,p1,p2,p3,p4)[2], rightB(start,p1,p2,p3,p4)[3])
     * middleB3(start,ep, p1,p2,p3,p4)= leftB3(ep, rightB(start,p1,p2,p3,p4)[0], rightB(start,p1,p2,p3,p4)[1], rightB(start,p1,p2,p3,p4)[2], rightB(start,p1,p2,p3,p4)[3])
     * 
	 * </PRE>
	 */
	public static void middleSplitCubicBezier(CubicBezier2D res, 
			double start, double end, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		if (start == 1.0) {
			res.startPt.set(p4);
			res.p1.set(p4);
			res.p2.set(p4);
			res.endPt.set(p4);
			return;
		}
		double start2 = start*start, start3 = start2*start;
		double sm1 = 1.0 - start, sm12 = sm1*sm1, sm13 = sm12*sm1;
		double ep = (end-start) / (1.0-start), ep2= ep*ep;
		double ep1 = 1.0 - ep, ep12 = ep1*ep1;
	    
		CubicBezier2D.eval(res.startPt, start, p1, p2, p3, p4);
		// res.startPt.setLinear(sm13, p1, 3*sm12*start, p2, 3*sm1*start2, p3, start3, p4);
		
		// middleSplitCubicBezier_ctrlPts(res, start, end, p1, p2, p3, p4);
		res.p1.setLinear(sm13*ep1, p1, //
				3*sm12*start*ep1 + sm12*ep, p2, //
				3*sm1*start2*ep1 + 2*sm1*start*ep, p3, //
				start3*ep1 + start2*ep, p4);
		
		res.p2.setLinear(sm13*ep12, p1, //
				3*sm12*start*ep12 + 2*sm12*ep1*ep, p2, //
				3*sm1*start2*ep12 + 4*sm1*start*ep1*ep + sm1*ep2, p3, //
				start3*ep12 + 2*start2*ep1*ep + start*ep2, p4);

		CubicBezier2D.eval(res.endPt, end, p1, p2, p3, p4);

//		double zm1 = 1-end, zm12 = zm1*zm1;
//		double end2 = end*end;
//		res.endPt.setLinear(end2*end, p4, 3*end2*zm1, p3, 3*end*zm12, p2, zm12*zm1, p1);
	}
	
	public static void middleSplitCubicBezier_ctrlPts(CubicBezier2D res, 
			double start, double end, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		double start2 = start*start, start3 = start2*start;
		double sm1 = 1.0 - start, sm12 = sm1*sm1, sm13 = sm12*sm1;
		double ep = (end-start) / (1.0-start), ep2= ep*ep;
		double ep1 = 1.0 - ep, ep12 = ep1*ep1;
	     
		res.p1.setLinear(sm13*ep1, p1, //
				3*sm12*start*ep1 + sm12*ep, p2, //
				3*sm1*start2*ep1 + 2*sm1*start*ep, p3, //
				start3*ep1 + start2*ep, p4);
		
		res.p2.setLinear(sm13*ep12, p1, //
				3*sm12*start*ep12 + 2*sm12*ep1*ep, p2, //
				3*sm1*start2*ep12 + 4*sm1*start*ep1*ep + sm1*ep2, p3, //
				start3*ep12 + 2*start2*ep1*ep + start*ep2, p4);
	}
	
	/*pp*/ static void middleSplitCubicBezier_check(CubicBezier2D res, 
			double sStart, double sEnd, Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		if (sStart == 1.0) {
			res.startPt.set(p4);
			res.p1.set(p4);
			res.p2.set(p4);
			res.endPt.set(p4);
			return;
		}
		CubicBezier2D tmp = new CubicBezier2D();
		rightSplitCubicBezier(tmp, sStart, p1, p2, p3, p4);

		double sEndP = (sEnd-sStart) / (1.0-sStart);
		leftSplitCubicBezier(res, sEndP, tmp.startPt, tmp.p1, tmp.p2, tmp.endPt);
	}
	
	// ------------------------------------------------------------------------
	
	public static double[] splitWeight4Params(CubicBezier2D bezier) {
		return splitWeight4Params(bezier.startPt, bezier.p1, bezier.p2, bezier.endPt);
	}

	public static double[] splitWeight4Params(Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		// compute barycenter of p1,p2,p3,p4
		Pt2D g = new Pt2D(0, 0);
		double c = 0.25;
		g.setLinear(c, p1, c, p2, c, p3, c, p4);
		// compute renormalized dist to barycenter
		double d1 = g.distTo(p1);
		double d2 = g.distTo(p2);
		double d3 = g.distTo(p3);
		double d4 = g.distTo(p4);
		double max = Math.max(Math.max(Math.max(d1, d2), d3), d4);
		if (max == 0) {
			return new double[] { 0.25, 0.5, 0.75 };
		}
		double invMax = 1.0 / max;
		double r1 = d1 * invMax, r2 = d2 * invMax, r3 = d3 * invMax, r4 = d4 * invMax;
		
		// s where term GP1 >= term GP2
		//     (1-s)^3 r1 >= 3(1-s)^2 s r2
		// <=> (1-s) r1   >= 3 s r2
		// <=>  r1 >= (r1+3 r2) s
		// <=>  s  <=  r1/(r1+3 r2)
		double s1 = r1/(r1+3*r2);
		
		// s where term GP2 >= GP3
		//    3(1-s)^2 s r2 >= 3(1-s) s^2 r3
		// <=>     (1-s) r2 >= s r3
		// <=>           r2 >= (r2+r3)s
		// <=>            s <= r2/(r2+r3)
		double s2 = r2/(r2+r3);
		
		// s where term GP3 >= GP4
		//     3 (1-s) s^2 r3 >= s^3 r4
		// <=>      3(1-s) r3 >= s r4
		// <=>       3r3      >= s(3 r3+r4)
		// <=>              s <= 3 r3 / (3 r3 + r4)
		double s3 = 3 * r3 / (3 * r3 + r4);
		
		double res1 = s1;
		double res2 = s2;
		if (res2 < res1) {
			double tmp = res1;
			res1 = res2;
			res2 = tmp;
		}
		double res3 = s3;
		if (res3 < res2) {
			double tmp = res2;
			res2 = res3;
			res3 = tmp;
		}
		return new double[] { res1, res2, res3 };
	}

	public static void splitWeight4CubicBezier(CubicBezier2D res0, CubicBezier2D res1, CubicBezier2D res2, CubicBezier2D res3, 
			CubicBezier2D bezier) {
		splitWeight4CubicBezier(res0, res1, res2, res3, bezier.startPt, bezier.p1, bezier.p2, bezier.endPt);
	}
	
	public static void splitWeight4CubicBezier(CubicBezier2D res0, CubicBezier2D res1, CubicBezier2D res2, CubicBezier2D res3, 
			Pt2D p1, Pt2D p2, Pt2D p3, Pt2D p4) {
		double[] splitParams = splitWeight4Params(p1, p2, p3, p4);
		double s0 = splitParams[0], s1 = splitParams[1], s2 = splitParams[2];

		// equivalent to.. but avoid recomputing several times same start/end pt 
		// leftSplitCubicBezier(res0, s0, p1, p2, p3, p4);
		// middleSplitCubicBezier(res1, s0, s1, p1, p2, p3, p4);
		// middleSplitCubicBezier(res2, s1, s2, p1, p2, p3, p4);
		// rightSplitCubicBezier(res3, s2, p1, p2, p3, p4);
		leftSplitCubicBezier(res0, s0, p1, p2, p3, p4);
		res1.startPt.set(res0.endPt);
		middleSplitCubicBezier_ctrlPts(res1, s0, s1, p1, p2, p3, p4);
		CubicBezier2D.eval(res1.endPt, s1, p1, p2, p3, p4);		
		res2.startPt.set(res1.endPt);
		middleSplitCubicBezier_ctrlPts(res2, s1, s2, p1, p2, p3, p4);
		rightSplitCubicBezier(res3, s2, p1, p2, p3, p4);
		res2.endPt.set(res3.startPt);
	}

}
