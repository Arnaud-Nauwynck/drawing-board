package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.StrokeDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef;
import fr.an.drawingboard.model.stroke2shape.DiscreteTimesToAbsciss;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceStroke;
import fr.an.drawingboard.model.trace.TraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.CubicBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.QuadBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElementVisitor2;
import fr.an.drawingboard.util.DrawingValidationUtils;

public class StrokeDefToCostExprBuilder {

	public Expr costStrokeToStrokeDefWithAbsciss(
			TraceStroke stroke,
			StrokeDef strokeDef, 
			DiscreteTimesToAbsciss indexToAbsciss
			) {
		int pathDefCount = strokeDef.pathElements.size();
		List<Expr> pathCostExprs = new ArrayList<>(pathDefCount);
		int pathCount = stroke.pathElements.size();
		if (pathDefCount == pathCount) {
			int startPtIndex = 0;
			for(int pathIndex = 0; pathIndex < pathCount; pathIndex++) {
				TraceStrokePathElement pathElement = stroke.pathElements.get(pathIndex);
				StrokePathElementDef pathElementDef = strokeDef.pathElements.get(pathIndex);

				Expr pathCostExpr = costPathEltToPathEltDefWithAbsciss(pathElement, pathElementDef, indexToAbsciss, startPtIndex);
				pathCostExprs.add(pathCostExpr);
				startPtIndex += DiscreteTimesToAbsciss.countPathElementPoint(pathElement);
			}
			
		} else {
			throw new UnsupportedOperationException("not impl yet..");
		}

		return new SumExpr(pathCostExprs);
	}


	public Expr costPathEltToPathEltDefWithAbsciss(
			TraceStrokePathElement pathElement,
			StrokePathElementDef pathElementDef, 
			DiscreteTimesToAbsciss indexToAbsciss,
			int startPtIndex
			) {
		return (Expr) pathElement.visit(new TraceStrokePathElementVisitor2<Expr,Void>() {
			@Override
			public Expr caseSegment(SegmentTraceStrokePathElement elt, Void p) {
				return costPathEltToPathEltDefWithAbsciss_Segment(elt, pathElementDef, indexToAbsciss, startPtIndex);
			}
			@Override
			public Expr caseDiscretePts(DiscretePointsTraceStrokePathElement elt, Void p) {
				return costPathEltToPathEltDefWithAbsciss_DiscretePts(elt, pathElementDef, indexToAbsciss, startPtIndex);
			}
			@Override
			public Expr caseQuadBezier(QuadBezierTraceStrokePathElement elt, Void p) {
				return costPathEltToPathEltDefWithAbsciss_QuadBezier(elt, pathElementDef, indexToAbsciss, startPtIndex);
			}
			@Override
			public Expr caseCubicBezier(CubicBezierTraceStrokePathElement elt, Void p) {
				return costPathEltToPathEltDefWithAbsciss_CubicBezier(elt, pathElementDef, indexToAbsciss, startPtIndex);
			}
		}, null);
	}


	public Expr costPathEltToPathEltDefWithAbsciss_Segment( //
			SegmentTraceStrokePathElement pathElement, //
			StrokePathElementDef pathElementDef, //
			DiscreteTimesToAbsciss indexToAbsciss, int startPtIndex
			) {
		TracePt pathStartPt = pathElement.startPt, pathEndPt = pathElement.endPt;
		PtExpr startPtExpr = pathElementDef.startPt, endPtExpr = pathElementDef.endPt;
		ExprBuilder b = ExprBuilder.INSTANCE;
		Expr squareDistStart = b.squareDist(startPtExpr, pathStartPt);
		Expr squareDistEnd = b.squareDist(endPtExpr, pathEndPt);
		return b.sum(squareDistStart, squareDistEnd);
	}
	
	public Expr costPathEltToPathEltDefWithAbsciss_DiscretePts( //
			DiscretePointsTraceStrokePathElement elt, //
			StrokePathElementDef pathElementDef, // 
			DiscreteTimesToAbsciss indexToAbsciss, int startPtIndex) {
		return costListTracePtsToPathEltDefWithAbsciss(elt.tracePts, pathElementDef, indexToAbsciss, startPtIndex);
	}


