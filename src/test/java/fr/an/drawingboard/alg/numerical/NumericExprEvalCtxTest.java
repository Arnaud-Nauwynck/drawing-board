package fr.an.drawingboard.alg.numerical;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;

public class NumericExprEvalCtxTest {
	
	private static final ExprBuilder b = ExprBuilder.INSTANCE;

	@Test
	public void testEvalExpr() {
		// given
		NumericEvalCtx ctx = new NumericEvalCtx();
		VarDef xDef = new VarDef("x");
		VariableExpr x = xDef.expr;
		double xVal = 12.0;
		ctx.put(x, xVal);
		// when-then
		assertEqualsEval(1.0, ctx, b.lit1());
		assertEqualsEval(xVal, ctx, x);
		assertEqualsEval(1+xVal, ctx, b.sum(b.lit1(), x));
		assertEqualsEval(2*xVal, ctx, b.mult(b.lit2(), x));
	}
	
	private static void assertEqualsEval(double expected, NumericEvalCtx ctx, Expr exprToEval) {
		double actual = ctx.evalExpr(exprToEval);
		Assert.assertEquals(expected, actual, 1e-9);
	}

}
