package fr.an.drawingboard.geom2d;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CubicBezier2D {
	public final Pt2D startPt;
	public final Pt2D p1;
	public final Pt2D p2;
	public final Pt2D endPt;
	

    public CubicBezier2D() {
        this.startPt = new Pt2D(0, 0);
        this.p1 = new Pt2D(0, 0);
        this.p2 = new Pt2D(0, 0);
        this.endPt = new Pt2D(0, 0);
    }

	public double eval_x(double s) {
		return evalB(s, startPt.x, p1.x, p2.x, endPt.x);
	}
	
	public double eval_y(double s) {
		return evalB(s, startPt.y, p1.y, p2.y, endPt.y);
	}
	
	public Pt2D eval(double s) {
		return new Pt2D(eval_x(s), eval_y(s));
	}

	public static Pt2D eval(double s, Pt2D p0, Pt2D p1, Pt2D p2, Pt2D p3) {
		return new Pt2D(evalB(s, p0.x, p1.x, p2.x, p3.x), evalB(s, p0.y, p1.y, p2.y, p3.y));
	}

	public static void eval(Pt2D res, double s, Pt2D p0, Pt2D p1, Pt2D p2, Pt2D p3) {
		res.x = evalB(s, p0.x, p1.x, p2.x, p3.x);
		res.y = evalB(s, p0.y, p1.y, p2.y, p3.y);
	}

	public static void eval(Pt2D res, double s, double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
		res.x = evalB(s, p0_x, p1_x, p2_x, p3_x);
		res.y = evalB(s, p0_y, p1_y, p2_y, p3_y);
	}

	public static double evalB(double s, double p0, double p1, double p2, double p3) {
		double _1s = 1.0-s;
		double s2 = s*s;
		double _1s2 = _1s * _1s;
		return _1s2*_1s * p0 + 3*_1s2*s * p1 + 3*_1s*s2 * p2 + s2*s * p3;
	}
	
	public void setTranslate(Pt2D vect) {
        this.startPt.setTranslate(vect);
        this.p1.setTranslate(vect);
        this.p2.setTranslate(vect);
        this.endPt.setTranslate(vect);
	}
}
