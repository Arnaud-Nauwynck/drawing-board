package fr.an.drawingboard.recognizer.shape;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj;
import fr.an.drawingboard.model.shapedef.obj.ShapeObj;
import fr.an.drawingboard.model.shapedef.paramdef.ParamCategoryRegistry;
import fr.an.drawingboard.recognizer.initialParamEstimators.ParamEvalCtx;
import fr.an.drawingboard.recognizer.shape.PtToSlotDefDynamicProgOptimizer.ProjToPathUpToIndex;
import fr.an.drawingboard.stddefs.shapedef.ShapeDefRegistryBuilder;
import lombok.AllArgsConstructor;
import lombok.val;

public class PtToSlotDefDynamicProgOptimizerTest {

	private static final double PREC = 1e-9;
	
	@Test
	public void testAddPt_Line() {
		// given
		val scenario = createLineTestScenario();
		PtToSlotDefDynamicProgOptimizer sut = scenario.sut;
		
		//     x=0      50      100
		// y=0  +---------------->
		// 
		//        +Pt0
		//         10,15
		int pt0y = 15;
		Pt2D pt = new Pt2D(10, pt0y);
		double weight0 = 1.0;
		WeightedPt2D weigthedPt = new WeightedPt2D(pt , weight0);

		// when
		sut.addPt(weigthedPt);
		
		// then
		ProjToPathUpToIndex curr = sut.currProjToPathUpToIndexes.get(0);
		double costValue = curr.minCostValue;
		Assert.assertEquals(weight0 * pt0y*pt0y, costValue, PREC);
	}

	@AllArgsConstructor
	private static class PtToFragTestScenario {
		ShapeObj shape;
		ParamEvalCtx ctx;
		PtToSlotDefDynamicProgOptimizer sut;
	}

	private PtToFragTestScenario createLineTestScenario() {
		ShapeDefRegistry registry = new ShapeDefRegistry();
		new ShapeDefRegistryBuilder(registry, ParamCategoryRegistry.INSTANCE).addStdShapes();
		ShapeDef shapeDef = registry.getShapeDef("line");

		//     x=0      50      100
		// y=0  +---------------->
		// 
		ParamEvalCtx ctx = new ParamEvalCtx();
		ctx.put(shapeDef.getParam("x"), 50);
		ctx.put(shapeDef.getParam("w"), 100);
		ctx.put(shapeDef.getParam("y"), 0);
		ctx.put(shapeDef.getParam("h"), 0);
		
		ShapeObj shape = new ShapeObj(shapeDef);
		shape.eval(ctx);
		List<PathElementObj> pathElts = shape.toPathElementCtxEvals();
		PtToSlotDefDynamicProgOptimizer sut = new PtToSlotDefDynamicProgOptimizer(pathElts);

		return new PtToFragTestScenario(shape, ctx, sut);
	}
}
