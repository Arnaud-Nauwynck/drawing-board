package fr.an.drawingboard.stddefs.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePt;

public class StdTraceBuilder {

	/**
	 * <PRE>
	 * 
	 * +--------+
	 *          |
	 *          |
	 *          +
	 * </PRE>
	 */
	public static TraceGesture traceDownRight_3pts() {
		TraceGesture res = new TraceGesture ();
		TracePath path = new TracePath();
		List<TracePt> pts = new ArrayList<>();
		pts.add(new TracePt(50, 100));
		pts.add(new TracePt(400, 100));
		pts.add(new TracePt(400, 450));
		TracePathElement elt0 = new DiscretePointsTracePathElement(pts);
		path.addPathElement(elt0);
		res.addPath(path);
		return res;
	}
	
	/**
	 * <PRE>
	 * 
	 * +-+-+-+-+
	 *          |
	 *          +
	 *          |
	 *          +
	 * </PRE>
	 */
	public static TraceGesture traceDownRight_15pts() {
		TraceGesture res = new TraceGesture ();
		TracePath path = new TracePath();
		List<TracePt> pts = new ArrayList<>();
		pts.add(new TracePt(40, 90));
		pts.add(new TracePt(50, 100));
		pts.add(new TracePt(100, 100));
		pts.add(new TracePt(150, 100));
		pts.add(new TracePt(200, 100));
		pts.add(new TracePt(250, 100));
		pts.add(new TracePt(300, 100));
		pts.add(new TracePt(350, 100));
		pts.add(new TracePt(400, 100));
		pts.add(new TracePt(400, 150));
		pts.add(new TracePt(400, 200));
		pts.add(new TracePt(400, 250));
		pts.add(new TracePt(400, 300));
		pts.add(new TracePt(400, 350));
		pts.add(new TracePt(400, 400));
		pts.add(new TracePt(410, 410));
		TracePathElement elt0 = new DiscretePointsTracePathElement(pts);
		path.addPathElement(elt0);
		res.addPath(path);
		return res;
	}

	
	/**
	 * <PRE>
	 * 
	 * +---------
	 *            \
	 *             \
	 *              |
	 *              |
	 *              |
	 * </PRE>
	 */
	public static TraceGesture traceDownRight_rounded() {
		TraceGesture res = new TraceGesture ();
		TracePath path = new TracePath();
		List<TracePt> pts = new ArrayList<>();
		pts.add(new TracePt(50, 100));
		pts.add(new TracePt(200, 100));
		pts.add(new TracePt(250, 100));
		pts.add(new TracePt(300, 100));
		pts.add(new TracePt(345, 120));
		pts.add(new TracePt(370, 140));
		pts.add(new TracePt(395, 180));
		pts.add(new TracePt(400, 200));
		pts.add(new TracePt(400, 250));
		pts.add(new TracePt(400, 300));
		pts.add(new TracePt(400, 400));
		TracePathElement elt0 = new DiscretePointsTracePathElement(pts);
		path.addPathElement(elt0);
		res.addPath(path);
		return res;
	}
}
