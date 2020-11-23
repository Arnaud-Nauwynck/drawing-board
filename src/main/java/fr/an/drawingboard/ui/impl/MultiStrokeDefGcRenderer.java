package fr.an.drawingboard.ui.impl;

import java.util.List;
import java.util.Map;

import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shape.ParamValue;
import fr.an.drawingboard.model.shapedef.MultiStrokeDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.StrokeDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.CubicBezierStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.DiscretePointsStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.QuadBezierStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.SegmentStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.StrokePathElementDefVisitor;
import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.canvas.GraphicsContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MultiStrokeDefGcRenderer {

	protected final GraphicsContext gc;

	public static NumericExprEvalCtx toExprEvalCtx(Map<ParamDef, Double> paramValues) {
		NumericExprEvalCtx res = new NumericExprEvalCtx();
		for(val e : paramValues.entrySet()) {
			res.paramValues.put(e.getKey(), e.getValue());
		}
		return res;
	}
	
	public void draw(MultiStrokeDef multiStrokeDef, Map<ParamDef, Double> paramValues) {
		NumericExprEvalCtx exprCtx = toExprEvalCtx(paramValues);
		draw(multiStrokeDef, exprCtx);
	}
	
	public void draw(MultiStrokeDef multiStrokeDef, NumericExprEvalCtx varCtx) {
		for(StrokeDef stroke : multiStrokeDef.strokes) {
			draw(stroke, varCtx);
		}
	}

	public void draw(StrokeDef stroke, NumericExprEvalCtx varCtx) {
		for(StrokePathElementDef pathElement : stroke.pathElements) {
			drawPathElementDef(pathElement, varCtx);
		}
	}

	public void drawPathElementDef(StrokePathElementDef pathElement, NumericExprEvalCtx varCtx) {
		pathElement.accept(new StrokePathElementDefVisitor() {
			@Override
			public void caseSegmentDef(SegmentStrokePathElementDef def) {
				drawSegmentDef(def, varCtx);
			}
			@Override
			public void caseDiscretePointsDef(DiscretePointsStrokePathElementDef def) {
				drawDiscretePointsDef(def, varCtx);
			}
			@Override
			public void caseQuadBezierDef(QuadBezierStrokePathElementDef def) {
				throw DrawingValidationUtils.notImplYet();
			}
			@Override
			public void caseCubicBezierDef(CubicBezierStrokePathElementDef def) {
				throw DrawingValidationUtils.notImplYet();
			}
		});
	}

	public void drawSegmentDef(SegmentStrokePathElementDef def, NumericExprEvalCtx varCtx) {
		Pt2D startPt = evalPt(def.startPt, varCtx);
		Pt2D endPt = evalPt(def.endPt, varCtx);
		gc.beginPath();
		moveTo(startPt);
		lineTo(endPt);
		gc.stroke();
	}

	public void drawDiscretePointsDef(DiscretePointsStrokePathElementDef def, NumericExprEvalCtx varCtx) {
		drawLinePoints(def.ptExprs, varCtx);
	}
	
	public void drawLinePoints(List<PtExpr> ptExprs, NumericExprEvalCtx varCtx) {
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

	public Pt2D evalPt(PtExpr ptExpr, NumericExprEvalCtx varCtx) {
		double x = varCtx.evalExpr(ptExpr.x);
		double y = varCtx.evalExpr(ptExpr.y);
		return new Pt2D(x, y);
	}

}
