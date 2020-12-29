package fr.an.drawingboard.model.shapedef.obj;

import java.util.Arrays;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.geom2d.bezier.BezierEnclosingRect2DUtil;
import fr.an.drawingboard.geom2d.bezier.BezierLenUtils;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
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
public abstract class PathElementObj {
	
	public static final PathElementDefFunc0<PathElementObj> CREATE_FUNC = new PathElementDefFunc0<PathElementObj>() {
		@Override
		public PathElementObj caseSegmentDef(SegmentPathElementDef def) {
			return new SegmentPathElementCtxEval(def);
		}
		@Override
		public PathElementObj caseDiscretePointsDef(DiscretePointsPathElementDef def) {
			return new DiscretePointsPathElementCtxEval(def);
		}
		@Override
		public PathElementObj caseQuadBezierDef(QuadBezierPathElementDef def) {
			return new QuadBezierPathElementCtxEval(def);
		}
		@Override
		public PathElementObj caseCubicBezierDef(CubicBezierPathElementDef def) {
			return new CubicBezierPathElementCtxEval(def);
		}
	};

	public static PathElementObj create(PathElementDef def) {
		return def.accept(CREATE_FUNC);
	}

	// ------------------------------------------------------------------------

	private double dist;
	private boolean dirtyDist = true;
	
	public abstract Pt2D getStartPt();
	public abstract Pt2D getEndPt();

	public void update(NumericEvalCtx ctx) {
		doUpdate(ctx);
		dirtyDist = true;
	}
	protected abstract void doUpdate(NumericEvalCtx ctx);

	public double getDist() {
		if (dirtyDist) {
			this.dist = computeDist();
			dirtyDist = false;
		}
		return dist;
	}
	protected abstract double computeDist();
	
	public abstract void pointAtParam(Pt2D res, double param);
	
	public Pt2D pointAtParam(double param) {
		Pt2D res = new Pt2D();
		pointAtParam(res, param);
		return res;
	}
	
	public abstract PtExpr pointExprAtParam(double s);
	

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
	public static class SegmentPathElementCtxEval extends PathElementObj {
		public final SegmentPathElementDef def;
		public final Pt2D startPt = new Pt2D();
		public final Pt2D endPt = new Pt2D();
		
		public SegmentPathElementCtxEval(SegmentPathElementDef def) {
			this.def = def;
		}
		
		@Override
		public void doUpdate(NumericEvalCtx ctx) {
			ctx.evalPtExpr(startPt, def.startPt);
			ctx.evalPtExpr(endPt, def.endPt);
		}
		
		@Override
		protected double computeDist() {
			return startPt.distTo(endPt);
		}

		@Override
		public void pointAtParam(Pt2D res, double s) {
			res.setLinear(1-s, startPt, s, endPt);
		}

