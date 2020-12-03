package fr.an.drawingboard.ui.impl;

import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.model.shape.GesturePathesCtxEval;
import fr.an.drawingboard.model.shape.PathCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.CubicBezierPathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.DiscretePointsPathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.PathElementCtxEvalVisitor;
import fr.an.drawingboard.model.shape.PathElementCtxEval.QuadBezierPathElementCtxEval;
import fr.an.drawingboard.model.shape.PathElementCtxEval.SegmentPathElementCtxEval;
import fr.an.drawingboard.model.shape.ShapeCtxEval;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.canvas.GraphicsContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ShapeDefGcRenderer {

	protected final GraphicsContext gc;

	public void draw(ShapeCtxEval shape) {
		for(val gesture : shape.gestures) {
			draw(gesture);
		}
	}

	public void draw(GesturePathesCtxEval gesture) {
		for(val path : gesture.pathes) {
			draw(path);
		}
	}

	public void draw(PathCtxEval path) {
		for(val pathElement : path.pathElements) {
			draw(pathElement);
		}
	}

	public void draw(PathElementCtxEval pathElement) {
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

}
