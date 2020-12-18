package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.paramdef.ParametrizableEltDef;
import fr.an.drawingboard.model.trace.TracePathElementType;
import fr.an.drawingboard.util.DrawingValidationUtils;

/**
 * definition (algebric expr) of a path element
 * similar to javafx.scene.shape.PathElement
 *
 * 
 */
public abstract class PathElementDef extends ParametrizableEltDef {

	public PtExpr startPt;
	public PtExpr endPt;

	public PathElementDef(PtExpr startPt, PtExpr endPt) {
		this.startPt = startPt;
		this.endPt = endPt;
	}

	public abstract TracePathElementType getType();
	
	public abstract PtExpr ptExprAtAbscissExpr(Expr exprS, Expr expr1minusS);

	public abstract PtExpr ptExprAtAbsciss(double s);

	public abstract void accept(PathElementDefVisitor visitor);

	public abstract <TRes> TRes accept(PathElementDefFunc0<TRes> visitor);

	public static abstract class PathElementDefVisitor {

		public abstract void caseSegmentDef(SegmentPathElementDef def);

		public abstract void caseDiscretePointsDef(DiscretePointsPathElementDef def);

		public abstract void caseQuadBezierDef(QuadBezierPathElementDef def);

		public abstract void caseCubicBezierDef(CubicBezierPathElementDef def);
		
	}

	public static abstract class PathElementDefFunc0<TRes> {

		public abstract TRes caseSegmentDef(SegmentPathElementDef def);

		public abstract TRes caseDiscretePointsDef(DiscretePointsPathElementDef def);

		public abstract TRes caseQuadBezierDef(QuadBezierPathElementDef def);

		public abstract TRes caseCubicBezierDef(CubicBezierPathElementDef def);
		
	}

	// --------------------------------------------------------------------------------------------

	public static class SegmentPathElementDef extends PathElementDef {

		public SegmentPathElementDef(PtExpr startPt, PtExpr endPt) {
			super(startPt, endPt);
		}
	
		@Override
		public TracePathElementType getType() {
			return TracePathElementType.Segment;
		}

		@Override
		public void accept(PathElementDefVisitor visitor) {
			visitor.caseSegmentDef(this);
		}

		@Override
		public <TRes> TRes accept(PathElementDefFunc0<TRes> visitor) {
			return visitor.caseSegmentDef(this);
		}

		@Override
		public PtExpr ptExprAtAbscissExpr(Expr s, Expr expr1minusS) {
			ExprBuilder b = ExprBuilder.INSTANCE;
			// pt = (1-s) * startPt + s * endPt
			Expr x = b.sum(b.mult(expr1minusS, startPt.x), b.mult(s, endPt.x));
			Expr y = b.sum(b.mult(expr1minusS, startPt.y), b.mult(s, endPt.y));
			return new PtExpr(x, y);
		}

		@Override
		public PtExpr ptExprAtAbsciss(double s) {
			double val1minusS = 1.0 - s;
			ExprBuilder b = ExprBuilder.INSTANCE;
			// pt = (1-s) * startPt + s * endPt
			Expr x = b.sum(b.mult(val1minusS, startPt.x), b.mult(s, endPt.x));
			Expr y = b.sum(b.mult(val1minusS, startPt.y), b.mult(s, endPt.y));
			return new PtExpr(x, y);
		}

	}
	
	// --------------------------------------------------------------------------------------------

	public static class DiscretePointsPathElementDef extends PathElementDef {

		public final List<PtExpr> ptExprs;

		public DiscretePointsPathElementDef(List<PtExpr> ptExprs) {
			super(ptExprs.get(0), ptExprs.get(ptExprs.size()-1));
			this.ptExprs = new ArrayList<>(ptExprs);
		}

		@Override
		public TracePathElementType getType() {
			return TracePathElementType.DiscretePoints;
		}

		@Override
		public void accept(PathElementDefVisitor visitor) {
			visitor.caseDiscretePointsDef(this);
		}

		@Override
		public <TRes> TRes accept(PathElementDefFunc0<TRes> visitor) {
			return visitor.caseDiscretePointsDef(this);
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
	public static class QuadBezierPathElementDef extends PathElementDef {

		public PtExpr controlPt;

		public QuadBezierPathElementDef(PtExpr startPt, PtExpr controlPt, PtExpr endPt) {
			super(startPt, endPt);
			this.controlPt = controlPt;
		}

		@Override
		public TracePathElementType getType() {
			return TracePathElementType.QuadBezier;
		}

		@Override
		public void accept(PathElementDefVisitor visitor) {
			visitor.caseQuadBezierDef(this);
		}

		@Override
		public <TRes> TRes accept(PathElementDefFunc0<TRes> visitor) {
			return visitor.caseQuadBezierDef(this);
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
	public static class CubicBezierPathElementDef extends PathElementDef {

		public PtExpr controlPt1;
		public PtExpr controlPt2;

		public CubicBezierPathElementDef(PtExpr startPt, 
				PtExpr controlPt1, PtExpr controlPt2,
				PtExpr endPt) {
			super(startPt, endPt);
			this.controlPt1 = controlPt1;
			this.controlPt2 = controlPt2;
		}

		@Override
		public TracePathElementType getType() {
			return TracePathElementType.CubicBezier;
		}

		@Override
		public void accept(PathElementDefVisitor visitor) {
			visitor.caseCubicBezierDef(this);
		}

		@Override
		public <TRes> TRes accept(PathElementDefFunc0<TRes> visitor) {
			return visitor.caseCubicBezierDef(this);
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
