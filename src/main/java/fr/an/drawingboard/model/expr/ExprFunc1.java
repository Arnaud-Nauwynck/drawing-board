package fr.an.drawingboard.model.expr;

import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;

public abstract class ExprFunc1<TRes,TParam> {

	public abstract TRes caseLiteral(LiteralDoubleExpr expr, TParam param);

	public abstract TRes caseSum(SumExpr expr, TParam param);

	public abstract TRes caseMult(MultExpr expr, TParam param);

	public abstract TRes caseVariable(VariableExpr expr, TParam param);

	public abstract TRes caseParamDef(ParamDefExpr expr, TParam param);

}
