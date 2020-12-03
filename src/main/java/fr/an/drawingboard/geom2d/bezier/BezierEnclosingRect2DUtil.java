package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;

/**
 * see https://pomax.github.io/bezierinfo/#boundingbox
 * https://github.com/Pomax/bezierinfo/blob/master/js/graphics-element/lib/bezierjs/bezier.js
 */
public class BezierEnclosingRect2DUtil {

	public static void bestEnclosing(BoundingRect2DBuilder res, QuadBezier2D quadBezier) {
		bestEnclosing_QuadBezier(res, quadBezier.startPt, quadBezier.controlPt, quadBezier.endPt);
	}
	
	public static void bestEnclosing_QuadBezier(BoundingRect2DBuilder res, 
			Pt2D p0, Pt2D p1, Pt2D p2
			) {
		res.enclosingPt(p0);
		res.enclosingPt(p2);
		// do not add p1 .. compute horyzontal tangent pt, and vertical tangent pt if any
		if (! res.isContainsPt(p1)) {
			// b(s)  = (1-s)^2 p0 + 2(1-s)s p1 + s^2 p2
			// b(s)  = (1 -2s + s^2) p0 + (2s -2s^2) p1 + s^2 p2
			
			// b'(s) = (-2 + 2s) p0 + (2 - 4s) p1 + 2s p2
			//       = 2[ ( p0 - 2p1 + p2) s + ( p1 - p0 ) ]
			//       = 2 (a s + b)
			double a_x = p0.x - 2*p1.x + p2.x;
			double a_y = p0.y - 2*p1.y + p2.y;
			double b_x = p1.x - p0.x;
			double b_y = p1.y - p0.y;

			// b_x'(s) = 0 <=>  a s + b = 0  <=> s = -b/a
			if (a_x != 0.0) {
				double s = -b_x / a_x;
				if (0.0 < s && s < 1.0) {
					double bs_x = QuadBezier2D.evalB(s, p0.x, p1.x, p2.x);
					res.enclosingX(bs_x);
				}
			}
			// idem y.. b_y'(s) = 0 <=> ..
			if (a_y != 0.0) {
				double s = -b_y / a_y;
				if (0.0 < s && s < 1.0) {
					double bs_y = QuadBezier2D.evalB(s, p0.y, p1.y, p2.y);
					res.enclosingY(bs_y);
				}
			}
		}
	}

	public static void bestEnclosing_CubicBezier(BoundingRect2DBuilder res, 
			CubicBezier2D cubicBezier
			) {
		bestEnclosing_CubicBezier(res, cubicBezier.startPt, cubicBezier.p1, cubicBezier.p2, cubicBezier.endPt);
	}
	
	public static void bestEnclosing_CubicBezier(BoundingRect2DBuilder res, 
			Pt2D p0, Pt2D p1, Pt2D p2, Pt2D p3
			) {
		res.enclosingPt(p0);
		res.enclosingPt(p3);
		// do not add p1,pt2 .. compute horyzontal tangent pts, and vertical tangent pts if any
		if (! res.isContainsPt(p1) || ! res.isContainsPt(p2)) {
			// b(s)  = (1-s)^3 p0              + 3(1-s)^2 s p1         + 3(1-s)s^2 p2    + s^3 p3
			// b(s)  = (1 -3s + 3s^2 - s^3) p0 + 3(s - 2 s^2 + s^3) p1 + 3(s^2 - s^3) p2 + s^3 p3
			
			// b'(s) = 3(-1 + 2s - s^2) p0 + 3(1 - 4s + 3s^2) p1 + 3(2s - 3s^2) p2 + 3s^2 p3
			//       = 3[ (-p0 + 3p1 - 3p2 + p3) s^2 + 2( p0 - 2p1 + p2) s + ( -p0 + p1 ) ]
			//       = 3 (a s^2 + 2b s + c)
			double a_x = -p0.x + 3*p1.x -3*p2.x + p3.x;
			double a_y = -p0.y + 3*p1.y -3*p2.y + p3.y;
			double b_x = p0.x - 2*p1.x + p2.x;
			double b_y = p0.y - 2*p1.y + p2.y;
			double c_x = p1.x - p0.x;
			double c_y = p1.y - p0.y;

			// b_x'(s) = 0 <=>  a s^2 + 2bs + c = 0  <=> s = (-b +/- sqrt(b^2-ac))/a
			if (a_x != 0.0) {
				double delta = b_x*b_x - a_x * c_x;
				if (delta >= 0) {
					double sqrt_delta = Math.sqrt(delta);
					{ // root 1
						double s = (-b_x - sqrt_delta) / a_x;
						if (0.0 < s && s < 1.0) {
							double bs_x = CubicBezier2D.evalB(s, p0.x, p1.x, p2.x, p3.x);
							res.enclosingX(bs_x);
							// useless?
							res.enclosingPt(CubicBezier2D.eval(s, p0, p1, p2, p3));
						}
					}
					{ // root 2
						double s = (-b_x + sqrt_delta) / a_x;
						if (0.0 < s && s < 1.0) {
							double bs_x = CubicBezier2D.evalB(s, p0.x, p1.x, p2.x, p3.x);
							res.enclosingX(bs_x);
							// useless?
							res.enclosingPt(CubicBezier2D.eval(s, p0, p1, p2, p3));
						}
					}
				}
			} else {
				// 2bs + c = 0 <=> s=-c/(2b)
				if (b_x != 0.0) {
					double s = -c_x / (2 * b_x);
					if (0.0 < s && s < 1.0) {
						double bs_x = CubicBezier2D.evalB(s, p0.x, p1.x, p2.x, p3.x);
						res.enclosingX(bs_x);
						// useless?
						res.enclosingPt(CubicBezier2D.eval(s, p0, p1, p2, p3));
					}
				}
			}
			// idem y.. b_y'(s) = 0 <=> ..
			if (a_y != 0.0) {
				double delta = b_y*b_y - a_y * c_y;
				if (delta >= 0) {
					double sqrt_delta = Math.sqrt(delta);
					{ // root 1
						double s = (-b_y - sqrt_delta) / a_y;
						if (0.0 < s && s < 1.0) {
							double bs_y = CubicBezier2D.evalB(s, p0.y, p1.y, p2.y, p3.y);
							res.enclosingY(bs_y);
							// useless?
							res.enclosingPt(CubicBezier2D.eval(s, p0, p1, p2, p3));
						}
					}
					{ // root 2
						double s = (-b_y + sqrt_delta) / a_y;
						if (0.0 < s && s < 1.0) {
							double bs_y = CubicBezier2D.evalB(s, p0.y, p1.y, p2.y, p3.y);
							res.enclosingY(bs_y);
							// useless?
							res.enclosingPt(CubicBezier2D.eval(s, p0, p1, p2, p3));
						}
					}
				}
			} else {
				// 2bs + c = 0 <=> s=-c/(2b)
				if (b_y != 0.0) {
					double s = -c_y / (2 * b_y);
					if (0.0 < s && s < 1.0) {
						double bs_y = CubicBezier2D.evalB(s, p0.y, p1.y, p2.y, p3.y);
						res.enclosingY(bs_y);
						// useless?
						res.enclosingPt(CubicBezier2D.eval(s, p0, p1, p2, p3));
					}
				}
			}
		}
	}

}
