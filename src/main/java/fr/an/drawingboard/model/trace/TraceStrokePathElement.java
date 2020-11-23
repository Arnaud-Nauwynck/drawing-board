package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * curve path element: Segment, continuous polyline curve, Bezier Curve, .. 
 * 
 * similar to javafx.scene.shape.PathElement
 * 
 * <PRE> 
 *    TraceStrokePathElement
 *                 - startPt
 *                 - endPoint
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
public abstract class TraceStrokePathElement {

	public TracePt startPt;
	public TracePt endPt;
	
	public TraceStrokePathElement(TracePt startPt, TracePt endPt) {
		this.startPt = startPt;
		this.endPt = endPt;
	}

	public abstract StrokePathElementType getType();
	
	public abstract void visit(TraceStrokePathElementVisitor visitor);
	public abstract <TRes,TParam> TRes visit(TraceStrokePathElementVisitor2<TRes,TParam> visitor, TParam p);
	
	// --------------------------------------------------------------------------------------------

	public static class SegmentTraceStrokePathElement extends TraceStrokePathElement {

		public SegmentTraceStrokePathElement(TracePt startPt, TracePt endPt) {
			super(startPt, endPt);
		}
	
		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.Segment;
		}

		@Override
		public void visit(TraceStrokePathElementVisitor visitor) {
			visitor.caseSegment(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TraceStrokePathElementVisitor2<TRes,TParam> visitor, TParam p) {
			return visitor.caseSegment(this, p);
		}

		public double pathDistLength() {
			return TracePt.dist(startPt, endPt);
		}
	}
	
	// --------------------------------------------------------------------------------------------

	public static class DiscretePointsTraceStrokePathElement extends TraceStrokePathElement {

		public final List<TracePt> tracePts;

		public DiscretePointsTraceStrokePathElement(List<TracePt> tracePts) {
			super(tracePts.get(0), tracePts.get(tracePts.size()-1));
			this.tracePts = new ArrayList<>(tracePts);
		}

		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.DiscretePoints;
		}

		@Override
		public void visit(TraceStrokePathElementVisitor visitor) {
			visitor.caseDiscretePts(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TraceStrokePathElementVisitor2<TRes,TParam> visitor, TParam p) {
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
	public static class QuadBezierTraceStrokePathElement extends TraceStrokePathElement {

		public Pt2D controlPt;

		public QuadBezierTraceStrokePathElement(TracePt startPt, Pt2D controlPt, TracePt endPt) {
			super(startPt, endPt);
			this.controlPt = controlPt;
		}

		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.QuadBezier;
		}

		@Override
		public void visit(TraceStrokePathElementVisitor visitor) {
			visitor.caseQuadBezier(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TraceStrokePathElementVisitor2<TRes,TParam> visitor, TParam p) {
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
	public static class CubicBezierTraceStrokePathElement extends TraceStrokePathElement {

		public Pt2D controlPt1;
		public Pt2D controlPt2;

		public CubicBezierTraceStrokePathElement(TracePt startPt, 
				Pt2D controlPt1, Pt2D controlPt2,
				TracePt endPt) {
			super(startPt, endPt);
			this.controlPt1 = controlPt1;
			this.controlPt2 = controlPt2;
		}

		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.CubicBezier;
		}

		@Override
		public void visit(TraceStrokePathElementVisitor visitor) {
			visitor.caseCubicBezier(this);
		}

		@Override
		public <TRes,TParam> TRes visit(TraceStrokePathElementVisitor2<TRes,TParam> visitor, TParam p) {
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
