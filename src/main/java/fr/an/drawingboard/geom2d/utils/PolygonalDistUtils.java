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

	public static double[] ptsToPolygonalDists(List<Pt2D> pts) {
		// dist[0] = 0, dist[i] = d(pt[0],pt[1]) + ... + d(pt[i-1],pt[i])
		int ptsCount = pts.size();
		double[] dist = new double[ptsCount];
		dist[0] = 0.0;
		double currDist = 0.0;
		Pt2D prevPt = pts.get(0);
		for(int i = 1; i < ptsCount; i++) {
			Pt2D pti = pts.get(i);
			currDist += prevPt.distTo(pti);
			dist[i] = currDist;
			prevPt = pti;
		}
		return dist;
	}
	
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
		double[] dist = ptsToPolygonalDists(pts);
		double totalDist = dist[ptsCount-1];
		double coefDist = 1.0 / ((totalDist != 0.0)? totalDist : 1.0);
		// assign pt weight with average neighboor dist (and coef 2 for start/end pt.. all segment dist are counted x2)
		double coefWeight = 0.5 * coefDist;
		List<ParamWeightedPt2D> res = new ArrayList<>(ptsCount);
		double weigthPt0 = coefWeight * 2 * dist[1];
		res.add(new ParamWeightedPt2D(pts.get(0), 0.0, weigthPt0));
		for(int i = 1; i < ptsCount-1; i++) {
			double distRatioPti = dist[i] * coefDist;
			double weigthPti = coefWeight * (dist[i+1] - dist[i-1]);
			res.add(new ParamWeightedPt2D(pts.get(i), distRatioPti, weigthPti));
		}
		double weigthLastPt = coefWeight * 2 * (dist[ptsCount-1] - dist[ptsCount-2]);
		res.add(new ParamWeightedPt2D(pts.get(ptsCount-1), 1.0, weigthLastPt));
		return new TotalDistAndWeigthPts(res, totalDist);
	}

	public static List<WeightedPt2D> ptsToWeightedPts_polygonalDistance(List<Pt2D> pts) {
		val tmp = ptsParamWeightedPts_polygonalDistance(pts);
		return LsUtils.map(tmp.pts, p -> new WeightedPt2D(p.pt, p.weight));
	}

}
