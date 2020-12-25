package fr.an.drawingboard.geom2d;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.PtExpr;
import lombok.AllArgsConstructor;

/**
 * cf https://github.com/Pomax/bezierinfo/blob/master/js/graphics-element/lib/bezierjs/bezier.js
 *
 */
@AllArgsConstructor
public class QuadBezier2D {
	public final Pt2D startPt;
	public final Pt2D controlPt;
	public final Pt2D endPt;
	
	public QuadBezier2D() {
	    this.startPt = new Pt2D(0, 0);
	    this.controlPt = new Pt2D(0, 0);
	    this.endPt = new Pt2D(0, 0);
	}

	public double eval_x(double s) {
		return evalB(s, startPt.x, controlPt.x, endPt.x);
	}
	
	public double eval_y(double s) {
		return evalB(s, startPt.y, controlPt.y, endPt.y);
	}
	
	public Pt2D eval(double s) {
		return new Pt2D(eval_x(s), eval_y(s));
	}

	public void eval(Pt2D res, double s) {
		res.x = eval_x(s);
		res.y = eval_y(s);
	}

	public static void eval(Pt2D res, double s, double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y) {
		res.x = evalB(s, p0_x, p1_x, p2_x);
		res.y = evalB(s, p0_y, p1_y, p2_y);
	}

	public static double evalB(double s, double p0, double p1, double p2) {
		double _1s = 1.0-s;
		return _1s*_1s * p0 + 2*_1s*s * p1 + s*s * p2;
	}

	public static PtExpr pointExprAtParam(double s, PtExpr p0, PtExpr p1, PtExpr p2) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double s2 = s*s, cs = 1.0-s, cs2 = cs*cs;
		Expr x = b.linear(cs2, p0.x, 2*cs*s, p1.x, s2, p2.x);
		Expr y = b.linear(cs2, p0.y, 2*cs*s, p1.y, s2, p2.y);
		return new PtExpr(x, y);
	}

	public static Expr exprAtParam(double s, Expr p0, Expr p1, Expr p2) {
		ExprBuilder b = ExprBuilder.INSTANCE;
		double s2 = s*s, cs = 1.0-s, cs2 = cs*cs;
		return b.linear(cs2, p0, 2*cs*s, p1, s2, p2);
	}

	public void setTranslate(Pt2D vect) {
        this.startPt.setTranslate(vect);
        this.controlPt.setTranslate(vect);
        this.endPt.setTranslate(vect);
    }

}
