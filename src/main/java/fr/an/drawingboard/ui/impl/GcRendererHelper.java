package fr.an.drawingboard.ui.impl;

import java.util.Arrays;
import java.util.List;

import javax.swing.border.StrokeBorder;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.obj.GestureObj;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj;
import fr.an.drawingboard.model.shapedef.obj.PathObj;
import fr.an.drawingboard.model.shapedef.obj.ShapeObj;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj.CubicBezierPathElementCtxEval;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj.DiscretePointsPathElementCtxEval;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj.PathElementCtxEvalVisitor;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj.QuadBezierPathElementCtxEval;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj.SegmentPathElementCtxEval;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceShape;
import fr.an.drawingboard.recognizer.shape.GesturePtToAbscissMatch;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class GcRendererHelper {

	protected final GraphicsContext gc;
	boolean debugTrace;
	boolean debugTraceStopPoints;
	
	public void draw(TraceShape traceShape) {
		for(val gesture : traceShape.gestures) {
			draw(gesture);
		}
	}
	
	public void draw(TraceGesture gesture) {
		for (val path : gesture.pathes()) {
			draw(path);
		}
	}

	public void draw(ShapeObj shape) {
		for(val gesture : shape.gestures) {
			draw(gesture);
		}
	}

	public void draw(GestureObj gesture) {
		for(val path : gesture.pathes()) {
			draw(path);
		}
	}


	public void draw(Paint paint, GestureObj gesture) {
		Paint prevStroke = gc.getStroke();
		gc.setStroke(paint);
		draw(gesture);
		gc.setStroke(prevStroke);
	}

	public void draw(PathObj path) {
		for(val pathElement : path.pathElements) {
			draw(pathElement);
		}
	}

	public void draw(PathElementObj pathElement) {
		pathElement.accept(new PathElementCtxEvalVisitor() {
			@Override
			public void caseSegment(SegmentPathElementCtxEval segment) {
				drawSegment(segment);
			}
			@Override
			public void caseDiscretePoints(DiscretePointsPathElementCtxEval discretePts) {
				drawDiscretePoints(discretePts);
			}
			@Override
			public void caseQuadBezier(QuadBezierPathElementCtxEval quadBezier) {
				throw DrawingValidationUtils.notImplYet();
			}
			@Override
			public void caseCubicBezier(CubicBezierPathElementCtxEval cubicBezier) {
				throw DrawingValidationUtils.notImplYet();
			}
		});
	}

	public void drawSegment(SegmentPathElementCtxEval src) {
		gc.beginPath();
		moveTo(src.startPt);
		lineTo(src.endPt);
		gc.stroke();
	}

	public void drawDiscretePoints(DiscretePointsPathElementCtxEval src) {
		drawLinePoints(Arrays.asList(src.pts));
	}
	
	public void drawLinePoints(List<Pt2D> ptExprs) {
		int ptsCount = ptExprs.size();
		Pt2D prevPt = ptExprs.get(0);
		gc.beginPath();
		moveTo(prevPt);
		for(int i = 1; i < ptsCount; i++) {
			Pt2D pt = ptExprs.get(i);
			lineTo(pt);
			prevPt = pt;
		}
		gc.stroke();
	}

	public void lineTo(Pt2D pt) {
		gc.lineTo(pt.x, pt.y);
	}

	public void moveTo(Pt2D pt) {
		gc.moveTo(pt.x, pt.y);
	}


    public void drawBezier(QuadBezier2D bezier) {
//        int maxStep = 100;
//        for(int step = 0; step <= maxStep; step++) {
//        	double s = ((double)step) / maxStep;
//        	Pt2D pt = bezier.eval(s);
//        	drawPtCircle(pt, 1);
//        }
    	gc.beginPath();
    	gc.moveTo(bezier.startPt.x, bezier.startPt.y);
    	gc.quadraticCurveTo(bezier.controlPt.x, bezier.controlPt.y, bezier.endPt.x, bezier.endPt.y);
    	gc.stroke();
    	
        Paint prevStroke = gc.getStroke();
        gc.setStroke(Color.RED);
        drawPtCircle(bezier.startPt, 3);
        drawPtCircle(bezier.controlPt, 3);
        drawPtCircle(bezier.endPt, 3);
        gc.setStroke(prevStroke);

        gc.setStroke(Color.GREY);
        drawSegment(bezier.startPt, bezier.controlPt);
        drawSegment(bezier.controlPt, bezier.endPt);
        gc.setStroke(prevStroke);
    }

    public void drawBezier(CubicBezier2D bezier) {
//        int maxStep = 100;
//        for(int step = 0; step <= maxStep; step++) {
//        	double s = ((double)step) / maxStep;
//        	Pt2D pt = bezier.eval(s);
//        	drawPtCircle(pt, 1);
//        }
    	gc.beginPath();
    	gc.moveTo(bezier.startPt.x, bezier.startPt.y);
    	gc.bezierCurveTo(bezier.p1.x, bezier.p1.y, bezier.p2.x, bezier.p2.y, bezier.endPt.x, bezier.endPt.y);
    	gc.stroke();

        Paint prevStroke = gc.getStroke();
        gc.setStroke(Color.RED);
        drawPtCircle(bezier.startPt, 3);
        drawPtCircle(bezier.p1, 3);
        drawPtCircle(bezier.p2, 3);
        drawPtCircle(bezier.endPt, 3);
        gc.setStroke(prevStroke);

        gc.setStroke(Color.GREY);
        drawSegment(bezier.startPt, bezier.p1);
        drawSegment(bezier.p1, bezier.p2);
        drawSegment(bezier.p2, bezier.endPt);
        gc.setStroke(prevStroke);
    }

	public void draw(TracePath path) {
		for(TracePathElement pathElement : path.pathElements) {
			switch(pathElement.getType()) {
			case Segment:
				drawSegment((SegmentTracePathElement) pathElement);
				break;
			case DiscretePoints:
				drawDiscretePoints((DiscretePointsTracePathElement) pathElement);
				break;
			case QuadBezier:
				break;
			case CubicBezier: 
				break;
			}
		}
	}
	
	public void drawSegment(SegmentTracePathElement segment) {
		drawSegment(segment.startPt, segment.endPt);
	}
	public void drawSegment(TracePt startPt, TracePt endPt) {
		drawSegment(startPt.xy(), endPt.xy());
	}
	public void drawSegment(Pt2D startPt, Pt2D endPt) {
		gc.beginPath();
		gc.moveTo(startPt.x, startPt.y);
		gc.lineTo(endPt.x, endPt.y);
		gc.stroke();
	}

	public void drawSegment(double startX, double startY, double endX, double endY) {
		gc.beginPath();
		gc.moveTo(startX, startY);
		gc.lineTo(endX, endY);
		gc.stroke();
	}
	
	public void drawDiscretePoints(DiscretePointsTracePathElement curve) {
		drawDiscretePoints(curve.tracePts);
	}
	
	public void drawDiscretePoints(List<TracePt> tracePts) {
		int tracePtsLen = tracePts.size();
		if (tracePtsLen > 0) {
			val pt0 = tracePts.get(0);
			gc.beginPath();
			gc.moveTo(pt0.x, pt0.y);
			for(int i = 1; i < tracePtsLen; i++) {
				val pt = tracePts.get(i);
				gc.lineTo(pt.x, pt.y);
			}
			gc.stroke();
			
			// debug
			val dbgTraceEndPoint = false;
			if (dbgTraceEndPoint) {
				drawPtCircle(pt0, 5);
			}
			TracePt prevDisplayIndexPt = null;
			for(int i = 1; i < tracePtsLen; i++) {
				val pt = tracePts.get(i);
				if (debugTraceStopPoints && pt.isStopPoint()) {
					drawPtCircle(pt, 5);
				}
				if (debugTrace) {
					if (prevDisplayIndexPt == null || TracePt.dist(pt, prevDisplayIndexPt) > 20) {
						gc.strokeText("" + i, pt.x, pt.y + 10);
						prevDisplayIndexPt = pt;
					}
				}
			}
			if (dbgTraceEndPoint) {
				val lastPt = tracePts.get(tracePtsLen - 1);
				drawPtCircle(lastPt, 3);
			}
			
		}
	}

	public void drawPtToAbscissMatch(GraphicsContext gc, GesturePtToAbscissMatch ptToAbscissMatch, NumericEvalCtx currMatchParamCtx) {
		Paint prevStroke = gc.getStroke();
		gc.setStroke(Color.RED);
		for (val matchPt : ptToAbscissMatch.gestureMatchDiscretizedPts) {
			TracePt pt = matchPt.weighedPt().pt;
			PtExpr ptDefExpr = matchPt.currMatchPtExpr.build();
			Pt2D ptDef = currMatchParamCtx.evalPtExpr(ptDefExpr);

			// drawPtCircle(pt, 3);
			drawSegment(pt.xy(), ptDef);
			// drawPtCircle(ptDef, 3);
			
		}
		gc.setStroke(prevStroke);
	}

	public void drawPtCircle(Pt2D pt, int r) {
		drawPtCircle(pt.x, pt.y, r);
	}
	public void drawPtCircle(TracePt pt, int r) {
		drawPtCircle(pt.x, pt.y, r);
	}
	public void drawPtCircle(double x, double y, int r) {
		gc.strokeOval(x-r, y-r, r+r, r+r);
	}

}
