package fr.an.drawingboard.math.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.TracePt;
import lombok.val;

public class ExprBuilder {

	public static final ExprBuilder INSTANCE = new ExprBuilder();
	protected ExprBuilder() {
	}

	public Expr lit(double value) {
		if (value == 0.0) {
			return lit0();
		} else if (value == 1.0) {
			return lit1();
		} else if (value == -1.0) {
			return litMinus1();
		} else if (value == 2.0) {
			return lit2();
		} else if (value == -2.0) {
			return litMinus2();
		} else if (value == 0.5) {
			return litInv2();
		} else if (value == -0.5) {
			return litMinusInv2();
		} else {
			return new LiteralDoubleExpr(value);
		}
	}

	public Expr lit0() {
		return LiteralDoubleExpr.VAL_0;
	}
	public Expr lit1() {
		return LiteralDoubleExpr.VAL_1;
	}
	public Expr litMinus1() {
		return LiteralDoubleExpr.VAL_minus1;
	}
	public Expr lit2() {
		return LiteralDoubleExpr.VAL_2;
	}
	public Expr litMinus2() {
		return LiteralDoubleExpr.VAL_minus2;
	}
	public Expr litInv2() {
		return LiteralDoubleExpr.VAL_inv2;
	}
	public Expr litMinusInv2() {
		return LiteralDoubleExpr.VAL_minusInv2;
	}

	public Expr sum(Expr lhs, Expr rhs) {
		return new SumExpr(lhs, rhs);
	}

	public Expr sum(double lhs, Expr rhs) {
		return sum(lit(lhs), rhs);
	}

	public Expr sum(Expr... exprs) {
		if (exprs.length == 0) {
			return lit0();
		} else if (exprs.length == 1) {
			return exprs[0];
		}
		return new SumExpr(exprs);
	}

	public Expr sum(List<Expr> exprs) {
		if (exprs.isEmpty()) {
			return lit0();
		} else if (exprs.size() == 1) {
			return exprs.get(0);
		}
		return sum(0.0, exprs);
	}

	public Expr sum(double expr0, List<Expr> exprs) {
		if (exprs.isEmpty()) {
			return lit(expr0);
		} else if (exprs.size() == 1 && expr0 == 0.0) {
			return exprs.get(0);
		}
		// extract all literal from exprs, sum them
		double resultLiteral = expr0;
		List<Expr> remainingExprs = new ArrayList<>(exprs.size());
		for(val e : exprs) {
			if (e instanceof LiteralDoubleExpr) {
				double eDouble = ((LiteralDoubleExpr) e).value;
				resultLiteral += eDouble;
			} else if (e instanceof SumExpr) {
				// flattern .. TODO recurse
				remainingExprs.addAll(((SumExpr)e).exprs);
			} else {
				remainingExprs.add(e);
			}
		}
		if (resultLiteral != 0.0) {
			remainingExprs.add(lit(resultLiteral));
		}
		if (remainingExprs.isEmpty()) {
			return lit0();
		}
		if (remainingExprs.size() == 1) {
			return remainingExprs.get(0);
		}
		return new SumExpr(remainingExprs);
	}

	public Expr minus(Expr expr) {
		return mult(litMinus1(), expr);
	}
	
	public Expr minus(Expr lhs, Expr rhs) {
		return sum(lhs, minus(rhs));
	}

	public Expr minus(Expr lhs, double rhs) {
		return sum(lhs, lit(-rhs));
	}

	public Expr minus(double lhs, Expr rhs) {
		return sum(lit(lhs), minus(rhs));
	}

	public Expr mult(Expr lhs, Expr rhs) {
		return new MultExpr(lhs, rhs);
	}

	public Expr mult(Expr lhs, double rhs) {
		return mult(lhs, lit(rhs));
	}

	public Expr div(Expr lhs, double rhs) {
		return mult(lhs, lit(1.0 / rhs));
	}

	public Expr mult(Expr... exprs) {
		if (exprs.length == 0) {
			return lit1(); // should not occur
		} else if (exprs.length == 1) {
			return exprs[0];
		}
		return new MultExpr(exprs);
	}

	public Expr mult(List<Expr> exprs) {
		if (exprs.isEmpty()) {
			return lit1(); // should not occur
		} else if (exprs.size() == 1) {
			return exprs.get(0);
		}
		return mult(1.0, exprs);
	}

	public Expr mult(double expr0, Expr expr1) {
		return mult(lit(expr0), expr1);
	}

	public Expr mult(double expr0, Expr expr1, List<Expr> exprs) {
		return mult(expr0, ImmutableList.<Expr>builder().add(expr1).addAll(exprs).build());
	}

	public Expr mult(double expr0, Expr expr1, Expr expr2, List<Expr> exprs) {
		return mult(expr0, ImmutableList.<Expr>builder().add(expr1).add(expr2).addAll(exprs).build());
	}

	public Expr mult(double expr0, Expr... exprs) {
		return mult(expr0, Arrays.asList(exprs));
	}

	public Expr mult(double expr0, List<Expr> exprs) {
		if (expr0 == 0.0) {
			return lit0();
		}
		if (exprs.isEmpty()) {
			return lit(expr0);
		} else {
			// extract all literal from exprs, multiply them
			double resultCoef = expr0;
			List<Expr> remainingExprs = new ArrayList<>(exprs.size());
			for(val e : exprs) {
				if (e instanceof LiteralDoubleExpr) {
					double eDouble = ((LiteralDoubleExpr) e).value;
					if (eDouble == 0.0) {
						return lit0(); // 0 *x*y ... = 0 !!
					}
					resultCoef *= eDouble;
				} else if (e instanceof MultExpr) {
					// flatten .. TODO recurse
					remainingExprs.addAll(((MultExpr)e).exprs);
				} else {
					remainingExprs.add(e);
				}
			}
			if (resultCoef != 1.0) {
				remainingExprs.add(lit(resultCoef));
			} else if (remainingExprs.isEmpty()) {
				return lit(resultCoef);
			}
			if (remainingExprs.size() == 1) {
				return remainingExprs.get(0);
			}
			return new MultExpr(remainingExprs);
		}
	}

	public Expr square(Expr expr) {
		return mult(expr, expr);
	}

	public Expr sumSquare(Expr x, Expr y) {
		return sum(square(x), square(y));
	}

	public Expr squareDist(PtExpr startPt, TracePt endPt) {
		return squareDist(startPt.x, startPt.y, endPt.x, endPt.y);
	}
	
	public Expr squareDist(Expr startX, Expr startY, double endX, double endY) {
		Expr distX = minus(startX, endX);
		Expr distY = minus(startY, endY);
		return sum(square(distX), square(distY));
	}

	public Expr linear(double coef0, Expr expr0, double coef1, Expr expr1) {
		return sum(mult(coef0, expr0), mult(coef1, expr1));
	}

	public Expr linear(double coef0, Expr expr0, double coef1, Expr expr1, double coef2, Expr expr2) {
		return sum(mult(coef0, expr0), mult(coef1, expr1), mult(coef2, expr2));
	}

	public Expr linear(double coef0, Expr expr0, double coef1, Expr expr1, double coef2, Expr expr2, double coef3, Expr expr3) {
		return sum(mult(coef0, expr0), mult(coef1, expr1), mult(coef2, expr2), mult(coef3, expr3));
	}

}
