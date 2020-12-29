package fr.an.drawingboard.model.shapedef.obj;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class CompositeSplitParamsPathElementsObjTest {

	@Test
	public void testBinarySearchSplitParams() {
		double[] values = new double[] { 0.2, 0.5 };
		int found = Arrays.binarySearch(values, 0.1);
		int indexBefore = -(found+1);
		Assert.assertEquals(0, indexBefore);
		
		int found1 = Arrays.binarySearch(values, 0.2);
		Assert.assertEquals(0, found1);

		int found2 = Arrays.binarySearch(values, 0.3);
		int indexBefore2 = -(found2+1);
		Assert.assertEquals(1, indexBefore2);
	}

	// TOADD write JUnit tests
}
