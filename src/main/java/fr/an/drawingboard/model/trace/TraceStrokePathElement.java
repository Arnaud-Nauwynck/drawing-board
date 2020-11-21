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
	
	public enum TraceStrokePathElementType {
		Segment, DiscretePoints, QuadBezier, CubicBezier
	}
	
	public TraceStrokePathElement(TracePt startPt, TracePt endPt) {
		this.startPt = startPt;
		this.endPt = endPt;
	}

	public abstract TraceStrokePathElementType getType();
	
	// --------------------------------------------------------------------------------------------

	public static class SegmentTraceStrokePathElement extends TraceStrokePathElement {

		public SegmentTraceStrokePathElement(TracePt startPt, TracePt endPt) {
			super(startPt, endPt);
		}
	
		@Override
		public TraceStrokePathElementType getType() {
			return TraceStrokePathElementType.Segment;
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
		public TraceStrokePathElementType getType() {
			return TraceStrokePathElementType.DiscretePoints;
		}

		public int tracePtCount() {
			return tracePts.size();
		}
		
		public TracePt tracePt(int index) {
			return tracePts.get(index);
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
		public TraceStrokePathElementType getType() {
			return TraceStrokePathElementType.QuadBezier;
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
		public TraceStrokePathElementType getType() {
			return TraceStrokePathElementType.CubicBezier;
		}

	}

}
