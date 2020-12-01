package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import fr.an.drawingboard.model.shapedef.PathElementDef.CubicBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.QuadBezierPathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.SegmentPathElementDef;
import fr.an.drawingboard.recognizer.initialParamEstimators.InitialParamForShapeEstimator;
import fr.an.drawingboard.util.DrawingValidationUtils;

/**
 * a multipath is a definiton (algebric expr) of a single gesture, 
 * containing pathes separated by stop points
 */
public class GesturePathesDef extends ParametrizableEltDef {

	public List<PathDef> pathes = new ArrayList<>();

	public InitialParamForShapeEstimator initalParamEstimator;
	
	// --------------------------------------------------------------------------------------------

	public GesturePathesDef(ParametrizableEltDef parent,
			InitialParamForShapeEstimator initalParamEstimator) {
		super(parent);
		this.initalParamEstimator = initalParamEstimator;
	}

	// --------------------------------------------------------------------------------------------

	public PathDef addPath(List<PathElementDef> pathElements) {
		PathDef res = new PathDef(pathElements);
		pathes.add(res);
		return res;
	}

	public PathDef addPath(PathElementDef... pathElements) {
		return addPath(Arrays.asList(pathElements));
	}

	public PathDef addPath(PathElementDef pathElement) {
		return addPath(Arrays.asList(pathElement));
	}

	public PathDef addPath_Segment(PtExpr startPt, PtExpr endPt) {
		return addPath(new SegmentPathElementDef(startPt, endPt));
	}

	public PathDef addPath_DiscreteLine(List<PtExpr> pts) {
		return addPath(new DiscretePointsPathElementDef(pts));
	}
	
	public PathDef addPath_QuadBezier(PtExpr startPt, PtExpr controlPt1, PtExpr endPt) {
		return addPath(new QuadBezierPathElementDef(startPt, controlPt1, endPt));
	}

	public PathDef addPath_CubicBezier(PtExpr startPt, PtExpr controlPt1, PtExpr controlPt2, PtExpr endPt) {
		return addPath(new CubicBezierPathElementDef(startPt, controlPt1, controlPt2, endPt));
	}

	

	public static class PathDefWithElement {
		public PathDef path;
		public PathElementDef pathElement;
	}
	
	public Iterator<PathDefWithElement> iteratorPathDefWithElement() {
		return new PathDefWithElementIterator(pathes.iterator());
	}
	
	private static class PathDefWithElementIterator implements Iterator<PathDefWithElement> {
		final PathDefWithElement curr = new PathDefWithElement();
		final Iterator<PathDef> pathIter;
		Iterator<PathElementDef> pathElementIter;

		private PathDefWithElementIterator(Iterator<PathDef> pathIter) {
			this.pathIter = pathIter;
		}

		@Override
		public boolean hasNext() {
			if (pathElementIter != null && pathElementIter.hasNext()) {
				return true;
			}
			return pathIter.hasNext();
		}

		@Override
		public PathDefWithElement next() {
			if (pathElementIter == null || !pathElementIter.hasNext()) {
				DrawingValidationUtils.checkTrue(pathIter.hasNext(), "hasNext");
				curr.path = pathIter.next();
				this.pathElementIter = curr.path.pathElements.iterator(); 
			}
			curr.pathElement = pathElementIter.next();
			return curr;
		}
		
	}
	
}
