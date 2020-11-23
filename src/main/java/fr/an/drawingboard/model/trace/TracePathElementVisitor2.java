package fr.an.drawingboard.model.trace;

import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;

public abstract class TracePathElementVisitor2<TRes,TParam> {
	
	public abstract TRes caseSegment(SegmentTracePathElement elt, TParam p);

	public abstract TRes caseDiscretePts(DiscretePointsTracePathElement elt, TParam p);

	public abstract TRes caseQuadBezier(QuadBezierTracePathElement elt, TParam p);

	public abstract TRes caseCubicBezier(CubicBezierTracePathElement elt, TParam p);

}
