package fr.an.drawingboard.math.expr;

import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;

public abstract class ExprVisitor {

	public abstract void caseLiteral(LiteralDoubleExpr expr);

	public abstract void caseSum(SumExpr expr);

	public abstract void caseMult(MultExpr expr);

	public abstract void caseVariable(VariableExpr expr);

}
