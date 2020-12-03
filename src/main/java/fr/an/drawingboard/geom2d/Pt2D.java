package fr.an.drawingboard.geom2d;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Pt2D {
	
	public double x;
	public double y;
	
	public Pt2D copy() {
		return new Pt2D(x, y);
	}

	public Pt2D mult(double k) {
		return new Pt2D(k * x, k * y);
	}
	
	public Pt2D divSafe(double k) {
		return mult((k != 0)? 1.0/k: 1.0);
	}
	
	public Pt2D plus(Pt2D right) {
		return new Pt2D(x + right.x, y + right.y);
	}
	public Pt2D minus(Pt2D right) {
		return new Pt2D(x - right.x, y - right.y);
	}

	public final double scalarProduct(Pt2D right) {
		return x * right.x + y * right.y;
	}

	public final double squareNorm() {
		return x * x + y * y;
	}

	public final double norm() {
		return Math.sqrt(x * x + y * y);
	}

	public final double invNorm() {
		double norm = norm();
		return (norm != 0)? 1.0/norm : 1.0;
	}
	
	public Pt2D normalized() {
		return mult(invNorm());
	}

	public Pt2D vectTo(Pt2D to) {
		return to.minus(this);
	}

	public Pt2D normalizedVectTo(Pt2D to) {
		return vectTo(to).normalized();
	}

	public double distTo(Pt2D pt) {
		return dist(x, y, pt.x, pt.y); 
	}

	public static double dist(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		double res = Math.sqrt(dx * dx + dy * dy);
		return res;
	}
	
	public static Pt2D linearSum(double coef1, Pt2D pt1, double coef2, Pt2D pt2) {
		double x = coef1 * pt1.x + coef2 * pt2.x;
		double y = coef1 * pt1.y + coef2 * pt2.y;
		return new Pt2D(x, y);
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

}
