package fr.an.drawingboard.model.trace;

import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;

public abstract class TracePathElementVisitorFunc0<TRes> {
	
	public abstract TRes caseSegment(SegmentTracePathElement elt);

	public abstract TRes caseDiscretePts(DiscretePointsTracePathElement elt);

	public abstract TRes caseQuadBezier(QuadBezierTracePathElement elt);

	public abstract TRes caseCubicBezier(CubicBezierTracePathElement elt);

}
