package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * curve path element: Segment, continuous polyline curve, Bezier Curve, .. 
 * 
 * similar to javafx.scene.shape.PathElement
 * 
 * <PRE> 
 *    TracePathElement
 *         - startPt
 *         - endPoint
 *             /\
 *             |
 *     --------------------------------------------  
 *     |            |                |             |
 *    Segment    DiscretePoints  QuadBezier     CubicBezier
 *                  |              - controlPt1    - controlPt1   
 *                  |    pts                       - controlPt2
 *                  +--> * TracePt
 *                          - (x, y)
 *                          - t
 * </PRE>
 */
public abstract class TracePathElement {

	public TracePt startPt;
	public TracePt endPt;
	
	public TracePathElement(TracePt startPt, TracePt endPt) {
		this.startPt = startPt;
		this.endPt = endPt;
	}

	public abstract TracePathElementType getType();
	
	public abstract void visit(TracePathElementVisitor visitor);
	public abstract <TRes> TRes visit(TracePathElementVisitorFunc0<TRes> visitor);
	public abstract <TRes,TParam> TRes visit(TracePathElementVisitorFunc1<TRes,TParam> visitor, TParam p);
	
	// --------------------------------------------------------------------------------------------

	public static class SegmentTracePathElement extends TracePathElement {

		public SegmentTracePathElement(TracePt startPt, TracePt endPt) {
			super(startPt, endPt);
		}
	
		@Override
		public TracePathElementType getType() {
			return TracePathElementType.Segment;
		}

		@Override
		public void visit(TracePathElementVisitor visitor) {
			visitor.caseSegment(this);
		}

		@Override
		public <TRes> TRes visit(TracePathElementVisitorFunc0<TRes> visitor) {
			return visitor.caseSegment(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TracePathElementVisitorFunc1<TRes,TParam> visitor, TParam p) {
			return visitor.caseSegment(this, p);
		}

		public double pathDistLength() {
			return TracePt.dist(startPt, endPt);
		}
	}
	
	// --------------------------------------------------------------------------------------------

	public static class DiscretePointsTracePathElement extends TracePathElement {

		public final List<TracePt> tracePts;

		public DiscretePointsTracePathElement(List<TracePt> tracePts) {
			super(tracePts.get(0), tracePts.get(tracePts.size()-1));
			this.tracePts = new ArrayList<>(tracePts);
		}

		@Override
		public TracePathElementType getType() {
			return TracePathElementType.DiscretePoints;
		}

		@Override
		public void visit(TracePathElementVisitor visitor) {
			visitor.caseDiscretePts(this);
		}

		@Override
		public <TRes> TRes visit(TracePathElementVisitorFunc0<TRes> visitor) {
			return visitor.caseDiscretePts(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TracePathElementVisitorFunc1<TRes,TParam> visitor, TParam p) {
			return visitor.caseDiscretePts(this, p);
		}

		public int tracePtCount() {
			return tracePts.size();
		}
		
		public TracePt tracePt(int index) {
			return tracePts.get(index);
		}

		public double pathDistLength() {
			int count = tracePts.size();
			if (count == 0) {
				return 0.0;
			}
			double res = 0.0;
			TracePt prevPt = tracePts.get(0);
			for(int i = 1; i < count; i++) {
				TracePt pt = tracePts.get(i);
				res += TracePt.dist(prevPt, pt);
				prevPt = pt;
			}
			return res;
		}

	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Quadratic Bezier Curve
	 * 
	 * similar to javafx.scene.shape.QuadCurveTo
	 */
	public static class QuadBezierTracePathElement extends TracePathElement {

		public Pt2D controlPt;

		public QuadBezierTracePathElement(TracePt startPt, Pt2D controlPt, TracePt endPt) {
			super(startPt, endPt);
			this.controlPt = controlPt;
		}

		@Override
		public TracePathElementType getType() {
			return TracePathElementType.QuadBezier;
		}

		@Override
		public void visit(TracePathElementVisitor visitor) {
			visitor.caseQuadBezier(this);
		}

		@Override
		public <TRes> TRes visit(TracePathElementVisitorFunc0<TRes> visitor) {
			return visitor.caseQuadBezier(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TracePathElementVisitorFunc1<TRes,TParam> visitor, TParam p) {
			return visitor.caseQuadBezier(this, p);
		}
		
		public double estimePathDistLength() {
			// TOCHANGE estimation only..
			double d1 = TracePt.dist(startPt, controlPt);
			double d2 = TracePt.dist(controlPt, endPt);
			return 0.5 * (d1 + d2);
		}
	}


	// --------------------------------------------------------------------------------------------

	/**
	 * Quadratic Bezier Curve
	 * 
	 * similar to javafx.scene.shape.CubicCurveTo
	 */
	public static class CubicBezierTracePathElement extends TracePathElement {

		public Pt2D controlPt1;
		public Pt2D controlPt2;

		public CubicBezierTracePathElement(TracePt startPt, 
				Pt2D controlPt1, Pt2D controlPt2,
				TracePt endPt) {
			super(startPt, endPt);
			this.controlPt1 = controlPt1;
			this.controlPt2 = controlPt2;
		}

		@Override
		public TracePathElementType getType() {
			return TracePathElementType.CubicBezier;
		}

		@Override
		public void visit(TracePathElementVisitor visitor) {
			visitor.caseCubicBezier(this);
		}

		@Override
		public <TRes> TRes visit(TracePathElementVisitorFunc0<TRes> visitor) {
			return visitor.caseCubicBezier(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TracePathElementVisitorFunc1<TRes,TParam> visitor, TParam p) {
			return visitor.caseCubicBezier(this, p);
		}

		public double estimePathDistLength() {
			// TOCHANGE estimation only..
			double d1 = TracePt.dist(startPt, controlPt1);
			double d2 = TracePt.dist(controlPt1, controlPt2);
			double d3 = TracePt.dist(controlPt2, endPt);
			return 1.0/3.0 * (d1 + d2 + d3);
		}

	}

}
