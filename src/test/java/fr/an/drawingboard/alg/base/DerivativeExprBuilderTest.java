package fr.an.drawingboard.alg.base;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.math.algo.base.DerivativeExprBuilder;
import fr.an.drawingboard.math.algo.base.FlattenExprTransformer;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;

public class DerivativeExprBuilderTest {
	
	private static final ExprBuilder B = ExprBuilder.INSTANCE;
	private static final VarDef xDef = new VarDef("x");
	private static final VarDef yDef = new VarDef("y");
	private static final VarDef zDef = new VarDef("z");
	private static final VariableExpr x = xDef.expr;
	private static final VariableExpr y = yDef.expr;
	private static final VariableExpr z = zDef.expr;
	
	@Test
	public void testDerivBy_varX() {
		// given
		// when-then
		Assert.assertSame(B.lit0(), DerivativeExprBuilder.deriveBy(B.lit(123), x));

		Assert.assertSame(B.lit1(), DerivativeExprBuilder.deriveBy(x, x));
		Assert.assertSame(B.lit0(), DerivativeExprBuilder.deriveBy(y, x));

		Assert.assertSame(B.lit1(), DerivativeExprBuilder.deriveBy(B.sum(x, y), x));
		Assert.assertSame(B.lit1(), DerivativeExprBuilder.deriveBy(B.sum(x, y, z), x));
		Assert.assertSame(B.lit1(), DerivativeExprBuilder.deriveBy(B.sum(y, x, z), x));
		Assert.assertEquals(new SumExpr(Arrays.asList(B.lit2())), DerivativeExprBuilder.deriveBy(B.sum(x, x), x)); // TODO should be 2, not sum(2)
		
		Assert.assertSame(y, DerivativeExprBuilder.deriveBy(B.mult(x, y), x));
		Assert.assertSame(y, DerivativeExprBuilder.deriveBy(B.mult(y, x), x));

		Expr mult_y_z = DerivativeExprBuilder.deriveBy(B.mult(x, y, z), x);
		Assert.assertEquals(B.mult(y, z), mult_y_z);

		mult_y_z = DerivativeExprBuilder.deriveBy(B.mult(y, x, z), x);
		Assert.assertEquals(B.mult(y, z), mult_y_z);
	}

	@Test
	public void testDerive_lin() {
		// given
		Expr expr = B.sum( // 5 x + 6
				B.mult(5, x), //
				B.lit(6));
		// when
		Expr deriv = DerivativeExprBuilder.deriveBy(expr, xDef);
		// then
		Assert.assertEquals(B.lit(5), deriv);
	}

	@Test
	public void testDerive_quad() {
		// given
		Expr expr = B.sum( // 4 x^2 + 5 x + 6
				B.mult(4, x, x), //
				B.mult(5, x), //
				B.lit(6));
		// when
		Expr deriv = DerivativeExprBuilder.deriveBy(expr, xDef);
		// then
		Assert.assertEquals(B.sum(
				B.sum(B.sum(B.mult(x, B.lit(4))), B.mult(x, B.lit(4))), // (x*4 + x*4)  .. TODO unwrap sum!
				B.lit(5)) // + 5
				, deriv);
		
		Expr derivFlatten = FlattenExprTransformer.flattenExpr(deriv);
		Assert.assertEquals(B.sum(
				B.mult(x, B.lit(4)), // x*4
				B.mult(x, B.lit(4)), // x*4
				B.lit(5)), // 5
				derivFlatten);
		
		// Assert.assertEquals(B.sum(B.mult(8, x), B.lit(5)), deriv);
		
		// when
		Expr derivDeriv = FlattenExprTransformer.flattenExpr(DerivativeExprBuilder.deriveBy(derivFlatten, xDef));
		// then .. 8
		Assert.assertEquals(B.lit(8), derivDeriv);
		
	}

	@Test
	public void testDerive_cub() {
		// given
		Expr x_m1 = B.sum(x, B.lit(-1)); // x-1
		Expr x_m2 = B.sum(x, B.lit(-2)); // x-2
		Expr x_m3 = B.sum(x, B.lit(-3)); // x-3
		Expr expr = B.mult(x_m1, x_m2, x_m3); // (x-1)*(x-2)(x-3)
		// when
		Expr deriv = DerivativeExprBuilder.deriveBy(expr, xDef);
		// then
		Assert.assertEquals(B.sum( //
				B.mult(x_m2, x_m3), // 1*(x-2)*(x-3)
				B.mult(x_m1, x_m3), // (x-1)*1*(x-3)
				B.mult(x_m1, x_m2) // (x-1)*(x-2)*1
				), deriv);
		
		Expr derivFlatten = FlattenExprTransformer.flattenExpr(deriv); // 
		
		// when
		Expr deriv2 = FlattenExprTransformer.flattenExpr(DerivativeExprBuilder.deriveBy(derivFlatten, xDef)); 
		// then 
		
		// when
		Expr deriv3 = FlattenExprTransformer.flattenExpr(DerivativeExprBuilder.deriveBy(deriv2, xDef)); 
		// then 
		Assert.assertEquals(B.lit(6), deriv3);
	}

	@Test
	public void testDerive_x4() {
		// given
		Expr expr = B.mult(x, x, x, x); // x^4
		// when
		Expr deriv = DerivativeExprBuilder.deriveBy(expr, xDef); // 4 x^3
		Expr derivFlatten = FlattenExprTransformer.flattenExpr(deriv); // 
		// then
		Expr x3 = B.mult(x, x, x);
		Assert.assertEquals(B.sum(x3, x3, x3, x3), deriv);
		
		// when
		Expr deriv2 = FlattenExprTransformer.flattenExpr(DerivativeExprBuilder.deriveBy(derivFlatten, xDef)); 
		// then .. 12 x^2
		
		// when
		Expr deriv3 = FlattenExprTransformer.flattenExpr(DerivativeExprBuilder.deriveBy(deriv2, xDef)); 
		// then .. 24 x
		// Assert.assertEquals(B.lit(6), deriv3);

		// when
		Expr deriv4 = FlattenExprTransformer.flattenExpr(DerivativeExprBuilder.deriveBy(deriv3, xDef)); 
		// then .. 24
		Assert.assertEquals(B.lit(24), deriv4);

	}
	
}
