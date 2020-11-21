package fr.an.drawingboard.recognizer.trace;

import java.util.List;

import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElementBuilder;

public class TraceStrokePathElementDetector {

	public TraceStrokePathElement recognizePathElement(TraceStrokePathElementBuilder src) {
		List<TracePt> tracePts = src.tracePts;
		int count = tracePts.size();
		if (count < 2) {
			return null;
		}
		TracePt startPt = tracePts.get(0);
		TracePt endPt = tracePts.get(tracePts.size() - 1);
		if (count == 2) {
			return new SegmentTraceStrokePathElement(startPt, endPt);
		}
		// TODO detect if approximatly straight line => simplify to Segment
		
		// TODO detect simple QuadBezier or CubicBezier ...
		
		// TODO simplify remove too-narrow points?
		
		// unrecognized, fallback to discrete line
		return new DiscretePointsTraceStrokePathElement(tracePts);
	}
}
