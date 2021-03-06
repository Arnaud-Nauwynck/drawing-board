package fr.an.drawingboard.alg.base;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.math.algo.base.ExprVarDependiesAnalyzer;
import fr.an.drawingboard.math.algo.base.ExprVarDependiesAnalyzer.ExprDependencies;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.math.expr.VarDef;

public class ExprVarDependiesAnalyzerTest {
	private static final ExprBuilder b = ExprBuilder.INSTANCE;
	
	@Test
	public void testAnalyzeVarDependencies() {
		// given
		VarDef xDef = new VarDef("x");
		VariableExpr x = xDef.expr;
		VarDef yDef = new VarDef("y");
		VariableExpr y = yDef.expr;
		VarDef zDef = new VarDef("z");
		VariableExpr z = zDef.expr;
		// when
		ExprDependencies varDeps = ExprVarDependiesAnalyzer.analyzeVarDependencies(b.mult(b.sum(x, x, y), z, b.lit1()));
		// then
		Assert.assertEquals(3, varDeps.vars.size());
		Assert.assertTrue(varDeps.vars.contains(xDef));
		Assert.assertTrue(varDeps.vars.contains(yDef));
		Assert.assertTrue(varDeps.vars.contains(zDef));
	}
}