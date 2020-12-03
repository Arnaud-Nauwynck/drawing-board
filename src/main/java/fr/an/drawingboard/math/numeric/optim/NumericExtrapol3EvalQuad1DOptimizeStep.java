package fr.an.drawingboard.math.numeric.optim;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.DoubleCtxFunc;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * single optimization step, on 1 variable, by using 3 evaluations at f(x), f(x+d), f(x-d), 
 * then interpol quadratic ax^2+bx+c for these 3 points, and apply stop x=-b/(2a)
 * 
 * 
 * very similar to class NumericQuadExtractOtherwiseDeriveQuad1DOptimizeStep, or NumericTailorDeriv2Quad1DOptimizeStep
 * but does not use internally neither derivative, nor QuadraticTerms1VarCollector
 */
@RequiredArgsConstructor
public class NumericExtrapol3EvalQuad1DOptimizeStep {

	public final VarDef stepVarDef;
	public final double step;
	
	public double optimStep(Expr expr, NumericEvalCtx ctx, double prevRes) {
		PrepareExtrapol3EvalQuad1DOptimizeStep prep = prepareOptimStep(expr);
		return optimStep(prep, ctx, prevRes);
	}

	@AllArgsConstructor
	public static class PrepareExtrapol3EvalQuad1DOptimizeStep {
		final DoubleCtxFunc func;
		// final DoubleCtxFunc funcVarPlusStep; // TOADD.. possible optim
		// final DoubleCtxFunc funcVarPlusStep;
	}
	
	public PrepareExtrapol3EvalQuad1DOptimizeStep prepareOptimStep(Expr expr) {
		val func = DoubleCtxFunc.funcFor(expr);
		return new PrepareExtrapol3EvalQuad1DOptimizeStep(func);
	}

	public double optimStep(PrepareExtrapol3EvalQuad1DOptimizeStep prepare, NumericEvalCtx ctx, double prevResValue) {
		// assert double prevResValue = func.eval(ctx);
		final double x = ctx.get(stepVarDef);
		double f_x = prevResValue;
		
		double xPlusS = x + step;
		ctx.put(stepVarDef, xPlusS);
		double f_xPlusS = prepare.func.eval(ctx);

		// if f_plusStep < prevResValue => TOCHANGE should use (prevVarValue + 2 * step) .. and switch
		double xMinusS = x - step;
		ctx.put(stepVarDef, xMinusS);
		double f_xMinusS = prepare.func.eval(ctx);

		// interpolate coefs a, b, c
		// such that
		// f(x-S) = a (x-S)^2 + b (x-S) + c
		// f(x)   = a x^2     + b x     + c
		// f(x+S) = a (x+S)^2 + b (x+S) + c
		
		// => f(x+S)+f(S-X)-2f(x) = 2aS^2
		// => f(x+S)-f(x-S) = 4aSx + 2bS
		
		double invStep = 1.0 / step;
		double a = ( (f_xPlusS - f_x) + (f_xMinusS - f_x) ) * invStep * invStep * 0.5; // rounding precision!..
		if (a == 0.0) {
			// can not perform min quadratic interpolation!
			// restore best of 3 vars
			return restoreBestOf3(ctx, x, f_x, xPlusS, f_xPlusS, xMinusS, f_xMinusS);
		}
		
		double b = 0.5 / step * ( (f_xPlusS - f_xMinusS) - 4*a*step*x);
		
		double nextVar = -b/(2*a);
		ctx.put(stepVarDef, nextVar);
		
		// reeval if result is better (interpolation approximation may give worst value)
		double nextResValue = prepare.func.eval(ctx);
		if (nextResValue <= prevResValue) {
			// ok
		} else {
			// do not apply!
			return restoreBestOf3(ctx, x, f_x, xPlusS, f_xPlusS, xMinusS, f_xMinusS);
		}
		return nextResValue;
	}

	private double restoreBestOf3(NumericEvalCtx ctx, //
			final double x, double f_x, //
			double xPlusS, double f_xPlusS, //
			double xMinusS, double f_xMinusS //
			) {
		double bestX = x;
		double bestF = f_x;
		if (f_xPlusS < bestF) {
			bestX = xPlusS;
			bestF = f_xPlusS;
		}
		if (f_xMinusS < bestF) {
			bestX = xMinusS;
			bestF = f_xMinusS;
		}
		ctx.put(stepVarDef, bestX);
		return bestF;
	}

}
