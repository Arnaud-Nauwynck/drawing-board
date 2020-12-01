package fr.an.drawingboard.math.algo.base;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.util.DrawingValidationUtils;

public class ExprArrayUtils {

	private ExprArrayUtils() {
	}

	public static <T> void checkDim2D(int dim0, int dim1, T[][] src) {
		DrawingValidationUtils.checkEquals(dim0, src.length, "array length[.][]");
		for(int i = 0; i < dim0; i++) {
			checkDim1D(dim1, src[i], "[][.]");
		}
	}

	public static <T> void checkDim1D(int dim0, T[] src, String displayMsg) {
		DrawingValidationUtils.checkEquals(dim0, src.length, "array length[.]");
	}

	public static Expr[][] createArray2D(int dim0, int dim1) {
		Expr[][] res = new Expr[dim0][];
		for(int i = 0; i < dim0; i++) {
			res[i] = new Expr[dim1];
		}
		return res;
	}

	public static Expr[][] copyArray2D(Expr[][] src) {
		int dim0 = src.length;
		Expr[][] res = new Expr[dim0][];
		for(int i = 0; i < dim0; i++) {
			res[i] = copyArray1D(src[i]);
		}
		return res;
	}
	
	public static Expr[] copyArray1D(Expr[] src) {
		int dim0 = src.length;
		Expr[] res = new Expr[dim0];
		System.arraycopy(src, 0, res, 0, dim0);
		return res;
	}
	

}
