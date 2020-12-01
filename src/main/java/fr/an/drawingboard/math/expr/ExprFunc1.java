package fr.an.drawingboard.math.expr;

import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;

public abstract class ExprFunc1<TRes,TParam> {

	public abstract TRes caseLiteral(LiteralDoubleExpr expr, TParam param);

	public abstract TRes caseSum(SumExpr expr, TParam param);

	public abstract TRes caseMult(MultExpr expr, TParam param);

	public abstract TRes caseVariable(VariableExpr expr, TParam param);

}
