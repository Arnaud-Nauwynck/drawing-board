package fr.an.drawingboard.model.trace2shape;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr.PtExprBuilder;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePathElement.CubicBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.QuadBezierTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementVisitorFunc0;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder.WeightedDiscretizationPt;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class GesturePtToAbscissMatch {

	public final TraceGesture gesture;
	public final GesturePathesDef gestureDef;
	
	/** expanded all pts for Gesture -> * TracePath -> TracePathElt -> .. 
	 * for each Bezier curve PathElement, an inital discretisation is chosen
	 */
	public final ImmutableList<WeightedDiscretizationPt> gestureDiscretizedPts;
	
	public final ImmutableList<GestureMatchPt> gestureMatchDiscretizedPts;
	
	
	@RequiredArgsConstructor
	public static class GestureMatchPt {
		public final GesturePtToAbscissMatch parent;
		public final int ptIndex;
		public final WeightedDiscretizationPt weighedPt() { return parent.gestureDiscretizedPts.get(ptIndex); }
		public final VariableExpr abscissExpr;
		
		// current match value
		public final PtExprBuilder currMathPtExpr;
		public PathDef currMatchPathDef;
		public PathElementDef currMatchPathElementDef;
		
	}

	// --------------------------------------------------------------------------------------------

	public GesturePtToAbscissMatch(TraceGesture gesture, GesturePathesDef gestureDef, // 
			int discretizationPrecision, //
			NumericExprEvalCtx currInitialParamCtx
			) {
		this.gesture = gesture;
		this.gestureDef = gestureDef;
		// discretize pts for gesture
		this.gestureDiscretizedPts = WeightedDiscretizationPathPtsBuilder.weigthedDiscretizationPts(gesture, discretizationPrecision);
		int ptsCount = gestureDiscretizedPts.size();
		// estimate gestureDef inital pathDef distLengthes, for initial assignment of pt to pathDef
		List<PathDef> pathDefs = gestureDef.pathes;
		List<Double> initalPathElementLengthes = new ArrayList<>();
		for(val pathDef: pathDefs) {
			for(val pathElementDef : pathDef.pathElements) {
				pathElementDef.
			}
		}
		// build corresponding variable+PtExpr, and assign curr PathDef-PathElementDef
		
	}

	public static double evalEstimatePathElementDefDistLength(PathElementDef pathElementDef, NumericExprEvalCtx evalCtx) {
		return pathElementDef.accept(new TracePathElementVisitorFunc0<Double>() {
			@Override
			public Double caseSegment(SegmentTracePathElement elt) {
				// TODO Auto-generated method stub
				return null;
			}
			@Override
			public Double caseDiscretePts(DiscretePointsTracePathElement elt) {
				// TODO Auto-generated method stub
				return null;
			}
			@Override
			public Double caseQuadBezier(QuadBezierTracePathElement elt) {
				// TODO Auto-generated method stub
				return null;
			}
			@Override
			public Double caseCubicBezier(CubicBezierTracePathElement elt) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
}
