package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import lombok.RequiredArgsConstructor;

/**
 * matching between a GesturePathesDef and 0,1,..* Trace(Gesture-Path-PathElt) fragments
 * wrapper for exactly 1 GesturePathesDef
 */
@RequiredArgsConstructor
public class TraceGestureDefMatching {

	public final GesturePathesDef gestureDef;
	
	// InterpolatedTracePt startInterpolTracePt; // = pathes.get(0).startInterpolTracePt;
	// InterpolatedTracePt endInterpolTracePt; // = pathes.get(pathes.size()-1).endInterpolTracePt;
	
	public List<TracePathDefMatching> pathMatchings = new ArrayList<>();

	/**
	 * matching between a PathDef and 0,1,..* Trace(Gesture-Path-PathElt) fragments
	 * wrapper for exactly 1 PathDef
	 */
	@RequiredArgsConstructor
	public static class TracePathDefMatching {
		public final PathDef pathDef;
		
		InterpolatedTracePt startInterpolTracePt;
		InterpolatedTracePt endInterpolTracePt;
		
		public List<TracePathElementDefMatching<?>> pathElementMatchings = new ArrayList<>();

	}
	
	/**
	 * 
	 */
	@RequiredArgsConstructor
	public static abstract class TracePathElementDefMatching<TPathElementDef extends PathElementDef> {
		public final TPathElementDef pathElementDef;

		InterpolatedTracePt startInterpolTracePt;
		InterpolatedTracePt endInterpolTracePt;
		
	}
	
	/**
	 * matching for a SegmentPathElementDef
	 */
	public static class SegmentTracePathElementDefMatching extends TracePathElementDefMatching<SegmentPathElementDef> {

		public SegmentTracePathElementDefMatching(SegmentPathElementDef pathElementDef) {
			super(pathElementDef);
		}
		
	}
	
	/**
	 * matching for a DiscretePointsPathElementDef
	 */
	public static class DiscretePointsPathElementDefMatching extends TracePathElementDefMatching<DiscretePointsPathElementDef> {

		public final List<InterpolatedTracePt> interpolTracePts;

		public DiscretePointsPathElementDefMatching(DiscretePointsPathElementDef pathElementDef) {
			super(pathElementDef);
			this.interpolTracePts = new ArrayList<>();
		}
		
	}
	
	/**
	 * matching for a QuadBezierPathElementDef
	 */
	public static class QuadBezierPathElementDefMatching extends TracePathElementDefMatching<QuadBezierPathElementDef> {

		// matchingControlPt;

		public QuadBezierPathElementDefMatching(QuadBezierPathElementDef pathElementDef) {
			super(pathElementDef);
		}
		
	}

	/**
	 * matching for a CubicBezierPathElementDef
	 */
	public static class CubicBezierPathElementDefMatching extends TracePathElementDefMatching<CubicBezierPathElementDef> {

		// matchingControlPt1;
		// matchingControlPt2;

		public CubicBezierPathElementDefMatching(CubicBezierPathElementDef pathElementDef) {
			super(pathElementDef);
		}
		
	}

}
