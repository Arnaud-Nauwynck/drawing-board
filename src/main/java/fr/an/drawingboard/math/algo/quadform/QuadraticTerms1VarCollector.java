package fr.an.drawingboard.math.algo.quadform;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.math.algo.base.ExpandExprTransformer;
import fr.an.drawingboard.math.algo.base.ExprVarDependiesAnalyzer;
import fr.an.drawingboard.math.algo.base.FlattenExprTransformer;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.ExprVisitor;
import fr.an.drawingboard.math.expr.VarDef;
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
		
		// when not literal coefficient, independant of varDef
		public final Expr quadTermVarIndepExpr;
		public final Expr linTermVarIndepExpr;
		public final Expr constTermVarIndepExpr;

		public final Expr otherNonQuadTermVarDependentExpr;

	}
	

	public static QuadraticForm1D extractQuadTerms(Expr expr, VarDef varDef) {
		QuadraticTerms1DBuilder res = new QuadraticTerms1DBuilder(varDef);
		extractQuadTerms(res, expr);
		return res.build();
	}
	
	public static void extractQuadTerms(QuadraticTerms1DBuilder res, Expr expr) {
		Expr expandedExpr = ExpandExprTransformer.expandExpr(expr); //ex: (a+b)(c+d) => ac+ad+bc+bd
		Expr flatternExpandedExpr = FlattenExprTransformer.flattenExpr(expandedExpr); // ex: (a+(b+(c+..))) => (a+b+c+..), idem mult
		
		val visitor = new QuadraticTerms1DExtractVisitor(res);
		flatternExpandedExpr.accept(visitor);
	}
	
	public static class QuadraticTerms1DBuilder {
		public final VarDef varDef;
		
		// when detected litteral coefficient on variable term:
		public double quadTermLiteral;
		public double linTermLiteral;
		public double constTermLiteral;
		
		// when not litteral coefficient, independant of varDef
		public final List<Expr> quadTermVarIndepExpr = new ArrayList<>();
		public final List<Expr> linTermVarIndepExpr = new ArrayList<>();
		public final List<Expr> constTermVarIndepExpr = new ArrayList<>();

		public final List<Expr> otherNonQuadTermVarDependentExpr = new ArrayList<>();
		
		public QuadraticTerms1DBuilder(VarDef varDef) {
			this.varDef = varDef;
		}
		
		public QuadraticForm1D build() {
			val B = ExprBuilder.INSTANCE;
			return new QuadraticForm1D(varDef, quadTermLiteral, linTermLiteral, constTermLiteral, 
					B.sum(quadTermVarIndepExpr), B.sum(linTermVarIndepExpr), B.sum(constTermVarIndepExpr),
					B.sum(otherNonQuadTermVarDependentExpr));
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
			val varDef = res.varDef;
			int varPower = termsVarPow.varPower;
			ExprBuilder b = ExprBuilder.INSTANCE;
			if (varPower == 2) {
				if (!termsVarPow.multExpr.isEmpty()) {
					if (! termsVarPow.dependsOnVar(varDef)) {
						res.quadTermVarIndepExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
					} else {
						res.otherNonQuadTermVarDependentExpr.add(b.mult(termsVarPow.multLiteral, varDef.expr, varDef.expr, termsVarPow.multExpr));
					}
				} else {
					res.quadTermLiteral += termsVarPow.multLiteral;
				}
			} else if (varPower == 1) {
				if (!termsVarPow.multExpr.isEmpty()) {
					if (! termsVarPow.dependsOnVar(varDef)) {
						res.linTermVarIndepExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
					} else {
						res.otherNonQuadTermVarDependentExpr.add(b.mult(termsVarPow.multLiteral, varDef.expr, termsVarPow.multExpr));
					}
				} else {
					res.linTermLiteral += termsVarPow.multLiteral;
				}
			} else if (varPower == 0) {
				if (!termsVarPow.multExpr.isEmpty()) {
					if (! termsVarPow.dependsOnVar(varDef)) {
						res.constTermVarIndepExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
					} else {
						res.otherNonQuadTermVarDependentExpr.add(b.mult(termsVarPow.multLiteral, termsVarPow.multExpr));
					}
				} else {
					res.constTermLiteral += termsVarPow.multLiteral;
				}
				
			} else { // if (varPower > 2) {
				res.otherNonQuadTermVarDependentExpr.add(expr);
			}
		}

		@Override
		public void caseVariable(VariableExpr expr) {
			if (expr.varDef == res.varDef) {
				res.linTermLiteral += 1;
			}
		}
	}
	
	private static class Terms1DVarPowBuilder {
		int varPower = 0;
		double multLiteral = 1.0;
		List<Expr> multExpr = new ArrayList<>();
		
		public void addMultPow(VarDef varDef) {
			varPower++;
		}
		public boolean dependsOnVar(VarDef varDef) {
			return ExprVarDependiesAnalyzer.analyzeIfDependsOnVar(multExpr, varDef);
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
	}
	
}
