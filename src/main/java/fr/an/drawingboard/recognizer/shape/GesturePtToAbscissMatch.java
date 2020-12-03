package fr.an.drawingboard.recognizer.shape;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.GesturePathesDef.PathElementDefEntry;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.PtExpr.PtExprBuilder;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.recognizer.trace.PathDistLengthesUtils;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder.WeightedTracePt;
import fr.an.drawingboard.util.LsUtils;
import lombok.AllArgsConstructor;
import lombok.val;

@Deprecated
public class GesturePtToAbscissMatch {

	public final TraceGesture gesture;
	public final GesturePathesDef gestureDef;

	/**
	 * expanded all pts for Gesture -> * TracePath -> TracePathElt -> .. for each
	 * Bezier curve PathElement, an inital discretisation is chosen
	 */
	public final ImmutableList<WeightedTracePt> gestureDiscretizedPts;

	public final ImmutableList<GestureMatchPt> gestureMatchDiscretizedPts;

	@AllArgsConstructor
	public static class GestureMatchPt {
		public final GesturePtToAbscissMatch parent;
		public final int ptIndex;

		public final WeightedTracePt weighedPt() {
			return parent.gestureDiscretizedPts.get(ptIndex);
		}

		public final VariableExpr abscissExpr;

		// current match value
		public final PtExprBuilder currMatchPtExpr;
		public PathDef currMatchPathDef;
		public PathElementDef currMatchPathElementDef;

	}

	// --------------------------------------------------------------------------------------------

	// TODO does not work yet ...
	public GesturePtToAbscissMatch(TraceGesture gesture, GesturePathesDef gestureDef, //
			int discretizationPrecision, //
			NumericEvalCtx currInitialParamCtx) {
		this.gesture = gesture;
		this.gestureDef = gestureDef;
		// discretize pts for gesture
		this.gestureDiscretizedPts = WeightedDiscretizationPathPtsBuilder.weigthedDiscretizationPts(gesture,
				discretizationPrecision);

		// distLengthes between discretized points
		List<TracePt> pts = LsUtils.map(gestureDiscretizedPts, wpt -> wpt.pt);
		List<Double> gestureDiscretizedDistLengthes = PathDistLengthesUtils.distLengthes(pts);

		// estimate gestureDef inital pathDef distLengthes, for initial assignment of pt
		// to pathDef
		List<Double> gestureDefEstimateDistLengthes = PathDistLengthesUtils.evalEstimateDistLengthes(gestureDef,
				currInitialParamCtx);

		// ratio for distLengthTotal gesture / gestureDefEstimate
		double gestureDiscretizedDistLengthTotal = PathDistLengthesUtils.sum(gestureDiscretizedDistLengthes);
		double gestureDefEstimateDistLengthesTotal = PathDistLengthesUtils.sum(gestureDefEstimateDistLengthes);
		double distLenToS = 1.0 / gestureDiscretizedDistLengthTotal;
		double distDefLenToS = 1.0 / gestureDefEstimateDistLengthesTotal;

		// List<PathDef> pathDefs = gestureDef.pathes;
		// build corresponding variables+PtExprs, and assign curr PathDef-PathElementDef
		// loop on discretize points(with corr TracePath), advance on estimate matching
		// PathDef
		// Iterator<TracePathWithElement> gestureTracePathEltIter =
		// gesture.iteratorPathWithElement();
		int currPathDefWithEltIndex = 0;
		Iterator<PathElementDefEntry> gestureDefTracePathEltIter = gestureDef.iteratorPathElementDef();
		PathElementDefEntry currPathDefWithElt = gestureDefTracePathEltIter.next();

		double currTracePathDist = 0;

		double currPathDefAbsciss = 0;
		double currNextPathDefDist = currPathDefAbsciss
				+ gestureDefEstimateDistLengthes.get(currPathDefWithEltIndex);
		double currS = 0; // in [0, 1] ... not in 0.. distLen !

		final int ptsCount = gestureDiscretizedPts.size();
		val gestureMatchDiscretizedPtsBuilder = ImmutableList.<GestureMatchPt>builder();
		for (int ptIndex = 0; ptIndex < ptsCount; ptIndex++) {
			// WeightedDiscretizationPt discretizedPt = gestureDiscretizedPts.get(ptIndex);
			// lookup pt in TracePath-TracePathElt
			// TracePath currPath = discretizedPt.tracePath;
			// TracePathElement currPathElt = discretizedPt.tracePathElement;

			// corresponding estimate dist in gestureDef
			double correspPathDefAbsciss = currTracePathDist * distLenToS;

			// advance pathDef-pathEltDef to enclosing absciss
			while (currTracePathDist > currNextPathDefDist) {
				if (!gestureDefTracePathEltIter.hasNext()) {
					break;
				}
				currPathDefWithEltIndex++;
				val prevPathDefElt = currPathDefWithElt.pathElement;
				currPathDefWithElt = gestureDefTracePathEltIter.next(); // ** advance in pathDef
				double pathEltDefDist = gestureDefEstimateDistLengthes.get(currPathDefWithEltIndex);
				
				if (prevPathDefElt != currPathDefWithElt.pathElement) {
					currS = 0; // TODO cut should be more precise..
				}
				currNextPathDefDist = currPathDefAbsciss + pathEltDefDist;
			}
			PathDef currMatchPathDef = currPathDefWithElt.path;
			PathElementDef currMatchPathElementDef = currPathDefWithElt.pathElement;
			
			ExprBuilder b = ExprBuilder.INSTANCE;
			VarDef varDef = new VarDef("pathEltAbsciss" + ptIndex);
			
			currInitialParamCtx.put(varDef, currS);
			VariableExpr exprS = varDef.expr;
			Expr expr1minusS = b.minus(b.lit1(), exprS);

			// build initial ptExpr in curr pathEltDef
			PtExpr initMatchPtExpr = currMatchPathElementDef.ptExprAtAbscissExpr(exprS, expr1minusS);

			PtExprBuilder currMatchPtExpr = initMatchPtExpr.toBuilder();

			// build matching pt expr
			GestureMatchPt matchPt = new GestureMatchPt(this, ptIndex, //
					exprS, currMatchPtExpr, //
					currMatchPathDef, currMatchPathElementDef);
			gestureMatchDiscretizedPtsBuilder.add(matchPt);

			if (ptIndex + 1 < ptsCount) {
				double pathEltDist = gestureDiscretizedDistLengthes.get(ptIndex);
				currTracePathDist += pathEltDist;
				currS += pathEltDist * distLenToS;
			}
		}
		this.gestureMatchDiscretizedPts = gestureMatchDiscretizedPtsBuilder.build();
	}

}
