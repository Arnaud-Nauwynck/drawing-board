package fr.an.drawingboard.model.expr;

import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;

public abstract class ExprFuncVisitor<TRes> {

	public abstract TRes caseLiteral(LiteralDoubleExpr expr);

	public abstract TRes caseSum(SumExpr expr);

	public abstract TRes caseMult(MultExpr expr);

	public abstract TRes caseVariable(VariableExpr expr);

	public abstract TRes caseParamDef(ParamDefExpr expr);

	
	public static class DefaultExprTransformer extends ExprFuncVisitor<Expr> {

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

		@Override
		public Expr caseParamDef(ParamDefExpr expr) {
			return expr;
		}
	}
	
}
