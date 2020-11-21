package fr.an.drawingboard.model.expr;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.RequiredArgsConstructor;

public abstract class Expr {

	@RequiredArgsConstructor
	public static class LiteralDoubleExpr extends Expr {
		public final double value;
		
		public static final LiteralDoubleExpr VAL_0 = new LiteralDoubleExpr(0);
		public static final LiteralDoubleExpr VAL_1 = new LiteralDoubleExpr(1);
		public static final LiteralDoubleExpr VAL_minus1 = new LiteralDoubleExpr(-1);
		public static final LiteralDoubleExpr VAL_2 = new LiteralDoubleExpr(2);
		public static final LiteralDoubleExpr VAL_minus2 = new LiteralDoubleExpr(-2);
		public static final LiteralDoubleExpr VAL_05 = new LiteralDoubleExpr(0.5);
		public static final LiteralDoubleExpr VAL_minus05 = new LiteralDoubleExpr(-0.5);
	}

	
	public static class SumExpr extends Expr {
		public final ImmutableList<Expr> exprs;

		public SumExpr(Expr... exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}
		public SumExpr(List<Expr> exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}
		
	}

	public static class MultExpr extends Expr {
		public final ImmutableList<Expr> exprs;

		public MultExpr(Expr... exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}
		public MultExpr(List<Expr> exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}
		
	}
	
}