		@Override
		public PtExpr pointExprAtParam(double s) {
			ExprBuilder b = ExprBuilder.INSTANCE;
			Expr x = b.linear(1-s, def.startPt.x, s, def.endPt.x);
			Expr y = b.linear(1-s, def.startPt.y, s, def.endPt.y);
			return new PtExpr(x, y);
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
	public static class DiscretePointsPathElementCtxEval extends PathElementObj {
		public final DiscretePointsPathElementDef def;
		public final Pt2D[] pts;
		public final double[] ptsDist;
		public BoundingRect2D boundingRect;
		
		public DiscretePointsPathElementCtxEval(DiscretePointsPathElementDef def) {
			this.def = def;
			int ptsCount = def.ptExprs.size();
			this.pts = new Pt2D[ptsCount];
			this.ptsDist = new double[ptsCount];
			for(int i = 0; i < ptsCount; i++) {
				pts[i] = new Pt2D();
			}
		}

		@Override
		public void doUpdate(NumericEvalCtx ctx) {
			val boundingRectBuilder = BoundingRect2D.builder();
			ctx.evalPtExpr(pts[0], def.ptExprs.get(0));
			boundingRectBuilder.enclosingPt(pts[0]);
			this.ptsDist[0] = 0.0;
			for(int i = 1; i < pts.length; i++ ) {
				ctx.evalPtExpr(pts[i], def.ptExprs.get(i));
				boundingRectBuilder.enclosingPt(pts[i]);
				this.ptsDist[i] = ptsDist[i-1] + pts[i-1].distTo(pts[i]);
			}
			this.boundingRect = boundingRectBuilder.build();
		}

		@Override
		protected double computeDist() {
			double sum = 0;
			Pt2D prev = pts[0];
			for(int i = 1; i < pts.length; i++ ) {
				Pt2D pt = pts[i];
				sum += prev.distTo(pt);
				prev = pt;
			}
			return sum;
		}

		@Override
		public void pointAtParam(Pt2D res, double param) {
			if (param <= 0.0) {
				res.set(pts[0]);
			} else if (param >= 1.0) {
				res.set(pts[pts.length-1]);
			} else {
				double totalDist = ptsDist[pts.length-1];
				double targetDist = totalDist * param;
				int foundIndex = Arrays.binarySearch(ptsDist, targetDist);
				if (foundIndex > 0) {
					// found exact
					res.set(pts[foundIndex]);
				} else {
					// found between pts
					int indexBefore = -(foundIndex+1);
					double distBefore = ptsDist[indexBefore];
					double distAfter = ptsDist[indexBefore+1];
					double inv = 1.0 / (distAfter - distBefore);
					double c0 = (targetDist-distBefore)*inv;
					double c1 = (distAfter-targetDist)*inv;
					res.setLinear(c0, pts[indexBefore], c1, pts[indexBefore+1]);
				}
			}
		}

		@Override
		public PtExpr pointExprAtParam(double param) {
			ExprBuilder b = ExprBuilder.INSTANCE;
			if (param <= 0.0) {
				return def.startPt;
			} else if (param >= 1.0) {
				return def.endPt;
			} else {
				double totalDist = ptsDist[pts.length-1];
				double targetDist = totalDist * param;
				int foundIndex = Arrays.binarySearch(ptsDist, targetDist);
				if (foundIndex > 0) {
					// found exact
					return def.ptExpr(foundIndex);
				} else {
					// found between pts
					int indexBefore = -(foundIndex+1);
					double distBefore = ptsDist[indexBefore];
					double distAfter = ptsDist[indexBefore+1];
					double inv = 1.0 / (distAfter - distBefore);
					double c0 = (targetDist-distBefore)*inv;
					double c1 = (distAfter-targetDist)*inv;
					PtExpr ptBefore = def.ptExpr(indexBefore);
					PtExpr ptAfter = def.ptExpr(indexBefore+1);
					Expr x = b.linear(c0, ptBefore.x, c1, ptAfter.x);
					Expr y = b.linear(c0, ptBefore.y, c1, ptAfter.y);
					return new PtExpr(x, y);
				}
			}
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
	public static class QuadBezierPathElementCtxEval extends PathElementObj {
		public final QuadBezierPathElementDef def;
		public final QuadBezier2D bezier = new QuadBezier2D();
		private BoundingRect2D boundingRect;
		
		public QuadBezierPathElementCtxEval(QuadBezierPathElementDef def) {
			this.def = def;
		}

		@Override
		public void doUpdate(NumericEvalCtx ctx) {
			ctx.evalPtExpr(bezier.startPt, def.startPt);
			ctx.evalPtExpr(bezier.controlPt, def.controlPt);
			ctx.evalPtExpr(bezier.endPt, def.endPt);
		
			this.boundingRect = null;
		}

		@Override
		protected double computeDist() {
			return BezierLenUtils.len(bezier);
		}

		@Override
		public void pointAtParam(Pt2D res, double param) {
			bezier.eval(res, param);
		}

		@Override
		public PtExpr pointExprAtParam(double s) {
			return QuadBezier2D.pointExprAtParam(s, def.startPt, def.controlPt, def.endPt);
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
	public static class CubicBezierPathElementCtxEval extends PathElementObj {
		public final CubicBezierPathElementDef def;
		public final CubicBezier2D bezier = new CubicBezier2D();
		private BoundingRect2D boundingRect;
		
		public CubicBezierPathElementCtxEval(CubicBezierPathElementDef def) {
			this.def = def;
		}
		
		@Override
		public void doUpdate(NumericEvalCtx ctx) {
			ctx.evalPtExpr(bezier.startPt, def.startPt);
			ctx.evalPtExpr(bezier.p1, def.controlPt1);
			ctx.evalPtExpr(bezier.p2, def.controlPt2);
			ctx.evalPtExpr(bezier.endPt, def.endPt);
		
			this.boundingRect = null;
		}

		@Override
		protected double computeDist() {
			return BezierLenUtils.len(bezier);
		}

		@Override
		public void pointAtParam(Pt2D res, double param) {
			bezier.eval(res, param);
		}

		@Override
		public PtExpr pointExprAtParam(double s) {
			return CubicBezier2D.pointExprAtParam(s, def.startPt, def.controlPt1, def.controlPt2, def.endPt);
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