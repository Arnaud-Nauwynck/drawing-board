package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.ExprFunc0.DefaultExprTransformer;
import lombok.val;

/**
 * Flattern expr transformer: 
 * <PRE>
 * sum(a, sum(b,...) => sum(a,b,..)
 * mult(a, mult(b,...) => mult(a,b,..)
 * </PRE>
 * 
 */
public class FlattenExprTransformer {

	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	protected FlattenExprTransformer() {
	}

	public static Expr flatternExpr(Expr expr) {
		Expr res = expr.accept(FlatternExprVisitor.INSTANCE);
		return res;
	}

	/**
	 *
	 */
	public static final class FlatternExprVisitor extends DefaultExprTransformer {
		public static final FlatternExprVisitor INSTANCE = new FlatternExprVisitor();

		Expr flatten(Expr e) {
			if (e == null) {
				return null; // should not occur!
			}
			return e.accept(this);
		}

		@Override
		public Expr caseSum(SumExpr expr) {
			return expr.accept(FlatternSumExprVisitor.INSTANCE);
		}

		@Override
		public Expr caseMult(MultExpr expr) {
			return expr.accept(FlatternMultExprVisitor.INSTANCE);
		}

	}

	public static final class FlatternSumExprVisitor extends DefaultExprTransformer {
		public static final FlatternSumExprVisitor INSTANCE = new FlatternSumExprVisitor();

		@Override
		public Expr caseSum(SumExpr sum) {
			List<Expr> flattenSumExprs = new ArrayList<>(sum.exprs.size());
			double literal = 0.0;
			for(val e : sum.exprs) {
				Expr flattenChildExpr = e.accept(this);// recurse sum(..sum(...sum())
				if (flattenChildExpr instanceof LiteralDoubleExpr) {
					literal += ((LiteralDoubleExpr) flattenChildExpr).value;
				} else if (flattenChildExpr instanceof SumExpr) {
					List<Expr> flattenChildTerms = ((SumExpr) flattenChildExpr).exprs;
					for(val flattenChildTerm: flattenChildTerms) {
						if (flattenChildTerm instanceof LiteralDoubleExpr) {
							literal += ((LiteralDoubleExpr) flattenChildTerm).value;
						} else {
							flattenSumExprs.add(flattenChildTerm);
						}
					}
				} else {
					if (flattenChildExpr instanceof MultExpr) {
						// recursive on other FlatternMultExprVisitor: flatten mult(mult(...)))
						flattenChildExpr = flattenChildExpr.accept(FlatternMultExprVisitor.INSTANCE);
					}
					flattenSumExprs.add(flattenChildExpr);
				}
			}
			return b.sum(literal, flattenSumExprs);
		}

	}
	
	public static final class FlatternMultExprVisitor extends DefaultExprTransformer {
		public static final FlatternMultExprVisitor INSTANCE = new FlatternMultExprVisitor();

		@Override
		public Expr caseMult(MultExpr mult) {
			List<Expr> flattenMultExprs = new ArrayList<>(mult.exprs.size());
			double literal = 1.0;
			for(val e : mult.exprs) {
				Expr flattenChildExpr = e.accept(this);// recurse sum(..sum(...sum())
				if (flattenChildExpr instanceof LiteralDoubleExpr) {
					literal *= ((LiteralDoubleExpr) flattenChildExpr).value;
					if (literal == 0.0) {
						return b.lit0(); // 0 * x*y*..= 0 !
					}
				} else if (flattenChildExpr instanceof MultExpr) {
					List<Expr> flattenChildTerms = ((MultExpr) flattenChildExpr).exprs;
					for(val flattenChildTerm: flattenChildTerms) {
						if (flattenChildTerm instanceof LiteralDoubleExpr) {
							literal *= ((LiteralDoubleExpr) flattenChildTerm).value;
							if (literal == 0.0) {
								return b.lit0(); // 0 * x*y*..= 0 !
							}
						} else {
							flattenMultExprs.add(flattenChildTerm);
						}
					}
				} else {
					if (flattenChildExpr instanceof SumExpr) {
						// recursive on other FlatternSumExprVisitor: flatten sum(sum(...)))
						flattenChildExpr = flattenChildExpr.accept(FlatternSumExprVisitor.INSTANCE);
					}
					flattenMultExprs.add(flattenChildExpr);
				}
			}
			return b.mult(literal, flattenMultExprs);
		}

	}


}
