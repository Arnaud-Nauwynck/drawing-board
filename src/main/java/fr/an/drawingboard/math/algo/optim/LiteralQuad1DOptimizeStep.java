package fr.an.drawingboard.math.algo.optim;

import fr.an.drawingboard.math.algo.base.DerivativeExprBuilder;
import fr.an.drawingboard.math.algo.quadform.QuadraticTerms1VarCollector;
import fr.an.drawingboard.math.algo.quadform.QuadraticTerms1VarCollector.QuadraticForm1D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * single optimization step, on 1 variable, by using Quadratic Form terms extract
 * Q(X) = a.x^2 + b.x + c + f(x)
 * 
 * if "f(x)=0" => optimal step: x= -b/(2a)
 * else use limited development around varValue:
 * f(x) ~= f(x0) + f'(x0) (x-x0) + 1/2 f''(x0) (x-x0)^2 + o((x-x0)^3)
 * 
 * f(x) ~=   x^2 (1/2 f''(x0) )
 *         + x   (f'(x0) - 1/2(-2)f''(x0) x0 )
 *         +     ( ... )
 *         + o((x-x0)^3)
 *         
 * Q(X) = a' x^2 + b' x + c' + o((x-xo)^3) 
 *    a'= a + 1/2 f''(x0)
 *    b'= b + f'(x0) - f''(x0) x0
 *    c'= ..
 */
@RequiredArgsConstructor
public class LiteralQuad1DOptimizeStep {

	public final VarDef stepVarDef;
	
	public double optimStep(Expr expr, NumericEvalCtx ctx) {
		PrepareLiteralQuad1DOptimizeStep prep = prepareOptimStep(expr);
		return optimStep(prep, ctx);
	}

	@AllArgsConstructor
	public static class PrepareLiteralQuad1DOptimizeStep {
		final Expr expr;
		final QuadraticForm1D quadForm;
		final Expr deriv;
		final Expr deriv2;
	}
	
	public PrepareLiteralQuad1DOptimizeStep prepareOptimStep(Expr expr) {
		QuadraticForm1D quadForm = QuadraticTerms1VarCollector.extractQuadTerms(expr, stepVarDef);
		
		// if non quadratic term depending of var! => deriv + limited development around var value
		Expr deriv = null;
		Expr deriv2 = null;
		if (quadForm.otherNonQuadTermVarDependentExpr != null) {
			deriv = DerivativeExprBuilder.deriveBy(quadForm.otherNonQuadTermVarDependentExpr, stepVarDef);
			deriv2 = DerivativeExprBuilder.deriveBy(deriv, stepVarDef);
		}
		return new PrepareLiteralQuad1DOptimizeStep(expr, quadForm, deriv, deriv2);
	}

	public double optimStep(PrepareLiteralQuad1DOptimizeStep prepare, NumericEvalCtx ctx) {
		QuadraticForm1D quadForm = prepare.quadForm;
		double a = evalSum(ctx, quadForm.quadTermLiteral, quadForm.quadTermVarIndepExpr);
		double b = evalSum(ctx, quadForm.linTermLiteral, quadForm.linTermVarIndepExpr);

		final double prevVarValue = ctx.get(stepVarDef);
		if (quadForm.otherNonQuadTermVarDependentExpr != null) {
		    double derivValue = ctx.evalExpr(prepare.deriv);
			double derive2Value = ctx.evalExpr(prepare.deriv2);
			a += 0.5 * derive2Value;
			b += derivValue - derive2Value * prevVarValue;
		}
		
		double prevResValue = ctx.evalExpr(prepare.expr);
		if (a != 0.0) {
			double varValue = -0.5 * b / a;
			ctx.put(stepVarDef, varValue);
			
			// reeval if result is better (limited development approximation may give worst value)
			double nextResValue = ctx.evalExpr(prepare.expr);
			if (nextResValue <= prevResValue) {
				// ok
			} else {
				// do not apply!
				ctx.put(stepVarDef, prevVarValue);
				return prevResValue;
			}
			return nextResValue;
		} else {
			// can not perform optim step!
			return prevResValue;
		}
	}

	private static double evalSum(NumericEvalCtx ctx, double value, Expr expr) {
		double res = value;
		if (expr != null) {
			res += ctx.evalExpr(expr);
		}
		return res;
	}
}
