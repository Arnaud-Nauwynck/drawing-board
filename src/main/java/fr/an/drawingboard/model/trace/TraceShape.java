package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

/**
 * <PRE>
 * TraceShape
 *   |
 *   | gestures
 *   |
 *   +----> *  TraceGesturePathes
 *               |    pathes
 *               |
 *               +-----> * TracePath
 *               		      |
 *                            +-- startStopPoint, endStopPoint 
 *                            |
 *                            |    pathElements
 *                            +-----> * TracePathElement
 *                                         ( PolyLine, SimpleSegment, Quadratic/Cubic Bezier Curve, ..)
 *                                         |
 *                                         |    pts
 *                                         +-----> * TracePt
 *                                                      - (x, y)
 *                                                      - t
 * </PRE>
 */
public class TraceShape {

	public List<TraceGesture> gestures = new ArrayList<>();

	public TraceGesture appendNewGesture() {
		val res = new TraceGesture();
		gestures.add(res);
		return res;
	}

	public TraceGesture getLast() {
		return (gestures.isEmpty())? null : gestures.get(gestures.size() - 1);
	}

	public void remove(TraceGesture p) {
		this.gestures.remove(p);
	}

}
