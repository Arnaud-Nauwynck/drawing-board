package fr.an.drawingboard.model.shapedef;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.model.shapedef.GesturePathesDef.PathDefWithElement;
import fr.an.drawingboard.stddefs.shapedef.ShapeDefRegistryBuilder;

public class GesturePathesDefTest {

	@Test
	public void testIteratorPath() {
		ShapeDefRegistry reg = new ShapeDefRegistry();
		new ShapeDefRegistryBuilder(reg).addStdShapes();
		ShapeDef shapeDef = reg.getShapeDef("rectangle");
		GesturePathesDef gestureDef = shapeDef.gestures.get(0);
		PathDef path0 = gestureDef.pathes.get(0);
		PathElementDef elt0_0 = path0.pathElements.get(0);
		PathDef path1 = gestureDef.pathes.get(1);
		PathElementDef elt1_0 = path1.pathElements.get(0);
		PathDef path2 = gestureDef.pathes.get(2);
		PathElementDef elt2_0 = path2.pathElements.get(0);
		PathDef path3 = gestureDef.pathes.get(3);
		PathElementDef elt3_0 = path3.pathElements.get(0);
		
		Iterator<PathDefWithElement> iter = gestureDef.iteratorPathDefWithElement();
		
		PathDefWithElement curr = iter.next();
		Assert.assertSame(path0, curr.path);
		Assert.assertSame(elt0_0, curr.pathElement);

		curr = iter.next();
		Assert.assertSame(path1, curr.path);
		Assert.assertSame(elt1_0, curr.pathElement);
		
		curr = iter.next();
		Assert.assertSame(path2, curr.path);
		Assert.assertSame(elt2_0, curr.pathElement);

		curr = iter.next();
		Assert.assertSame(path3, curr.path);
		Assert.assertSame(elt3_0, curr.pathElement);
	}
}