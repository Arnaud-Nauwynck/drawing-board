package fr.an.drawingboard.alg.optim;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.math.algo.optim.LiteralQuad1DOptimizeStep;
import fr.an.drawingboard.math.algo.optim.LiteralQuad1DOptimizeStep.PrepareLiteralQuad1DOptimizeStep;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;

public class LiteralQuad1DOptimizeStepTest {

	private static final ExprBuilder B = ExprBuilder.INSTANCE;
	private static final double	PREC = 1e-9;
	private static final VarDef xDef = new VarDef("x");
	private static final Expr x = xDef.expr;
	
	private final LiteralQuad1DOptimizeStep sut = new LiteralQuad1DOptimizeStep(xDef);
	
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
		
		// when
		sut.optimStep(expr, ctx);
		
		// then
		// x = -b/(2a) = -3/4
		double xValue = ctx.get(xDef);
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
		doTestOptimStep_PureNumeric_NotQuad(expr, 4, -0.8285177655516776, -6.05094548e-4);
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
		doTestOptimStep_PureNumeric_NotQuad(expr, 5, -0.50850724272, 0.48410645091698534); // TODO use real value from solver (Mathematica,Sage,PariGP,geogebra,..)
	}
	
	private void doTestOptimStep_PureNumeric_NotQuad(Expr expr, 
			int expectedMaxFinishedStep,
			double expectedX,
			double expectedRes
			) {
		double prevRes = ctx.evalExpr(expr);

		PrepareLiteralQuad1DOptimizeStep prep = sut.prepareOptimStep(expr);

		int finishedStep = -1;
		double finishConvergencePrecision = 1e-8;
		for(int step = 0; step < 30; step++) {
			// when
			double nextRes = sut.optimStep(prep, ctx);
			
			// then
			if (nextRes == prevRes) {
				finishedStep = step; // was exact, no optim, or rounding same result..
				break;
			}
			Assert.assertTrue(nextRes < prevRes);
			if (prevRes - nextRes < finishConvergencePrecision) {
				// ok, converged
				finishedStep = step;
				break;
			}
			
			prevRes = nextRes;
		}
		Assert.assertTrue(-1 != finishedStep);
		Assert.assertTrue(finishedStep <= expectedMaxFinishedStep);
		
		double resX = ctx.get(xDef);
		Assert.assertEquals(expectedRes, prevRes, PREC);
		Assert.assertEquals(expectedX, resX, PREC);
	}

}
