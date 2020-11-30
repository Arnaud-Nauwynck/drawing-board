package fr.an.drawingboard.model.expr.helper;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.ExprBuilder;
import fr.an.drawingboard.model.expr.VarDef;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;

public class FlattenExprTransformerTest {

	private static ExprBuilder B = ExprBuilder.INSTANCE;

	@Test
	public void testFlatternExpr() {
		// given
		VarDef aDef = new VarDef("a"), bDef = new VarDef("b"), cDef = new VarDef("c"), dDef = new VarDef("d");
		Expr a = aDef.expr, b = bDef.expr, c = cDef.expr, d = dDef.expr;
		
		Expr expr = B.mult(B.sum(a, b), B.sum(c, d)); // =(a+b)(c+d)

		// when
		Expr expandedExpr = ExpandExprTransformer.expandExpr(expr); //ex: (a+b)(c+d) => ac+ad+bc+bd
		Expr flatternExpandedExpr = FlattenExprTransformer.flatternExpr(expandedExpr); // ex: (a+(b+(c+..))) => (a+b+c+..), idem mult

		// then
		List<Expr> terms = ((SumExpr) flatternExpandedExpr).exprs;
		List<List<Expr>> termMults = new ArrayList<>();
		for(Expr term: terms) {
			Assert.assertTrue(term instanceof MultExpr);
			termMults.add(((MultExpr) term).exprs);
		}
		Assert.assertEquals(B.mult(a, c), terms.get(0));
		Assert.assertEquals(B.mult(a, d), terms.get(1));
		Assert.assertEquals(B.mult(b, c), terms.get(2));
		Assert.assertEquals(B.mult(b, d), terms.get(3));
		Assert.assertEquals(4, terms.size());
	}

	@Test
	public void testFlatternExpr_mult_axb_cxd() {
		// given
		VarDef xDef = new VarDef("x");
		VarDef aDef = new VarDef("a"), bDef = new VarDef("b"), cDef = new VarDef("c"), dDef = new VarDef("d");
		Expr x = xDef.expr;
		Expr a = aDef.expr, b = bDef.expr, c = cDef.expr, d = dDef.expr;
		
		Expr expr = B.mult(B.sum(B.mult(a, x), b), B.sum(B.mult(c, x), d)); // =(a.x+b)(c.x+d)

		// when
		Expr expandedExpr = ExpandExprTransformer.expandExpr(expr); // = (ax)(cx) + (ax)d + b(cx) + bd
		Expr flatternExpandedExpr = FlattenExprTransformer.flatternExpr(expandedExpr); // = (axcx) + (axd) + (bcx) + bd

		// then
		List<Expr> terms = ((SumExpr) flatternExpandedExpr).exprs;
		List<List<Expr>> termMults = new ArrayList<>();
		for(Expr term: terms) {
			Assert.assertTrue(term instanceof MultExpr);
			termMults.add(((MultExpr) term).exprs);
		}
		Assert.assertEquals(B.mult(a, x, c, x), terms.get(0));
		Assert.assertEquals(B.mult(a, x, d), terms.get(1));
		Assert.assertEquals(B.mult(b, c, x), terms.get(2));
		Assert.assertEquals(B.mult(b, d), terms.get(3));
		Assert.assertEquals(4, terms.size());
	}
	
	@Test
	public void testFlatternExpr_mult_axbyc_dxeyf() {
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
		Expr expandedExpr = ExpandExprTransformer.expandExpr(expr); //ex: (a+b)(c+d) => ac+ad+bc+bd
		Expr flatternExpandedExpr = FlattenExprTransformer.flatternExpr(expandedExpr); // ex: (a+(b+(c+..))) => (a+b+c+..), idem mult

		// then
		// = ax dx g + 
		// System.out.println(ExprAsExprBuilderJavaCodePrinter.exprToExprBuilderJavaCode(flatternExpandedExpr));
		List<Expr> terms = ((SumExpr) flatternExpandedExpr).exprs;
		List<List<Expr>> termMults = new ArrayList<>();
		for(Expr term: terms) {
			Assert.assertTrue(term instanceof MultExpr);
			termMults.add(((MultExpr) term).exprs);
		}
		// List<Expr> term0 = termMults;
		Assert.assertEquals(B.mult(g, a, x, d, x), terms.get(0));
//		, B.mult(g, B.mult(a, x), B.mult(e, y)), B.mult(g, B.mult(a, x), f), B.mult(g, B.mult(b, y), B.mult(d, x)), B.mult(g, B.mult(b, y), B.mult(e, y)), B.mult(g, B.mult(b, y), f), B.mult(g, c, B.mult(d, x)), B.mult(g, c, B.mult(e, y)), B.mult(g, c, f))
		
	}
		
}
