package fr.an.drawingboard.model.expr.matrix;

import fr.an.drawingboard.util.DoubleArrayUtils;
import fr.an.drawingboard.util.DrawingValidationUtils;
import lombok.Getter;

/**
 * Immutable matrix of double
 */
public class ImmutableDoubleMatrix {

	@Getter
	public final int dim0;
	@Getter
	public final int dim1;

	private final double[][] data;

	public static MatrixDoubleBuilder builder(int dim0, int dim1) {
		return new MatrixDoubleBuilder(dim0, dim1); 
	}

	public ImmutableDoubleMatrix(int dim0, int dim1, double[][] data) {
		this.dim0 = dim0;
		this.dim1 = dim1;
		DoubleArrayUtils.checkDim2D(dim0, dim1, data);
		this.data = DoubleArrayUtils.copyArray2D(data);
	}

	public static ImmutableDoubleMatrix createMatN1(double[] data) {
		int dim0 = data.length;
		double[][] res = new double[dim0][];
		for(int i0 = 0; i0 < dim0; i0++) {
			res[i0] = new double[] { data[i0] };
		}
		return new ImmutableDoubleMatrix(dim0, 1, res);
	}

	public static ImmutableDoubleMatrix createMat1N(double[] data) {
		int dim1 = data.length;
		double[][] res = new double[1][dim1];
		double[] res0 = res[0] = new double[dim1];
		for(int i1 = 0; i1 < dim1; i1++) {
			res0[i1] = data[i1];
		}
		return new ImmutableDoubleMatrix(1, dim1, res);
	}

	public double get(int i, int j) {
		return data[i][j];
	}

	public double[][] getDataArrayCopy() {
		return DoubleArrayUtils.copyArray2D(data);
	}

	public ImmutableDoubleMatrix extractRow(int i0) {
		return createMat1N(data[i0]);
	}
	
	public ImmutableDoubleMatrix extractCol(int i1) {
		double[][] res = new double[1][dim1];
		double[] res0 = res[0] = new double[dim1];
		for(int i0 = 0; i0 < dim1; i0++) {
			res0[i1] = data[i0][i1];
		}
		return new ImmutableDoubleMatrix(dim0, 1, res);	
	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Builder pattern for immutable ImmutableMatrixDouble
	 */
	public static class MatrixDoubleBuilder {

		@Getter
		public final int dim0;
		@Getter
		public final int dim1;

		public final double[][] data;
	
		public MatrixDoubleBuilder(int dim0, int dim1) {
			this.dim0 = dim0;
			this.dim1 = dim1;
			this.data = DoubleArrayUtils.createArray2D(dim0, dim1);
		}
		
		public MatrixDoubleBuilder(int dim0, int dim1, double[][] data, boolean copy) {
			this.dim0 = dim0;
			this.dim1 = dim1;
			DoubleArrayUtils.checkDim2D(dim0, dim1, data);
			this.data = (copy)? DoubleArrayUtils.copyArray2D(data) : data;
		}

		public ImmutableDoubleMatrix build() {
			return new ImmutableDoubleMatrix(dim0, dim1, data);
		}
		
		public double get(int i, int j) {
			return data[i][j];
		}

		public MatrixDoubleBuilder set(int i, int j, double value) {
			data[i][j] = value;
			return this;
		}

		public MatrixDoubleBuilder add(int i, int j, double value) {
			double res = data[i][j] + value;
			return set(i, j, res);
		}
		
		public MatrixDoubleBuilder addRow(int i0, double[] values) {
			DrawingValidationUtils.checkEquals(dim1, values.length);
			for(int i1 = 0; i1 < dim1; i1++) {
				add(i0, i1, values[i1]);
			}
			return this;
		}

		public MatrixDoubleBuilder addCol(int i1, double[] values) {
			DrawingValidationUtils.checkEquals(dim0, values.length);
			for(int i0 = 0; i0 < dim0; i0++) {
				add(i0, i1, values[i0]);
			}
			return this;
		}

		public MatrixDoubleBuilder addDiag(double[] values) {
			int dim = Math.min(dim0, dim1);
			DrawingValidationUtils.checkEquals(dim, values.length);
			for(int d = 0; d < dim; d++) {
				add(d, d, values[d]);
			}
			return this;
		}
		
	}
}
