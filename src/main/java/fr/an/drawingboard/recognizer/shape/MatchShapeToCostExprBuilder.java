package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementVisitorFunc0;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace2shape.GesturePtToAbscissMatch;
import fr.an.drawingboard.util.DrawingValidationUtils;

public class MatchShapeToCostExprBuilder {

//	public Expr costMatchGestureWithAbsciss(
//			TraceGesture gesture,
//			GesturePathesDef gestureDef, 
//			GesturePtToAbscissMatch ptToAbscissMatch
//			) {
//		// simple case: matching exactly one by one
//		// should implement more advanced mapping of split fragment / merge !!
//		// path 1 <-> pathDef 1
//		// path 2 <-> pathDef 2
//		// ..
//		// path count <-> pathDef count
//		int pathCount = gesture.pathes.size();
//		int pathDefCount = gestureDef.pathes.size();
//		List<Expr> pathCostExprs = new ArrayList<>(pathDefCount);
//		if (pathDefCount == pathCount) {
//			for(int pathIndex = 0; pathIndex < pathCount; pathIndex++) {
//				TracePath path = gesture.pathes.get(pathIndex);
//				PathDef pathDef = gestureDef.pathes.get(pathIndex);
//
//				Expr pathCostExpr = costMatchPathWithAbsciss(path, pathDef, ptToAbscissMatch);
//				pathCostExprs.add(pathCostExpr);
//			}
//		} else {
//			throw new UnsupportedOperationException("not impl yet..");
//		}
//
//		return new SumExpr(pathCostExprs);
//	}
//	
//	public Expr costMatchPathWithAbsciss(
//			TracePath path,
//			PathDef pathDef, 
//			GesturePtToAbscissMatch indexToAbsciss
//			) {
//		int pathEltDefCount = pathDef.pathElements.size();
//		List<Expr> pathEltCostExprs = new ArrayList<>(pathEltDefCount);
//		int pathEltCount = path.pathElements.size();
//		// simple case: matching exactly one by one
//		// should implement more advanced mapping of split fragment / merge !!
//		// pathElt 1 <-> pathElementDef 1
//		// pathElt 2 <-> pathElementDef 2
//		// .. 
//		// pathElt ncount <-> pathElementDef count
//		if (pathEltDefCount == pathEltCount) {
//			int startPtIndex = 0;
//			for(int pathIndex = 0; pathIndex < pathEltCount; pathIndex++) {
//				TracePathElement pathElement = path.pathElements.get(pathIndex);
//				PathElementDef pathElementDef = pathDef.pathElements.get(pathIndex);
//
//				Expr pathCostExpr = costPathEltToPathEltDefWithAbsciss(pathElement, pathElementDef, 
//						indexToAbsciss, startPtIndex);
//				pathEltCostExprs.add(pathCostExpr);
//				startPtIndex += GesturePtToAbscissMatch.countPathElementPoint(pathElement);
//			}
//			
//		} else {
//			throw new UnsupportedOperationException("not impl yet..");
//		}
//
//		return new SumExpr(pathEltCostExprs);
//	}
//
//
//	public Expr costPathEltToPathEltDefWithAbsciss(
//			TracePathElement pathElement,
//			PathElementDef pathElementDef, 
//			GesturePtToAbscissMatch indexToAbsciss,
//			int startPtIndex
//			) {
//		return pathElement.visit(new TracePathElementVisitorFunc0<Expr>() {
//			@Override
//			public Expr caseSegment(SegmentTracePathElement elt) {
//				return costMatchPathEltWithAbsciss_Segment(elt, pathElementDef, indexToAbsciss, startPtIndex);
//			}
//			@Override
//			public Expr caseDiscretePts(DiscretePointsTracePathElement elt) {
//				return costMatchPathEltWithAbsciss_DiscretePts(elt, pathElementDef, indexToAbsciss, startPtIndex);
//			}
//			@Override
//			public Expr caseQuadBezier(QuadBezierTracePathElement elt) {
//				return costMatchPathEltWithAbsciss_QuadBezier(elt, pathElementDef, indexToAbsciss, startPtIndex);
//			}
//			@Override
//			public Expr caseCubicBezier(CubicBezierTracePathElement elt) {
//				return costMatchPathEltWithAbsciss_CubicBezier(elt, pathElementDef, indexToAbsciss, startPtIndex);
//			}
//		});
//	}
//
//
//	public Expr costMatchPathEltWithAbsciss_Segment( //
//			SegmentTracePathElement pathElement, //
//			PathElementDef pathElementDef, //
//			GesturePtToAbscissMatch indexToAbsciss, int startPtIndex
//			) {
//		TracePt pathStartPt = pathElement.startPt, pathEndPt = pathElement.endPt;
//		PtExpr startPtExpr = pathElementDef.startPt, endPtExpr = pathElementDef.endPt;
//		ExprBuilder b = ExprBuilder.INSTANCE;
//		Expr squareDistStart = b.squareDist(startPtExpr, pathStartPt);
//		Expr squareDistEnd = b.squareDist(endPtExpr, pathEndPt);
//		return b.sum(squareDistStart, squareDistEnd);
//	}
//	
//	public Expr costMatchPathEltWithAbsciss_DiscretePts( //
//			DiscretePointsTracePathElement elt, //
//			PathElementDef pathElementDef, // 
//			GesturePtToAbscissMatch indexToAbsciss, int startPtIndex) {
//		return costMatchListTracePtsWithAbsciss(elt.tracePts, pathElementDef, indexToAbsciss, startPtIndex);
//	}
//
//
//	private Expr costMatchListTracePtsWithAbsciss( //
//			List<TracePt> tracePts, //
//			PathElementDef pathElementDef, //
//			GesturePtToAbscissMatch indexToAbsciss, int startPtIndex) {
//		final int ptsCount = tracePts.size();
//		List<Expr> ptCostExprs = new ArrayList<>(ptsCount);
//		
//		// sum of cost per point, using coefficient per pt
//		double[] abscissTracePt = new double[ptsCount];
//		double totalSumDist;
//		{ // compute abscissSumDistPt[i] = absciss of pt = sum dist
//			abscissTracePt[0] = 0.0;
//			TracePt prevPt = tracePts.get(0);
//			double prevSumDist = 0.0;
//			for(int i = 1; i < ptsCount; i++) {
//				TracePt pt = tracePts.get(i);
//				double dist = TracePt.dist(prevPt, pt);
//				prevSumDist += dist;
//				abscissTracePt[i] = prevSumDist; 
//				prevPt = pt;
//			}
//			totalSumDist = prevSumDist;
//		}
//		
//		double[] ptCoefs = new double[ptsCount];
//		{ // compute ptCoefs[i] = coef for pt i = renormDist * dist 1/2 [ (pt_i-1 - pt_i) + (pt_i - pt_i+1) ] 
//			double renormDist = 1.0 / ((totalSumDist != 0)? totalSumDist : 1);
//			ptCoefs[0] = renormDist * abscissTracePt[1];
//			for(int i = 1; i < ptsCount-1; i++) {
//				// double distBeforePt = abscissTracePt[i] - abscissTracePt[i-1]; 
//				// double distAfterPt = abscissTracePt[i+1] - abscissTracePt[i];
//				double avgDistPt_ip1_im1 = 0.5 * (abscissTracePt[i+1] - abscissTracePt[i-1]);
//				ptCoefs[i] = renormDist * avgDistPt_ip1_im1;
//			}
//			ptCoefs[ptsCount-1] = renormDist * (abscissTracePt[ptsCount-1] - abscissTracePt[ptsCount-2]);
//		}
//		
//		final double startAbsciss = indexToAbsciss.ptToPathAbsciss[startPtIndex];
//		ExprBuilder b = ExprBuilder.INSTANCE;
//		for(int i = 0; i < ptsCount; i++) {
//			TracePt tracePt = tracePts.get(i);
//			int ptIndex = startPtIndex + i;
//			double relAbsciss = indexToAbsciss.ptToPathAbsciss[ptIndex] - startAbsciss;
//			
//			PtExpr ptiExpr = pathElementDef.ptExprAtAbsciss(relAbsciss);
//			
//			// quad distance: tracePts[i] - ptiExpr
//			Expr distX = b.minus(ptiExpr.x, tracePt.x);
//			Expr distY = b.minus(ptiExpr.y, tracePt.y);
//			Expr squareDistPti = b.sumSquare(distX, distY);
//			
//			Expr renormSquareDistPti = b.mult(ptCoefs[i], squareDistPti);
//			
//			ptCostExprs.add(renormSquareDistPti);
//		}
//		return new SumExpr(ptCostExprs);
//	}
//
//	public Expr costMatchPathEltWithAbsciss_QuadBezier( //
//			QuadBezierTracePathElement elt, //
//			PathElementDef pathElementDef, //
//			GesturePtToAbscissMatch indexToAbsciss, int startPtIndex) {
//		// discretize trace quad bezier to N points... then fall costListTracePtsToPathEltDefWithAbsciss(tracePts, ..)
//		throw DrawingValidationUtils.notImplYet();
//	}
//
//	public Expr costMatchPathEltWithAbsciss_CubicBezier( //
//			CubicBezierTracePathElement elt, //
//			PathElementDef pathElementDef, //
//			GesturePtToAbscissMatch indexToAbsciss, int startPtIndex) {
//		// discretize trace quad bezier to N points... then fall costListTracePtsToPathEltDefWithAbsciss(tracePts, ..)
//		throw DrawingValidationUtils.notImplYet();
//	}

}
