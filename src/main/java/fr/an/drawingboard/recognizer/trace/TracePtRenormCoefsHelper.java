package fr.an.drawingboard.recognizer.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.trace.TraceMultiStroke;
import fr.an.drawingboard.model.trace.TraceStroke;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.CubicBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.QuadBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElementVisitor2;
import lombok.val;

public class TracePtRenormCoefsHelper {

	public static List<Integer> countPts(TraceMultiStroke multiStroke) {
		List<Integer> res = new ArrayList<>(multiStroke.strokes.size());
		for(val stroke : multiStroke.strokes) {
			res.add(countPts(stroke));
		}
		return res;
	}
	
	private static class TraceStrokePathElementPtsCounter extends TraceStrokePathElementVisitor2<Integer,Void> {
		public static final TraceStrokePathElementPtsCounter INSTANCE = new TraceStrokePathElementPtsCounter();
		private TraceStrokePathElementPtsCounter() {}

		@Override
		public Integer caseSegment(SegmentTraceStrokePathElement elt, Void p) {
			return 2;
		}
		@Override
		public Integer caseDiscretePts(DiscretePointsTraceStrokePathElement elt, Void p) {
			return elt.tracePtCount();
		}
		@Override
		public Integer caseQuadBezier(QuadBezierTraceStrokePathElement elt, Void p) {
			return 2; // 3?
		}
		@Override
		public Integer caseCubicBezier(CubicBezierTraceStrokePathElement elt, Void p) {
			return 2; // 4?
		}
	}

	public static int countPts(TraceStroke stroke) {
		int res = 0;
		for(val pathElt : stroke.pathElements) {
			res += pathElt.visit(TraceStrokePathElementPtsCounter.INSTANCE, null);
		}
		return res;
	}
	

//	@Deprecated // ?? cf TraceStrokePathElement.endPoint.strokeCurveAbsciss - TraceStrokePathElement.startPoint.strokeCurveAbsciss 
//	private static class TraceStrokePathElementDistCalculator extends TraceStrokePathElementVisitor2<Double,Void> {
//		public static final TraceStrokePathElementDistCalculator INSTANCE = new TraceStrokePathElementDistCalculator();
//		private TraceStrokePathElementDistCalculator() {}
//
//		@Override
//		public Double caseSegment(SegmentTraceStrokePathElement elt, Void p) {
//			return elt.pathDistLength();
//		}
//		@Override
//		public Double caseDiscretePts(DiscretePointsTraceStrokePathElement elt, Void p) {
//			return elt.pathDistLength();
//		}
//		@Override
//		public Double caseQuadBezier(QuadBezierTraceStrokePathElement elt, Void p) {
//			return elt.estimePathDistLength();
//		}
//		@Override
//		public Double caseCubicBezier(CubicBezierTraceStrokePathElement elt, Void p) {
//			return elt.estimePathDistLength();
//		}
//	}
//
//	public static double pathElementsDistLength(TraceStroke stroke) {
//		double res = 0.0;
//		for(val pathElt : stroke.pathElements) {
//			res += pathElt.visit(TraceStrokePathElementDistCalculator.INSTANCE, null);
//		}
//		return res;
//	}
	
			
}
