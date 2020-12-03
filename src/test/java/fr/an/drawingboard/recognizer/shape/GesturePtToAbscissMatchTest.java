package fr.an.drawingboard.recognizer.shape;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.recognizer.shape.GesturePtToAbscissMatch.GestureMatchPt;
import fr.an.drawingboard.stddefs.shapedef.ShapeDefRegistryBuilder;
import lombok.val;

public class GesturePtToAbscissMatchTest {

	@Test
	public void test_SegmentTracePathElement() {
		// build trace
		TraceGesture gesture = new TraceGesture();
		TracePath path = gesture.appendNewPath();
		TracePt startPt = new TracePt(0, 0, 0, 0, 0);
		final int endX = 100, endY = 200;
		TracePt endPt = new TracePt(endX, endY, 0, 0, 0);
		SegmentTracePathElement pathElement = new SegmentTracePathElement(startPt, endPt);
		path.add(pathElement);
		
		// build shapeDef
		ShapeDefRegistry reg = new ShapeDefRegistry();
		new ShapeDefRegistryBuilder(reg).addStdShapes();
		ShapeDef shapeDef = reg.getShapeDef("line");
		GesturePathesDef gestureDef = shapeDef.gestures.get(0);
		
		
		NumericEvalCtx paramCtx = new NumericEvalCtx();
		
		
		gestureDef.initalParamEstimator.estimateInitialParamsFor(
				gesture, gestureDef, paramCtx);

		GesturePtToAbscissMatch ptToAbscissMatch = new GesturePtToAbscissMatch(gesture, gestureDef, 
				10, paramCtx);
		ImmutableList<GestureMatchPt> matchPts = ptToAbscissMatch.gestureMatchDiscretizedPts;
		int ptIndex = 0;
		for(GestureMatchPt gestureMatchPt : matchPts) {
			TracePt pt = gestureMatchPt.weighedPt().pt;
			val matchPtExpr = gestureMatchPt.currMatchPtExpr.build();
			Pt2D matchPt = paramCtx.evalPtExpr(matchPtExpr);
			double si = paramCtx.evalExpr(gestureMatchPt.abscissExpr);
			System.out.println("pt[" + ptIndex + "] " + pt + " <-> " + matchPt + " s:" + si);
		
			ptIndex++;
		}
	}
}
