package fr.an.drawingboard.model.shape;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.PathElementDefFunc0;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import lombok.val;

/**
 * numerical object instance of a PathElementDef for evaluating on a NumericEvalCtx
 */
public abstract class PathElementCtxEval {
	
	public static final PathElementDefFunc0<PathElementCtxEval> CREATE_FUNC = new PathElementDefFunc0<PathElementCtxEval>() {
		@Override
		public PathElementCtxEval caseSegmentDef(SegmentPathElementDef def) {
			return new SegmentPathElementCtxEval(def);
		}
		@Override
		public PathElementCtxEval caseDiscretePointsDef(DiscretePointsPathElementDef def) {
			return new DiscretePointsPathElementCtxEval(def);
		}
		@Override
		public PathElementCtxEval caseQuadBezierDef(QuadBezierPathElementDef def) {
			return new QuadBezierPathElementCtxEval(def);
		}
		@Override
		public PathElementCtxEval caseCubicBezierDef(CubicBezierPathElementDef def) {
			return new CubicBezierPathElementCtxEval(def);
		}
	};

	public Pt2D startPt;
	public Pt2D endPt;

	public static PathElementCtxEval create(PathElementDef def) {
		return def.accept(CREATE_FUNC);
	}
	
	public abstract void eval(NumericEvalCtx ctx);

	public void evalStarEndPt(NumericEvalCtx ctx, PtExpr startPtExpr, PtExpr endPtExpr) {
		this.startPt = ctx.evalPtExpr(startPtExpr);
		this.endPt = ctx.evalPtExpr(endPtExpr);
	}
	
	public static abstract class PathElementCtxEvalVisitor {
		public abstract void caseSegment(SegmentPathElementCtxEval segment);
		public abstract void caseDiscretePoints(DiscretePointsPathElementCtxEval discretePts);
		public abstract void caseQuadBezier(QuadBezierPathElementCtxEval quadBezier);
		public abstract void caseCubicBezier(CubicBezierPathElementCtxEval cubicBezier);
	}
	
	public abstract void accept(PathElementCtxEvalVisitor visitor);

	public abstract void addEnclosing(BoundingRect2DBuilder boundingRectBuilder);

	public void addEnclosingStarEndPts(BoundingRect2DBuilder boundingRectBuilder) {
		boundingRectBuilder.enclosingPts(startPt, endPt);
	}
	
	// ------------------------------------------------------------------------

	/**
	 * numerical object instance of a SegmentPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class SegmentPathElementCtxEval extends PathElementCtxEval {
		public final SegmentPathElementDef def;

		public SegmentPathElementCtxEval(SegmentPathElementDef def) {
			this.def = def;
		}
		
		@Override
		public void eval(NumericEvalCtx ctx) {
			super.evalStarEndPt(ctx, def.startPt, def.endPt);
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseSegment(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			super.addEnclosingStarEndPts(boundingRectBuilder);
		}
		
	}

	/**
	 * numerical object instance of a DiscretePointsPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class DiscretePointsPathElementCtxEval extends PathElementCtxEval {
		public final DiscretePointsPathElementDef def;
		public final Pt2D[] pts;
		public BoundingRect2D boundingRect;
		
		public DiscretePointsPathElementCtxEval(DiscretePointsPathElementDef def) {
			this.def = def;
			this.pts = new Pt2D[def.ptExprs.size()];
		}

		@Override
		public void eval(NumericEvalCtx ctx) {
			val boundingRectBuilder = BoundingRect2D.builder();
			super.evalStarEndPt(ctx, def.startPt, def.endPt);
			boundingRectBuilder.enclosingPts(startPt, endPt);
			
			for(int i = 0; i < pts.length; i++ ) {
				pts[i] = ctx.evalPtExpr(def.ptExprs.get(i));
				boundingRectBuilder.enclosingPt(pts[i]);
			}
			this.boundingRect = boundingRectBuilder.build();
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseDiscretePoints(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			super.addEnclosingStarEndPts(boundingRectBuilder);
			boundingRectBuilder.enclosingBoundingRect(boundingRect);
		}

	}

	/**
	 * numerical object instance of a QuadBezierPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class QuadBezierPathElementCtxEval extends PathElementCtxEval {
		public final QuadBezierPathElementDef def;
		public Pt2D controlPt;
		
		public QuadBezierPathElementCtxEval(QuadBezierPathElementDef def) {
			this.def = def;
		}

		@Override
		public void eval(NumericEvalCtx ctx) {
			super.evalStarEndPt(ctx, def.startPt, def.endPt);
			this.controlPt = ctx.evalPtExpr(def.controlPt);
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseQuadBezier(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			super.addEnclosingStarEndPts(boundingRectBuilder);
			boundingRectBuilder.enclosingPt(controlPt);
		}
		
	}

	/**
	 * numerical object instance of a CubicBezierPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class CubicBezierPathElementCtxEval extends PathElementCtxEval {
		public final CubicBezierPathElementDef def;
		public Pt2D controlPt1;
		public Pt2D controlPt2;
		
		public CubicBezierPathElementCtxEval(CubicBezierPathElementDef def) {
			this.def = def;
		}
		
		@Override
		public void eval(NumericEvalCtx ctx) {
			super.evalStarEndPt(ctx, def.startPt, def.endPt);
			this.controlPt1 = ctx.evalPtExpr(def.controlPt1);
			this.controlPt2 = ctx.evalPtExpr(def.controlPt2);
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseCubicBezier(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			super.addEnclosingStarEndPts(boundingRectBuilder);
			boundingRectBuilder.enclosingPts(controlPt1, controlPt2);
		}
		
	}
		
}