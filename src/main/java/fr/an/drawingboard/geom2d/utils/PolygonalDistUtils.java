package fr.an.drawingboard.geom2d.utils;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.util.LsUtils;
import lombok.AllArgsConstructor;
import lombok.val;

public class PolygonalDistUtils {

	@AllArgsConstructor
	public static class TotalDistAndWeigthPts {
		public List<ParamWeightedPt2D> pts;
		public double totalDist;
	}
	public static TotalDistAndWeigthPts ptsParamWeightedPts_polygonalDistance(List<Pt2D> pts) {
		// compute avgNeightboorsDist
		int ptsCount = pts.size();
		if (ptsCount == 0) {
			return new TotalDistAndWeigthPts(new ArrayList<ParamWeightedPt2D>(), 0.0);
		} else if (ptsCount == 1) {
			return new TotalDistAndWeigthPts(LsUtils.of(new ParamWeightedPt2D(pts.get(0), 0.0, 1.0)), 0.0);
		}
		double[] dist = new double[ptsCount];
		double[] avgNeightboorsDist = new double[ptsCount];
		dist[0] = 0.0;
		avgNeightboorsDist[0] = pts.get(0).distTo(pts.get(1));
		avgNeightboorsDist[1] = 0.5 * avgNeightboorsDist[0];  
		double sumDist = 0.0;
		for(int i = 1; i < ptsCount-1; i++) {
			val dist_i_ip1 = pts.get(i).distTo(pts.get(i+1));
			sumDist += dist_i_ip1;
			dist[i] = dist[i-1] + dist_i_ip1;
			val dist_i_ip1_div2 = 0.5 * dist_i_ip1;
			avgNeightboorsDist[i] += dist_i_ip1_div2;
			avgNeightboorsDist[i+1] += dist_i_ip1_div2;
		}
		double lastDist = pts.get(ptsCount-2).distTo(pts.get(ptsCount-1));
		sumDist += lastDist;
		dist[ptsCount-1] = dist[ptsCount-2] + lastDist;
		avgNeightboorsDist[ptsCount-1] = lastDist;
		// compute sum  (startPt and endPt have same coef here than intermediate pts)
		double sumNeightboorDist = 0.0;
		for(val d: avgNeightboorsDist) {
			sumNeightboorDist += d;
		}
		// normalize to sum=1.0
		double coefNeightboorDist = 1.0 / ((sumNeightboorDist != 0)? sumNeightboorDist : 1.0);
		double coefDist = 1.0 / ((dist[ptsCount-1] != 0)? dist[ptsCount-1] : 1.0);
		List<ParamWeightedPt2D> res = new ArrayList<>(ptsCount);
		for(int i = 0; i < ptsCount; i++) {
			res.add(new ParamWeightedPt2D(pts.get(i), dist[i]*coefDist, avgNeightboorsDist[i]*coefNeightboorDist));
		}
		return new TotalDistAndWeigthPts(res, sumDist);
	}

	public static List<WeightedPt2D> ptsToWeightedPts_polygonalDistance(List<Pt2D> pts) {
		val tmp = ptsParamWeightedPts_polygonalDistance(pts);
		return LsUtils.map(tmp.pts, p -> new WeightedPt2D(p.pt, p.weight));
	}

}
