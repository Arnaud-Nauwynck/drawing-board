package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.matrix.ImmutableMatrixDouble;
import fr.an.drawingboard.model.expr.matrix.ImmutableMatrixDouble.MatrixDoubleBuilder;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr.MatrixExprBuilder;
import lombok.AllArgsConstructor;

/**
 * collect quadratic terms out of an expression
 *
 */
public class QuadraticTermsCollector {

	@AllArgsConstructor
	public static class QuadraticTerms {
		public final ImmutableList<VariableExpr> vars;
		public final ImmutableMap<VariableExpr,Integer> varToIndex;
		
		// when detected litteral coefficient on variable term:
		public final ImmutableMatrixDouble doubleQuadTerms;
		public final ImmutableMatrixDouble doubleLinearTerms;
		public final double doubleTerm;
		
		// when not litteral coefficient on variable term:
		public final MatrixExpr otherQuadTerms;
		public final MatrixExpr otherLinearTerms;
		public final ImmutableList<Expr> otherTerms;
	}
	
	public static class QuadraticTermsBuilder {
		public final ImmutableList<VariableExpr> vars;
		public final ImmutableMap<VariableExpr,Integer> varToIndex;
		
		// when detected litteral coefficient on variable term:
		public final MatrixDoubleBuilder doubleQuadTerms;
		public final MatrixDoubleBuilder doubleLinearTerms;
		public double doubleTerm;
		
		// when not litteral coefficient on variable term:
		public final MatrixExprBuilder otherQuadTerms;
		public final MatrixExprBuilder otherLinearTerms;
		public final List<Expr> otherTerms;
		
		public QuadraticTermsBuilder(List<VariableExpr> vars) {
			final int dim = vars.size();
			this.vars = ImmutableList.copyOf(vars);
			Builder<VariableExpr,Integer> varToIndexBuilder = ImmutableMap.builder();
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
		
	}
	
}
