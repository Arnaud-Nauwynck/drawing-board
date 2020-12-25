package fr.an.drawingboard.recognizer.trace;

import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.bezier.BezierFlattenize;
import fr.an.drawingboard.geom2d.utils.DistinctPt2DListBuilder;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementVisitor;
import fr.an.drawingboard.model.trace.TraceShape;
import lombok.val;

public class TraceDiscretisationPtsBuilder {
	
	private double discretizationPrecision = 5; // todo unused yet..
	
	public List<Pt2D> discretizeToPts(TraceShape trace) {
		val res = new DistinctPt2DListBuilder();
		discretizeToPts(res, trace);
		return res.build();
	}

	public List<Pt2D> discretizeToPts(TraceGesture gesture) {
		val res = new DistinctPt2DListBuilder();
		discretizeToPts(res, gesture);
		return res.build();
	}

	public List<Pt2D> discretizeToPts(TracePath path) {
		val res = new DistinctPt2DListBuilder();
		discretizeToPts(res, path);
		return res.build();
	}

	public List<Pt2D> discretizeToPts(TracePathElement pathElt) {
		val res = new DistinctPt2DListBuilder();
		discretizeToPts(res, pathElt);
		return res.build();
	}

	
	public void discretizeToPts(DistinctPt2DListBuilder res, TraceShape trace) {
		for(val gesture: trace.gestures) {
			discretizeToPts(res, gesture);
		}
	}

	public void discretizeToPts(DistinctPt2DListBuilder res, TraceGesture gesture) {
		for(val path: gesture.pathes) {
			discretizeToPts(res, path);
		}
	}

	public void discretizeToPts(DistinctPt2DListBuilder res, TracePath path) {
		for(val pathElt: path.pathElements) {
			discretizeToPts(res, pathElt);
		}
	}

	public void discretizeToPts(DistinctPt2DListBuilder res, TracePathElement pathElt) {
		pathElt.visit(new TracePathElementVisitor() {
			@Override
			public void caseSegment(SegmentTracePathElement elt) {
				res.add(elt.startPt);
				res.add(elt.endPt);
			}
			@Override
			public void caseDiscretePts(DiscretePointsTracePathElement elt) {
				for(val pt: elt.tracePts) {
					res.add(pt);
				}
			}
			@Override
			public void caseQuadBezier(QuadBezierTracePathElement elt) {
				BezierFlattenize.flattenizeQuadBezier(res, elt.toQuadBezier(), discretizationPrecision);
			}
			@Override
			public void caseCubicBezier(CubicBezierTracePathElement elt) {
				BezierFlattenize.flattenizeCubicBezier(res, elt.toCubicBezier(), discretizationPrecision);
			}
		});
	}

}
