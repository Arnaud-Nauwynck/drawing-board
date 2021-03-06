package fr.an.drawingboard.model.trace;

import fr.an.drawingboard.geom2d.Pt2D;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TracePt {
	
	public final double x;
	public final double y;
	
	public long time;
	public int pressure;
	
	// TODO @Deprecated
	public double pathAbsciss;
	
	boolean isStopPoint;
	boolean isMergeStopPoint;

//	public final int leftSpeedx;
//	public final int leftSpeedy;

//	public final int rightSpeedx;
//	public final int rightSpeedy;

	public boolean isStopPointOrMergeStop() {
		return isStopPoint || isMergeStopPoint;
	}

	public Pt2D xy() {
		return new Pt2D(x, y);
	}

	public static double distRoundedPixel(TracePt pt0, TracePt pt1) {
		return distRoundedPixel(pt0.x, pt0.y, pt1.x, pt1.y); 
	}
	
	public static double distRoundedPixel(double x0, double y0, double x1, double y1) {
		double dx = Math.abs(x1 - x0);
		double dy = Math.abs(y1 - y0);
		double roundDx = Math.max(0, dx - 0.5);
		double roundDy = Math.max(0, dy - 0.5);
		double res = Math.sqrt(roundDx * roundDx + roundDy * roundDy);
		return res;
	}

	public static double dist(TracePt pt0, TracePt pt1) {
		return dist(pt0.x, pt0.y, pt1.x, pt1.y); 
	}

	public static double dist(TracePt pt0, Pt2D pt1) {
		return dist(pt0.x, pt0.y, pt1.x, pt1.y); 
	}

	public static double dist(Pt2D pt0, TracePt pt1) {
		return dist(pt0.x, pt0.y, pt1.x, pt1.y); 
	}

	public static double dist(Pt2D pt0, Pt2D pt1) {
		return dist(pt0.x, pt0.y, pt1.x, pt1.y); 
	}

	public static double dist(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		double res = Math.sqrt(dx * dx + dy * dy);
		return res;
	}

}

