package fr.an.drawingboard.model.expr.helper;

import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprFunc0;

public class ExprAsExprBuilderJavaCodePrinter {

	public static String exprToExprBuilderJavaCode(Expr expr) {
		if (expr == null) {
			return "null";
		}
		return expr.accept(DumpJavaExprBuilderCodeFunc.INSTANCE);
	}

	private static final class DumpJavaExprBuilderCodeFunc extends ExprFunc0<String> {
		public static final DumpJavaExprBuilderCodeFunc INSTANCE = new DumpJavaExprBuilderCodeFunc();
		private DumpJavaExprBuilderCodeFunc() {
		}

		String str(Expr e) {
			if (e == null) {
				return "null"; // should not occur!
			}
			return e.accept(this);
		}

		@Override
		public String caseLiteral(LiteralDoubleExpr expr) {
			if (expr.value == 0.0) {
				return "B.lit0()";
			} else if (expr.value == 1.0) {
				return "B.lit1()";
			} else if (expr.value == -1.0) {
				return "B.litMinus1()";
			} else {
				return "B.lit(" + expr.value + ")";
			}
		}

		@Override
		public String caseSum(SumExpr expr) {
			return "B.sum(" + strsCommaSep(expr.exprs) + ")";
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
			return "B.mult(" + strsCommaSep(expr.exprs) + ")";
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
