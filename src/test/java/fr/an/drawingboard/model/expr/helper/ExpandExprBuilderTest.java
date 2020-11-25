package fr.an.drawingboard.model.expr.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.var.VarDef;

public class ExpandExprBuilderTest {
	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	@Test
	public void testExpand() {
		// given
		VariableExpr x0 = new VarDef("x0").expr;
		VariableExpr x1 = new VarDef("x1").expr;
		VariableExpr y0 = new VarDef("y0").expr;
		VariableExpr y1 = new VarDef("y1").expr;
		VariableExpr y2 = new VarDef("y2").expr;
		VariableExpr z0 = new VarDef("z0").expr;
		VariableExpr z1 = new VarDef("z1").expr;
		VariableExpr coef = new VarDef("coef").expr;
		// when-then
		assertEqualsExpand(b.sum(b.mult(x0, y0), b.mult(x0, y1)),
				b.mult(x0, b.sum(y0, y1)));
		assertEqualsExpand(b.sum(b.mult(coef, x0, y0), b.mult(coef, x0, y1)),
				b.mult(coef, x0, b.sum(y0, y1)));
		assertEqualsExpand(b.sum(b.mult(x0, coef, y0), b.mult(x0, coef, y1)),
				b.mult(x0, b.sum(y0, y1), coef));
		assertEqualsExpand(b.sum(b.mult(x0, y0), b.mult(x0, y1), b.mult(x0, y2)),
				b.mult(x0, b.sum(y0, y1, y2)));
		assertEqualsExpand(b.sum(b.mult(x0, y0), b.mult(x0, y1), b.mult(x1, y0), b.mult(x1, y1)),
				b.mult(b.sum(x0, x1), b.sum(y0, y1)));
		assertEqualsExpand(b.sum(
				b.mult(x0, y0, z0), b.mult(x0, y0, z1), //
				b.mult(x0, y1, z0), b.mult(x0, y1, z1), //
				b.mult(x1, y0, z0), b.mult(x1, y0, z1), //
				b.mult(x1, y1, z0), b.mult(x1, y1, z1) //
				),
				b.mult(b.sum(x0, x1), b.sum(y0, y1), b.sum(z0, z1)));
		
	}

	protected void assertEqualsExpand(Expr expected, Expr actualToExpand) {
		Expr actual = ExpandExprBuilder.expandExpr(actualToExpand);
		Assert.assertEquals(expected, actual);
		
	}

}
