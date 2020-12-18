package fr.an.drawingboard.model.shapedef.ctxeval;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.geom2d.bezier.BezierEnclosingRect2DUtil;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.PathElementDefFunc0;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
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

	public abstract Pt2D getStartPt();
	public abstract Pt2D getEndPt();

	public static PathElementCtxEval create(PathElementDef def) {
		return def.accept(CREATE_FUNC);
	}
	
	public abstract void eval(NumericEvalCtx ctx);

	public static abstract class PathElementCtxEvalVisitor {
		public abstract void caseSegment(SegmentPathElementCtxEval segment);
		public abstract void caseDiscretePoints(DiscretePointsPathElementCtxEval discretePts);
		public abstract void caseQuadBezier(QuadBezierPathElementCtxEval quadBezier);
		public abstract void caseCubicBezier(CubicBezierPathElementCtxEval cubicBezier);
	}
	
	public abstract void accept(PathElementCtxEvalVisitor visitor);

	public abstract void addEnclosing(BoundingRect2DBuilder boundingRectBuilder);

	public void addEnclosingStarEndPts(BoundingRect2DBuilder boundingRectBuilder) {
		boundingRectBuilder.enclosingPts(getStartPt(), getEndPt());
	}
	
	// ------------------------------------------------------------------------

	/**
	 * numerical object instance of a SegmentPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class SegmentPathElementCtxEval extends PathElementCtxEval {
		public final SegmentPathElementDef def;
		public final Pt2D startPt = new Pt2D();
		public final Pt2D endPt = new Pt2D();
		
		public SegmentPathElementCtxEval(SegmentPathElementDef def) {
			this.def = def;
		}
		
		@Override
		public void eval(NumericEvalCtx ctx) {
			ctx.evalPtExpr(startPt, def.startPt);
			ctx.evalPtExpr(endPt, def.endPt);
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseSegment(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			super.addEnclosingStarEndPts(boundingRectBuilder);
		}

		@Override
		public Pt2D getStartPt() {
			return startPt;
		}

		@Override
		public Pt2D getEndPt() {
			return endPt;
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
			int ptsCount = def.ptExprs.size();
			this.pts = new Pt2D[ptsCount];
			for(int i = 0; i < ptsCount; i++) {
				pts[i] = new Pt2D();
			}
		}

		@Override
		public void eval(NumericEvalCtx ctx) {
			val boundingRectBuilder = BoundingRect2D.builder();
			for(int i = 0; i < pts.length; i++ ) {
				ctx.evalPtExpr(pts[i], def.ptExprs.get(i));
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
			boundingRectBuilder.enclosingBoundingRect(boundingRect);
		}

		@Override
		public Pt2D getStartPt() {
			return pts[0];
		}

		@Override
		public Pt2D getEndPt() {
			return pts[pts.length-1];
		}

		
	}

	/**
	 * numerical object instance of a QuadBezierPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class QuadBezierPathElementCtxEval extends PathElementCtxEval {
		public final QuadBezierPathElementDef def;
		public final QuadBezier2D bezier = new QuadBezier2D();
		private BoundingRect2D boundingRect;
		
		public QuadBezierPathElementCtxEval(QuadBezierPathElementDef def) {
			this.def = def;
		}

		@Override
		public void eval(NumericEvalCtx ctx) {
			ctx.evalPtExpr(bezier.startPt, def.startPt);
			ctx.evalPtExpr(bezier.controlPt, def.controlPt);
			ctx.evalPtExpr(bezier.endPt, def.endPt);
		
			this.boundingRect = null;
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseQuadBezier(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			if (boundingRect == null) {
				val b = BoundingRect2D.builder();
				BezierEnclosingRect2DUtil.bestEnclosing_QuadBezier(b, bezier);
				boundingRect = b.build();
			}
			boundingRectBuilder.enclosingBoundingRect(boundingRect);
		}
		
		@Override
		public Pt2D getStartPt() {
			return bezier.startPt;
		}

		@Override
		public Pt2D getEndPt() {
			return bezier.endPt;
		}
	}

	/**
	 * numerical object instance of a CubicBezierPathElementDef for evaluating on a NumericEvalCtx
	 */
	public static class CubicBezierPathElementCtxEval extends PathElementCtxEval {
		public final CubicBezierPathElementDef def;
		public final CubicBezier2D bezier = new CubicBezier2D();
		private BoundingRect2D boundingRect;
		
		public CubicBezierPathElementCtxEval(CubicBezierPathElementDef def) {
			this.def = def;
		}
		
		@Override
		public void eval(NumericEvalCtx ctx) {
			ctx.evalPtExpr(bezier.startPt, def.startPt);
			ctx.evalPtExpr(bezier.p1, def.controlPt1);
			ctx.evalPtExpr(bezier.p2, def.controlPt2);
			ctx.evalPtExpr(bezier.endPt, def.endPt);
		
			this.boundingRect = null;
		}

		@Override
		public void accept(PathElementCtxEvalVisitor visitor) {
			visitor.caseCubicBezier(this);
		}

		@Override
		public void addEnclosing(BoundingRect2DBuilder boundingRectBuilder) {
			if (boundingRect == null) {
				val b = BoundingRect2D.builder();
				BezierEnclosingRect2DUtil.bestEnclosing_CubicBezier(b, bezier);
				boundingRect = b.build();
			}
			boundingRectBuilder.enclosingBoundingRect(boundingRect);
		}

		@Override
		public Pt2D getStartPt() {
			return bezier.startPt;
		}

		@Override
		public Pt2D getEndPt() {
			return bezier.endPt;
		}
		
	}
		
}