package fr.an.drawingboard.recognizer.trace;

import java.util.List;

import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementBuilder;
import fr.an.drawingboard.model.trace.TracePt;

public class TracePathElementDetector {

	public TracePathElement recognizePathElement(TracePathElementBuilder src) {
		List<TracePt> tracePts = src.tracePts;
		int count = tracePts.size();
		if (count < 2) {
			return null;
		}
		TracePt startPt = tracePts.get(0);
		TracePt endPt = tracePts.get(tracePts.size() - 1);
		if (count == 2) {
			return new SegmentTracePathElement(startPt, endPt);
		}
		// TODO detect if approximatly straight line => simplify to Segment
		
		// TODO detect simple QuadBezier or CubicBezier ...
		
		// TODO simplify remove too-narrow points?
		
		// unrecognized, fallback to discrete line
		return new DiscretePointsTracePathElement(tracePts);
	}
}
