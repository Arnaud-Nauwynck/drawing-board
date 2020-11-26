package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import lombok.val;
import fr.an.drawingboard.model.expr.ExprFunc1Visitor;

public class ConstantFoldingExprTransformer extends ExprFunc1Visitor<Expr, Void> {

	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	public static final ConstantFoldingExprTransformer INSTANCE = new ConstantFoldingExprTransformer();

	private ConstantFoldingExprTransformer() {
	}

	public static Expr constFold(Expr expr) {
		return expr.accept(INSTANCE, null);
	}

	protected Expr constFoldExpr(Expr expr) {
		return expr.accept(this, null);
	}

	protected boolean isConst(Expr expr) {
		return expr instanceof LiteralDoubleExpr;
	}

	@Override
	public Expr caseLiteral(LiteralDoubleExpr expr, Void param) {
		return expr;
	}

	@Override
	public Expr caseSum(SumExpr expr, Void param) {
		List<Expr> resChildExprs = constFoldExprListExprs(expr.exprs);
		boolean changed = false;
		if (resChildExprs == null) {
			resChildExprs = expr.exprs;
		} else {
			changed = true;
		}
		double sumValue = 0.0;
		List<Expr> remainChildExprs = new ArrayList<>();
		for (val childExpr : resChildExprs) {
			if (isConst(childExpr)) {
				changed = true;
				double childValue = ((LiteralDoubleExpr) childExpr).value;
				if (childValue != 0.0) {
					sumValue += childValue;
				}
			} else {
				remainChildExprs.add(childExpr);
			}
		}
		if (!changed) {
			return expr;
		}
		if (sumValue != 0.0) {
			remainChildExprs.add(b.lit(sumValue));
		}
		return b.sum(remainChildExprs);
	}

	@Override
	public Expr caseMult(MultExpr expr, Void param) {
		List<Expr> resChildExprs = constFoldExprListExprs(expr.exprs);
		boolean changed = false;
		if (resChildExprs == null) {
			resChildExprs = expr.exprs;
		} else {
			changed = true;
		}
		List<Expr> remainChildExprs = new ArrayList<>();
		double multValue = 1.0;
		for (val childExpr : resChildExprs) {
			if (isConst(childExpr)) {
				changed = true;
				double childValue = ((LiteralDoubleExpr) childExpr).value;
				if (childValue == 0.0) {
					return b.lit0();
				}
				if (childValue != 1.0) {
					multValue *= childValue;
				}
			} else {
				remainChildExprs.add(childExpr);
			}
		}
		if (!changed) {
			return expr;
		}
		if (multValue != 1.0) {
			remainChildExprs.add(b.lit(multValue));
		}
		return b.mult(remainChildExprs);
	}

	protected List<Expr> constFoldExprListExprs(List<Expr> childExprs) {
		List<Expr> resChildExprs = new ArrayList<>(childExprs.size());
		boolean changed = false;
		for (val childExpr : childExprs) {
			Expr resChild = constFoldExpr(childExpr);
			resChildExprs.add(resChild);
			if (resChild != childExpr) {
				changed = true;
			}
		}
		return (changed) ? resChildExprs : null;
	}

	@Override
	public Expr caseVariable(VariableExpr expr, Void param) {
		return expr;
	}

	@Override
	public Expr caseParamDef(ParamDefExpr expr, Void param) {
		return expr;
	}

}
