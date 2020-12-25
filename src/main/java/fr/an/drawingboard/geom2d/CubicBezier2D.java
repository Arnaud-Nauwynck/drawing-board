package fr.an.drawingboard.geom2d;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.PtExpr;
import lombok.AllArgsConstructor;

/**
 * https://pomax.github.io/BezierInfo-2
 * 
 * http://www.caffeineowl.com/graphics/2d/vectorial/cubic2quad01.html
 * https://blend2d.com/research/simplify_and_offset_bezier_curves.pdf
 * https://github.com/fonttools/fonttools/blob/master/Lib/fontTools/misc/bezierTools.py
 */
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

	public void eval(Pt2D res, double s) {
		res.set(eval_x(s), eval_y(s));
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
	
	public static PtExpr pointExprAtParam(double s, PtExpr p0, PtExpr p1, PtExpr p2, PtExpr p3) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double s2 = s*s, s3 = s2*s, cs = 1.0-s, cs2 = cs*cs, cs3=cs2*cs;
		Expr x = b.linear(cs3, p0.x, 3*cs2*s, p1.x, 3*cs*s2, p2.x, s3, p3.x);
		Expr y = b.linear(cs3, p0.y, 3*cs2*s, p1.y, 3*cs*s2, p2.y, s3, p3.y);
		return new PtExpr(x, y);
	}

	public static Expr exprAtParam(double s, Expr p0, Expr p1, Expr p2, Expr p3) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double s2 = s*s, s3 = s2*s, cs = 1.0-s, cs2 = cs*cs, cs3=cs2*cs;
		return b.linear(cs3, p0, 3*cs2*s, p1, 3*cs*s2, p2, s3, p3);
	}
	
	public void setTranslate(Pt2D vect) {
        this.startPt.setTranslate(vect);
        this.p1.setTranslate(vect);
        this.p2.setTranslate(vect);
        this.endPt.setTranslate(vect);
	}
}
