package fr.an.drawingboard.ui.impl;

import java.util.List;
import java.util.Map;

import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.PathElementDefVisitor;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.canvas.GraphicsContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ShapeDefGcRenderer {

	protected final GraphicsContext gc;

	public static NumericEvalCtx toExprEvalCtx(Map<VarDef, Double> paramValues) {
		NumericEvalCtx res = new NumericEvalCtx();
		for(val e : paramValues.entrySet()) {
			res.put(e.getKey(), e.getValue());
		}
		return res;
	}
	
	public void draw(GesturePathesDef gestureDef, Map<VarDef, Double> paramValues) {
		NumericEvalCtx exprCtx = toExprEvalCtx(paramValues);
		draw(gestureDef, exprCtx);
	}
	
	public void draw(GesturePathesDef gestureDef, NumericEvalCtx varCtx) {
		for(PathDef path : gestureDef.pathes) {
			draw(path, varCtx);
		}
	}

	public void draw(PathDef path, NumericEvalCtx varCtx) {
		for(PathElementDef pathElement : path.pathElements) {
			drawPathElementDef(pathElement, varCtx);
		}
	}

	public void drawPathElementDef(PathElementDef pathElement, NumericEvalCtx varCtx) {
		pathElement.accept(new PathElementDefVisitor() {
			@Override
			public void caseSegmentDef(SegmentPathElementDef def) {
				drawSegmentDef(def, varCtx);
			}
			@Override
			public void caseDiscretePointsDef(DiscretePointsPathElementDef def) {
				drawDiscretePointsDef(def, varCtx);
			}
			@Override
			public void caseQuadBezierDef(QuadBezierPathElementDef def) {
				throw DrawingValidationUtils.notImplYet();
			}
			@Override
			public void caseCubicBezierDef(CubicBezierPathElementDef def) {
				throw DrawingValidationUtils.notImplYet();
			}
		});
	}

	public void drawSegmentDef(SegmentPathElementDef def, NumericEvalCtx varCtx) {
		Pt2D startPt = evalPt(def.startPt, varCtx);
		Pt2D endPt = evalPt(def.endPt, varCtx);
		gc.beginPath();
		moveTo(startPt);
		lineTo(endPt);
		gc.stroke();
	}

	public void drawDiscretePointsDef(DiscretePointsPathElementDef def, NumericEvalCtx varCtx) {
		drawLinePoints(def.ptExprs, varCtx);
	}
	
	public void drawLinePoints(List<PtExpr> ptExprs, NumericEvalCtx varCtx) {
		int ptsCount = ptExprs.size();
		Pt2D prevPt = evalPt(ptExprs.get(0), varCtx);
		gc.beginPath();
		moveTo(prevPt);
		for(int i = 1; i < ptsCount; i++) {
			Pt2D pt = evalPt(ptExprs.get(i), varCtx);
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

	public Pt2D evalPt(PtExpr ptExpr, NumericEvalCtx varCtx) {
		double x = varCtx.evalExpr(ptExpr.x);
		double y = varCtx.evalExpr(ptExpr.y);
		return new Pt2D(x, y);
	}

}
