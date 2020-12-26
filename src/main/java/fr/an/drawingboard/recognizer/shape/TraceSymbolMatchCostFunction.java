package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.ctxeval.CompositePathElementsCtxEval;
import fr.an.drawingboard.model.shapedef.ctxeval.CompositePathElementsCtxEval.PtAtDistIterator;
import fr.an.drawingboard.model.shapedef.ctxeval.PathElementCtxEval;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.PathCtxEvalSymbol;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.TracePathSymbol;
import fr.an.drawingboard.recognizer.trace.TraceDiscretisationPtsBuilder;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import fr.an.drawingboard.util.LsUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class TraceSymbolMatchCostFunction {

	@Getter @Setter
	private TraceDiscretisationPtsBuilder discretisationPtBuilder = new TraceDiscretisationPtsBuilder();
	
	@Deprecated
	public double deletionCost(TracePathSymbol src, Pt2D target) {
		double cost = 0.0;
		for(val srcWeigthedPt : src.pts) {
			cost += srcWeigthedPt.weight * srcWeigthedPt.pt.squareDistTo(target);
		}
		return cost;
	}

	public double deleteMergeCost(TracePathSymbol src1, TracePathSymbol src2, PathCtxEvalSymbol target) {
		List<ParamWeightedPt2D> mergeSrcPts = unionPts(src1.pts, src2.pts);
		return cost(mergeSrcPts, target.elementsCtxEval);
	}

	private static List<ParamWeightedPt2D> unionPts(List<ParamWeightedPt2D> src1, List<ParamWeightedPt2D> src2) {
		val mergeSrcPts = LsUtils.union(LsUtils.map(src1, wp -> wp.pt), LsUtils.map(src2, wp -> wp.pt));
		return PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(mergeSrcPts);
	}


	@Deprecated
	public double insertionCost(Pt2D src, PathCtxEvalSymbol target) {
		double cost = 0.0;
		for(val srcWeigthedPt : target.pts) {
			cost += srcWeigthedPt.weight * srcWeigthedPt.pt.squareDistTo(src);
		}
		return cost;
	}

	public double insertionCost(TracePathSymbol src, PathCtxEvalSymbol target1, PathCtxEvalSymbol target2) {
		CompositePathElementsCtxEval mergeTarget = new CompositePathElementsCtxEval(target1.elementsCtxEval, target2.elementsCtxEval);
		return cost(src.pts, mergeTarget);
	}

	
	public double matchCost(TracePathSymbol src, PathCtxEvalSymbol target) {
		int targetEltCount = target.elementsCtxEval.getElementCount();
		List<TracePathElement> srcPathElts = src.tracePath.pathElements;
		if (targetEltCount == 0 || srcPathElts.size() == 0) {
			throw new IllegalArgumentException();
		}
//		if (targetEltCount != 1) {
//			System.out.println("targetEltCount: " + targetEltCount);
//		}
//		if (srcPathElts.size() != 1) {
//			System.out.println("srcEltCount: " + srcPathElts.size());
//		}
		PathElementCtxEval targetElt0 = target.elementsCtxEval.getElement(0);
		TracePathElement srcPathElt0 = srcPathElts.get(0);
		return cost(srcPathElt0, targetElt0);
	}

	public double cost(TracePathElement src, PathElementCtxEval target) {
		List<Pt2D> discretizeSrcPts = discretisationPtBuilder.discretizeToPts(src);
		List<ParamWeightedPt2D> wSrcPts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeSrcPts);
		return cost(wSrcPts, target);
	}

	public double cost(List<ParamWeightedPt2D> src, PathElementCtxEval target) {
		double cost = 0;
		Pt2D targetPtAtParam = new Pt2D();
		// double targetDist = target.getDist();
		for(val srcWPt: src) {
			double s = srcWPt.distRatio; // * targetDist;
			target.pointAtParam(targetPtAtParam, s);
			cost += srcWPt.weight * targetPtAtParam.squareDistTo(srcWPt.pt);
		}
		return cost;
	}

	public double cost(List<ParamWeightedPt2D> src, CompositePathElementsCtxEval target) {
		double cost = 0;
		PtAtDistIterator pointAtDistIterator = target.pointAtDistIterator();
		double targetTotalDist = target.getTotalDist();
		for(val srcWPt: src) {
			double targetDist = srcWPt.distRatio * targetTotalDist;
			val targetPt = pointAtDistIterator.nextPtAtDist(targetDist);
			cost += srcWPt.weight * srcWPt.pt.squareDistTo(targetPt.pt);
		}
		return cost;
	}

	
	// Expr cost
	// ------------------------------------------------------------------------

	/**
	 * same as polygonalDistCost(), with algebraic Expr on pathElementDef control points
	 */
	public Expr costExpr(TracePathElement tracePathElt, PathElementCtxEval pathEltCtxEval) {
		List<Pt2D> discretizeTracePts = discretisationPtBuilder.discretizeToPts(tracePathElt);
		List<ParamWeightedPt2D> paramWeightTracePts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeTracePts);
		List<Expr> cost = new ArrayList<>();
		ExprBuilder b = ExprBuilder.INSTANCE;
		for(val paramWeightTracePt: paramWeightTracePts) {
			double s = paramWeightTracePt.distRatio;
			PtExpr ptExpr = pathEltCtxEval.pointExprAtParam(s);
			Pt2D pt = paramWeightTracePt.pt;
			Expr dx = b.sum(-pt.x, ptExpr.x);
			Expr dy = b.sum(-pt.y, ptExpr.y);
			Expr squareDistExpr = b.sum(b.mult(dx, dx), b.mult(dy, dy));
			cost.add(b.mult(paramWeightTracePt.weight, squareDistExpr));
		}
		return b.sum(cost);
	}

	// Draw cost
	// ------------------------------------------------------------------------
	
	public void drawCost(GcRendererHelper gc, TracePathSymbol src, PathCtxEvalSymbol target) {
		int targetEltCount = target.elementsCtxEval.getElementCount();
//		if (targetEltCount != 1) {
//			return; //throw DrawingValidationUtils.notImplYet(); // TODO
//		}
//		PathElementCtxEval targetElt0 = target.elementsCtxEval.getElement(0);
//		List<TracePathElement> srcPathElts = src.tracePath.pathElements;
//		if (srcPathElts.size() != 1) {
//			return; // throw DrawingValidationUtils.notImplYet(); // TODO
//		}
//		TracePathElement srcPathElt0 = srcPathElts.get(0);
//		drawCost(gc, srcPathElt0, targetElt0);
		
		if (1 == targetEltCount) {
			PathElementCtxEval targetElt0 = target.elementsCtxEval.getElement(0);
			Pt2D targetPtAtParam = new Pt2D();
			for(val srcWPt: src.pts) {
				double s = srcWPt.distRatio; // * targetDist;
				targetElt0.pointAtParam(targetPtAtParam, s);
				gc.drawSegment(srcWPt.pt, targetPtAtParam);
			}
		} else {
			
		}
	}

	public void drawCost(GcRendererHelper gc, TracePathElement tracePathElt, PathElementCtxEval pathEltCtxEval) {
		List<Pt2D> discretizeTracePts = discretisationPtBuilder.discretizeToPts(tracePathElt);
		List<ParamWeightedPt2D> paramWeightTracePts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeTracePts);
		for(val paramWeightTracePt: paramWeightTracePts) {
			double s = paramWeightTracePt.distRatio;
			Pt2D pt = new Pt2D();
			pathEltCtxEval.pointAtParam(pt, s);
			gc.drawSegment(pt, paramWeightTracePt.pt);
		}
	}
	

}
