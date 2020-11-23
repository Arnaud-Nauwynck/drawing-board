package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.trace.TraceStrokePathElement.CubicBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.QuadBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;
import javafx.scene.paint.Color;
import lombok.val;

public class TraceMultiStroke {

	public Color color;
	public int lineWidth;
	
	public List<TraceStroke> strokes = new ArrayList<>();
	
	public void removeLastStroke() {
		if (! strokes.isEmpty()) {
			strokes.remove(strokes.size() - 1);
		}
	}

	public boolean isEmpty() {
		return strokes.isEmpty();
	}
	
	public TraceStroke getLast() {
		return (strokes.isEmpty())? null : strokes.get(strokes.size() - 1);
	}
	
	public TraceStroke appendNewStroke() {
		val res = new TraceStroke();
		strokes.add(res);
		return res;
	}
	

	public List<Double> strokeDistLengths() {
		List<Double> res = new ArrayList<>(strokes.size());
		for(val stroke : strokes) {
			res.add(stroke.pathDistLength());
		}
		return res;
	}

	

	public static double sum(List<Double> values) {
		double res = 0.0;
		for(val d : values) {
			res += d;
		}
		return res;
	}

	public void updatePtCoefs() {
		List<Double> strokeDistLengths = strokeDistLengths();
		double distLengthTotal = sum(strokeDistLengths);
		double renormCoefTotal = 1.0 / distLengthTotal;
		final int strokesCount = strokes.size();
		for (int strokei = 0; strokei < strokesCount; strokei++) {
			TraceStroke stroke = strokes.get(strokei);
			double strokeDistLength = strokeDistLengths.get(strokei);
			double renormStroke = strokeDistLength * renormCoefTotal;

			for (val pathElement : stroke.pathElements) {
				switch (pathElement.getType()) {
				case Segment: {
					SegmentTraceStrokePathElement elt = (SegmentTraceStrokePathElement) pathElement;
					elt.startPt.coefInMultiStroke = 0.5 * renormStroke;
					elt.endPt.coefInMultiStroke = 0.5 * renormStroke;
				} break;
				case DiscretePoints: {
					DiscretePointsTraceStrokePathElement elt = (DiscretePointsTraceStrokePathElement) pathElement;
					List<TracePt> tracePts = elt.tracePts;
					final int ptsCount = tracePts.size();
					if (ptsCount > 1) {
						double eltDistLength = elt.pathDistLength();
						double renormStrokePts = renormStroke / eltDistLength;
						double firstPtDist = tracePts.get(1).strokeCurveAbsciss - tracePts.get(0).strokeCurveAbsciss;
						tracePts.get(0).coefInMultiStroke = renormStrokePts * firstPtDist;
						for (int pti = 1; pti < ptsCount - 1; pti++) {
							// double distBeforePt = abscissTracePt[pti] - abscissTracePt[pti-1];
							// double distAfterPt = abscissTracePt[pti+1] - abscissTracePt[pti];
							TracePt ptBefore = tracePts.get(pti - 1);
							TracePt ptAfter = tracePts.get(pti + 1);
							double avgDistPt_ip1_im1 = 0.5 * (ptAfter.strokeCurveAbsciss - ptBefore.strokeCurveAbsciss);
							tracePts.get(pti).coefInMultiStroke = avgDistPt_ip1_im1 * renormStroke;
						}
						double lastPtDist = tracePts.get(ptsCount - 1).strokeCurveAbsciss
								- tracePts.get(ptsCount - 2).strokeCurveAbsciss;
						tracePts.get(ptsCount - 1).coefInMultiStroke = renormStrokePts * lastPtDist;
					} else {
						// ??
					}
				} break;
				case QuadBezier: {
					QuadBezierTraceStrokePathElement elt = (QuadBezierTraceStrokePathElement) pathElement;
					double coefPt = 1.0 / 3;
					elt.startPt.coefInMultiStroke = coefPt * renormStroke;
					// elt.controlPt .. = coefPt * renormStroke;
					elt.endPt.coefInMultiStroke = coefPt * renormStroke;
				} break;
				case CubicBezier: {
					CubicBezierTraceStrokePathElement elt = (CubicBezierTraceStrokePathElement) pathElement;
					double coefPt = 1.0 / 4;
					elt.startPt.coefInMultiStroke = coefPt * renormStroke;
					// elt.controlPt1 ..
					// elt.controlPt2 ..
					elt.endPt.coefInMultiStroke = coefPt * renormStroke;
				} break;
				}
			}
		}
	}
}
