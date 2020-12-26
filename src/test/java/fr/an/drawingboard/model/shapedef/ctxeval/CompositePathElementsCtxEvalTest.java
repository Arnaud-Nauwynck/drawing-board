package fr.an.drawingboard.model.shapedef.ctxeval;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.ctxeval.CompositePathElementsCtxEval.PtAtPathElementCtxEval;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

public class CompositePathElementsCtxEvalTest {

	@Test
	public void testPointAtDistIterator() {
		// given
		ExprBuilder b = ExprBuilder.INSTANCE;
		Expr cst0 = b.lit0();
		PtExpr pt0 = new PtExpr(cst0, cst0);
		PtExpr pt1 = new PtExpr(b.lit(10), cst0);
		PtExpr pt2 = new PtExpr(b.lit(30), cst0);
		PtExpr pt3 = new PtExpr(b.lit(30.5), cst0);
		PtExpr pt4 = new PtExpr(b.lit(50), cst0);
		
		// 0   elt0     x1   elt1       x2 x3     x4
		// +----------- + -------------  + + -----+ 
		SegmentPathElementDef pathElt0 = new SegmentPathElementDef(pt0, pt1);
		SegmentPathElementDef pathElt1 = new SegmentPathElementDef(pt1, pt2);
		SegmentPathElementDef pathElt2 = new SegmentPathElementDef(pt2, pt3);
		SegmentPathElementDef pathElt3 = new SegmentPathElementDef(pt3, pt4);

		NumericEvalCtx ctx = new NumericEvalCtx();
		PathElementCtxEval elt0 = PathElementCtxEval.create(pathElt0);
		PathElementCtxEval elt1 = PathElementCtxEval.create(pathElt1);
		PathElementCtxEval elt2 = PathElementCtxEval.create(pathElt2);
		PathElementCtxEval elt3 = PathElementCtxEval.create(pathElt3);
		elt0.update(ctx);
		elt1.update(ctx);
		elt2.update(ctx);
		elt3.update(ctx);

		CompositePathElementsCtxEval sut = new CompositePathElementsCtxEval(LsUtils.of(elt0, elt1, elt2, elt3));
		val pointAtDistIterator = sut.pointAtDistIterator();

		PtAtPathElementCtxEval ptAtDist;
		for(int i = 0; i < 50; i++) {
			// when
			ptAtDist = pointAtDistIterator.nextPtAtDist(i);
			// then
			assertPtx(i, ptAtDist.pt);
		}
		// when
		ptAtDist = pointAtDistIterator.nextPtAtDist(55);
		// then
		assertPtx(50, ptAtDist.pt);
		
	}
	
	private static void assertPtx(double expectedX, Pt2D pt) {
		Assert.assertEquals(expectedX, pt.x, 1e-9);
	}
}
