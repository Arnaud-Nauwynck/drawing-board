package fr.an.drawingboard.math.algo.base;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.ExprFunc1;
import fr.an.drawingboard.math.expr.VarDef;
import lombok.AllArgsConstructor;
import lombok.val;

public class DerivativeExprBuilder {

	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	protected DerivativeExprBuilder() {
	}

	public static Expr deriveBy(Expr expr, VarDef varDef) {
		Expr res = expr.accept(DeriveExprVisitor.INSTANCE, varDef);
		return res;
	}

	public static Expr deriveBy(Expr expr, VariableExpr varExpr) {
		return deriveBy(expr, varExpr.varDef);
	}
	
	@AllArgsConstructor
	protected static class DeriveByVarDef {
		public final VarDef varDef;
	}

	/**
	 *
	 */
	public static final class DeriveExprVisitor extends ExprFunc1<Expr, VarDef> {

		public static final DeriveExprVisitor INSTANCE = new DeriveExprVisitor();

		private DeriveExprVisitor() {
		}

		Expr deriveBy(Expr e, VarDef by) {
			if (e == null) {
				return null; // should not occur!
			}
			return e.accept(this, by);
		}

		
		@Override
		public Expr caseLiteral(LiteralDoubleExpr expr, VarDef by) {
			return LiteralDoubleExpr.VAL_0;
		}

		@Override
		public Expr caseSum(SumExpr expr, VarDef by) {
			List<Expr> derivChildExprs = new ArrayList<>(expr.exprs.size());
			for (val childExpr : expr.exprs) {
				Expr derivChildExpr = deriveBy(childExpr, by);
				if (LiteralDoubleExpr.isLit0(derivChildExpr)) {
					// skip!
				} else {
					derivChildExprs.add(derivChildExpr);
				}
			}
			return b.sum(derivChildExprs);
		}

		@Override
		public Expr caseMult(MultExpr expr, VarDef by) {
			List<Expr> childExprs = expr.exprs;
			int exprsCount = childExprs.size();
			if (exprsCount == 1) {
				// not a real product!
				return deriveBy(childExprs.get(0), by);
			}
			List<Expr> sumDerivExprs = new ArrayList<>(exprsCount);
			for (int i = 0; i < exprsCount; i++) {
				Expr expr_i = childExprs.get(i);
				Expr der_expr_i = deriveBy(expr_i, by);
				if (LiteralDoubleExpr.isLit0(der_expr_i)) {
					// skip term
				} else {
					// multiply by other remaining terms
					List<Expr> multExprs = new ArrayList<>(exprsCount);
					if (i > 0) {
						multExprs.addAll(childExprs.subList(0, i));
					}
					if (!LiteralDoubleExpr.isLit1(der_expr_i)) {
						multExprs.add(der_expr_i);
					}
					if (i < exprsCount-1) {
						multExprs.addAll(childExprs.subList(i + 1, exprsCount));
					}
					sumDerivExprs.add(b.mult(multExprs));
				}
			}
			return b.sum(sumDerivExprs);
		}

		@Override
		public Expr caseVariable(VariableExpr expr, VarDef by) {
			return (expr.varDef == by)? LiteralDoubleExpr.VAL_1 : LiteralDoubleExpr.VAL_0;
		}

	}

}
