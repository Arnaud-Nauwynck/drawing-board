package fr.an.drawingboard.recognizer.shape;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.model.shape.GesturePathesCtxEval;
import fr.an.drawingboard.model.shape.PathCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.CubicBezierPathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.DiscretePointsPathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.PathElementCtxEvalVisitor;
import fr.an.drawingboard.model.shape.PathElementCtxEval.QuadBezierPathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.SegmentPathElementCtxEval;
import fr.an.drawingboard.model.shape.ShapeCtxEval;
import fr.an.drawingboard.util.DrawingValidationUtils;
import lombok.val;

/**
 * compute distance between Pt and TracePathElement, if lower than a specified value otherwise skip
 */
public class PtToPathElementLoweringDistUtils {

	public static class PtToPathElementLoweringDistResult {
		// public TracePathElement resultBestPathElement;
		// public int resultBestPathElementPtIndex;
		public double resultDist;
		public double resultProjPathParam; // in [0.0, 1.0]
	}

	public static boolean evalMinDistIfLowerThan(PtToPathElementLoweringDistResult resDist, 
			Pt2D from, ShapeCtxEval to, double ifLowerThan) {
		boolean res = false;
		double boundRectDist = to.boundingRect.outerDistOr0ToPt(from);
		if (boundRectDist > ifLowerThan) {
			return false;
		}
		for(val gesture: to.gestures) {
			boolean tmpres = evalMinDistIfLowerThan(resDist, from, gesture, ifLowerThan);
			if (tmpres) {
				res = true;
				ifLowerThan = resDist.resultDist;
			}	
		}
		return res;
	}

	public static boolean evalMinDistIfLowerThan(PtToPathElementLoweringDistResult resDist, 
			Pt2D from, GesturePathesCtxEval to, double ifLowerThan) {
		boolean res = false;
		double boundRectDist = to.boundingRect.outerDistOr0ToPt(from);
		if (boundRectDist > ifLowerThan) {
			return false;
		}
		for(val path: to.pathes) {
			boolean tmpres = evalMinDistIfLowerThan(resDist, from, path, ifLowerThan);
			if (tmpres) {
				res = true;
				ifLowerThan = resDist.resultDist;
			}	
		}
		return res;
	}
	
	public static boolean evalMinDistIfLowerThan(PtToPathElementLoweringDistResult resDist, 
			Pt2D from, PathCtxEval to, double ifLowerThan) {
		boolean res = false;
		double boundRectDist = to.boundingRect.outerDistOr0ToPt(from);
		if (boundRectDist > ifLowerThan) {
			return false;
		}
		for(val pathElement: to.pathElements) {
			boolean tmpres = evalMinDistIfLowerThan(resDist, from, pathElement, ifLowerThan);
			if (tmpres) {
				res = true;
				ifLowerThan = resDist.resultDist;
			}	
		}
		return res;
	}
	
	public static double evalMinDist(Pt2D from, PathElementCtxEval to) {
		val resDist = new PtToPathElementLoweringDistResult();
		evalMinDistIfLowerThan(resDist, from, to, Double.MAX_VALUE);
		return resDist.resultDist;
	}

	public static boolean evalMinDistIfLowerThan(PtToPathElementLoweringDistResult resDist, 
			Pt2D from, PathElementCtxEval to, double ifLowerThan) {
		boolean[] res = new boolean[1];
		to.accept(new PathElementCtxEvalVisitor() {
			@Override
			public void caseSegment(SegmentPathElementCtxEval to) {
				res[0] = evalMinDistIfLowerThan_Segment(resDist, from, to, ifLowerThan);
			}
			@Override
			public void caseDiscretePoints(DiscretePointsPathElementCtxEval to) {
				res[0] = evalMinDistIfLowerThan_DiscretePoints(resDist, from, to, ifLowerThan);
			}
			@Override
			public void caseQuadBezier(QuadBezierPathElementCtxEval to) {
				res[0] = evalMinDistIfLowerThan_QuadBezier(resDist, from, to, ifLowerThan);
			}
			@Override
			public void caseCubicBezier(CubicBezierPathElementCtxEval to) {
				res[0] = evalMinDistIfLowerThan_CubicBezier(resDist, from, to, ifLowerThan);
			}
		});
		return res[0];
	}

	public static boolean evalMinDistIfLowerThan_Segment(PtToPathElementLoweringDistResult resDist, //
			Pt2D from, SegmentPathElementCtxEval to, double ifLowerThan) {
		return evalMinDistIfLowerThan_SegmentPt2D(resDist, from, to.startPt, to.endPt, ifLowerThan);
	}

