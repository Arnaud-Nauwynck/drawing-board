package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.List;

/**
 * definition of a path (with algebric expr), similar to javafx.scene.shape.Path
 *
 * a path define a curve "path" between 2 stop points startPt, endPt
 * it contains pathElements that are "continuous", without stop points
 */
public class PathDef extends ParametrizableEltDef {

	public List<PathElementDef> pathElements = new ArrayList<>();

	public PathDef() {
	}

	public PathDef(List<PathElementDef> pathElements) {
		this.pathElements.addAll(pathElements);
	}
	
}
