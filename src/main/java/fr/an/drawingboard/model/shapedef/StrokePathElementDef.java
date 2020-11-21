package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.trace.StrokePathElementType;
import fr.an.drawingboard.model.var.ParametrizableEltDef;

public abstract class StrokePathElementDef extends ParametrizableEltDef {

	public PtExpr startPt;
	public PtExpr endPt;

	public StrokePathElementDef(PtExpr startPt, PtExpr endPt) {
		this.startPt = startPt;
		this.endPt = endPt;
	}

	public abstract StrokePathElementType getType();
	
	// --------------------------------------------------------------------------------------------

	public static class SegmentStrokePathElementDef extends StrokePathElementDef {

		public SegmentStrokePathElementDef(PtExpr startPt, PtExpr endPt) {
			super(startPt, endPt);
		}
	
		@Override
		public StrokePathElementType getType() {
			return StrokePathElementType.Segment;
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

		public int ptExprCount() {
			return ptExprs.size();
		}
		
		public PtExpr ptExpr(int index) {
			return ptExprs.get(index);
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

	}

}
