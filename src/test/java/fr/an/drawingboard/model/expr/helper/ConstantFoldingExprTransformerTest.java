package fr.an.drawingboard.model.expr.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.VarDef;
import fr.an.drawingboard.model.expr.Expr.VariableExpr;

public class ConstantFoldingExprTransformerTest {

	private static final ExprBuilder b = ExprBuilder.INSTANCE;

	@Test
	public void testConstFold() {
		// given
		VarDef xDef = new VarDef("x");
		VariableExpr x = xDef.expr;
		// when-then
		assertEqualsConstantFold(b.lit1(), b.lit1());
		// sum
		assertEqualsConstantFold(b.lit1(), b.sum(b.lit1(), b.lit0()));
		assertEqualsConstantFold(b.lit2(), b.sum(b.lit1(), b.lit1(), b.lit0()));
		assertEqualsConstantFold(b.sum(x, b.lit2()), b.sum(b.lit1(), x, b.lit1(), b.lit0()));
		// mult
		assertEqualsConstantFold(b.lit0(), b.mult(b.lit1(), b.lit1(), b.lit0()));
		assertEqualsConstantFold(b.lit(4), b.mult(b.lit1(), b.lit2(), b.lit2()));
		assertEqualsConstantFold(b.mult(x, b.lit(4)), b.mult(b.lit2(), x, b.lit2(), b.lit1()));
	}

	private static void assertEqualsConstantFold(Expr expected, Expr exprToFold) {
		Expr actual = ConstantFoldingExprTransformer.constFold(exprToFold);
		Assert.assertEquals(expected, actual);
	}
	

}
