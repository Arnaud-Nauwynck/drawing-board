package fr.an.drawingboard.model.expr.helper;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.helper.QuadraticTermsNVarsCollector.QuadraticForm;
import fr.an.drawingboard.model.expr.matrix.MatrixExpr;
import fr.an.drawingboard.model.var.VarDef;

public class QuadraticTermsNVarsCollectorTest {

	private static ExprBuilder B = ExprBuilder.INSTANCE;
	private static final double PREC = 1e-9;
	
	@Test
	public void testExtractQuadTerms_alg2D() {
		// given
		VarDef xDef = new VarDef("x"), yDef = new VarDef("y");
		VarDef aDef = new VarDef("a"), bDef = new VarDef("b"), cDef = new VarDef("c"), dDef = new VarDef("d"), 
				eDef = new VarDef("e"), fDef = new VarDef("f"), gDef = new VarDef("g");
		Expr x = xDef.expr, y = yDef.expr;
		Expr a = aDef.expr, b = bDef.expr, c = cDef.expr, d = dDef.expr, e = eDef.expr, f = fDef.expr, g = gDef.expr;
		
		Expr ax_plus_by_plus_c = B.sum(B.mult(a, x), B.mult(b, y), c); // = (a.x+b.y+c)
		Expr dx_plus_ey_plus_f = B.sum(B.mult(d, x), B.mult(e, y), f); // = (d.x+e.y+f)
		Expr expr = B.mult(ax_plus_by_plus_c, dx_plus_ey_plus_f, g); // =(a.x+b.y+c)(d.x+e.y+f)g
		
		// when
		QuadraticForm quadForm = QuadraticTermsNVarsCollector.extractQuadTerms(expr, Arrays.asList(xDef, yDef));

		// then
		Assert.assertEquals(0, quadForm.varToIndex.get(xDef).intValue());
		Assert.assertEquals(1, quadForm.varToIndex.get(yDef).intValue());
		// res =   (adg)       x^2 +   1/2 (aeg+bdg) xy     +  (afg+dcg) x  +   cfg
		//       1/2 (aeg+bdg) xy  +   (beg)         y^2       (bfg+ceg) y
		// 
		//     = t|x| .| adg           1/2(aeg+bdg)  |. |x|  + t|x|.|afg+dcg|  +  cfg
		//        |y|  | 1/2(aeg+bdg)  beg           |  |y|     |y| |bfg+ceg|

		MatrixExpr quadTerms = quadForm.quadExprMatrix;
		Assert.assertEquals(B.mult(g, a, d), quadTerms.get(0, 0));
		Assert.assertEquals(B.mult(g, b, e), quadTerms.get(1, 1));
		Expr q_01 = B.sum(B.mult(g,a,e,B.litInv2()), B.mult(g,b,d,B.litInv2()));
		Assert.assertEquals(q_01, quadTerms.get(0, 1));
		Assert.assertEquals(q_01, quadTerms.get(1, 0));

		MatrixExpr linTerms = quadForm.linExprMatrix;
		Assert.assertEquals(B.sum(B.mult(g,a,f), B.mult(g,c,d)), linTerms.get(0, 0));
		Assert.assertEquals(B.sum(B.mult(g,b,f), B.mult(g,c,e)), linTerms.get(0, 1));
		
		Assert.assertEquals(B.mult(g,c,f), quadForm.constExpr);
		
		Assert.assertEquals(0.0, quadForm.quadLiteralMatrix.get(0, 0), PREC);
		Assert.assertEquals(0.0, quadForm.quadLiteralMatrix.get(0, 1), PREC);
		Assert.assertEquals(0.0, quadForm.quadLiteralMatrix.get(1, 0), PREC);
		Assert.assertEquals(0.0, quadForm.quadLiteralMatrix.get(1, 1), PREC);
		Assert.assertEquals(0.0, quadForm.linLiteralMatrix.get(0, 0), PREC);
		Assert.assertEquals(0.0, quadForm.linLiteralMatrix.get(0, 1), PREC);
	}
	
}
