package fr.an.drawingboard.model.trace;

import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;

public abstract class TracePathElementVisitor {
	
	public abstract void caseSegment(SegmentTracePathElement elt);

	public abstract void caseDiscretePts(DiscretePointsTracePathElement elt);

	public abstract void caseQuadBezier(QuadBezierTracePathElement elt);

	public abstract void caseCubicBezier(CubicBezierTracePathElement elt);

}
