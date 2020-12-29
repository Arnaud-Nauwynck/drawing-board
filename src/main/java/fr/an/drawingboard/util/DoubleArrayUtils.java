package fr.an.drawingboard.util;

public class DoubleArrayUtils {

	private DoubleArrayUtils() {
	}

	public static void checkDim2D(int dim0, int dim1, double[][] src) {
		DrawingValidationUtils.checkEquals(dim0, src.length, "array length[.][]");
		for(int i = 0; i < dim0; i++) {
			checkDim1D(dim1, src[i], "[][.]");
		}
	}

	public static void checkDim1D(int dim0, double[] src, String displayMsg) {
		DrawingValidationUtils.checkEquals(dim0, src.length, "array length[.]");
	}

	public static double[][] createArray2D(int dim0, int dim1) {
		double[][] res = new double[dim0][];
		for(int i = 0; i < dim0; i++) {
			res[i] = new double[dim1];
		}
		return res;
	}

	public static double[][] copyArray2D(double[][] src) {
		int dim0 = src.length;
		double[][] res = new double[dim0][];
		for(int i = 0; i < dim0; i++) {
			res[i] = copyArray1D(src[i]);
		}
		return res;
	}
	
	public static double[] copyArray1D(double[] src) {
		int dim0 = src.length;
		double[] res = new double[dim0];
		System.arraycopy(src, 0, res, 0, dim0);
		return res;
	}

}
