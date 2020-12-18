package fr.an.drawingboard.recognizer.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.PathElementDefFunc0;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.recognizer.initialParamEstimators.ParamEvalCtx;
import lombok.val;

public class PathDistLengthesUtils {


	public static double[] ptsToRatioDistLengthes(List<Pt2D> pts) {
		int ptCount = pts.size();
		double[] len_pt0_pti = new double[ptCount];
		len_pt0_pti[0] = 0.0;
		Pt2D pt_im1 = pts.get(0);
		double currLen = 0.0;
		for(int i = 1; i < ptCount; i++) {
			Pt2D pt_i = pts.get(i);
			double dist_ptim1_pti = pt_im1.distTo(pt_i);
			currLen += dist_ptim1_pti;
			len_pt0_pti[i] = currLen;
			pt_im1 = pt_i;
		}
		// renorm ratio to total len
		double coefNorm = 1.0 / ((len_pt0_pti[ptCount-1]!=0.0)? len_pt0_pti[ptCount-1] : 1.0);
		double[] s_i = len_pt0_pti; // reuse same array.. replace
		for(int i = 0; i < ptCount; i++) {
			s_i[i] = len_pt0_pti[i] * coefNorm;
		}
		return s_i;
	}

	public static double sum(List<Double> values) {
		double res = 0.0;
		for(val d : values) {
			res += d;
		}
		return res;
	}
	
	public static List<Double> distLengthes(Collection<TracePt> pts) {
		List<Double> res = new ArrayList<>();
		val iter = pts.iterator();
		if (! iter.hasNext()) {
			return res;
		}
		TracePt prevPt = iter.next();
		for(; iter.hasNext(); ) {
			TracePt pt = iter.next();
			res.add(TracePt.dist(prevPt, pt));
			prevPt = pt;
		}
		return res;
	}

	public static List<Double> evalEstimateDistLengthes(
			GesturePathesDef gestureDef, 
			ParamEvalCtx evalCtx) {
		List<Double> res = new ArrayList<>();
		for(val pathDef: gestureDef.pathes) {
			for(val pathElementDef : pathDef.pathElements) {
				double dist = evalEstimatePathElementDefDistLength(pathElementDef, evalCtx);
				res.add(dist);
			}
		}
		return res;
	}
	
	public static double evalEstimatePathElementDefDistLength(PathElementDef pathElementDef, ParamEvalCtx evalCtx) {
		return pathElementDef.accept(new PathElementDefFunc0<Double>() {
			@Override
			public Double caseSegmentDef(SegmentPathElementDef def) {
				val startPt = evalCtx.evalPtExpr(def.startPt);
				val endPt = evalCtx.evalPtExpr(def.endPt);
				return TracePt.dist(startPt, endPt);
			}
			@Override
			public Double caseDiscretePointsDef(DiscretePointsPathElementDef def) {
				List<PtExpr> ptExprs = def.ptExprs;
				int ptExprsCount = ptExprs.size();
				if (ptExprsCount <= 1) {
					return 0.0;
				}
				Pt2D prevPt = evalCtx.evalPtExpr(def.startPt);
				double res = 0;
				for (int i = 1; i < ptExprsCount; i++) {
					Pt2D pt = evalCtx.evalPtExpr(ptExprs.get(i));
					res += TracePt.dist(prevPt, pt);
					prevPt = pt;
				}
				return res;
			}
			@Override
			public Double caseQuadBezierDef(QuadBezierPathElementDef def) {
				val startPt = evalCtx.evalPtExpr(def.startPt);
				val controlPt = evalCtx.evalPtExpr(def.controlPt);				
				val endPt = evalCtx.evalPtExpr(def.endPt);
				double dist1 = TracePt.dist(startPt, controlPt);
				double dist2 = TracePt.dist(controlPt, endPt);
				return 0.5 * (dist1 + dist2); // TOCHECK estimate??
			}
			@Override
			public Double caseCubicBezierDef(CubicBezierPathElementDef def) {
				val startPt = evalCtx.evalPtExpr(def.startPt);
				val controlPt1 = evalCtx.evalPtExpr(def.controlPt1);				
				val controlPt2 = evalCtx.evalPtExpr(def.controlPt2);				
				val endPt = evalCtx.evalPtExpr(def.endPt);
				double dist1 = TracePt.dist(startPt, controlPt1);
				double dist2 = TracePt.dist(controlPt1, controlPt2);
				double dist3 = TracePt.dist(controlPt2, endPt);
				return 0.5 * (dist1 + dist2 + dist3); // TOCHECK estimate??
			}
		});
	}

}
