package fr.an.drawingboard.model.expr.helper;

import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprFunc0;

public class ExprPrinter {

	public static String exprToString(Expr expr) {
		if (expr == null) {
			return "null";
		}
		return expr.accept(DumpStringExprFunc.INSTANCE);
	}

	public static final class DumpStringExprFunc extends ExprFunc0<String> {
		public static final DumpStringExprFunc INSTANCE = new DumpStringExprFunc();

		private DumpStringExprFunc() {
		}

		String str(Expr e) {
			if (e == null) {
				return "null"; // should not occur!
			}
			return e.accept(this);
		}

		@Override
		public String caseLiteral(LiteralDoubleExpr expr) {
			return Double.toString(expr.value);
		}

		@Override
		public String caseSum(SumExpr expr) {
			if (expr.exprs.size() == 2) {
				return "(" + str(expr.exprs.get(0)) + " + " + str(expr.exprs.get(1)) + ")";  
			}
			return "sum(" + strsCommaSep(expr.exprs) + ")";
		}

		public String strsCommaSep(List<Expr> exprs) {
			StringBuilder sb = new StringBuilder();
			int count = exprs.size();
			for(int i = 0; i < count; i++) {
				sb.append(str(exprs.get(i)));
				if (i + 1 < count) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
		
		@Override
		public String caseMult(MultExpr expr) {
			if (expr.exprs.size() == 2) {
				return "(" + str(expr.exprs.get(0)) + " * " + str(expr.exprs.get(1)) + ")";  
			}
			return "mult(" + strsCommaSep(expr.exprs) + ")";
		}

		@Override
		public String caseVariable(VariableExpr expr) {
			return expr.varDef.name;
		}

		@Override
		public String caseParamDef(ParamDefExpr expr) {
			return expr.paramDef.name;
		}

	}

}
