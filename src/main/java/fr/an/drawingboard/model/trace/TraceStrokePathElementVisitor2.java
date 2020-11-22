package fr.an.drawingboard.model.trace;

import fr.an.drawingboard.model.trace.TraceStrokePathElement.CubicBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.QuadBezierTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;

public abstract class TraceStrokePathElementVisitor2<TRes,TParam> {
	
	public abstract TRes caseSegment(SegmentTraceStrokePathElement elt, TParam p);

	public abstract TRes caseDiscretePts(DiscretePointsTraceStrokePathElement elt, TParam p);

	public abstract TRes caseQuadBezier(QuadBezierTraceStrokePathElement elt, TParam p);

	public abstract TRes caseCubicBezier(CubicBezierTraceStrokePathElement elt, TParam p);

}
