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
import fr.an.drawingboard.model.expr.matrix.ImmutableMatrixDouble;
import fr.an.drawingboard.model.expr.matrix.ImmutableMatrixDouble.MatrixDoubleBuilder;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr.MatrixExprBuilder;
import fr.an.drawingboard.model.var.VarDef;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * collect quadratic terms out of an expression
 *
 */
public class QuadraticTermsNVarsCollector {

	@AllArgsConstructor
	public static class QuadraticForm {
		public final ImmutableList<VarDef> vars;
		public final ImmutableMap<VarDef,Integer> varToIndex;
		
		// when detected litteral coefficient on variable term:
		public final ImmutableMatrixDouble doubleQuadTerms;
		public final ImmutableMatrixDouble doubleLinearTerms;
		public final double doubleTerm;
		
		// when not litteral coefficient on variable term:
		public final MatrixExpr otherQuadTerms;
		public final MatrixExpr otherLinearTerms;
		public final Expr constTerm;
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
		public final MatrixDoubleBuilder doubleQuadTerms;
		public final MatrixDoubleBuilder doubleLinearTerms;
		public double doubleTerm;
		
		// when not litteral coefficient on variable term:
		public final MatrixExprBuilder otherQuadTerms;
		public final MatrixExprBuilder otherLinearTerms;
		public final List<Expr> otherTerms;
		
		public QuadraticTermsBuilder(List<VarDef> vars) {
			final int dim = vars.size();
			this.vars = ImmutableList.copyOf(vars);
			Builder<VarDef,Integer> varToIndexBuilder = ImmutableMap.builder();
			for(int i = 0; i < dim; i++) {
				varToIndexBuilder.put(vars.get(i), i);
			}
			this.varToIndex = varToIndexBuilder.build();
			
			this.doubleQuadTerms = new MatrixDoubleBuilder(dim, dim);
			this.doubleLinearTerms = new MatrixDoubleBuilder(1, dim);
			this.doubleTerm = 0;
			this.otherQuadTerms = new MatrixExprBuilder(dim, dim);
			this.otherLinearTerms = new MatrixExprBuilder(1, dim);
			this.otherTerms = new ArrayList<>();
		}
		
		public QuadraticForm build() {
			return new QuadraticForm(vars, varToIndex, //
					doubleQuadTerms.build(), doubleLinearTerms.build(), doubleTerm, //
					otherQuadTerms.build(), otherLinearTerms.build(), ExprBuilder.INSTANCE.sum(otherTerms));
		}
	}
	
	@RequiredArgsConstructor
	private static class QuadraticTermsExtractVisitor extends ExprVisitor {
		private final QuadraticTermsBuilder res;
		//public static final QuadraticTermsExtractVisitor INSTANCE = new QuadraticTermsExtractVisitor();
		
		@Override
		public void caseLiteral(LiteralDoubleExpr expr) {
			res.doubleTerm += expr.value;
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
			val multExtract = new QuadraticMultTermVarPowExtractVisitor(termsVarPow);
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
						b.mult(0.5 * termsVarPow.literalTerm, termsVarPow.multOtherTerms)
						: null;
				if (var0Power == 1 && var1Power == 1) {
					if (coef05Expr != null) {
						res.otherQuadTerms.add(var0Index, var1Index, coef05Expr);
						res.otherQuadTerms.add(var1Index, var0Index, coef05Expr);
					} else {
						res.doubleQuadTerms.add(var0Index, var1Index, 0.5 * termsVarPow.literalTerm);
						res.doubleQuadTerms.add(var1Index, var0Index, 0.5 * termsVarPow.literalTerm);
					}
				} else {
					// not quadratic term
					res.otherTerms.add(expr);
				}
				
			} else if (distinctVarsCount == 1) {
				Iterator<Entry<VarDef, AtomicInteger>> varTermsIter = termsVarPow.varPowers.entrySet().iterator();
						
				var varTermEntry0 = varTermsIter.next();
				VarDef var0 = varTermEntry0.getKey();
				int var0Index = res.varToIndex.get(var0);
				int var0Power = varTermEntry0.getValue().intValue();
				
				Expr coefExpr = (!termsVarPow.multOtherTerms.isEmpty())?
						b.mult(termsVarPow.literalTerm, termsVarPow.multOtherTerms)
						: null;
				if (var0Power == 2) {
					if (coefExpr != null) {
						res.otherQuadTerms.add(var0Index, var0Index, coefExpr);
					} else {
						res.doubleQuadTerms.add(var0Index, var0Index, termsVarPow.literalTerm);
					}
				} else if (var0Power == 1) {
					if (coefExpr != null) {
						res.otherLinearTerms.add(0, var0Index, coefExpr);
					} else {
						res.doubleLinearTerms.add(0, var0Index, termsVarPow.literalTerm);
					}
				}
				
			} else if (distinctVarsCount == 0) {
				Expr coefExpr = (!termsVarPow.multOtherTerms.isEmpty())?
						b.mult(termsVarPow.literalTerm, termsVarPow.multOtherTerms)
						: null;
				if (coefExpr != null) {
					res.otherTerms.add(coefExpr);
				} else {
					res.doubleTerm += termsVarPow.literalTerm;
				}
				
			} else { // not  quadratic term!
				res.otherTerms.add(expr);
			}
		}

		@Override
		public void caseVariable(VariableExpr expr) {
			Integer foundVarIndex = res.varToIndex.get(expr.varDef);
			if (null != foundVarIndex) {
				res.doubleLinearTerms.add(0, foundVarIndex, 1.0);
			}
		}

		@Override
		public void caseParamDef(ParamDefExpr expr) {
			res.otherTerms.add(expr);
		}
		
	}
	
	private static class TermsVarPowBuilder {
		Map<VarDef,AtomicInteger> varPowers = new HashMap<>();
		double literalTerm = 1.0;
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
		private final TermsVarPowBuilder res;

		@Override
		public void caseLiteral(LiteralDoubleExpr expr) {
			res.literalTerm *= expr.value;
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
			res.addMultPow(expr.varDef);
		}
		@Override
		public void caseParamDef(ParamDefExpr expr) {
			res.multOtherTerms.add(expr);
		}
	}
	
}
