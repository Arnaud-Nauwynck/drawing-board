package fr.an.drawingboard.model.expr.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.var.VarDef;

public class DerivativeExprBuilderTest {
	
	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	@Test
	public void testDerivBy_varX() {
		// given
		VarDef xDef = new VarDef("x");
		VarDef yDef = new VarDef("y");
		VarDef zDef = new VarDef("z");
		VariableExpr x = xDef.expr;
		VariableExpr y = yDef.expr;
		VariableExpr z = zDef.expr;
		// when-then
		Assert.assertSame(b.lit0(), DerivativeExprBuilder.deriveBy(b.lit(123), x));

		Assert.assertSame(b.lit1(), DerivativeExprBuilder.deriveBy(x, x));
		Assert.assertSame(b.lit0(), DerivativeExprBuilder.deriveBy(y, x));

		Assert.assertSame(b.lit1(), DerivativeExprBuilder.deriveBy(b.sum(x, y), x));
		Assert.assertSame(b.lit1(), DerivativeExprBuilder.deriveBy(b.sum(x, y, z), x));
		Assert.assertSame(b.lit1(), DerivativeExprBuilder.deriveBy(b.sum(y, x, z), x));
		Expr expr_1_plus_1 = DerivativeExprBuilder.deriveBy(b.sum(x, x), x);
		Assert.assertEquals(b.sum(b.lit1(), b.lit1()), expr_1_plus_1);
		
		Assert.assertSame(y, DerivativeExprBuilder.deriveBy(b.mult(x, y), x));
		Assert.assertSame(y, DerivativeExprBuilder.deriveBy(b.mult(y, x), x));

		Expr mult_y_z = DerivativeExprBuilder.deriveBy(b.mult(x, y, z), x);
		Assert.assertEquals(b.mult(y, z), mult_y_z);

		mult_y_z = DerivativeExprBuilder.deriveBy(b.mult(y, x, z), x);
		Assert.assertEquals(b.mult(y, z), mult_y_z);
	}
	
}
