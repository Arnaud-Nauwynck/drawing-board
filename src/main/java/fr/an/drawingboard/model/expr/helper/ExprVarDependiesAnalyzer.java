package fr.an.drawingboard.model.expr.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprVisitor;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.VarDef;
import lombok.val;

public class ExprVarDependiesAnalyzer {

	public static class ExprDependencies {
		public Set<VarDef> vars = new HashSet<>();
		public Set<ParamDef> params = new HashSet<>();
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

			@Override
			public void caseParamDef(ParamDefExpr expr) {
				res.params.add(expr.paramDef);
			}
			
		});
		return res;
	}
	
}
