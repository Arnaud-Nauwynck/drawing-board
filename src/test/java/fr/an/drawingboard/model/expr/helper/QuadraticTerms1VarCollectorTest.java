package fr.an.drawingboard.model.expr.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.helper.QuadraticTerms1VarCollector.QuadraticForm1D;
import fr.an.drawingboard.model.var.VarDef;

public class QuadraticTerms1VarCollectorTest {

	private static ExprBuilder B = ExprBuilder.INSTANCE;
	private static final double PREC = 1e-9;
	
	@Test
	public void testExtractQuadTerms_axb_cxd() {
		// given
		VarDef xDef = new VarDef("x");
		VarDef aDef = new VarDef("a"), bDef = new VarDef("b"), cDef = new VarDef("c"), dDef = new VarDef("d"), eDef = new VarDef("e");
		Expr x = xDef.expr;
		Expr a = aDef.expr, b = bDef.expr, c = cDef.expr, d = dDef.expr, e = eDef.expr;
		
		Expr ax_plus_b = B.sum(B.mult(a, x), b); // = (a.x+b)
		Expr cx_plus_d = B.sum(B.mult(c, x), d); // = (c.x+d)
		Expr expr = B.mult(ax_plus_b, cx_plus_d, e); // =(a.x+b)(c.x+d)e
		
		// when
		QuadraticForm1D quadForm = QuadraticTerms1VarCollector.extractQuadTerms(expr, xDef);

		// then
		// expand  => sum(mult(e, (a * x), (c * x)), mult(e, (a * x), d), mult(e, b, (c * x)), mult(e, b, d))
		// flatten => sum(mult(e, a, x, c, x), mult(e, a, x, d), mult(e, b, c, x), mult(e, b, d))
		// res =   (a.c.e) x^2 +  (ade+bce) x + (bde)
		Assert.assertEquals(B.mult(e, a, c), quadForm.quadTermExpr);
		Assert.assertEquals(B.sum(B.mult(e, a, d), B.mult(e, b, c)), quadForm.linTermExpr);
		Assert.assertEquals(B.mult(e, b, d), quadForm.constTermExpr);

		Assert.assertEquals(0.0, quadForm.quadTermLiteral, PREC);
		Assert.assertEquals(0.0, quadForm.linTermLiteral, PREC);
		Assert.assertEquals(0.0, quadForm.constTermLiteral, PREC);
	}
	
	@Test
	public void testExtractQuadTerms_2x3_4x5() {
		// given
		VarDef xDef = new VarDef("x");
		Expr x = xDef.expr;
		
		Expr _2x_plus_3 = B.sum(B.mult(B.lit2(), x), B.lit(3)); // = (2 x + 3)
		Expr _4x_plus_5 = B.sum(B.mult(B.lit(4), x), B.lit(5)); // = (4 x + 5)
		Expr expr = B.mult(_2x_plus_3, _4x_plus_5, B.lit(2)); // = (2 x + 3)(4 x + 5) 2
		
		// when
		QuadraticForm1D quadForm = QuadraticTerms1VarCollector.extractQuadTerms(expr, xDef);

		// then
		// res = 16 x^2 + 44 x + 30
		Assert.assertEquals(B.lit(0), quadForm.quadTermExpr);
		Assert.assertEquals(B.lit(0), quadForm.linTermExpr);
		Assert.assertEquals(B.lit(0), quadForm.constTermExpr);

		Assert.assertEquals(16.0, quadForm.quadTermLiteral, PREC);
		Assert.assertEquals(44.0, quadForm.linTermLiteral, PREC);
		Assert.assertEquals(30.0, quadForm.constTermLiteral, PREC);
	}

}
