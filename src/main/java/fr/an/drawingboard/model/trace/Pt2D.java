package fr.an.drawingboard.model.trace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pt2D {
	
	public double x;
	public double y;

	

	public Pt2D mult(double k) {
		return new Pt2D(k * x, k * y);
	}
	public Pt2D plus(Pt2D right) {
		return new Pt2D(x + right.x, y + right.y);
	}
	public Pt2D minus(Pt2D right) {
		return new Pt2D(x - right.x, y - right.y);
	}

	public double scalarProduct(Pt2D right) {
		return x * right.x + y * right.y;
	}
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

}
