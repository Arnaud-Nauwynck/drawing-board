package fr.an.drawingboard.recognizer.trace;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
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

	public static void updatePtCoefs(TraceGesture gesture) {
		List<Double> pathDistLengths = gesture.pathDistLengths();
		double distLengthTotal = TraceGesture.sum(pathDistLengths);
		double renormCoefTotal = 1.0 / distLengthTotal;
		final int pathesCount = gesture.pathes.size();
		for (int pathi = 0; pathi < pathesCount; pathi++) {
			TracePath path = gesture.pathes.get(pathi);
			double pathDistLength = pathDistLengths.get(pathi);
			double renormPath = pathDistLength * renormCoefTotal;

			for (val pathElement : path.pathElements) {
				pathElement.visit(new TracePathElementVisitor() {
					@Override
					public void caseSegment(SegmentTracePathElement elt) {
						elt.startPt.coefInPathes = 0.5 * renormPath;
						elt.endPt.coefInPathes = 0.5 * renormPath;
					}
					@Override
					public void caseDiscretePts(DiscretePointsTracePathElement elt) {
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
					}
					@Override
					public void caseQuadBezier(QuadBezierTracePathElement elt) {
						double coefPt = 1.0 / 3;
						elt.startPt.coefInPathes = coefPt * renormPath;
						// elt.controlPt .. = coefPt * renormPath;
						elt.endPt.coefInPathes = coefPt * renormPath;
					}
					@Override
					public void caseCubicBezier(CubicBezierTracePathElement elt) {
						double coefPt = 1.0 / 4;
						elt.startPt.coefInPathes = coefPt * renormPath;
						// elt.controlPt1 ..
						// elt.controlPt2 ..
						elt.endPt.coefInPathes = coefPt * renormPath;
					}
				});
			}
		}
	}

	@RequiredArgsConstructor
	public static class WeightedDiscretizationPt {
		public final TracePt pt;
		public final double ptWeight; // different of pt.coefInPathes!! (not discretizing Quad/CubicBezierPathElement)
	}

	/**
	 * builder of List<WeightedDiscretizationPt> to add weighted pts
	 * avoid duplicate with consecutive same pts by summing coef
	 */
	protected static class ListWeightedDiscretizationBuilder {
		private List<WeightedDiscretizationPt> pts = new ArrayList<>(50);
		private TracePt currPt;
		private double currPtWeight;
		
		public void add(TracePt pt, double ptWeight) {
			if (currPt != null && pt != currPt) {
				flushAddPt();
			}
			if (pt != currPt) {
				currPt = pt;
				currPtWeight = ptWeight;
			} else {
				currPtWeight += ptWeight;
			}
		}
		public ImmutableList<WeightedDiscretizationPt> build() {
			flushAddPt();
			return ImmutableList.copyOf(pts);
		}
		protected void flushAddPt() {
			if (currPt != null) {
				pts.add(new WeightedDiscretizationPt(currPt, currPtWeight));
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
	public static ImmutableList<WeightedDiscretizationPt> weigthedDiscretizationPts(
			TraceGesture gesture, int discretizationPrecision
			) {
		ListWeightedDiscretizationBuilder res = new ListWeightedDiscretizationBuilder();
		List<Double> pathDistLengths = gesture.pathDistLengths();
		double distLengthTotal = TraceGesture.sum(pathDistLengths);
		double renormCoefTotal = 1.0 / distLengthTotal;
		final int pathesCount = gesture.pathes.size();
		for (int pathi = 0; pathi < pathesCount; pathi++) {
			TracePath path = gesture.pathes.get(pathi);
			double pathDistLength = pathDistLengths.get(pathi);
			double renormPath = pathDistLength * renormCoefTotal;

			for(val pathElt : path.pathElements) {
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
							double firstPtDist = tracePts.get(1).pathAbsciss - tracePts.get(0).pathAbsciss;
							res.add(tracePts.get(0), renormPathPts * firstPtDist);
							for (int pti = 1; pti < ptsCount - 1; pti++) {
								TracePt ptBefore = tracePts.get(pti - 1);
								TracePt ptAfter = tracePts.get(pti + 1);
								double avgDistPt_ip1_im1 = 0.5 * (ptAfter.pathAbsciss - ptBefore.pathAbsciss);
								res.add(tracePts.get(pti), avgDistPt_ip1_im1 * renormPath);
							}
							double lastPtDist = tracePts.get(ptsCount - 1).pathAbsciss
									- tracePts.get(ptsCount - 2).pathAbsciss;
							res.add(tracePts.get(ptsCount - 1), renormPathPts * lastPtDist);
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
