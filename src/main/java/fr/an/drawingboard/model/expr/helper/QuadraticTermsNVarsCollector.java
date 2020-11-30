package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.ExprVisitor;
import fr.an.drawingboard.model.expr.VarDef;
import fr.an.drawingboard.model.expr.matrix.ImmutableDoubleMatrix;
import fr.an.drawingboard.model.expr.matrix.ImmutableDoubleMatrix.MatrixDoubleBuilder;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr.MatrixExprBuilder;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * collect quadratic terms out of an expression
 *
 */
public class QuadraticTermsNVarsCollector {

	/**
	 * QuadForm(X)= 'X A X + 'B X + C
	 * where A = quadLiteralMatrix .. for literal terms
	 *           + quadExprMatrix .. for algebric terms
	 */
	@AllArgsConstructor
	public static class QuadraticForm {
		public final ImmutableList<VarDef> vars;
		public final ImmutableMap<VarDef,Integer> varToIndex;
		
		// when detected litteral coefficient on variable term:
		public final ImmutableDoubleMatrix quadLiteralMatrix;
		public final ImmutableDoubleMatrix linLiteralMatrix;
		public final double constLiteral;
		
		// when not litteral coefficient on variable term:
		public final MatrixExpr quadExprMatrix;
		public final MatrixExpr linExprMatrix;
		public final Expr constExpr;
	}
	

	public static QuadraticForm extractQuadTerms(Expr expr, List<VarDef> vars) {
		QuadraticTermsBuilder res = new QuadraticTermsBuilder(vars);
		extractQuadTerms(res, expr);
		return res.build();
	}
	
	public static void extractQuadTerms(QuadraticTermsBuilder res, Expr expr) {
		Expr expandedExpr = ExpandExprTransformer.expandExpr(expr); //ex: (a+b)(c+d) => ac+ad+bc+bd
		Expr flatternExpandedExpr = FlattenExprTransformer.flatternExpr(expandedExpr); // ex: (a+(b+(c+..))) => (a+b+c+..), idem mult
		
		val visitor = new QuadraticTermsExtractVisitor(res);
		flatternExpandedExpr.accept(visitor);
	}
	
	public static class QuadraticTermsBuilder {
		public final ImmutableList<VarDef> vars;
		public final ImmutableMap<VarDef,Integer> varToIndex;
		
		// when detected litteral coefficient on variable term:
		public final MatrixDoubleBuilder quadLiteralMatrix;
		public final MatrixDoubleBuilder linLiteralMatrix;
		public double constLiteral;
		
		// when not litteral coefficient on variable term:
		public final MatrixExprBuilder quadExprMatrix;
		public final MatrixExprBuilder linExprMatrix;
		public final List<Expr> constExpr;
		
		public QuadraticTermsBuilder(List<VarDef> vars) {
			final int dim = vars.size();
			this.vars = ImmutableList.copyOf(vars);
			Builder<VarDef,Integer> varToIndexBuilder = ImmutableMap.builder();
			for(int i = 0; i < dim; i++) {
				varToIndexBuilder.put(vars.get(i), i);
			}
			this.varToIndex = varToIndexBuilder.build();
			
			this.quadLiteralMatrix = new MatrixDoubleBuilder(dim, dim);
			this.linLiteralMatrix = new MatrixDoubleBuilder(1, dim);
			this.constLiteral = 0;
			this.quadExprMatrix = new MatrixExprBuilder(dim, dim);
			this.linExprMatrix = new MatrixExprBuilder(1, dim);
			this.constExpr = new ArrayList<>();
		}
		
		public QuadraticForm build() {
			return new QuadraticForm(vars, varToIndex, //
					quadLiteralMatrix.build(), linLiteralMatrix.build(), constLiteral, //
					quadExprMatrix.build(), linExprMatrix.build(), ExprBuilder.INSTANCE.sum(constExpr));
		}
	}
	
	@RequiredArgsConstructor
	private static class QuadraticTermsExtractVisitor extends ExprVisitor {
		private final QuadraticTermsBuilder res;
		//public static final QuadraticTermsExtractVisitor INSTANCE = new QuadraticTermsExtractVisitor();
		
