package fr.an.drawingboard.model.expr.helper;

import java.util.HashMap;
import java.util.Map;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprFunc1;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.VarDef;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class NumericExprEvalCtx {

	/** for VariableExpr */
	public final Map<VarDef, Double> varValues = new HashMap<>();

	/** for ParamDefExpr */
	public final Map<ParamDef, Double> paramValues = new HashMap<>();

	// --------------------------------------------------------------------------------------------

	public void putVarValue(VarDef varDef, double value) {
		varValues.put(varDef, value);
	}

	public void putVarValue(VariableExpr varExpr, double value) {
		putVarValue(varExpr.varDef, value);
	}

	public void putParamValue(ParamDef paramDef, double value) {
		paramValues.put(paramDef, value);
	}

	public void putParamValue(ParamDefExpr paramDefExpr, double value) {
		putParamValue(paramDefExpr.paramDef, value);
	}

	public double varValue(VarDef varDef) {
		Double found = varValues.get(varDef);
		if (null == found) {
			throw new IllegalStateException("no numerical value for var '" + varDef + "'");
		}
		return found.doubleValue();
	}

	public double paramValue(ParamDef paramDef) {
		Double found = paramValues.get(paramDef);
		if (null == found) {
			throw new IllegalStateException("no numerical value for param '" + paramDef + "'");
		}
		return found.doubleValue();
	}

	public double evalExpr(Expr expr) {
		ResCtx resCtx = new ResCtx(this);
		expr.accept(NumericalEvalExprVisitor.INSTANCE, resCtx);
		return resCtx.value;
	}

	public Pt2D evalPtExpr(PtExpr ptExpr) {
		double x = evalExpr(ptExpr.x);
		double y = evalExpr(ptExpr.y);
		return new Pt2D(x, y);
	}

	@RequiredArgsConstructor
	public static class ResCtx {
		public final NumericExprEvalCtx ctx;
		public double value;
	}

	public static final class NumericalEvalExprVisitor extends ExprFunc1<Void, ResCtx> {
		public static final NumericalEvalExprVisitor INSTANCE = new NumericalEvalExprVisitor();

		private NumericalEvalExprVisitor() {
		}

		double evalExpr(Expr e, ResCtx res) {
			if (e == null) {
				return 0; // should not occur!
			}
			e.accept(this, res);
			return res.value;
		}

		@Override
		public Void caseLiteral(LiteralDoubleExpr expr, ResCtx res) {
			res.value = expr.value;
			return null;
		}

		@Override
		public Void caseSum(SumExpr expr, ResCtx res) {
			double sumValue = 0.0;
			for (val e : expr.exprs) {
				double exprValue = evalExpr(e, res);
				sumValue += exprValue;
			}
			res.value = sumValue;
			return null;
		}

		@Override
		public Void caseMult(MultExpr expr, ResCtx res) {
			double multValue = 1.0;
			for (val e : expr.exprs) {
				double exprValue = evalExpr(e, res);
				if (exprValue == 0.0) {
					multValue = 0.0;
					break;
				}
				multValue *= exprValue;
			}
			res.value = multValue;
			return null;
		}

		@Override
		public Void caseVariable(VariableExpr expr, ResCtx res) {
			res.value = res.ctx.varValue(expr.varDef);
			return null;
		}

		@Override
		public Void caseParamDef(ParamDefExpr expr, ResCtx res) {
			res.value = res.ctx.paramValue(expr.paramDef);
			return null;
		}

	}

}
