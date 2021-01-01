package fr.an.drawingboard.recognizer.shape;

import java.util.Iterator;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.model.shapedef.GestureDef;
import fr.an.drawingboard.model.shapedef.GestureDef.PathElementDefEntry;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.recognizer.initialParamEstimators.ParamEvalCtx;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder.WeightedTracePt;

@Deprecated
public class TraceGestureDefMatchingBuilder {

	public static TraceGestureDefMatching match(
			GestureDef gestureDef,
			TraceGesture gesture, 
			int discretizationPrecision, 
			ParamEvalCtx currMatchParamCtx
			) {
		TraceGestureDefMatching gestureDefMatching = new TraceGestureDefMatching(gestureDef);

		// discretize pts for gesture with associated weigth (expand bezier pathElements)
		List<WeightedTracePt> tracePts = WeightedDiscretizationPathPtsBuilder.weigthedDiscretizationPts(gesture,
				discretizationPrecision);

		Iterator<WeightedTracePt> currTracePtIter = tracePts.iterator(); 
		if (! currTracePtIter.hasNext()) {
			return gestureDefMatching;
		}
		WeightedTracePt currTracePt = currTracePtIter.next();
		InterpolatedTracePt currMatchTracePt = new InterpolatedTracePt(currTracePt.pt);
		
		Iterator<PathElementDefEntry> pathEltDefIter = gestureDef.iteratorPathElementDef();
		PathElementDefEntry pathEltDefEntry = pathEltDefIter.next();
		PathElementDef pathElementDef = pathEltDefEntry.pathElement;
		
		PtExpr currMatchPtDefExpr = pathElementDef.startPt;
		Pt2D currMatchPtDef = currMatchParamCtx.evalPtExpr(currMatchPtDefExpr);
		
		// advance on tracePtIter for corresponding pathElementDef
		if (pathElementDef instanceof SegmentPathElementDef) {
			SegmentPathElementDef segmentDef = (SegmentPathElementDef) pathElementDef;
		
		} else if (pathElementDef instanceof DiscretePointsPathElementDef) {
			DiscretePointsPathElementDef discretePtsDef = (DiscretePointsPathElementDef) pathElementDef;

			Iterator<PtExpr> ptDefIter = discretePtsDef.ptExprs.iterator();
			PtExpr ptDefExpr = ptDefIter.next(); 
			Pt2D ptDef = currMatchParamCtx.evalPtExpr(ptDefExpr);
			// assert currMatchPtDef == ptDef
			
			PtExpr nextPtDefExpr = ptDefIter.next(); 
			Pt2D nextPtDef = currMatchParamCtx.evalPtExpr(nextPtDefExpr);

			Pt2D ptDef_nextPtDef = ptDef.vectTo(nextPtDef);
			double dist_ptDef_nextPtDef = ptDef_nextPtDef.norm();
			Pt2D normalized_PtDef_NextPtDef = ptDef_nextPtDef.normalizedVectTo(nextPtDef);
			
			double remainCorrelDist = dist_ptDef_nextPtDef;
			while(remainCorrelDist > 0.0) {
				if (!currTracePtIter.hasNext()) {
					break;
				}
				// advance on prevInterpolatedTracePt->tracePtIter for corresponding segment ptDef->nextPtDef
				WeightedTracePt nextWTracePt = currTracePtIter.next();
				TracePt nextTracePt = nextWTracePt.pt;
				Pt2D currMatchTracePt_nextTracePt = currMatchTracePt.interpolPt.vectTo(nextTracePt.xy());
				
				double correlatedDist = currMatchTracePt_nextTracePt.scalarProduct(normalized_PtDef_NextPtDef);
				
				InterpolatedTracePt nextMatchTracePt;
				Pt2D projectionNextMatchPtDef;
				
				if (correlatedDist <= 0) {
					// maybe a pin shape
					//        nextPt
					//          +
					//         / \
					//            \
					//   ......   +
					//            /\
					//            |
					//            \/
					//   ......   +-----------+-------->
					//           ptDef     nextPtDef
					// 
					// do not consume as matching pts, stay at position ptDef
					// remainCorrelDist += 0.0;
					nextMatchTracePt = new InterpolatedTracePt(nextTracePt);
					projectionNextMatchPtDef = ptDef;
							
				} else if (correlatedDist >= remainCorrelDist) {
					// do not go further.. interpol
					//                                 nextPt
					//                  nextMatchTracePt ___+
					//  currMatchTracePt     + _________/
					//              ________ /
					//      ______/           projectionNextMatchPtDef
					//   +--------------------+------------->
					//  /\ -correlatedDist->
					//  |                     /\ 
					//  \/  -dist->           |
					//  +---------------------+
					//  ptDef              nextPtDef
					//
					double interpolDist = correlatedDist - remainCorrelDist;
					double interpolCoef = interpolDist / correlatedDist;

					remainCorrelDist = 0.0;
					nextMatchTracePt = new InterpolatedTracePt(currMatchTracePt, interpolCoef, nextTracePt);
					projectionNextMatchPtDef = nextPtDef;
					
				} else { // if (0 <= correlatedDist && correlatedDist < remainCorrelDist)
					// trace match only a fragment of current pathDef
					//
					//                  nextPt
					//  currMatchTracePt  +
					//          ________ /
					//      __/           projectionNextMatchPtDef
					//   +-----------------+
					//  /\ -correlatedDist->
					//  |                  /\ 
					//  \/ -dist->         |
					//  +---------------------------+
					//  ptDef                  nextPtDef
					//
					
					remainCorrelDist -= correlatedDist;
					nextMatchTracePt = new InterpolatedTracePt(nextTracePt);
					projectionNextMatchPtDef = ptDef.plus(normalized_PtDef_NextPtDef.mult(correlatedDist));
				}
				
				// add matching for nextMatchTracePt<->newPtDef
				// add cost expr term as square distance
				
				
				// advance matching either on tracePt, or tracePtDef
			}
			
		} else {
			// TODO QuadBezierDef, CubicBezierDef ..
		}
		
		return gestureDefMatching;
	}

}
