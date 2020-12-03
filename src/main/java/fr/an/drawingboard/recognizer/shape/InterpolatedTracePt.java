package fr.an.drawingboard.recognizer.shape;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.model.trace.TracePt;

/**
 * interpolation pt between 2 pts
 */
public final class InterpolatedTracePt {
	
	public final Pt2D fromPt;
	public final TracePt fromIfTracePt;
	public final InterpolatedTracePt fromIfInterpolTracePt;
	
	/** 0.0 for fromPt, 1.0 for toPt */
	public final double interpolCoef;
	
	public final Pt2D toPt;
	public final TracePt toIfTracePt;
	
	public final Pt2D interpolPt;

	public InterpolatedTracePt(TracePt tracePt) {
		this.fromPt = tracePt.xy();
		this.fromIfTracePt = tracePt;
		this.fromIfInterpolTracePt = null;
		this.interpolCoef = 1.0;
		this.toPt = tracePt.xy();
		this.toIfTracePt = tracePt;
		this.interpolPt = toPt;
	}

	public InterpolatedTracePt(TracePt fromTracePt, double interpolCoef, TracePt toTracePt) {
		this.fromPt = fromTracePt.xy();
		this.fromIfTracePt = fromTracePt;
		this.fromIfInterpolTracePt = null;
		this.interpolCoef = interpolCoef;
		this.toPt = toTracePt.xy();
		this.toIfTracePt = toTracePt;
		this.interpolPt = (fromTracePt != null)? Pt2D.linearSum(1.0-interpolCoef, fromPt, interpolCoef, toPt) : toPt;
	}

	public InterpolatedTracePt(InterpolatedTracePt fromInterpPt, double interpolCoef, TracePt toTracePt) {
		this.fromPt = fromInterpPt.interpolPt;
		this.fromIfTracePt = null;
		this.fromIfInterpolTracePt = fromInterpPt;
		this.interpolCoef = interpolCoef;
		this.toPt = toTracePt.xy();
		this.toIfTracePt = toTracePt;
		this.interpolPt = Pt2D.linearSum(1.0-interpolCoef, fromPt, interpolCoef, toPt);
	}

}
