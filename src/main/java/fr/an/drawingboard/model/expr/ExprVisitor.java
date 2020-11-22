package fr.an.drawingboard.model.expr;

import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;

public abstract class ExprVisitor {

	public abstract void caseLiteral(LiteralDoubleExpr expr);

	public abstract void caseSum(SumExpr expr);

	public abstract void caseMult(MultExpr expr);

	public abstract void caseVariable(VariableExpr expr);

	public abstract void caseParamDef(ParamDefExpr expr);

}
