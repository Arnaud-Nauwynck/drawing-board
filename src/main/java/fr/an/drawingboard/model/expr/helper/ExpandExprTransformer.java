package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.ExprFunc0.DefaultExprTransformer;

/**
 * Expand expr transformer: 
 * <PRE>
 * a x ( b + c ) => ( a x b)  + ( a x c )
 * </PRE>
 *
 */
public class ExpandExprTransformer {

	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	protected ExpandExprTransformer() {
	}

	public static Expr expandExpr(Expr expr) {
		Expr res = expr.accept(ExpandExprVisitor.INSTANCE);
		return res;
	}

	/**
	 *
	 */
	public static final class ExpandExprVisitor extends DefaultExprTransformer {

		public static final ExpandExprVisitor INSTANCE = new ExpandExprVisitor();

		private ExpandExprVisitor() {
		}

		Expr expand(Expr e) {
			if (e == null) {
				return null; // should not occur!
			}
			return e.accept(this);
		}

		@Override
		public Expr caseMult(MultExpr expr) {
			// collect sum child eprs, and non sum exprs
			List<SumExpr> childSumExprs = new ArrayList<>();
			List<Expr> childNotSumExprs = new ArrayList<>();
			for(Expr e : expr.exprs) {
				if (e instanceof SumExpr) {
					childSumExprs.add((SumExpr) e);
				} else {
					childNotSumExprs.add(e);
				}
			}
			if (childSumExprs.isEmpty()) {
				// nothing to expand
				return expr;
			}
			List<Expr> expandSumExprs = new ArrayList<>();
			if (childSumExprs.size() == 1) {
				// optim simple case: (sum00+sum01+sum02+..)*d*e..
				List<Expr> sum0s = childSumExprs.get(0).exprs;
				for(int i0 = 0; i0 < sum0s.size(); i0++) {
					Expr sum0i = sum0s.get(i0);
					expandSumExprs.add(mult(childNotSumExprs, sum0i));
				}
			} else if (childSumExprs.size() == 2) {
				// optim case 2: (sum00+.. sum0n)*(sum10+.. + sum1n)*f*g..
				List<Expr> sum0s = childSumExprs.get(0).exprs;
				List<Expr> sum1s = childSumExprs.get(1).exprs;
				for(int i0 = 0; i0 < sum0s.size(); i0++) {
					Expr sum0i = sum0s.get(i0);
					for(int i1 = 0; i1 < sum1s.size(); i1++) {
						Expr sum1i = sum1s.get(i1);
						expandSumExprs.add(mult(childNotSumExprs, sum0i, sum1i));
					}
				}
			} else if (childSumExprs.size() == 3) {
				// optim case 3: (sum00+.. sum0n)*(sum10+.. + sum1n)*(sum2+..)f*g..
				List<Expr> sum0s = childSumExprs.get(0).exprs;
				List<Expr> sum1s = childSumExprs.get(1).exprs;
				List<Expr> sum2s = childSumExprs.get(2).exprs;
				for(int i0 = 0; i0 < sum0s.size(); i0++) {
					Expr sum0i = sum0s.get(i0);
					for(int i1 = 0; i1 < sum1s.size(); i1++) {
						Expr sum1i = sum1s.get(i1);
						for(int i2 = 0; i2 < sum2s.size(); i2++) {
							Expr sum2i = sum2s.get(i2);
							expandSumExprs.add(mult(childNotSumExprs, sum0i, sum1i, sum2i));
						}
					}
				}
			} else {
				// general case: (a00+a01+..a0n)*(a10+a11+...a1m)*(a20+a21+...a2p)* ...
				recursiveCartesianProducts(expandSumExprs, childNotSumExprs, childSumExprs, 0);
			}
			return b.sum(expandSumExprs);
		}

		private void recursiveCartesianProducts(List<Expr> res, 
				List<Expr> multExprs,
				List<SumExpr> expandSums, int index) {
			List<Expr> expandExprs = expandSums.get(index).exprs;
			for(Expr expandExpr : expandExprs) {
				List<Expr> childMultExprs = ImmutableList.<Expr>builder().addAll(multExprs).add(expandExpr).build();
				if (index + 1 < expandSums.size()) {
					// ** recurse **
					recursiveCartesianProducts(res, 
							childMultExprs, expandSums, index+1);
				} else {
					res.add(b.mult(childMultExprs));
				}
			}
		}

	}

	protected static Expr mult(List<Expr> ls1, Expr elt2) {
		List<Expr> ls = ImmutableList.<Expr>builder().addAll(ls1).add(elt2).build();
		return b.mult(ls);
	}

	protected static Expr mult(List<Expr> ls1, Expr elt2, Expr elt3) {
		List<Expr> ls = ImmutableList.<Expr>builder().addAll(ls1).add(elt2).add(elt3).build();
		return b.mult(ls);
	}

	protected static Expr mult(List<Expr> ls1, Expr elt2, Expr elt3, Expr elt4) {
		List<Expr> ls = ImmutableList.<Expr>builder().addAll(ls1).add(elt2).add(elt3).add(elt4).build();
		return b.mult(ls);
	}

}
