package fr.an.drawingboard.math.algo.base;

import fr.an.drawingboard.math.expr.Expr;

public class SimplifyExpandExprUtils {

	public static Expr simplifyExpand(Expr expr) {
		Expr curr = ConstantFoldingExprTransformer.constFold(expr);
		curr = ExpandExprTransformer.expandExpr(curr);
		curr = ConstantFoldingExprTransformer.constFold(curr);
		curr = FlattenExprTransformer.flattenExpr(curr);
		curr = ConstantFoldingExprTransformer.constFold(curr);

		for(int i = 0; i < 3; i++) {
			Expr curr2 = FlattenExprTransformer.flattenExpr(ExpandExprTransformer.expandExpr(curr));
			if (curr2 == curr) {
				break;
			}
			curr = curr2;
		}
		return curr;
	}
}
