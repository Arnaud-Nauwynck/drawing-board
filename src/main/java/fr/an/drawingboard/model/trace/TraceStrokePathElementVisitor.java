package fr.an.drawingboard.model.trace;

import fr.an.drawingboard.model.trace.TraceStrokePathElement.CubicBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.QuadBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;

public abstract class TraceStrokePathElementVisitor {
	
	public abstract void caseSegment(SegmentTraceStrokePathElement elt);

	public abstract void caseDiscretePts(DiscretePointsTraceStrokePathElement elt);

	public abstract void caseQuadBezier(QuadBezierTraceStrokePathElement elt);

	public abstract void caseCubicBezier(CubicBezierTraceStrokePathElement elt);

}
