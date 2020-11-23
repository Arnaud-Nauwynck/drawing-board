package fr.an.drawingboard.recognizer.initialParamEstimators;

import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.model.trace.TraceGesturePathes;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.recognizer.shape.InitialParamForShapeEstimator;
import lombok.val;

public class StdInitialParamEstimators {

	public static InitialParamForShapeEstimator rectParamEstimator() {
		return (gestureDef, gesture, res) -> estimateRectInitialParamsFor(gestureDef, gesture, res);
	}

	public static void estimateRectInitialParamsFor( //
			TraceGesturePathes gestureDef,
			GesturePathesDef gesture,
			NumericExprEvalCtx res) {
		// ensure coefs per points are computed
		gestureDef.updatePtCoefs(); // useless?
		
		final double avgX, avgY;
		final double minX, maxX, minY, maxY;
		{ // compute avgX, avgY, minX, maxX, minY, maxY
			double sumX = 0, sumY = 0;
			double currMinX = Double.MAX_VALUE, currMaxX = Double.MIN_VALUE;
			double currMinY = Double.MAX_VALUE, currMaxY = Double.MIN_VALUE;
			for(val path : gestureDef.pathes) {
				for(val pathElement : path.pathElements) {
					switch (pathElement.getType()) {
					case Segment: {
						SegmentTracePathElement elt = (SegmentTracePathElement) pathElement;
						TracePt startPt = elt.startPt, endPt = elt.endPt;
						double coefStart = startPt.coefInPathes, coefEnd = endPt.coefInPathes; 
						sumX += coefStart * startPt.x;
						sumY += coefStart * startPt.y;
						sumX += coefEnd * endPt.x;
						sumY += coefEnd * endPt.y;

						currMinX = Math.min(currMinX, Math.min(startPt.x, endPt.x));
						currMaxX = Math.max(currMaxX, Math.max(startPt.x, endPt.x));
						currMinY = Math.min(currMinY, Math.min(startPt.y, endPt.y));
						currMaxY = Math.max(currMaxY, Math.max(startPt.y, endPt.y));
					} break;
					case DiscretePoints: {
						DiscretePointsTracePathElement elt = (DiscretePointsTracePathElement) pathElement;
						for (val pt : elt.tracePts) {
							double coef = pt.coefInPathes;
							sumX += coef * pt.x;
							sumY += coef * pt.y;

							currMinX = Math.min(currMinX, pt.x);
							currMaxX = Math.max(currMaxX, pt.x);
							currMinY = Math.min(currMinY, pt.y);
							currMaxY = Math.max(currMaxY, pt.y);
						}
					} break;
					case QuadBezier: {
						QuadBezierTracePathElement elt = (QuadBezierTracePathElement) pathElement;
						TracePt startPt = elt.startPt, endPt = elt.endPt;
						Pt2D controlPt = elt.controlPt;
						double coef = startPt.coefInPathes;
						sumX += coef * ( startPt.x + controlPt.x + endPt.x); 
						sumY += coef * ( startPt.y + controlPt.y + endPt.y); 

						currMinX = Math.min(currMinX, Math.min(startPt.x, endPt.x));
						currMaxX = Math.max(currMaxX, Math.max(startPt.x, endPt.x));
						currMinY = Math.min(currMinY, Math.min(startPt.y, endPt.y));
						currMaxY = Math.max(currMaxY, Math.max(startPt.y, endPt.y));
						// heuristic..
						currMinX = Math.min(currMinX, controlPt.x);
						currMaxX = Math.max(currMaxX, controlPt.x);
						currMinY = Math.min(currMinY, controlPt.y);
						currMaxY = Math.max(currMaxY, controlPt.y);
					} break;
					case CubicBezier: {
						CubicBezierTracePathElement elt = (CubicBezierTracePathElement) pathElement;
						TracePt startPt = elt.startPt, endPt = elt.endPt;
						Pt2D controlPt1 = elt.controlPt1, controlPt2 = elt.controlPt2;
						double coef = startPt.coefInPathes;
						sumX += coef * ( startPt.x + controlPt1.x + controlPt2.x + endPt.x); 
						sumY += coef * ( startPt.y + controlPt1.y + controlPt2.y + endPt.y); 

						currMinX = Math.min(currMinX, Math.min(startPt.x, endPt.x));
						currMaxX = Math.max(currMaxX, Math.max(startPt.x, endPt.x));
						currMinY = Math.min(currMinY, Math.min(startPt.y, endPt.y));
						currMaxY = Math.max(currMaxY, Math.max(startPt.y, endPt.y));
						// heuristic..
						currMinX = Math.min(currMinX, controlPt1.x);
						currMaxX = Math.max(currMaxX, controlPt1.x);
						currMinY = Math.min(currMinY, controlPt1.y);
						currMaxY = Math.max(currMaxY, controlPt1.y);

						currMinX = Math.min(currMinX, controlPt2.x);
						currMaxX = Math.max(currMaxX, controlPt2.x);
						currMinY = Math.min(currMinY, controlPt2.y);
						currMaxY = Math.max(currMaxY, controlPt2.y);
					} break;
					}
				}
			}
			avgX = sumX;
			avgY = sumY;
			minX = currMinX;
			maxX = currMaxX;
			minY = currMinY;
			maxY = currMaxY;
		}
		
		// compute momentum:  
		// VarX = Sum_i coef(pt_i) * (pt_i.x - avgX)^2
		// VarY = Sum_i coef(pt_i) * (pt_i.y - avgY)^2
		// CovXY = Sum_i coef(pt_i) * (pt_i.x - avgX) * (pt_i.y - avgY) 
		// TODO
		
		// finalize parameter estimations from computed values
//		double estimX = avgX;
//		double estimY = avgY;

		double estimX = 0.5 * (maxX + minX);
		double estimY = 0.5 * (maxY + minY);
		double estimW = maxX - minX;
		double estimH = maxY - minY;

		// fill in <code>res</code>
		ParamDef paramX = gesture.getParam("x");
		ParamDef paramY = gesture.getParam("y");
		ParamDef paramW = gesture.getParam("w");
		ParamDef paramH = gesture.getParam("h");
		res.putParamValue(paramX, estimX);
		res.putParamValue(paramY, estimY);
		res.putParamValue(paramW, estimW);
		res.putParamValue(paramH, estimH);
	}
	
}
