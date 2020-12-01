package fr.an.drawingboard.math.expr;

import fr.an.drawingboard.math.algo.base.ExprArrayUtils;
import fr.an.drawingboard.util.DrawingValidationUtils;
import lombok.Getter;

/**
 * Immutable matrix of (imutable) Expr
 */
public class MatrixExpr {

	@Getter
	public final int dim0;
	@Getter
	public final int dim1;

	private final Expr[][] data;

	public static MatrixExprBuilder builder(int dim0, int dim1) {
		return new MatrixExprBuilder(dim0, dim1); 
	}

	public MatrixExpr(int dim0, int dim1, Expr[][] data) {
		this.dim0 = dim0;
		this.dim1 = dim1;
		ExprArrayUtils.checkDim2D(dim0, dim1, data);
		this.data = ExprArrayUtils.copyArray2D(data);
	}

	public static MatrixExpr createMatN1(Expr[] data) {
		int dim0 = data.length;
		Expr[][] res = new Expr[dim0][];
		for(int i0 = 0; i0 < dim0; i0++) {
			res[i0] = new Expr[] { data[i0] };
		}
		return new MatrixExpr(dim0, 1, res);
	}

	public static MatrixExpr createMat1N(Expr[] data) {
		int dim1 = data.length;
		Expr[][] res = new Expr[1][dim1];
		Expr[] res0 = res[0] = new Expr[dim1];
		for(int i1 = 0; i1 < dim1; i1++) {
			res0[i1] = data[i1];
		}
		return new MatrixExpr(1, dim1, res);
	}

	public Expr get(int i, int j) {
		return data[i][j];
	}

	public Expr[][] getDataArrayCopy() {
		return ExprArrayUtils.copyArray2D(data);
	}

	public MatrixExpr extractRow(int i0) {
		return createMat1N(data[i0]);
	}
	
	public MatrixExpr extractCol(int i1) {
		Expr[][] res = new Expr[1][dim1];
		Expr[] res0 = res[0] = new Expr[dim1];
		for(int i0 = 0; i0 < dim1; i0++) {
			res0[i1] = data[i0][i1];
		}
		return new MatrixExpr(dim0, 1, res);	
	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Builder pattern for immutable MatrixExpr
	 */
	public static class MatrixExprBuilder {

		@Getter
		public final int dim0;
		@Getter
		public final int dim1;

		public final Expr[][] data;
	
		public MatrixExprBuilder(int dim0, int dim1) {
			this.dim0 = dim0;
			this.dim1 = dim1;
			this.data = ExprArrayUtils.createArray2D(dim0, dim1);
		}
		
		public MatrixExprBuilder(int dim0, int dim1, Expr[][] data, boolean copy) {
			this.dim0 = dim0;
			this.dim1 = dim1;
			ExprArrayUtils.checkDim2D(dim0, dim1, data);
			this.data = (copy)? ExprArrayUtils.copyArray2D(data) : data;
		}

		public MatrixExpr build() {
			return new MatrixExpr(dim0, dim1, data);
		}
		
		public Expr get(int i, int j) {
			return data[i][j];
		}

		public MatrixExprBuilder set(int i, int j, Expr expr) {
			data[i][j] = expr;
			return this;
		}

		public MatrixExprBuilder add(int i, int j, Expr expr) {
			Expr prev = data[i][j];
			Expr res = (prev == null)? expr : ExprBuilder.INSTANCE.sum(prev, expr);
			return set(i, j, res);
		}
		
		public MatrixExprBuilder addRow(int i0, Expr[] exprs) {
			DrawingValidationUtils.checkEquals(dim1, exprs.length);
			for(int i1 = 0; i1 < dim1; i1++) {
				add(i0, i1, exprs[i1]);
			}
			return this;
		}

		public MatrixExprBuilder addCol(int i1, Expr[] exprs) {
			DrawingValidationUtils.checkEquals(dim0, exprs.length);
			for(int i0 = 0; i0 < dim0; i0++) {
				add(i0, i1, exprs[i0]);
			}
			return this;
		}

		public MatrixExprBuilder addDiag(Expr[] exprs) {
			int dim = Math.min(dim0, dim1);
			DrawingValidationUtils.checkEquals(dim, exprs.length);
			for(int d = 0; d < dim; d++) {
				add(d, d, exprs[d]);
			}
			return this;
		}
		
	}
}
