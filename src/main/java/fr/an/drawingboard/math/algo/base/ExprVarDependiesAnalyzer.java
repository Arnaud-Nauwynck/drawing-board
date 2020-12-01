package fr.an.drawingboard.math.algo.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.ExprFunc0;
import fr.an.drawingboard.math.expr.ExprVisitor;
import fr.an.drawingboard.math.expr.VarDef;
import lombok.val;

public class ExprVarDependiesAnalyzer {

	public static class ExprDependencies {
		public Set<VarDef> vars = new HashSet<>();
	}

	public static ExprDependencies analyzeVarDependencies(Expr expr) {
		ExprDependencies res = new ExprDependencies();
		expr.accept(new ExprVisitor() {
			@Override
			public void caseLiteral(LiteralDoubleExpr expr) {
			}
			
			@Override
			public void caseSum(SumExpr expr) {
				analyseList(expr.exprs);
			}

			@Override
			public void caseMult(MultExpr expr) {
				analyseList(expr.exprs);
			}

			protected void analyseList(List<Expr> exprs) {
				for(val e : exprs) {
					e.accept(this);
				}
			}

			@Override
			public void caseVariable(VariableExpr expr) {
				res.vars.add(expr.varDef);
			}

		});
		return res;
	}

	public static boolean analyzeIfDependsOnVar(Collection<Expr> exprs, VarDef varDef) {
		for(val expr : exprs) {
			if (analyzeIfDependsOnVar(expr, varDef)) {
				return true;
			}
		}
		return false;
	}

	public static boolean analyzeIfDependsOnVar(Expr expr, VarDef varDef) {
		return analyzeIfDependsOnVars(expr, ImmutableSet.of(varDef));
	}

	public static boolean analyzeIfDependsOnVars(Expr expr, Set<VarDef> varDefs) {
		return expr.accept(new ExprFunc0<Boolean>() {
			@Override
			public Boolean caseLiteral(LiteralDoubleExpr expr) {
				return Boolean.FALSE;
			}
			
			@Override
			public Boolean caseSum(SumExpr expr) {
				return analyseList(expr.exprs);
			}

			@Override
			public Boolean caseMult(MultExpr expr) {
				return analyseList(expr.exprs);
			}

			protected Boolean analyseList(List<Expr> exprs) {
				for(val e : exprs) {
					boolean tmpres = e.accept(this);
					if (tmpres) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			}

			@Override
			public Boolean caseVariable(VariableExpr expr) {
				return (varDefs != null && varDefs.contains(expr.varDef));
			}
			
		});
	}
}