	public static boolean evalMinDistIfLowerThan_DiscretePoints(PtToPathElementLoweringDistResult res, //
			Pt2D from, DiscretePointsPathElementCtxEval to, double ifLowerThan) {
		double boundRectDist = to.boundingRect.outerDistOr0ToPt(from);
		if (boundRectDist > ifLowerThan) {
			return false;
		}
		boolean resFoundMin = false;
		val toPts = to.pts;
		int ptsCount = toPts.length;
		if (ptsCount == 0) return false; // should not occur
		Pt2D prevPt;
		{ // pt[0]
			Pt2D pt0 = toPts[0];
			double distPt0 = from.distTo(pt0);
			if (distPt0 < ifLowerThan) {
				resFoundMin = true;
				ifLowerThan = distPt0;
			}
			prevPt = pt0;
		}
		for(int i = 1; i < ptsCount; i++) {
			Pt2D pt = toPts[i];
			boolean tmpres = evalMinDistIfLowerThan_SegmentPt2D(res, from, prevPt, pt, ifLowerThan);
			if (tmpres) {
				resFoundMin = true;
				ifLowerThan = res.resultDist;
				// res.pathElement = to;
				
			}
		}
		return resFoundMin;
	}

	/**
	 * <PRE>
	 * 
	 * 
	 * </PRE>
	 */
	public static boolean evalMinDistIfLowerThan_SegmentPt2D(PtToPathElementLoweringDistResult res, 
			Pt2D p, Pt2D a, Pt2D b, double ifLowerThan) {
		val x = p.x, y = p.y, ax = a.x, ay = a.y, bx = b.x, by = b.y;
		val minx = Math.min(ax, bx);
		if ((x + ifLowerThan) < minx) return false;
		val miny = Math.min(ay, by);
		if ((y + ifLowerThan) < miny) return false;
		val maxx = Math.max(ay, by);
		if ((x -ifLowerThan) > maxx) return false;
		val maxy = Math.min(ay, by);
		if ((y - ifLowerThan) < maxy) return false;

		// case1        case2        case3
		//  p            p            pt
		//  +      u     +             +
		//        ->     \     ---+
		//      -/    ----+---/   b
		//      +----/    H        
		//      a    
		// Pt2D vect = toStartPt.vectTo(toEndPt);
		val ab_x = bx - ax, ab_y = by - ay;
		val ap_x = x - ax, ap_y = y - ay;
		val bp_x = x - bx, bp_y = y - by;
		double scalar_ap_ab = ap_x * ab_x + ap_y * ab_y;
		double dist;
		double resultProjPathParam;
		if (scalar_ap_ab <= 0.0) {
			// case 1
			dist = Math.sqrt(ap_x*ap_x + ap_y*ap_y); // =|ap|
			resultProjPathParam = 0.0;
		} else {
			double norm_ab = ab_x * ab_x + ab_y * ab_y; // 'ab'
			if (scalar_ap_ab >= norm_ab) { // includes case norm_ab==0.0
				// case 3
				dist = Math.sqrt(bp_x*bp_x + bp_y*bp_y); // =|bp|
				resultProjPathParam = 1.0;
			} else {
				// case 2
				val inv_norm_ab = 1.0 / norm_ab;
				double s = scalar_ap_ab * inv_norm_ab;
				val h_x = ax + s * ab_x, h_y = ay + s * ab_y;
				val hp_x = x-h_x, hp_y = y - h_y;
				dist = Math.sqrt(hp_x * hp_x + hp_y * hp_y);
				resultProjPathParam = s;
			}
		}

		if (dist < ifLowerThan) {
			res.resultDist = dist;
			res.resultProjPathParam = resultProjPathParam;
			return true;
		}
		return false;
	}
	
	public static boolean evalMinDistIfLowerThan_QuadBezier(PtToPathElementLoweringDistResult resDist, Pt2D from,
			QuadBezierPathElementCtxEval to, double ifLowerThan) {
		throw DrawingValidationUtils.notImplYet(); // TODO
	}

	public static boolean evalMinDistIfLowerThan_CubicBezier(PtToPathElementLoweringDistResult resDist, Pt2D from,
			CubicBezierPathElementCtxEval to, double ifLowerThan) {
		throw DrawingValidationUtils.notImplYet(); // TODO
	}

}