	private Expr costListTracePtsToPathEltDefWithAbsciss( //
			List<TracePt> tracePts, //
			StrokePathElementDef pathElementDef, //
			DiscreteTimesToAbsciss indexToAbsciss, int startPtIndex) {
		final int ptsCount = tracePts.size();
		List<Expr> ptCostExprs = new ArrayList<>(ptsCount);
		
		// sum of cost per point, using coefficient per pt
		double[] abscissTracePt = new double[ptsCount];
		double totalSumDist;
		{ // compute abscissSumDistPt[i] = absciss of pt = sum dist
			abscissTracePt[0] = 0.0;
			TracePt prevPt = tracePts.get(0);
			double prevSumDist = 0.0;
			for(int i = 1; i < ptsCount; i++) {
				TracePt pt = tracePts.get(i);
				double dist = TracePt.dist(prevPt, pt);
				prevSumDist += dist;
				abscissTracePt[i] = prevSumDist; 
				prevPt = pt;
			}
			totalSumDist = prevSumDist;
		}
		
		double[] ptCoefs = new double[ptsCount];
		{ // compute ptCoefs[i] = coef for pt i = renormDist * dist 1/2 [ (pt_i-1 - pt_i) + (pt_i - pt_i+1) ] 
			double renormDist = 1.0 / ((totalSumDist != 0)? totalSumDist : 1);
			ptCoefs[0] = renormDist * abscissTracePt[1];
			for(int i = 1; i < ptsCount-1; i++) {
				// double distBeforePt = abscissTracePt[i] - abscissTracePt[i-1]; 
				// double distAfterPt = abscissTracePt[i+1] - abscissTracePt[i];
				double avgDistPt_ip1_im1 = 0.5 * (abscissTracePt[i+1] - abscissTracePt[i-1]);
				ptCoefs[i] = renormDist * avgDistPt_ip1_im1;
			}
			ptCoefs[ptsCount-1] = renormDist * (abscissTracePt[ptsCount-1] - abscissTracePt[ptsCount-2]);
		}
		
		final double startAbsciss = indexToAbsciss.values[startPtIndex];
		ExprBuilder b = ExprBuilder.INSTANCE;
		for(int i = 0; i < ptsCount; i++) {
			TracePt tracePt = tracePts.get(i);
			int ptIndex = startPtIndex + i;
			double relAbsciss = indexToAbsciss.values[ptIndex] - startAbsciss;
			
			PtExpr ptiExpr = pathElementDef.ptExprAtAbsciss(relAbsciss);
			
			// quad distance: tracePts[i] - ptiExpr
			Expr distX = b.minus(ptiExpr.x, tracePt.x);
			Expr distY = b.minus(ptiExpr.y, tracePt.y);
			Expr squareDistPti = b.sumSquare(distX, distY);
			
			Expr renormSquareDistPti = b.mult(ptCoefs[i], squareDistPti);
			
			ptCostExprs.add(renormSquareDistPti);
		}
		return new SumExpr(ptCostExprs);
	}

	public Expr costPathEltToPathEltDefWithAbsciss_QuadBezier( //
			QuadBezierTraceStrokePathElement elt, //
			StrokePathElementDef pathElementDef, //
			DiscreteTimesToAbsciss indexToAbsciss, int startPtIndex) {
		// discretize trace quad bezier to N points... then fall costListTracePtsToPathEltDefWithAbsciss(tracePts, ..)
		throw DrawingValidationUtils.notImplYet();
	}

	public Expr costPathEltToPathEltDefWithAbsciss_CubicBezier( //
			CubicBezierTraceStrokePathElement elt, //
			StrokePathElementDef pathElementDef, //
			DiscreteTimesToAbsciss indexToAbsciss, int startPtIndex) {
		// discretize trace quad bezier to N points... then fall costListTracePtsToPathEltDefWithAbsciss(tracePts, ..)
		throw DrawingValidationUtils.notImplYet();
	}

}
