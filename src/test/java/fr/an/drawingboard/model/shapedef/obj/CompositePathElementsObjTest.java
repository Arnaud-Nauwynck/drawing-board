package fr.an.drawingboard.model.shapedef.obj;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.model.shapedef.obj.CompositePathElementsObj;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj;
import fr.an.drawingboard.model.shapedef.obj.CompositePathElementsObj.PtAtPathElementObj;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

public class CompositePathElementsObjTest {

	@Test
	public void testPointAtParamIterator() {
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
		PathElementObj elt0 = PathElementObj.create(pathElt0);
		PathElementObj elt1 = PathElementObj.create(pathElt1);
		PathElementObj elt2 = PathElementObj.create(pathElt2);
		PathElementObj elt3 = PathElementObj.create(pathElt3);
		elt0.update(ctx);
		elt1.update(ctx);
		elt2.update(ctx);
		elt3.update(ctx);

		List<PathElementObj> elts = LsUtils.of(elt0, elt1, elt2, elt3);
		val splitParams = CompositePathElementsObj.splitParamProportionalToDists(elts);
		CompositePathElementsObj sut = new CompositePathElementsObj(splitParams, elts);
		val pointAtDistIterator = sut.pointAtParamIterator();

		PtAtPathElementObj ptAtParam;
		int N = 50;
		double paramStep = 1.0/N;
		double param = 0;
		for(int i = 0; i < N; i++,param+=paramStep) {
			// when
			ptAtParam = pointAtDistIterator.nextPtAtParam(param);
			// then
			assertPtx(i, ptAtParam.pt);
		}
		// when
		ptAtParam = pointAtDistIterator.nextPtAtParam(1.1);
		// then
		assertPtx(50, ptAtParam.pt);
		
	}
	
	private static void assertPtx(double expectedX, Pt2D pt) {
		Assert.assertEquals(expectedX, pt.x, 1e-9);
	}
}
