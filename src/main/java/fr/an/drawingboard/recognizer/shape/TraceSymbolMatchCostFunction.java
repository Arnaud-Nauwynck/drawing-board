package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.ctxeval.PathElementCtxEval;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.PathCtxEvalSymbol;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.TracePathSymbol;
import fr.an.drawingboard.recognizer.trace.TraceDiscretisationPtsBuilder;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.canvas.GraphicsContext;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class TraceSymbolMatchCostFunction {

	@Getter @Setter
	private TraceDiscretisationPtsBuilder discretisationPtBuilder = new TraceDiscretisationPtsBuilder();
	
	
	public double deletionCost(TracePathSymbol src, Pt2D target) {
		double cost = 0.0;
		for(val srcWeigthedPt : src.pts) {
			cost += srcWeigthedPt.weight * srcWeigthedPt.pt.squareDistTo(target);
		}
		return cost;
	}

	public double insertionCost(Pt2D src, PathCtxEvalSymbol target) {
		double cost = 0.0;
		for(val srcWeigthedPt : target.pts) {
			cost += srcWeigthedPt.weight * srcWeigthedPt.pt.squareDistTo(src);
		}
		return cost;
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

	public double cost(TracePathElement tracePathElt, PathElementCtxEval pathEltCtxEval) {
		List<Pt2D> discretizeTracePts = discretisationPtBuilder.discretizeToPts(tracePathElt);
		List<ParamWeightedPt2D> paramWeightTracePts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizeTracePts);
		double cost = 0;
		for(val paramWeightTracePt: paramWeightTracePts) {
			double s = paramWeightTracePt.distRatio;
			Pt2D pt = new Pt2D();
			pathEltCtxEval.pointAtParam(pt, s);
			cost += paramWeightTracePt.weight * pt.squareDistTo(paramWeightTracePt.pt);
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
			Expr dx = b.sum(pt.x, ptExpr.x);
			Expr dy = b.sum(pt.y, ptExpr.y);
			Expr squareDistExpr = b.sum(b.mult(dx, dx), b.mult(dy, dy));
			cost.add(b.mult(paramWeightTracePt.weight, squareDistExpr));
		}
		return b.sum(cost);
	}

	// Draw cost
	// ------------------------------------------------------------------------
	
	public void drawCost(GcRendererHelper gc, TracePathSymbol src, PathCtxEvalSymbol target) {
		int targetEltCount = target.elementsCtxEval.getElementCount();
		if (targetEltCount != 1) {
			return; //throw DrawingValidationUtils.notImplYet(); // TODO
		}
		PathElementCtxEval targetElt0 = target.elementsCtxEval.getElement(0);
		List<TracePathElement> srcPathElts = src.tracePath.pathElements;
		if (srcPathElts.size() != 1) {
			return; // throw DrawingValidationUtils.notImplYet(); // TODO
		}
		TracePathElement srcPathElt0 = srcPathElts.get(0);
		drawCost(gc, srcPathElt0, targetElt0);
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
