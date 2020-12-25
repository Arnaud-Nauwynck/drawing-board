package fr.an.drawingboard.geom2d.utils;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.model.trace.TracePt;

public class DistinctPt2DListBuilder {
	List<Pt2D> pts = new ArrayList<>();
	Pt2D prev;
	
	public void add(Pt2D pt) {
		if (prev == null || !prev.equals(pt)) {
			pts.add(pt);
			prev = pt;
		}
	}
	public void add(TracePt tracePt) {
		add(tracePt.xy());
	}
	
	public List<Pt2D> build() {
		return pts;
	}
}