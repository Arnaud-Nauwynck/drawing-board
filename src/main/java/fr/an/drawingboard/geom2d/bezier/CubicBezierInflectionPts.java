package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import lombok.val;

public class CubicBezierInflectionPts {

	public static class InflectionPointList {
		public static final InflectionPointList EMPTY = new InflectionPointList();
		public final int count;
		public final double param0;
		public final double param1;

		public InflectionPointList() {
			this.count = 0;
			this.param0 = param1 = Double.NaN;
		}

		public InflectionPointList(double param0) {
			this.count = 1;
			this.param0 = param0;
			this.param1 = Double.NaN;
		}

		public InflectionPointList(double param0, double param1) {
			this.count = 2;
			this.param0 = param0;
			this.param1 = param1;
		}
	}

	public static InflectionPointList findInflectionPointParams(CubicBezier2D bezier) {
		return findInflectionPointParams(bezier.startPt, bezier.p1, bezier.p2, bezier.endPt);
	}

	public static InflectionPointList findInflectionPointParams(
			Pt2D p0, Pt2D p1, Pt2D p2, Pt2D p3) {
		return findInflectionPointParams(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
	}

	public static InflectionPointList findInflectionPointParams(
			double p0x, double p0y, double p1x, double p1y, double p2x, double p2y, double p3x, double p3y) {
		val ax = -p0x + 3 * p1x - 3 * p2x + p3x;
		val bx = 3 * p0x - 6 * p1x + 3 * p2x;
		val cx = -3 * p0x + 3 * p1x;

		val ay = -p0y + 3 * p1y - 3 * p2y + p3y;
		val by = 3 * p0y - 6 * p1y + 3 * p2y;
		val cy = -3 * p0y + 3 * p1y;
		
		val a = 3 * (ay * bx - ax * by);
		val b = 3 * (ay * cx - ax * cy);
		val c = by * cx - bx * cy;
		val r2 = b * b - 4 * a * c;
		double firstIfp;
		double secondIfp;
		if (r2 >= 0 && a != 0.0) {
			val r = Math.sqrt(r2);
			firstIfp = (-b + r) / (2 * a);
			secondIfp = (-b - r) / (2 * a);
			if ((firstIfp > 0 && firstIfp < 1.0) && (secondIfp > 0.0 && secondIfp < 1.0)) {
				if (firstIfp > secondIfp) {
					val tmp = firstIfp;
					firstIfp = secondIfp;
					secondIfp = tmp;
				}

				if (secondIfp - firstIfp > 0.00001) {
					return new InflectionPointList(firstIfp, secondIfp);
				} else {
					return new InflectionPointList(firstIfp);
				}
			} else if (firstIfp > 0 && firstIfp < 1.0)
				return new InflectionPointList(firstIfp);
			else if (secondIfp > 0 && secondIfp < 1.0) {
				firstIfp = secondIfp;
				return new InflectionPointList(firstIfp);
			}
			return InflectionPointList.EMPTY;
		} else {
			return InflectionPointList.EMPTY;
		}

	}

//	public static get_t_values_of_critical_points(p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y) {
//		    val a = (c2x - 2 * c1x + p1x) - (p2x - 2 * c2x + c1x),
//		    b = 2 * (c1x - p1x) - 2 * (c2x - c1x),
//		    c = p1x - c1x,
//		    t1 = (-b + Math.sqrt(b * b - 4 * a * c)) / 2 / a,
//		    t2 = (-b - Math.sqrt(b * b - 4 * a * c)) / 2 / a,
//		    tvalues=[];
//		    Math.abs(t1) > "1e12" && (t1 = 0.5);
//		    Math.abs(t2) > "1e12" && (t2 = 0.5);
//		    if (t1 >= 0 && t1 <= 1 && tvalues.indexOf(t1)==-1) tvalues.push(t1)
//		    if (t2 >= 0 && t2 <= 1 && tvalues.indexOf(t2)==-1) tvalues.push(t2);
//
//		    a = (c2y - 2 * c1y + p1y) - (p2y - 2 * c2y + c1y);
//		    b = 2 * (c1y - p1y) - 2 * (c2y - c1y);
//		    c = p1y - c1y;
//		    t1 = (-b + Math.sqrt(b * b - 4 * a * c)) / 2 / a;
//		    t2 = (-b - Math.sqrt(b * b - 4 * a * c)) / 2 / a;
//		    Math.abs(t1) > "1e12" && (t1 = 0.5);
//		    Math.abs(t2) > "1e12" && (t2 = 0.5);
//		    if (t1 >= 0 && t1 <= 1 && tvalues.indexOf(t1)==-1) tvalues.push(t1);
//		    if (t2 >= 0 && t2 <= 1 && tvalues.indexOf(t2)==-1) tvalues.push(t2);
//
//		    val inflectionpoints = find_inflection_points(p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y);
//		    if (inflectionpoints[0]) tvalues.push(inflectionpoints[0]);
//		    if (inflectionpoints[1]) tvalues.push(inflectionpoints[1]);
//
//		    tvalues.sort(compare_num);
//		    return tvalues;
//		}
//	}

}
