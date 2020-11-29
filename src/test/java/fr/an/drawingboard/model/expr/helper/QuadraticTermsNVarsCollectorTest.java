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

		MatrixExpr quadTerms = quadForm.otherQuadTerms;
		Assert.assertEquals(B.mult(a,d,g), quadTerms.get(0, 0));
		Assert.assertEquals(B.mult(b,e,g), quadTerms.get(1, 1));
		Expr q_01 = B.mult(B.lit(0.5), B.sum(B.mult(a,e,g), B.mult(b,d,g)));
		Assert.assertEquals(q_01, quadTerms.get(0, 1));
		Assert.assertEquals(q_01, quadTerms.get(1, 0));

		MatrixExpr linTerms = quadForm.otherLinearTerms;
		Assert.assertEquals(B.sum(B.mult(a,f,g), B.mult(d,c,g)), linTerms.get(0, 0));
		Assert.assertEquals(B.sum(B.mult(b,f,g), B.mult(c,e,g)), linTerms.get(1, 0));
		
		Assert.assertEquals(B.mult(c,f,g), quadForm.constTerm);
		
		Assert.assertEquals(0.0, quadForm.doubleLinearTerms.get(0, 0), PREC);
		Assert.assertEquals(0.0, quadForm.doubleLinearTerms.get(1, 0), PREC);
		Assert.assertEquals(0.0, quadForm.doubleLinearTerms.get(0, 1), PREC);
		Assert.assertEquals(0.0, quadForm.doubleLinearTerms.get(1, 1), PREC);
	}
	
}
