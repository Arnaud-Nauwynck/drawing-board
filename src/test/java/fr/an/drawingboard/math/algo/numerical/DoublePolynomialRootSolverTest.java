package fr.an.drawingboard.math.algo.numerical;

import org.junit.Assert;
import org.junit.Test;

import lombok.val;

public class DoublePolynomialRootSolverTest {

	private static final double PREC = 1e-9;
	
	@Test
	public void testQuad() {
		// given
		// when
		val res = DoublePolynomialRootSolver.solveQuad(1, -3, 2); // x^2-3x+2 = (x-1)(x-2)
		// then
		Assert.assertEquals(2, res.roots.length);
		Assert.assertEquals(1.0, res.roots[0], PREC);
		Assert.assertEquals(2.0, res.roots[1], PREC);
	}

	@Test
	public void testQuad_1root() {
		// given
		// when
		val res = DoublePolynomialRootSolver.solveQuad(1, -2, 1); // x^2-2x+1 = (x-1)^2
		// then
		Assert.assertEquals(1, res.roots.length);
		Assert.assertEquals(1.0, res.roots[0], PREC);
	}

	@Test
	public void testQuad_noroot() {
		// given
		// when
		val res = DoublePolynomialRootSolver.solveQuad(1, 1, 1); // x^2+x+1 .. delta=-3
		// then
		Assert.assertEquals(0, res.roots.length);
	}

	@Test
	public void testCubic() {
		// given
		// when
		val res = DoublePolynomialRootSolver.solveRoot_Cubic_cardan(1, 1, -3, 1); // x^3+x^2-3x+1 = ..
		// then
		Assert.assertEquals(3, res.roots.length);
		Assert.assertEquals(-1-Math.sqrt(2), res.roots[0], PREC);
		Assert.assertEquals(-1+Math.sqrt(2), res.roots[1], PREC);
		Assert.assertEquals(1, res.roots[2], PREC);
	}

	@Test
	public void testCubic_1() {
		// given
		// when
		val res = DoublePolynomialRootSolver.solveRoot_Cubic_cardan(1, 0, 0, -2); // x^3-2 =0 => x=pow(2, 1/3) 
		// then
		Assert.assertEquals(1, res.roots.length);
		Assert.assertEquals(Math.pow(2,  1.0/3), res.roots[0], PREC);
	}
	
}
