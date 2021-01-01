package fr.an.drawingboard.recognizer.shape;

import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils.TotalDistAndWeigthPts;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.obj.CompositePathElementsObj;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.PathObjSymbol;
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
		double squareDist = src.distAndWPts.totalDist * src.distAndWPts.totalDist;
		for(val srcWeigthedPt : src.distAndWPts.pts) {
			cost += srcWeigthedPt.weight * squareDist * srcWeigthedPt.pt.squareDistTo(target);
		}
		return cost;
	}

	public double deleteMergeCost(TracePathSymbol src1, TracePathSymbol src2, PathObjSymbol target) {
		val mergeDistAndPts = unionPts(src1.distAndWPts.pts, src2.distAndWPts.pts);
		return cost(mergeDistAndPts, target.elementsObj);
	}

	private static TotalDistAndWeigthPts unionPts(List<ParamWeightedPt2D> src1, List<ParamWeightedPt2D> src2) {
		val mergeSrcPts = LsUtils.union(LsUtils.map(src1, wp -> wp.pt), LsUtils.map(src2, wp -> wp.pt));
		return PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(mergeSrcPts);
	}


	@Deprecated
	public double insertionCost(Pt2D src, PathObjSymbol target) {
		double cost = 0.0;
		double targetDist = target.elementsObj.getDist();
		for(val srcWeigthedPt : target.pts) {
			cost += srcWeigthedPt.weight * targetDist * targetDist * srcWeigthedPt.pt.squareDistTo(src);
		}
		return cost;
	}

	
	public double matchCost(TracePathSymbol src, PathObjSymbol target) {
		int targetEltCount = target.elementsObj.getElementCount();
		List<TracePathElement> srcPathElts = src.tracePath.pathElements;
		if (targetEltCount == 0 || srcPathElts.size() == 0) {
			throw new IllegalArgumentException();
		}
		if (targetEltCount != 1) {
			System.out.println("TODO targetEltCount: " + targetEltCount);
		}
		if (srcPathElts.size() != 1) {
			System.out.println("TODO srcEltCount: " + srcPathElts.size());
		}
		PathElementObj targetElt0 = target.elementsObj.getElement(0);
		TracePathElement srcPathElt0 = srcPathElts.get(0);
		return cost(srcPathElt0, targetElt0);
	}

	public double cost(TracePathElement src, PathElementObj target) {
		List<Pt2D> discretizeSrcPts = discretisationPtBuilder.discretizeToPts(src);
		val wSrcPts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeSrcPts);
		return cost(wSrcPts, target);
	}

	public double cost(TotalDistAndWeigthPts src, PathElementObj target) {
		double cost = 0;
		Pt2D targetPtAtParam = new Pt2D();
		double squareDist = src.totalDist * src.totalDist;
		for(val srcWPt: src.pts) {
			double s = srcWPt.distRatio;
			target.pointAtParam(targetPtAtParam, s);
			cost += srcWPt.weight * squareDist *
					targetPtAtParam.squareDistTo(srcWPt.pt);
		}
		return cost;
	}

	public double cost(Pt2D src, CompositePathElementsObj target) {
		double cost = 0.0;
		double targetDist = target.getDist();
		val targetWPts = target.discretizeWeigthedPts();
		for(val targetWPt : targetWPts) {
			cost += targetWPt.weight * targetDist * targetDist * src.squareDistTo(targetWPt.pt);
		}
		return cost;
	}


	public double cost(TotalDistAndWeigthPts src, CompositePathElementsObj target) {
		double cost = 0;
		val pointAtParamIterator = target.pointAtParamIterator();
		double squareDist = src.totalDist * src.totalDist;
		for(val srcWPt: src.pts) {
			double targetParam = srcWPt.distRatio;
			val targetPt = pointAtParamIterator.nextPtAtParam(targetParam);
			cost += srcWPt.weight * squareDist *
					srcWPt.pt.squareDistTo(targetPt.pt);
		}
		return cost;
	}
	
	// Expr cost
	// ------------------------------------------------------------------------

	public void costExprs(List<Expr> res, TracePathSymbol src, PtExpr target) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double squareDist = src.distAndWPts.totalDist * src.distAndWPts.totalDist;
		for(val srcWPt : src.distAndWPts.pts) {
			res.add(b.mult(srcWPt.weight * squareDist, squareDistExpr(srcWPt.pt, target)));
		}
	}

	public void costExprs(List<Expr> res, TracePathSymbol src1, TracePathSymbol src2, PathObjSymbol target) {
		val mergeDistAndPts = unionPts(src1.distAndWPts.pts, src2.distAndWPts.pts);
		costExprs(res, mergeDistAndPts, target.elementsObj);
	}

	public void costExprs(List<Expr> res, Pt2D src, PathObjSymbol target) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double targetDist = target.elementsObj.getDist();
		double squareDist = targetDist * targetDist;
		val targetPtAtParamIter = target.elementsObj.pointExprAtParamIterator();
		for(val targetWPt : target.pts) {
			double s = targetWPt.distRatio;
			PtExpr targetPtExpr = targetPtAtParamIter.nextPtAtParam(s).pt;
			res.add(b.mult(targetWPt.weight * squareDist, squareDistExpr(src, targetPtExpr)));
		}
	}
	
	public void costExprs(List<Expr> res, TracePathSymbol src, PathObjSymbol target) {
		int targetEltCount = target.elementsObj.getElementCount();
		List<TracePathElement> srcPathElts = src.tracePath.pathElements;
		if (targetEltCount == 0 || srcPathElts.size() == 0) {
			throw new IllegalArgumentException();
		}
		if (targetEltCount != 1) {
			System.out.println("todo.. cost(..) target.length!=1");
		}
		if (srcPathElts.size() != 1) {
			System.out.println("todo.. cost(..) src.length!=1");
		}
		PathElementObj targetElt0 = target.elementsObj.getElement(0);
		TracePathElement srcPathElt0 = srcPathElts.get(0);
		costExprs(res, srcPathElt0, targetElt0);
	}

	public void costExprs(List<Expr> res, TracePathElement src, PathElementObj target) {
		List<Pt2D> discretizeSrcPts = discretisationPtBuilder.discretizeToPts(src);
		val wSrcPts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeSrcPts);
		costExprs(res, wSrcPts, target);
	}

	public void costExprs(List<Expr> res, TotalDistAndWeigthPts src, PathElementObj target) {
		double squareDist = src.totalDist * src.totalDist;
		ExprBuilder b = ExprBuilder.INSTANCE;
		for(val srcPt : src.pts) {
			Pt2D pt = srcPt.pt;
			double s = srcPt.distRatio;
			PtExpr ptExpr = target.pointExprAtParam(s);
			res.add(b.mult(srcPt.weight * squareDist, squareDistExpr(pt, ptExpr)));
		}
	}

	public void costExprs(List<Expr> res, Pt2D src, CompositePathElementsObj target) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double targetDist = target.getDist();
		double squareDist = targetDist * targetDist;
		val targetWPts = target.discretizeWeigthedPts();
		val targetPtAtParamIter = target.pointExprAtParamIterator();
		for(val targetWPt : targetWPts) {
			double s = targetWPt.distRatio;
			PtExpr targetPtExpr = targetPtAtParamIter.nextPtAtParam(s).pt;
			res.add(b.mult(targetWPt.weight * squareDist, squareDistExpr(src, targetPtExpr)));
		}
	}

	public void costExprs(List<Expr> res, TotalDistAndWeigthPts src, CompositePathElementsObj target) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		val targetPtAtParamIter = target.pointExprAtParamIterator();
		double squareDist = src.totalDist * src.totalDist;
		for(val srcWPt: src.pts) {
			double s = srcWPt.distRatio;
			PtExpr targetPtExpr = targetPtAtParamIter.nextPtAtParam(s).pt;
			res.add(b.mult(srcWPt.weight * squareDist, squareDistExpr(srcWPt.pt, targetPtExpr)));
		}
	}

	private static Expr squareDistExpr(Pt2D pt, PtExpr ptExpr) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		Expr dx = b.sum(-pt.x, ptExpr.x);
		Expr dy = b.sum(-pt.y, ptExpr.y);
		Expr res = b.sum(b.mult(dx, dx), b.mult(dy, dy));
		return res;
	}

	// Draw cost
	// ------------------------------------------------------------------------
	
	public void drawCost(GcRendererHelper gc, TracePathSymbol src, PathObjSymbol target) {
		int targetEltCount = target.elementsObj.getElementCount();
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
			PathElementObj targetElt0 = target.elementsObj.getElement(0);
			Pt2D targetPtAtParam = new Pt2D();
			for(val srcWPt: src.distAndWPts.pts) {
				double s = srcWPt.distRatio; // * targetDist;
				targetElt0.pointAtParam(targetPtAtParam, s);
				gc.drawSegment(srcWPt.pt, targetPtAtParam);
			}
		} else {
			
		}
	}

	public void drawCost(GcRendererHelper gc, TracePathElement tracePathElt, PathElementObj pathEltCtxEval) {
		List<Pt2D> discretizeTracePts = discretisationPtBuilder.discretizeToPts(tracePathElt);
		val paramWeightTracePts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeTracePts);
		for(val paramWeightTracePt: paramWeightTracePts.pts) {
			double s = paramWeightTracePt.distRatio;
			Pt2D pt = new Pt2D();
			pathEltCtxEval.pointAtParam(pt, s);
			gc.drawSegment(pt, paramWeightTracePt.pt);
		}
	}

	public void drawCost(GcRendererHelper gcRenderer, List<ParamWeightedPt2D> src, CompositePathElementsObj target) {
		val pointAtParamIterator = target.pointAtParamIterator();
		for(val srcWPt: src) {
			double targetParam = srcWPt.distRatio;
			val targetPt = pointAtParamIterator.nextPtAtParam(targetParam);
			gcRenderer.drawSegment(srcWPt.pt, targetPt.pt);
		}
	}

	@Deprecated
	public void drawCost(GcRendererHelper gcRenderer, TracePathSymbol src, Pt2D target) {
		drawCost(gcRenderer, src.distAndWPts.pts, target);
	}

	public void drawCost(GcRendererHelper gcRenderer, List<ParamWeightedPt2D> srcWPts, Pt2D target) {
		for(val srcWPt : srcWPts) {
			gcRenderer.drawSegment(srcWPt.pt, target);
		}
	}

	@Deprecated
	public void drawCost(GcRendererHelper gcRenderer, Pt2D src, PathObjSymbol target) {
		drawCost(gcRenderer, src, target.pts); 
	}

	public void drawCost(GcRendererHelper gcRenderer, Pt2D src, List<ParamWeightedPt2D> targetWPts) {
		for(val targetWPt : targetWPts) {
			gcRenderer.drawSegment(src, targetWPt.pt);
		}
	}

	public void drawCost(GcRendererHelper gcRenderer, Pt2D src, CompositePathElementsObj target) {
		val targetWPts = target.discretizeWeigthedPts();
		for(val targetWPt : targetWPts) {
			gcRenderer.drawSegment(src, targetWPt.pt);
		}
	}

}
