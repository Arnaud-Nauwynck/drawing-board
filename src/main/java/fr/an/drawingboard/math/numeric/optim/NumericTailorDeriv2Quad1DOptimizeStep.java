package fr.an.drawingboard.math.numeric.optim;

import fr.an.drawingboard.math.algo.base.DerivativeExprBuilder;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.DoubleCtxFunc;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * single optimization step, on 1 variable, by using quadratic taylor devlopment, then quad min approximation step
 * 
 * f(x-x0) ~= f(x0) + f'(x0) (x-x0) + 1/2 f''(x0) (x-x0)^2    + o((x-x0)^2)
 * 
 * so.. ~= aX^2+bX+c, with X=x-x0, a= 1/2 f''(x0), b= f'(x0)
 * test quad optimal step: X=-b/(2a)
 *  <=>  x= x0 - f'(x0) / f''(x0)
 * 
 * test that x gives better result that prevX ... (for Taylor development appromixation, rounding pb..)
 * 
 * very similar to class NumericQuadExtractOtherwiseDeriveQuad1DOptimizeStep, but does not use internally QuadraticTerms1VarCollector
 * always use derivative
 */
@RequiredArgsConstructor
public class NumericTailorDeriv2Quad1DOptimizeStep {

	public final VarDef stepVarDef;
	
	public double optimStep(Expr expr, NumericEvalCtx ctx, double prevRes) {
		PrepareNumericDerive2Quad1DOptimizeStep prep = prepareOptimStep(expr);
		return optimStep(prep, ctx, prevRes);
	}

	@AllArgsConstructor
	public static class PrepareNumericDerive2Quad1DOptimizeStep {
		final DoubleCtxFunc func;
		final DoubleCtxFunc derivFunc;
		final DoubleCtxFunc deriv2Func;
	}
	
	public PrepareNumericDerive2Quad1DOptimizeStep prepareOptimStep(Expr expr) {
		Expr derivExpr = DerivativeExprBuilder.deriveBy(expr, stepVarDef);
		Expr deriv2Expr = DerivativeExprBuilder.deriveBy(derivExpr, stepVarDef);
		val func = DoubleCtxFunc.funcFor(expr);
		val derivFunc = DoubleCtxFunc.funcFor(derivExpr);
		val deriv2Func = DoubleCtxFunc.funcFor(deriv2Expr);
		return new PrepareNumericDerive2Quad1DOptimizeStep(func, derivFunc, deriv2Func);
	}

	public double optimStep(PrepareNumericDerive2Quad1DOptimizeStep prepare, NumericEvalCtx ctx, double prevResValue) {
		// assert double prevResValue = func.eval(ctx);
		double deriv2Value = prepare.deriv2Func.eval(ctx);
		if (deriv2Value == 0.0) {
			// can not perform optim step!
			return prevResValue;
		}
		double derivValue = prepare.derivFunc.eval(ctx);
		final double prevVarValue = ctx.getEval(stepVarDef);
		double nextVar = prevVarValue - derivValue / deriv2Value;

		ctx.put(stepVarDef, nextVar);
		// reeval if result is better (limited development approximation may give worst value)
		double nextResValue = prepare.func.eval(ctx);
		if (nextResValue <= prevResValue) {
			// ok
		} else {
			// do not apply!
			ctx.put(stepVarDef, prevVarValue);
			return prevResValue;
		}
		return nextResValue;
	}

}