		@Override
		public void caseLiteral(LiteralDoubleExpr expr) {
			res.constLiteral += expr.value;
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
			// detect terms quad,linear,const: "a.x.y.b.c", "a.x.b", "a.b" ... where a,b does not depends on specified vars (x, y..)
			// can be flat mult(a, x, y, b, c) ... or recursive mult: mult(a, mult(x, mult(y ..)))
			// mult case requiring expand: (a+x)*(b*y) => a*b + a*y + x*b + x*y
			TermsVarPowBuilder termsVarPow = new TermsVarPowBuilder();
			val multExtract = new QuadraticMultTermVarPowExtractVisitor(res, termsVarPow);
			for(val e : expr.exprs) {
				e.accept(multExtract);
			}
			// add to quad/linear/other term depending on power variables
			int distinctVarsCount = termsVarPow.varPowers.size();
			ExprBuilder b = ExprBuilder.INSTANCE;
			if (distinctVarsCount == 2) {
				Iterator<Entry<VarDef, AtomicInteger>> varTermsIter = termsVarPow.varPowers.entrySet().iterator();
				
				var varTermEntry0 = varTermsIter.next();
				VarDef var0 = varTermEntry0.getKey();
				int var0Index = res.varToIndex.get(var0);
				int var0Power = varTermEntry0.getValue().intValue();
				
				var varTermEntry1 = varTermsIter.next();
				VarDef var1 = varTermEntry1.getKey();
				int var1Index = res.varToIndex.get(var1);
				int var1Power = varTermEntry1.getValue().intValue();

				Expr coef05Expr = (!termsVarPow.multOtherTerms.isEmpty())?
						b.mult(0.5 * termsVarPow.multLiteralTerm, termsVarPow.multOtherTerms)
						: null;
				if (var0Power == 1 && var1Power == 1) {
					if (coef05Expr != null) {
						res.quadExprMatrix.add(var0Index, var1Index, coef05Expr);
						res.quadExprMatrix.add(var1Index, var0Index, coef05Expr);
					} else {
						res.quadLiteralMatrix.add(var0Index, var1Index, 0.5 * termsVarPow.multLiteralTerm);
						res.quadLiteralMatrix.add(var1Index, var0Index, 0.5 * termsVarPow.multLiteralTerm);
					}
				} else {
					// not quadratic term
					res.constExpr.add(expr);
				}
				
			} else if (distinctVarsCount == 1) {
				Iterator<Entry<VarDef, AtomicInteger>> varTermsIter = termsVarPow.varPowers.entrySet().iterator();
						
				var varTermEntry0 = varTermsIter.next();
				VarDef var0 = varTermEntry0.getKey();
				int var0Index = res.varToIndex.get(var0);
				int var0Power = varTermEntry0.getValue().intValue();
				
				Expr coefExpr = (!termsVarPow.multOtherTerms.isEmpty())?
						b.mult(termsVarPow.multLiteralTerm, termsVarPow.multOtherTerms)
						: null;
				if (var0Power == 2) {
					if (coefExpr != null) {
						res.quadExprMatrix.add(var0Index, var0Index, coefExpr);
					} else {
						res.quadLiteralMatrix.add(var0Index, var0Index, termsVarPow.multLiteralTerm);
					}
				} else if (var0Power == 1) {
					if (coefExpr != null) {
						res.linExprMatrix.add(0, var0Index, coefExpr);
					} else {
						res.linLiteralMatrix.add(0, var0Index, termsVarPow.multLiteralTerm);
					}
				}
				
			} else if (distinctVarsCount == 0) {
				Expr coefExpr = (!termsVarPow.multOtherTerms.isEmpty())?
						b.mult(termsVarPow.multLiteralTerm, termsVarPow.multOtherTerms)
						: null;
				if (coefExpr != null) {
					res.constExpr.add(coefExpr);
				} else {
					res.constLiteral += termsVarPow.multLiteralTerm;
				}
				
			} else { // not  quadratic term!
				res.constExpr.add(expr);
			}
		}

		@Override
		public void caseVariable(VariableExpr expr) {
			Integer foundVarIndex = res.varToIndex.get(expr.varDef);
			if (null != foundVarIndex) {
				res.linLiteralMatrix.add(0, foundVarIndex, 1.0);
			}
		}

		@Override
		public void caseParamDef(ParamDefExpr expr) {
			res.constExpr.add(expr);
		}
		
	}
	
	private static class TermsVarPowBuilder {
		Map<VarDef,AtomicInteger> varPowers = new HashMap<>();
		double multLiteralTerm = 1.0;
		List<Expr> multOtherTerms = new ArrayList<>();
		
		public void addMultPow(VarDef varDef) {
			AtomicInteger pow = varPowers.get(varDef);
			if (pow == null) {
				pow = new AtomicInteger();
				varPowers.put(varDef, pow);
			}
			pow.incrementAndGet();
		}
	}
	
	@RequiredArgsConstructor
	private static class QuadraticMultTermVarPowExtractVisitor extends ExprVisitor {
		private final QuadraticTermsBuilder resBuilder;
		private final TermsVarPowBuilder res;

		@Override
		public void caseLiteral(LiteralDoubleExpr expr) {
			res.multLiteralTerm *= expr.value;
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
			Integer found = resBuilder.varToIndex.get(expr.varDef);
			if (null != found) {
				res.addMultPow(expr.varDef);
			} else {
				res.multOtherTerms.add(expr);
			}
		}
		@Override
		public void caseParamDef(ParamDefExpr expr) {
			res.multOtherTerms.add(expr);
		}
	}
	
}
