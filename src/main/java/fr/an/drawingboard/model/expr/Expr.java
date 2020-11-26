package fr.an.drawingboard.model.expr;

import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.VarDef;
import lombok.RequiredArgsConstructor;

/**
 * AST for Expression:
 * known sub-classes:
 * <PRE> 
 * - LiteralDoubleExpr : double value
 * - SumExpr  : e1 + e2 + .. eN
 * - MultExpr : e1 * e2 * .. eN
 * - VariableExpr: expr for var
 * - ParamDefExpr: expr for paramDef 
 * </PRE>
 */
public abstract class Expr {

	public abstract void accept(ExprVisitor visitor);
	
	public abstract <TRes,TParam> TRes accept(ExprFunc1Visitor<TRes,TParam> visitor, TParam param);

	public abstract <TRes> TRes accept(ExprFuncVisitor<TRes> visitor);

	// --------------------------------------------------------------------------------------------

	/**
	 * Literal expr
	 */
	@RequiredArgsConstructor
	public static class LiteralDoubleExpr extends Expr {

		public final double value;
		
		@Override
		public void accept(ExprVisitor visitor) {
			visitor.caseLiteral(this);
		}

		@Override
		public <TRes,TParam> TRes accept(ExprFunc1Visitor<TRes,TParam> visitor, TParam param) {
			return visitor.caseLiteral(this, param);
		}
		
		@Override
		public <TRes> TRes accept(ExprFuncVisitor<TRes> visitor) {
			return visitor.caseLiteral(this);
		}
		
		public static final LiteralDoubleExpr VAL_0 = new LiteralDoubleExpr(0);
		public static final LiteralDoubleExpr VAL_1 = new LiteralDoubleExpr(1);
		public static final LiteralDoubleExpr VAL_minus1 = new LiteralDoubleExpr(-1);
		public static final LiteralDoubleExpr VAL_2 = new LiteralDoubleExpr(2);
		public static final LiteralDoubleExpr VAL_minus2 = new LiteralDoubleExpr(-2);
		public static final LiteralDoubleExpr VAL_05 = new LiteralDoubleExpr(0.5);
		public static final LiteralDoubleExpr VAL_minus05 = new LiteralDoubleExpr(-0.5);

		public static boolean isLit0(Expr expr) {
			return (expr instanceof LiteralDoubleExpr)
					&& ((LiteralDoubleExpr) expr).value == 0.0;
		}

		public static boolean isLit1(Expr expr) {
			return (expr instanceof LiteralDoubleExpr)
					&& ((LiteralDoubleExpr) expr).value == 1.0;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LiteralDoubleExpr other = (LiteralDoubleExpr) obj;
			if (value != other.value) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Sum expr: expr0 + expr1 + ... + exprN
	 */
	public static class SumExpr extends Expr {
		public final ImmutableList<Expr> exprs;

		public SumExpr(Expr... exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}
		public SumExpr(List<Expr> exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.caseSum(this);
		}

		@Override
		public <TRes,TParam> TRes accept(ExprFunc1Visitor<TRes,TParam> visitor, TParam param) {
			return visitor.caseSum(this, param);
		}

		@Override
		public <TRes> TRes accept(ExprFuncVisitor<TRes> visitor) {
			return visitor.caseSum(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((exprs == null) ? 0 : exprs.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SumExpr other = (SumExpr) obj;
			if (exprs == null) {
				if (other.exprs != null)
					return false;
			} else if (!exprs.equals(other.exprs))
				return false;
			return true;
		}
		
	}

	/**
	 * Mult expr: expr0 * expr1 * ... * exprN
	 */
	public static class MultExpr extends Expr {
		public final ImmutableList<Expr> exprs;

		public MultExpr(Expr... exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}
		public MultExpr(List<Expr> exprs) {
			this.exprs = ImmutableList.copyOf(exprs);
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.caseMult(this);
		}

		@Override
		public <TRes,TParam> TRes accept(ExprFunc1Visitor<TRes,TParam> visitor, TParam param) {
			return visitor.caseMult(this, param);
		}
		
		@Override
		public <TRes> TRes accept(ExprFuncVisitor<TRes> visitor) {
			return visitor.caseMult(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((exprs == null) ? 0 : exprs.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MultExpr other = (MultExpr) obj;
			if (exprs == null) {
				if (other.exprs != null)
					return false;
			} else if (!exprs.equals(other.exprs))
				return false;
			return true;
		}

		
	}

	// --------------------------------------------------------------------------------------------

	public static class VariableExpr extends Expr {
		
		public final VarDef varDef;

		/* callable only from VarDef, for unicity */
		public VariableExpr(VarDef varDef, Object checkCreator) {
			this.varDef = varDef;
			VarDef.checkCreator(checkCreator);
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.caseVariable(this);
		}

		@Override
		public <TRes,TParam> TRes accept(ExprFunc1Visitor<TRes,TParam> visitor, TParam param) {
			return visitor.caseVariable(this, param);
		}

		@Override
		public <TRes> TRes accept(ExprFuncVisitor<TRes> visitor) {
			return visitor.caseVariable(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((varDef == null) ? 0 : varDef.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VariableExpr other = (VariableExpr) obj;
			if (varDef == null) {
				if (other.varDef != null)
					return false;
			} else if (!varDef.equals(other.varDef))
				return false;
			return true;
		}

		
	}

	// --------------------------------------------------------------------------------------------

	public static final class ParamDefExpr extends Expr {
		
		public final ParamDef paramDef;

		/* callable only from ParamDef, for unicity */
		public ParamDefExpr(ParamDef paramDef, Object checkCreator) {
			this.paramDef = paramDef;
			ParamDef.checkCreator(checkCreator);
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.caseParamDef(this);
		}

		@Override
		public <TRes, TParam> TRes accept(ExprFunc1Visitor<TRes,TParam> visitor, TParam param) {
			return visitor.caseParamDef(this, param);
		}

		@Override
		public <TRes> TRes accept(ExprFuncVisitor<TRes> visitor) {
			return visitor.caseParamDef(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((paramDef == null) ? 0 : paramDef.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ParamDefExpr other = (ParamDefExpr) obj;
			if (paramDef == null) {
				if (other.paramDef != null)
					return false;
			} else if (!paramDef.equals(other.paramDef))
				return false;
			return true;
		}
		
	}
}
