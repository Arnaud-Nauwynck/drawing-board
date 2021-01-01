package fr.an.drawingboard.math.numeric.optim;

import fr.an.drawingboard.math.algo.base.DerivativeExprBuilder;
import fr.an.drawingboard.math.algo.quadform.QuadraticTerms1VarCollector;
import fr.an.drawingboard.math.algo.quadform.QuadraticTerms1VarCollector.QuadraticForm1D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.DoubleCtxFunc;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * single optimization step, on 1 variable, by using Quadratic Form terms extract
 * Q(X) = a.x^2 + b.x + c + f(x)
 * 
 * <PRE>
 * if "f(x)=0" 
 * 		
 *   => optimal step: x= -b/(2a)
 * 
 * ELSE 
 *    ... no optimal quadratic solution !! 
 *    (should fallback to numeric function + using derivative OR extrapolation with 3 points )
 *    
 *    this class use Algebric derivative of the non-quadratic term, then correct numerical eval for quadTerms
 *    use limited development around varValue:
 *    f(x) ~= f(x0) + f'(x0) (x-x0) + 1/2 f''(x0) (x-x0)^2 + o((x-x0)^3)
 *    
 *    f(x) ~=   x^2 (1/2 f''(x0) )
 *            + x   (f'(x0) - 1/2(-2)f''(x0) x0 )
 *            +     ( ... )
 *            + o((x-x0)^3)
 *            
 *    Q(X) = a' x^2 + b' x + c' + o((x-xo)^3) 
 *       a'= a + 1/2 f''(x0)
 *       b'= b + f'(x0) - f''(x0) x0
 *       c'= ..
 * </PRE>
 * 
 * 
 * very similar to class NumericTailor2Quad1DOptimizeStep, but in the simple case of purely quadratic form, then does not use derivative
 */
@RequiredArgsConstructor
public class NumericQuadExtractOtherwiseDeriveQuad1DOptimizeStep {

	public final VarDef stepVarDef;
	
	public double optimStep(Expr expr, NumericEvalCtx ctx, double prevRes) {
		PrepareNumericQuadExtractOtherwiseDeriveQuad1DOptimize prep = prepareOptimStep(expr);
		return optimStep(prep, ctx, prevRes);
	}

	@AllArgsConstructor
	public static class PrepareNumericQuadExtractOtherwiseDeriveQuad1DOptimize {
		final DoubleCtxFunc func;
		final double a; // quadForm.quadTermLiteral
		final double b; // final QuadraticForm1D quadForm;
		final double c;
		final DoubleCtxFunc derivOtherNonQuadTermFunc;
		final DoubleCtxFunc deriv2OtherNonQuadTermFunc;
	}
	
	public PrepareNumericQuadExtractOtherwiseDeriveQuad1DOptimize prepareOptimStep(Expr expr) {
		QuadraticForm1D quadForm = QuadraticTerms1VarCollector.extractQuadTerms(expr, stepVarDef);
		
		val func = DoubleCtxFunc.funcFor(expr);
		// if non quadratic term depending of var! => deriv + limited development around var value
		DoubleCtxFunc derivOtherNonQuadTermFunc = null;
		DoubleCtxFunc deriv2OtherNonQuadTermFunc = null;
		if (quadForm.otherNonQuadTermVarDependentExpr != null) {
			Expr derivExpr = DerivativeExprBuilder.deriveBy(quadForm.otherNonQuadTermVarDependentExpr, stepVarDef);
			Expr deriv2Expr = DerivativeExprBuilder.deriveBy(derivExpr, stepVarDef);
			derivOtherNonQuadTermFunc = DoubleCtxFunc.funcFor(derivExpr);
			deriv2OtherNonQuadTermFunc = DoubleCtxFunc.funcFor(deriv2Expr);
		}
		return new PrepareNumericQuadExtractOtherwiseDeriveQuad1DOptimize(func, 
				quadForm.quadTermLiteral, quadForm.linTermLiteral, quadForm.constTermLiteral, 
				derivOtherNonQuadTermFunc, deriv2OtherNonQuadTermFunc);
	}

	public double optimStep(PrepareNumericQuadExtractOtherwiseDeriveQuad1DOptimize prepare, NumericEvalCtx ctx, double prevResValue) {
		// assert double prevResValue = ctx.evalExpr(prepare.expr);
		final double prevVarValue = ctx.getEval(stepVarDef);
		if (prepare.derivOtherNonQuadTermFunc == null) {
			double nextResValue;
			// pure quadratic: a x^2 + b x + c
			if (prepare.a != 0.0) {
				double varValue = -0.5 * prepare.b / prepare.a;
				ctx.put(stepVarDef, varValue);
			} else {
				// a == 0.. linear: b x + c
				if (prepare.b != 0.0) {
					double varValue = - prepare.c / prepare.b;
					ctx.put(stepVarDef, varValue);
				} else {
					// a=0, b=0 !
					return prevResValue;
				}
			}

			nextResValue = prepare.func.eval(ctx);
			// CHECK consistent?
			if (nextResValue > prevResValue) {
				// should not occur
				ctx.put(stepVarDef, prevVarValue);
				return prevResValue;
			}
			return nextResValue;
		}

	    double derivValue = prepare.derivOtherNonQuadTermFunc.eval(ctx);
		double derive2Value = prepare.deriv2OtherNonQuadTermFunc.eval(ctx);
		double a = prepare.a + 0.5 * derive2Value;
		double b = prepare.b + derivValue - derive2Value * prevVarValue;
		
		if (a != 0.0) {
			double varValue = -0.5 * b / a;
			ctx.put(stepVarDef, varValue);
			
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
		} else {
			// can not perform optim step: f''=a=0!
			return prevResValue;
		}
	}

}
