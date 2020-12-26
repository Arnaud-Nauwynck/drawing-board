package fr.an.drawingboard.recognizer.trace;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementVisitor;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.util.DrawingValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class WeightedDiscretizationPathPtsBuilder {


	@RequiredArgsConstructor
	public static class WeightedTracePt {
		public final TracePath tracePath;
		public final TracePathElement tracePathElement;
		public final TracePt pt;
		public final double ptWeight; // different of pt.coefInPathes!! (not discretizing Quad/CubicBezierPathElement)
	}

	/**
	 * builder of List<WeightedDiscretizationPt> to add weighted pts
	 * avoid duplicate with consecutive same pts by summing coef
	 */
	@Deprecated // TODO
	protected static class ListWeightedDiscretizationBuilder {
		private List<WeightedTracePt> pts = new ArrayList<>(50);
		private TracePath currTracePath;
		private TracePathElement currPathElement;
		private TracePt currPt;
		private double currPtWeight;
		
		public void setCurr(TracePath tracePath, TracePathElement pathElement) {
			this.currTracePath = tracePath;
			this.currPathElement = pathElement;
		}
		public void add(TracePt pt, double ptWeight) {
			if (currPt != null && pt != currPt) {
				flushAddPt();
			}
			if (pt != currPt) {
				this.currPt = pt;
				currPtWeight = ptWeight;
			} else {
				currPtWeight += ptWeight;
			}
		}
		public ImmutableList<WeightedTracePt> build() {
			flushAddPt();
			return ImmutableList.copyOf(pts);
		}
		protected void flushAddPt() {
			if (currPt != null) {
				pts.add(new WeightedTracePt(currTracePath, currPathElement, currPt, currPtWeight));
				currPt = null;
				currPtWeight = 0.0;
			}
		}
	}
	
	/**
	 * almost similar to updatePtCoefs(), but introduce discretization points for Quad/CubicBezierPathElement
	 * @param gesture
	 * @param discretizationPrecision
	 * @return
	 */
	@Deprecated // TODO
	public static ImmutableList<WeightedTracePt> weigthedDiscretizationPts(
			TraceGesture gesture, int discretizationPrecision
			) {
		ListWeightedDiscretizationBuilder res = new ListWeightedDiscretizationBuilder();
		List<Double> pathDistLengths = gesture.pathDistLengths();
		double distLengthTotal = PathDistLengthesUtils.sum(pathDistLengths);
		double renormCoefTotal = 1.0 / distLengthTotal;
		final int pathesCount = gesture.size();
		for (int pathi = 0; pathi < pathesCount; pathi++) {
			TracePath path = gesture.get(pathi);
			double pathDistLength = pathDistLengths.get(pathi);
			double renormPath = pathDistLength * renormCoefTotal;

			for(val pathElt : path.pathElements) {
				res.setCurr(path, pathElt);
				pathElt.visit(new TracePathElementVisitor() {
					@Override
					public void caseSegment(SegmentTracePathElement elt) {
						// TODO.. discretize segment (for coef in corners, add new points for discretizationPrecision)
						res.add(elt.startPt, 0.5 * renormPath);
						res.add(elt.endPt, 0.5 * renormPath);
					}
					@Override
					public void caseDiscretePts(DiscretePointsTracePathElement elt) {
						// TODO.. maybe re-discretize segment (remove pts, add new points for discretizationPrecision)
						List<TracePt> tracePts = elt.tracePts;
						final int ptsCount = tracePts.size();
						if (ptsCount > 1) {
							double eltDistLength = elt.pathDistLength();
							double renormPathPts = renormPath / eltDistLength;
							TracePt pt0 = tracePts.get(0);
							TracePt pt1 = tracePts.get(1);
							double firstPtDist = pt1.pathAbsciss - pt0.pathAbsciss;
							res.add(pt0, renormPathPts * firstPtDist);
							for (int pti = 1; pti < ptsCount - 1; pti++) {
								TracePt ptBefore = tracePts.get(pti - 1);
								TracePt ptAfter = tracePts.get(pti + 1);
								double avgDistPt_ip1_im1 = 0.5 * (ptAfter.pathAbsciss - ptBefore.pathAbsciss);
								TracePt ptI = tracePts.get(pti);
								res.add(ptI, avgDistPt_ip1_im1 * renormPath);
							}
							TracePt ptLast = tracePts.get(ptsCount - 1);
							TracePt ptBeforeLast = tracePts.get(ptsCount - 2);
							double lastPtDist = ptLast.pathAbsciss - ptBeforeLast.pathAbsciss;
							res.add(ptLast, renormPathPts * lastPtDist);
						} else {
							// ??
						}
					}
					@Override
					public void caseQuadBezier(QuadBezierTracePathElement elt) {
						DrawingValidationUtils.notImplYet();
					}
					@Override
					public void caseCubicBezier(CubicBezierTracePathElement elt) {
						DrawingValidationUtils.notImplYet();
					}
				});
			}
		}
		return res.build();
	}

}
