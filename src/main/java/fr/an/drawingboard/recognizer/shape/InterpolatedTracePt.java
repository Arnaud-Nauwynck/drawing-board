package fr.an.drawingboard.recognizer.shape;

import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.model.trace.TracePt;

/**
 * interpolation pt between 2 discrete TracePt
 */
public final class InterpolatedTracePt {
	
	public final TracePt fromPt;
	public final double interpolCoef;
	public final TracePt toPt;
	public final Pt2D interpolPt;
	
	public InterpolatedTracePt(TracePt fromPt, double interpolCoef, TracePt toPt) {
		this.fromPt = fromPt;
		this.interpolCoef = interpolCoef;
		this.toPt = toPt;
		double ptx = interpolCoef * fromPt.x + (1-interpolCoef) * toPt.x;
		double pty = interpolCoef * fromPt.y + (1-interpolCoef) * toPt.y;
		this.interpolPt = new Pt2D(ptx, pty);
	}

}