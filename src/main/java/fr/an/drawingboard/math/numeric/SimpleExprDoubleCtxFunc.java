package fr.an.drawingboard.math.numeric;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.ExprFunc1;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * naive adapter implementation of DoubleCtxFunc for Expr
 * 
 * .. real usage should use generated code
 */
@RequiredArgsConstructor
public class SimpleExprDoubleCtxFunc extends DoubleCtxFunc {

	public final Expr expr;

	@Override
	public double eval(NumericEvalCtx ctx) {
		return ctx.evalExpr(expr);
	}


	public static double evalExpr(Expr expr, NumericEvalCtx ctx) {
		ResCtx resCtx = new ResCtx(ctx);
		expr.accept(NumericalEvalExprVisitor.INSTANCE, resCtx);
		return resCtx.value;
	}
	
	@RequiredArgsConstructor
	public static class ResCtx {
		public final NumericEvalCtx ctx;
		public double value;
	}

	public static final class NumericalEvalExprVisitor extends ExprFunc1<Void, ResCtx> {
		public static final NumericalEvalExprVisitor INSTANCE = new NumericalEvalExprVisitor();

		private NumericalEvalExprVisitor() {
		}

		double evalExpr(Expr e, ResCtx res) {
			if (e == null) {
				return 0; // should not occur!
			}
			e.accept(this, res);
			return res.value;
		}

		@Override
		public Void caseLiteral(LiteralDoubleExpr expr, ResCtx res) {
			res.value = expr.value;
			return null;
		}

		@Override
		public Void caseSum(SumExpr expr, ResCtx res) {
			double sumValue = 0.0;
			for (val e : expr.exprs) {
				double exprValue = evalExpr(e, res);
				sumValue += exprValue;
			}
			res.value = sumValue;
			return null;
		}

		@Override
		public Void caseMult(MultExpr expr, ResCtx res) {
			double multValue = 1.0;
			for (val e : expr.exprs) {
				double exprValue = evalExpr(e, res);
				if (exprValue == 0.0) {
					multValue = 0.0;
					break;
				}
				multValue *= exprValue;
			}
			res.value = multValue;
			return null;
		}

		@Override
		public Void caseVariable(VariableExpr expr, ResCtx res) {
			res.value = res.ctx.get(expr.varDef);
			return null;
		}

	}
}
