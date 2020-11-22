package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprVisitor2;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.VarDef;
import lombok.val;

public class SubstitueExprCtx {

	/** for VariableExpr */
	public final Map<VarDef, Expr> varExprs = new HashMap<>();

	/** for ParamDefExpr */
	public final Map<ParamDef, Expr> paramExprs = new HashMap<>();

	public Expr varExpr(VarDef varDef) {
		return varExprs.get(varDef);
	}

	public Expr paramExpr(ParamDef paramDef) {
		return paramExprs.get(paramDef);
	}

	public Expr substituteExpr(Expr expr) {
		Expr res = expr.accept(SubstituteExprVisitor.INSTANCE, this);
		return res;
	}

	/**
	 *
	 */
	public static final class SubstituteExprVisitor extends ExprVisitor2<Expr, SubstitueExprCtx> {

		public static final SubstituteExprVisitor INSTANCE = new SubstituteExprVisitor();

		private SubstituteExprVisitor() {
		}

		Expr substituteExpr(Expr e, SubstitueExprCtx res) {
			if (e == null) {
				return null; // should not occur!
			}
			return e.accept(this, res);
		}

		@Override
		public Expr caseLiteral(LiteralDoubleExpr expr, SubstitueExprCtx res) {
			return expr;
		}

		@Override
		public Expr caseSum(SumExpr expr, SubstitueExprCtx ctx) {
			List<Expr> resChildExprs = substituteListExprs(expr.exprs, ctx);
			return (resChildExprs != null) ? new SumExpr(resChildExprs) : expr;
		}

		protected List<Expr> substituteListExprs(List<Expr> childExprs, SubstitueExprCtx ctx) {
			List<Expr> resChildExprs = new ArrayList<>(childExprs.size());
			boolean changed = false;
			for (val childExpr : childExprs) {
				Expr resChild = substituteExpr(childExpr, ctx);
				resChildExprs.add(resChild);
				if (resChild != childExpr) {
					changed = true;
				}
			}
			return (changed) ? resChildExprs : null;
		}

		@Override
		public Expr caseMult(MultExpr expr, SubstitueExprCtx ctx) {
			List<Expr> resChildExprs = substituteListExprs(expr.exprs, ctx);
			return (resChildExprs != null) ? new MultExpr(resChildExprs) : expr;
		}

		@Override
		public Expr caseVariable(VariableExpr expr, SubstitueExprCtx ctx) {
			Expr resExpr = ctx.varExpr(expr.varDef);
			return (resExpr != null) ? resExpr : expr;
		}

		@Override
		public Expr caseParamDef(ParamDefExpr expr, SubstitueExprCtx ctx) {
			Expr resExpr = ctx.paramExpr(expr.paramDef);
			return (resExpr != null) ? resExpr : expr;
		}

	}

}
