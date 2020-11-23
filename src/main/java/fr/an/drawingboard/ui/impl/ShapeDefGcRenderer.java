package fr.an.drawingboard.ui.impl;

import java.util.List;
import java.util.Map;

import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.PathElementDefVisitor;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.canvas.GraphicsContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ShapeDefGcRenderer {

	protected final GraphicsContext gc;

	public static NumericExprEvalCtx toExprEvalCtx(Map<ParamDef, Double> paramValues) {
		NumericExprEvalCtx res = new NumericExprEvalCtx();
		for(val e : paramValues.entrySet()) {
			res.paramValues.put(e.getKey(), e.getValue());
		}
		return res;
	}
	
	public void draw(GesturePathesDef gestureDef, Map<ParamDef, Double> paramValues) {
		NumericExprEvalCtx exprCtx = toExprEvalCtx(paramValues);
		draw(gestureDef, exprCtx);
	}
	
	public void draw(GesturePathesDef gestureDef, NumericExprEvalCtx varCtx) {
		for(PathDef path : gestureDef.pathes) {
			draw(path, varCtx);
		}
	}

	public void draw(PathDef path, NumericExprEvalCtx varCtx) {
		for(PathElementDef pathElement : path.pathElements) {
			drawPathElementDef(pathElement, varCtx);
		}
	}

	public void drawPathElementDef(PathElementDef pathElement, NumericExprEvalCtx varCtx) {
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

	public void drawSegmentDef(SegmentPathElementDef def, NumericExprEvalCtx varCtx) {
		Pt2D startPt = evalPt(def.startPt, varCtx);
		Pt2D endPt = evalPt(def.endPt, varCtx);
		gc.beginPath();
		moveTo(startPt);
		lineTo(endPt);
		gc.stroke();
	}

	public void drawDiscretePointsDef(DiscretePointsPathElementDef def, NumericExprEvalCtx varCtx) {
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
