package fr.an.drawingboard.math.numeric.optim;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import lombok.val;

public class NumericTailorDeriv2Quad1DOptimizeStepTest {

	private static final ExprBuilder B = ExprBuilder.INSTANCE;
	private static final double	PREC = 1e-9;
	private static final VarDef xDef = new VarDef("x");
	private static final Expr x = xDef.expr;
	
	private final NumericTailorDeriv2Quad1DOptimizeStep sut = 
			new NumericTailorDeriv2Quad1DOptimizeStep(xDef);
	
	private NumericEvalCtx ctx = createNumericExprEvalCtx(0.0);

	private static NumericEvalCtx createNumericExprEvalCtx(double xValue) {
		NumericEvalCtx res = new NumericEvalCtx();
		res.put(xDef, xValue);
		return res;
	}

	@Test
	public void testOptimStep_PureNumeric_ExactQuad() {
		// given
		Expr expr = B.sum(B.mult(2.0, x, x), B.mult(3, x), B.lit(4)); // 2 x^2 + 3 x + 4
		double prevRes = ctx.evalExpr(expr);
		
		// when
		sut.optimStep(expr, ctx, prevRes);
		
		// then
		// x = -b/(2a) = -3/4
		double xValue = ctx.getEval(xDef);
		Assert.assertEquals(-3.0/4, xValue, PREC);
	}

	@Test
	public void testOptimStep_PureNumeric_NotQuad() {
		// given
		Expr expr = B.sum( // 2 x^4 + 3 x^3 + 4 x^2 + 5 x + 2.16
				B.mult(2.0, x, x, x, x), //
				B.mult(3.0, x, x, x), //
				B.mult(4.0, x, x), //
				B.mult(5, x), //
				B.lit(2.16));
		// when-then
		doTestOptimStep_PureNumeric_NotQuad(expr, 4, -0.8285177655516776, -6.05094548e-4); // TODO use real value from math solver
	}

	@Test
	public void testOptimStep2_PureNumeric_NotQuad() {
		// given
		Expr expr = B.sum( // 7 x^6 + 6 x^5 + 5 x ^4 + 4 x^3 + 2 x + 1
				B.mult(7.0, x, x, x, x, x, x), //
				B.mult(6.0, x, x, x, x, x), //
				B.mult(5.0, x, x, x, x), //
				B.mult(4.0, x, x, x), //
				B.mult(3.0, x, x), //
				B.mult(2, x), //
				B.lit(1));
		
		// when-then
		doTestOptimStep_PureNumeric_NotQuad(expr, 5, -0.50850724272, 0.48410645091698534); // TODO use real value from math solver
	}
	
	private void doTestOptimStep_PureNumeric_NotQuad(Expr expr, 
			int expectedMaxFinishedStep,
			double expectedX,
			double expectedRes
			) {
		double prevRes = ctx.evalExpr(expr);

		val prep = sut.prepareOptimStep(expr);

		int finishedStep = -1;
		double finishConvergencePrecision = 1e-8;
		for(int stepCount = 0; stepCount < 30; stepCount++) {
			// when
			double nextRes = sut.optimStep(prep, ctx, prevRes);
			
			// then
			if (nextRes == prevRes) {
				finishedStep = stepCount; // was exact, no optim, or rounding same result..
				break;
			}
			Assert.assertTrue(nextRes < prevRes);
			if (prevRes - nextRes < finishConvergencePrecision) {
				// ok, converged
				finishedStep = stepCount;
				break;
			}
			
			prevRes = nextRes;
		}
		Assert.assertTrue(-1 != finishedStep);
		Assert.assertTrue(finishedStep <= expectedMaxFinishedStep);
		
		double resX = ctx.getEval(xDef);
		Assert.assertEquals(expectedRes, prevRes, PREC);
		Assert.assertEquals(expectedX, resX, PREC);
	}

}
