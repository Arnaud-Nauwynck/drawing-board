package fr.an.drawingboard.math.numeric;

import fr.an.drawingboard.math.expr.Expr;

public abstract class DoubleCtxFunc {

	public abstract double eval(NumericEvalCtx ctx);
	
	public static DoubleCtxFunc funcFor(Expr expr) {
		// TOCHANGE: may wrap for statistics, then mutate to generated code implementation
		return new SimpleExprDoubleCtxFunc(expr);
	}
}
