package fr.an.drawingboard.model.shapedef;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.MultExpr;
import fr.an.drawingboard.math.expr.Expr.SumExpr;
import lombok.AllArgsConstructor;

/**
 * <PRE>
 *           x
 *           |
 *    PtUL ------ PtUR   /\
 *     |            |     |
 *  y- |            |     h
 *     |            |     |
 *    PtDL ------ PtDR   \/
 * 
 *     <-----w----->
 * </PRE>
 */
@AllArgsConstructor
public class RectExpr {
	
	public final PtExpr ptUL;
	public final PtExpr ptUR;
	public final PtExpr ptDR;
	public final PtExpr ptDL;

	public static RectExpr fromXYHW(Expr x, Expr y, Expr w, Expr h) {
		Expr val05 = LiteralDoubleExpr.VAL_inv2;
		Expr valMinus05 = LiteralDoubleExpr.VAL_minusInv2;
		Expr xmin = new SumExpr(x, new MultExpr(valMinus05, w));
		Expr xmax = new SumExpr(x, new MultExpr(val05, w));
		Expr ymin = new SumExpr(y, new MultExpr(valMinus05, h));
		Expr ymax = new SumExpr(y, new MultExpr(val05, h));

		PtExpr ptUL = new PtExpr(xmin, ymin);
		PtExpr ptUR = new PtExpr(xmax, ymin);
		PtExpr ptDR = new PtExpr(xmax, ymax);
		PtExpr ptDL = new PtExpr(xmin, ymax);
		return new RectExpr(ptUL, ptUR, ptDR, ptDL);
	}
}