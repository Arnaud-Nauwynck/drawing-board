package fr.an.drawingboard.geom2d.utils;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

public class PolygonalDistUtils {

	public static List<ParamWeightedPt2D> ptsParamWeightedPts_polygonalDistance(List<Pt2D> pts) {
		// compute avgNeightboorsDist
		int ptsCount = pts.size();
		if (ptsCount == 0) {
			return new ArrayList<>();
		} else if (ptsCount == 1) {
			return LsUtils.of(new ParamWeightedPt2D(pts.get(0), 0.0, 1.0));
		}
		double[] dist = new double[ptsCount];
		double[] avgNeightboorsDist = new double[ptsCount];
		dist[0] = 0.0;
		avgNeightboorsDist[0] = pts.get(0).distTo(pts.get(1));
		avgNeightboorsDist[1] = 0.5 * avgNeightboorsDist[0];  
		for(int i = 1; i < ptsCount-1; i++) {
			val dist_i_ip1 = pts.get(i).distTo(pts.get(i+1));
			dist[i] = dist[i-1] + dist_i_ip1;
			val dist_i_ip1_div2 = 0.5 * dist_i_ip1;
			avgNeightboorsDist[i] += dist_i_ip1_div2;
			avgNeightboorsDist[i+1] += dist_i_ip1_div2;
		}
		double lastDist = pts.get(ptsCount-2).distTo(pts.get(ptsCount-1));
		dist[ptsCount-1] = dist[ptsCount-2] + lastDist;
		avgNeightboorsDist[ptsCount-1] = lastDist;
		// compute sum  (startPt and endPt have same coef here than intermediate pts)
		double sumWeigth = 0.0;
		for(val d: avgNeightboorsDist) {
			sumWeigth += d;
		}
		// normalize to sum=1.0
		double coefNorm = 1.0 / ((sumWeigth != 0)? sumWeigth : 1.0);
		double coefDist = 1.0 / ((dist[ptsCount-1] != 0)? dist[ptsCount-1] : 1.0);
		List<ParamWeightedPt2D> res = new ArrayList<>(ptsCount);
		for(int i = 0; i < ptsCount; i++) {
			res.add(new ParamWeightedPt2D(pts.get(i), dist[i]*coefDist, avgNeightboorsDist[i]*coefNorm));
		}
		return res;
	}

	public static List<WeightedPt2D> ptsToWeightedPts_polygonalDistance(List<Pt2D> pts) {
		val tmp = ptsParamWeightedPts_polygonalDistance(pts);
		return LsUtils.map(tmp, p -> new WeightedPt2D(p.pt, p.weight));
	}
	
	@Deprecated
	public static List<WeightedPt2D> ptsToWeightedPts_polygonalDistance_old(List<Pt2D> pts) {
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
