package fr.an.drawingboard.recognizer.initialParamEstimators;

import java.util.List;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.recognizer.trace.TraceDiscretisationPtsBuilder;
import lombok.val;

public class StdInitialParamEstimators {

	public static InitialParamForShapeEstimator lineParamEstimator() {
		return (gestureDef, gesture, res) -> estimateLineInitialParamsFor(gestureDef, gesture, res);
	}

	public static InitialParamForShapeEstimator line2ParamEstimator() {
		return (gestureDef, gesture, res) -> estimateLine2InitialParamsFor(gestureDef, gesture, res);
	}

	public static InitialParamForShapeEstimator rectParamEstimator() {
		return (gestureDef, gesture, res) -> estimateRectInitialParamsFor(gestureDef, gesture, res);
	}

	public static void estimateLineInitialParamsFor( //
			TraceGesture gesture,
			GesturePathesDef gestureDef,
			ParamEvalCtx res) {
		double estimX, estimY, estimW, estimH;
		if (! gesture.isEmpty()) {
			TracePath firstPath = gesture.get(0);
			List<TracePathElement> pathElements = firstPath.pathElements;
			if (pathElements != null && pathElements.size() > 0) {
				TracePathElement firstPathElt = pathElements.get(0);
				TracePt startPt = firstPathElt.startPt;
				TracePath lastPath = gesture.getLast();
				TracePathElement lastPathElt = lastPath.getLastPathElement();
				TracePt endPt = lastPathElt.endPt;
				
				estimX = 0.5 * (startPt.x + endPt.x);
				estimY = 0.5 * (startPt.y + endPt.y);
				estimW = endPt.x - startPt.x;
				estimH = endPt.y - startPt.y;
			} else {
				estimX = estimY = estimW = estimH = 0.0;
			}
		} else {
			estimX = estimY = estimW = estimH = 0.0;
		}
		// fill in <code>res</code>
		fillRectParam(res, gestureDef, estimX, estimY, estimW, estimH);
	}

	public static void estimateLine2InitialParamsFor( //
			TraceGesture gesture,
			GesturePathesDef gestureDef,
			ParamEvalCtx res) {
		estimateLineInitialParamsFor(gesture, gestureDef, res);
		// estimate mid point
		// find stop point if any, otherwise half distance (TODO)
		Pt2D controlPt;
		TracePath path0 = gesture.get(0);
		if (path0.pathElements.size() == 2) {
			controlPt = path0.pathElements.get(0).endPt.xy();
		} else {
			// TODO
			controlPt = path0.pathElements.get(0).endPt.xy();
		}
		// fill res ctx
		val ctrlPtX = gestureDef.getParam("ctrlPtX");
		val ctrlPtY = gestureDef.getParam("ctrlPtY");
		res.put(ctrlPtX, controlPt.x);
		res.put(ctrlPtY, controlPt.y);
	}
	
	private static void fillRectParam(
			ParamEvalCtx res, // 
			GesturePathesDef gestureDef, //  
			double estimX, double estimY, double estimW, double estimH) {
		val paramX = gestureDef.getParam("x");
		val paramY = gestureDef.getParam("y");
		val paramW = gestureDef.getParam("w");
		val paramH = gestureDef.getParam("h");
		res.put(paramX, estimX);
		res.put(paramY, estimY);
		res.put(paramW, estimW);
		res.put(paramH, estimH);
	}
	
	private static class AvgAndRectBuilder {
		Pt2D sum = new Pt2D();
		double sumCoef;
		BoundingRect2DBuilder boundingRect = new BoundingRect2DBuilder();
		public void addPt(double coef, Pt2D pt) {
			sum.x += coef * pt.x;
			sum.y += coef * pt.y;
			sumCoef += coef;
			boundingRect.enclosingPt(pt);
		}
		public void addPts(List<Pt2D> pts) {
			val wpts = PolygonalDistUtils.ptsToWeightedPts_polygonalDistance(pts);
			addWpts(wpts);
		}
		public void addWpts(List<WeightedPt2D> wpts) {
			for (val wpt : wpts) {
				addPt(wpt.weight, wpt.pt);
			}
		}
	}
	
	public static void estimateRectInitialParamsFor( //
			TraceGesture gesture,
			GesturePathesDef gestureDef,
			ParamEvalCtx res) {
		// ensure coefs per points are computed
		val discretization = new TraceDiscretisationPtsBuilder();
		val avg = new  AvgAndRectBuilder();
		{ // compute avgX, avgY, minX, maxX, minY, maxY
			for(val path : gesture.pathes()) {
				for(val pathElement : path.pathElements) {
					switch (pathElement.getType()) {
					case Segment: {
						SegmentTracePathElement elt = (SegmentTracePathElement) pathElement;
						avg.addPt(1.0, elt.startPt.xy());
						avg.addPt(1.0, elt.endPt.xy());
					} break;
					case DiscretePoints: {
						DiscretePointsTracePathElement elt = (DiscretePointsTracePathElement) pathElement;
						List<Pt2D> pts = discretization.discretizeToPts(elt);
						avg.addPts(pts);
					} break;
					case QuadBezier: {
						QuadBezierTracePathElement elt = (QuadBezierTracePathElement) pathElement;
						List<Pt2D> pts = discretization.discretizeToPts(elt);
						avg.addPts(pts);
					} break;
					case CubicBezier: {
						CubicBezierTracePathElement elt = (CubicBezierTracePathElement) pathElement;
						List<Pt2D> pts = discretization.discretizeToPts(elt);
						avg.addPts(pts);
					} break;
					}
				}
			}
		}
		// double avgX = avg.sum.x / avg.sumCoef;
		// double avgY = avg.sum.y / avg.sumCoef;
		BoundingRect2D rect = avg.boundingRect.build();
		double minX = rect.minx, maxX = rect.maxx, minY = rect.miny, maxY = rect.maxy;
		
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

		fillRectParam(res, gestureDef, estimX, estimY, estimW, estimH);
	}

}
