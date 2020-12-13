package fr.an.drawingboard.recognizer.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

public class WeightedPtsBuilder {

	public static List<WeightedPt2D> ptsToWeightedPts_polygonalDistance(List<Pt2D> pts) {
		// compute avgNeightboorsDist
		int ptsCount = pts.size();
		if (ptsCount == 0) {
			return new ArrayList<>();
		} else if (ptsCount == 1) {
			return LsUtils.of(new WeightedPt2D(pts.get(0), 1.0));
		}
		double[] avgNeightboorsDist = new double[ptsCount];
		avgNeightboorsDist[0] = pts.get(0).distTo(pts.get(1));
		for(int i = 1; i < ptsCount-2; i++) {
			double dist_i_ip1_div2 = 0.5 * pts.get(i).distTo(pts.get(i+1));
			avgNeightboorsDist[i] += dist_i_ip1_div2;
			avgNeightboorsDist[i+1] += dist_i_ip1_div2;
		}
		avgNeightboorsDist[ptsCount-1] = pts.get(ptsCount-2).distTo(pts.get(ptsCount-1));
		// compute sum  (startPt and endPt have same coef here than intermediate pts)
		double sum = 0.0;
		for(val dist : avgNeightboorsDist) {
			sum += dist;
		}
		// normalize to sum=1.0
		double coefNorm = 1.0 / ((sum != 0)? sum : 1.0);
		List<WeightedPt2D> res = new ArrayList<>(ptsCount);
		for(int i = 0; i < ptsCount; i++) {
			res.add(new WeightedPt2D(pts.get(i), avgNeightboorsDist[i]*coefNorm));
		}
		return res;
	}

}
