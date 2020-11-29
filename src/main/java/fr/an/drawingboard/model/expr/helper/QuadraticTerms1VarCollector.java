package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.ExprVisitor;
import fr.an.drawingboard.model.var.VarDef;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * collect quadratic terms out of an expression
 *
 */
public class QuadraticTerms1VarCollector {

	/**
	 * QuadForm(X)= A X^2 + B X + C
	 * 
	 * where A = quadLiteral .. for literal terms
	 *           + quadExpr .. for algebric terms
	 */
	@AllArgsConstructor
	public static class QuadraticForm1D {
		public final VarDef varDef;
		
		// when detected litteral coefficient on variable term:
		public final double quadTermLiteral;
		public final double linTermLiteral;
		public final double constTermLiteral;
		
		// when not litteral coefficient on variable term:
		public final Expr quadTermExpr;
		public final Expr linTermExpr;
		public final Expr constTermExpr;
	}
	

	public static QuadraticForm1D extractQuadTerms(Expr expr, VarDef varDef) {
		QuadraticTerms1DBuilder res = new QuadraticTerms1DBuilder(varDef);
		extractQuadTerms(res, expr);
		return res.build();
	}
	
	public static void extractQuadTerms(QuadraticTerms1DBuilder res, Expr expr) {
		Expr expandedExpr = ExpandExprTransformer.expandExpr(expr); //ex: (a+b)(c+d) => ac+ad+bc+bd
		Expr flatternExpandedExpr = FlattenExprTransformer.flatternExpr(expandedExpr); // ex: (a+(b+(c+..))) => (a+b+c+..), idem mult
		
		val visitor = new QuadraticTerms1DExtractVisitor(res);
		flatternExpandedExpr.accept(visitor);
	}
	
	public static class QuadraticTerms1DBuilder {
		public final VarDef varDef;
		
		// when detected litteral coefficient on variable term:
		public double quadTermLiteral;
		public double linTermLiteral;
		public double constTermLiteral;
		
		// when not litteral coefficient on variable term:
		public final List<Expr> quadTermExpr = new ArrayList<>();
		public final List<Expr> linTermExpr = new ArrayList<>();
		public final List<Expr> constTermExpr = new ArrayList<>();
		
		public QuadraticTerms1DBuilder(VarDef varDef) {
			this.varDef = varDef;
		}
		
		public QuadraticForm1D build() {
			val B = ExprBuilder.INSTANCE;
			return new QuadraticForm1D(varDef, quadTermLiteral, linTermLiteral, constTermLiteral, B.sum(quadTermExpr), B.sum(linTermExpr), B.sum(constTermExpr));
		}
	}
	
	@RequiredArgsConstructor
	private static class QuadraticTerms1DExtractVisitor extends ExprVisitor {
		private final QuadraticTerms1DBuilder res;
		
		@Override
		public void caseLiteral(LiteralDoubleExpr expr) {
			res.constTermLiteral += expr.value;
		}

		@Override
		public void caseSum(SumExpr expr) {
			// recurse on sum only (sum(a, b, c) ... or sum(a, sum(b..)) ... should be already expanded expr?!
			// should be only "sum( mult(..), mult(..), ... literal )"
			for(val e: expr.exprs) {
				e.accept(this);
			}
		}

		@Override
		public void caseMult(MultExpr expr) {
			// detect terms quad,linear,const: "a.x.b.x.c", "a.x.b", "a.b" ... where a,b does not depends on specified var x
			Terms1DVarPowBuilder termsVarPow = new Terms1DVarPowBuilder();
			val multExtract = new QuadraticMultTerm1DVarPowExtractVisitor(res, termsVarPow);
			for(val e : expr.exprs) {
				e.accept(multExtract);
			}
			// add to quad/linear/other term depending on power variables
			int varPower = termsVarPow.varPower;
			ExprBuilder b = ExprBuilder.INSTANCE;
			if (varPower == 2) {
				if (!termsVarPow.multExpr.isEmpty()) {
					res.quadTermExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
				} else {
					res.quadTermLiteral += termsVarPow.multLiteral;
				}
			} else if (varPower == 1) {
				if (!termsVarPow.multExpr.isEmpty()) {
					res.linTermExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
				} else {
					res.linTermLiteral += termsVarPow.multLiteral;
				}
			} else if (varPower == 0) {
				if (!termsVarPow.multExpr.isEmpty()) {
					res.constTermExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
				} else {
					res.constTermLiteral += termsVarPow.multLiteral;
				}
				
			} else { // not a quadratic term!
				res.constTermExpr.add(expr);
			}
		}

		@Override
		public void caseVariable(VariableExpr expr) {
			if (expr.varDef == res.varDef) {
				res.linTermLiteral += 1;
			}
		}

		@Override
		public void caseParamDef(ParamDefExpr expr) {
			res.constTermExpr.add(expr);
		}
		
	}
	
	private static class Terms1DVarPowBuilder {
		int varPower = 0;
		double multLiteral = 1.0;
		List<Expr> multExpr = new ArrayList<>();
		
		public void addMultPow(VarDef varDef) {
			varPower++;
		}
	}
	
	@RequiredArgsConstructor
	private static class QuadraticMultTerm1DVarPowExtractVisitor extends ExprVisitor {
		private final QuadraticTerms1DBuilder resBuilder;
		private final Terms1DVarPowBuilder resTerm;

		@Override
		public void caseLiteral(LiteralDoubleExpr expr) {
			resTerm.multLiteral *= expr.value;
		}
		@Override
		public void caseSum(SumExpr expr) {
			throw new IllegalStateException(); // should not recurse on sum, for an already expanded expr
		}
		@Override
		public void caseMult(MultExpr expr) {
			for(val e: expr.exprs) {
				// recurse on mult only
				e.accept(this);
			}
		}
		@Override
		public void caseVariable(VariableExpr expr) {
			if (expr.varDef == resBuilder.varDef) {
				resTerm.addMultPow(expr.varDef);
			} else {
				resTerm.multExpr.add(expr);
			}
		}
		@Override
		public void caseParamDef(ParamDefExpr expr) {
			resTerm.multExpr.add(expr);
		}
	}
	
}
