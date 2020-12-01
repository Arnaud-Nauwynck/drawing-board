package fr.an.drawingboard.math.expr;

import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;

public abstract class ExprFunc0<TRes> {

	public abstract TRes caseLiteral(LiteralDoubleExpr expr);

	public abstract TRes caseSum(SumExpr expr);

	public abstract TRes caseMult(MultExpr expr);

	public abstract TRes caseVariable(VariableExpr expr);

	
	public static class DefaultExprTransformer extends ExprFunc0<Expr> {

		@Override
		public Expr caseLiteral(LiteralDoubleExpr expr) {
			return expr;
		}

		@Override
		public Expr caseSum(SumExpr expr) {
			return expr;
		}

		@Override
		public Expr caseMult(MultExpr expr) {
			return expr;
		}

		@Override
		public Expr caseVariable(VariableExpr expr) {
			return expr;
		}

	}
	
}
