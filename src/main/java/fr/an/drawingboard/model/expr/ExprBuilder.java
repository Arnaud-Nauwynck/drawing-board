package fr.an.drawingboard.model.expr;

import java.util.List;

import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.trace.TracePt;

public class ExprBuilder {

	public static final ExprBuilder INSTANCE = new ExprBuilder();
	protected ExprBuilder() {
	}

	public Expr lit(double value) {
		return new LiteralDoubleExpr(value);
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
		return new SumExpr(exprs);
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

	public Expr mult(double lhs, Expr rhs) {
		return mult(lit(lhs), rhs);
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
		return new MultExpr(exprs);
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
	
}
