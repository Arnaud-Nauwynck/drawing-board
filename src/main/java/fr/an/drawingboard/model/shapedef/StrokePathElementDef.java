package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.trace.StrokePathElementType;
import fr.an.drawingboard.model.var.ParametrizableEltDef;
import fr.an.drawingboard.util.DrawingValidationUtils;

public abstract class StrokePathElementDef extends ParametrizableEltDef {

	public PtExpr startPt;
	public PtExpr endPt;

	public StrokePathElementDef(PtExpr startPt, PtExpr endPt) {
		this.startPt = startPt;
		this.endPt = endPt;
	}

	public abstract StrokePathElementType getType();
	
	public abstract PtExpr ptExprAtAbscissExpr(Expr exprS, Expr expr1minusS);

	public abstract PtExpr ptExprAtAbsciss(double s);

	public abstract void accept(StrokePathElementDefVisitor visitor);
	
	public static abstract class StrokePathElementDefVisitor {

		public abstract void caseSegmentDef(SegmentStrokePathElementDef def);

		public abstract void caseDiscretePointsDef(DiscretePointsStrokePathElementDef def);

		public abstract void caseQuadBezierDef(QuadBezierStrokePathElementDef def);

		public abstract void caseCubicBezierDef(CubicBezierStrokePathElementDef def);
		
	}
	
	// --------------------------------------------------------------------------------------------

	public static class SegmentStrokePathElementDef extends StrokePathElementDef {

		public SegmentStrokePathElementDef(PtExpr startPt, PtExpr endPt) {
			super(startPt, endPt);
		}
	
		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.Segment;
		}

		@Override
		public void accept(StrokePathElementDefVisitor visitor) {
			visitor.caseSegmentDef(this);
		}

		@Override
		public PtExpr ptExprAtAbscissExpr(Expr s, Expr expr1minusS) {
			ExprBuilder b = ExprBuilder.INSTANCE;
			// pt = s * startPt + (1-s) * endPt
			Expr x = b.sum(b.mult(s, startPt.x), b.mult(expr1minusS, endPt.x));
			Expr y = b.sum(b.mult(s, startPt.y), b.mult(expr1minusS, endPt.y));
			return new PtExpr(x, y);
		}

		@Override
		public PtExpr ptExprAtAbsciss(double s) {
			double val1minusS = 1.0 - s;
			ExprBuilder b = ExprBuilder.INSTANCE;
			// pt = s * startPt + (1-s) * endPt
			Expr x = b.sum(b.mult(s, startPt.x), b.mult(val1minusS, endPt.x));
			Expr y = b.sum(b.mult(s, startPt.y), b.mult(val1minusS, endPt.y));
			return new PtExpr(x, y);
		}

	}
	
	// --------------------------------------------------------------------------------------------

	public static class DiscretePointsStrokePathElementDef extends StrokePathElementDef {

		public final List<PtExpr> ptExprs;

		public DiscretePointsStrokePathElementDef(List<PtExpr> ptExprs) {
			super(ptExprs.get(0), ptExprs.get(ptExprs.size()-1));
			this.ptExprs = new ArrayList<>(ptExprs);
		}

		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.DiscretePoints;
		}

		@Override
		public void accept(StrokePathElementDefVisitor visitor) {
			visitor.caseDiscretePointsDef(this);
		}

		public int ptExprCount() {
			return ptExprs.size();
		}
		
		public PtExpr ptExpr(int index) {
			return ptExprs.get(index);
		}

		@Override
		public PtExpr ptExprAtAbscissExpr(Expr s, Expr expr1minusS) {
			throw DrawingValidationUtils.notImplYet();
		}

		@Override
		public PtExpr ptExprAtAbsciss(double s) {
			throw DrawingValidationUtils.notImplYet();
		}

	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Quadratic Bezier Curve
	 * 
	 * similar to javafx.scene.shape.QuadCurveTo
	 */
	public static class QuadBezierStrokePathElementDef extends StrokePathElementDef {

		public PtExpr controlPt;

		public QuadBezierStrokePathElementDef(PtExpr startPt, PtExpr controlPt, PtExpr endPt) {
			super(startPt, endPt);
			this.controlPt = controlPt;
		}

		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.QuadBezier;
		}

		@Override
		public void accept(StrokePathElementDefVisitor visitor) {
			visitor.caseQuadBezierDef(this);
		}

		@Override
		public PtExpr ptExprAtAbscissExpr(Expr s, Expr expr1minusS) {
			throw DrawingValidationUtils.notImplYet();
		}

		@Override
		public PtExpr ptExprAtAbsciss(double s) {
			throw DrawingValidationUtils.notImplYet();
		}

	}


	// --------------------------------------------------------------------------------------------

	/**
	 * Quadratic Bezier Curve
	 * 
	 * similar to javafx.scene.shape.CubicCurveTo
	 */
	public static class CubicBezierStrokePathElementDef extends StrokePathElementDef {

		public PtExpr controlPt1;
		public PtExpr controlPt2;

		public CubicBezierStrokePathElementDef(PtExpr startPt, 
				PtExpr controlPt1, PtExpr controlPt2,
				PtExpr endPt) {
			super(startPt, endPt);
			this.controlPt1 = controlPt1;
			this.controlPt2 = controlPt2;
		}

		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.CubicBezier;
		}

		@Override
		public void accept(StrokePathElementDefVisitor visitor) {
			visitor.caseCubicBezierDef(this);
		}

		@Override
		public PtExpr ptExprAtAbscissExpr(Expr s, Expr expr1minusS) {
			throw DrawingValidationUtils.notImplYet();
		}

		@Override
		public PtExpr ptExprAtAbsciss(double s) {
			throw DrawingValidationUtils.notImplYet();
		}
		
	}

}
