package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.shape.Shape;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import javafx.scene.paint.Color;
import lombok.val;

public class TraceGesturePathes {

	public Color color;
	public int lineWidth;
	
	public List<TracePath> pathes = new ArrayList<>();
	
	public Shape recognizedShape;
	
	public void removeLastPath() {
		if (! pathes.isEmpty()) {
			pathes.remove(pathes.size() - 1);
		}
	}

	public boolean isEmpty() {
		return pathes.isEmpty();
	}
	
	public TracePath getLast() {
		return (pathes.isEmpty())? null : pathes.get(pathes.size() - 1);
	}
	
	public TracePath appendNewPath() {
		val res = new TracePath();
		pathes.add(res);
		return res;
	}
	

	public List<Double> pathDistLengths() {
		List<Double> res = new ArrayList<>(pathes.size());
		for(val path : pathes) {
			res.add(path.pathDistLength());
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
		List<Double> pathDistLengths = pathDistLengths();
		double distLengthTotal = sum(pathDistLengths);
		double renormCoefTotal = 1.0 / distLengthTotal;
		final int pathesCount = pathes.size();
		for (int pathi = 0; pathi < pathesCount; pathi++) {
			TracePath path = pathes.get(pathi);
			double pathDistLength = pathDistLengths.get(pathi);
			double renormPath = pathDistLength * renormCoefTotal;

			for (val pathElement : path.pathElements) {
				switch (pathElement.getType()) {
				case Segment: {
					SegmentTracePathElement elt = (SegmentTracePathElement) pathElement;
					elt.startPt.coefInPathes = 0.5 * renormPath;
					elt.endPt.coefInPathes = 0.5 * renormPath;
				} break;
				case DiscretePoints: {
					DiscretePointsTracePathElement elt = (DiscretePointsTracePathElement) pathElement;
					List<TracePt> tracePts = elt.tracePts;
					final int ptsCount = tracePts.size();
					if (ptsCount > 1) {
						double eltDistLength = elt.pathDistLength();
						double renormPathPts = renormPath / eltDistLength;
						double firstPtDist = tracePts.get(1).pathAbsciss - tracePts.get(0).pathAbsciss;
						tracePts.get(0).coefInPathes = renormPathPts * firstPtDist;
						for (int pti = 1; pti < ptsCount - 1; pti++) {
							// double distBeforePt = abscissTracePt[pti] - abscissTracePt[pti-1];
							// double distAfterPt = abscissTracePt[pti+1] - abscissTracePt[pti];
							TracePt ptBefore = tracePts.get(pti - 1);
							TracePt ptAfter = tracePts.get(pti + 1);
							double avgDistPt_ip1_im1 = 0.5 * (ptAfter.pathAbsciss - ptBefore.pathAbsciss);
							tracePts.get(pti).coefInPathes = avgDistPt_ip1_im1 * renormPath;
						}
						double lastPtDist = tracePts.get(ptsCount - 1).pathAbsciss
								- tracePts.get(ptsCount - 2).pathAbsciss;
						tracePts.get(ptsCount - 1).coefInPathes = renormPathPts * lastPtDist;
					} else {
						// ??
					}
				} break;
				case QuadBezier: {
					QuadBezierTracePathElement elt = (QuadBezierTracePathElement) pathElement;
					double coefPt = 1.0 / 3;
					elt.startPt.coefInPathes = coefPt * renormPath;
					// elt.controlPt .. = coefPt * renormPath;
					elt.endPt.coefInPathes = coefPt * renormPath;
				} break;
				case CubicBezier: {
					CubicBezierTracePathElement elt = (CubicBezierTracePathElement) pathElement;
					double coefPt = 1.0 / 4;
					elt.startPt.coefInPathes = coefPt * renormPath;
					// elt.controlPt1 ..
					// elt.controlPt2 ..
					elt.endPt.coefInPathes = coefPt * renormPath;
				} break;
				}
			}
		}
	}
}
