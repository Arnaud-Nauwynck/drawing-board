package fr.an.drawingboard.recognizer.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.trace.TraceGesturePathes;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementVisitor2;
import lombok.val;

public class TracePtRenormCoefsHelper {

	public static List<Integer> countPts(TraceGesturePathes gesture) {
		List<Integer> res = new ArrayList<>(gesture.pathes.size());
		for(val path : gesture.pathes) {
			res.add(countPts(path));
		}
		return res;
	}
	
	private static class TracePathElementPtsCounter extends TracePathElementVisitor2<Integer,Void> {
		public static final TracePathElementPtsCounter INSTANCE = new TracePathElementPtsCounter();
		private TracePathElementPtsCounter() {}

		@Override
		public Integer caseSegment(SegmentTracePathElement elt, Void p) {
			return 2;
		}
		@Override
		public Integer caseDiscretePts(DiscretePointsTracePathElement elt, Void p) {
			return elt.tracePtCount();
		}
		@Override
		public Integer caseQuadBezier(QuadBezierTracePathElement elt, Void p) {
			return 2; // 3?
		}
		@Override
		public Integer caseCubicBezier(CubicBezierTracePathElement elt, Void p) {
			return 2; // 4?
		}
	}

	public static int countPts(TracePath path) {
		int res = 0;
		for(val pathElt : path.pathElements) {
			res += pathElt.visit(TracePathElementPtsCounter.INSTANCE, null);
		}
		return res;
	}
	

//	@Deprecated // ?? cf TracePathElement.endPoint.pathAbsciss - TracePathElement.startPoint.pathAbsciss 
//	private static class TracePathElementDistCalculator extends TracePathElementVisitor2<Double,Void> {
//		public static final TracePathElementDistCalculator INSTANCE = new TracePathElementDistCalculator();
//		private TracePathElementDistCalculator() {}
//
//		@Override
//		public Double caseSegment(SegmentTracePathElement elt, Void p) {
//			return elt.pathDistLength();
//		}
//		@Override
//		public Double caseDiscretePts(DiscretePointsTracePathElement elt, Void p) {
//			return elt.pathDistLength();
//		}
//		@Override
//		public Double caseQuadBezier(QuadBezierTracePathElement elt, Void p) {
//			return elt.estimePathDistLength();
//		}
//		@Override
//		public Double caseCubicBezier(CubicBezierTracePathElement elt, Void p) {
//			return elt.estimePathDistLength();
//		}
//	}
//
//	public static double pathElementsDistLength(TracePath path) {
//		double res = 0.0;
//		for(val pathElt : path.pathElements) {
//			res += pathElt.visit(TracePathElementDistCalculator.INSTANCE, null);
//		}
//		return res;
//	}
	
			
}
