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
import fr.an.drawingboard.model.expr.ExprFunc1Visitor;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.VarDef;
import lombok.AllArgsConstructor;
import lombok.val;

public class DerivativeExprBuilder {

	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	protected DerivativeExprBuilder() {
	}

	public static Expr deriveBy(Expr expr, VarDef varDef) {
		DeriveByVarOrParamDef by = new DeriveByVarOrParamDef(varDef, null);
		return deriveExprBy(expr, by);
	}

	public static Expr deriveBy(Expr expr, VariableExpr varExpr) {
		return deriveBy(expr, varExpr.varDef);
	}
	
	public static Expr deriveBy(Expr expr, ParamDef paramDef) {
		DeriveByVarOrParamDef by = new DeriveByVarOrParamDef(null, paramDef);
		return deriveExprBy(expr, by);
	}

	public static Expr deriveBy(Expr expr, ParamDefExpr paramDefExpr) {
		return deriveBy(expr, paramDefExpr.paramDef);
	}

	@AllArgsConstructor
	protected static class DeriveByVarOrParamDef {
		public final VarDef varDef;
		public final ParamDef paramDef;
	}
	
	protected static Expr deriveExprBy(Expr expr, DeriveByVarOrParamDef by) {
		Expr res = expr.accept(DeriveExprVisitor.INSTANCE, by);
		return res;
	}

	/**
	 *
	 */
	public static final class DeriveExprVisitor extends ExprFunc1Visitor<Expr, DeriveByVarOrParamDef> {

		public static final DeriveExprVisitor INSTANCE = new DeriveExprVisitor();

		private DeriveExprVisitor() {
		}

		Expr deriveBy(Expr e, DeriveByVarOrParamDef by) {
			if (e == null) {
				return null; // should not occur!
			}
			return e.accept(this, by);
		}

		
		@Override
		public Expr caseLiteral(LiteralDoubleExpr expr, DeriveByVarOrParamDef by) {
			return LiteralDoubleExpr.VAL_0;
		}

		@Override
		public Expr caseSum(SumExpr expr, DeriveByVarOrParamDef by) {
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
		public Expr caseMult(MultExpr expr, DeriveByVarOrParamDef by) {
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
		public Expr caseVariable(VariableExpr expr, DeriveByVarOrParamDef by) {
			return (expr.varDef == by.varDef)? LiteralDoubleExpr.VAL_1 : LiteralDoubleExpr.VAL_0;
		}

		@Override
		public Expr caseParamDef(ParamDefExpr expr, DeriveByVarOrParamDef by) {
			return (expr.paramDef == by.paramDef)? LiteralDoubleExpr.VAL_1 : LiteralDoubleExpr.VAL_0;
		}

	}

}
