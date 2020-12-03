package fr.an.drawingboard.geom2d;

import java.util.Collection;

import lombok.Builder;
import lombok.val;

/**
 * immutable enclosing rectangle
 */
@Builder
public class BoundingRect2D {
	
	public final double minx;
	public final double miny;
	public final double maxx;
	public final double maxy;

	// ------------------------------------------------------------------------
	
	/** cf <code>builder().enclosingPts(..).build()</code> */
	private BoundingRect2D(double minx, double miny, double maxx, double maxy) {
		this.minx = minx;
		this.miny = miny;
		this.maxx = maxx;
		this.maxy = maxy;
	}

	public static BoundingRect2DBuilder builder() {
		return new BoundingRect2DBuilder();
	}
	
	public static BoundingRect2DBuilder builder(Pt2D pt) {
		return new BoundingRect2DBuilder(pt.x, pt.y, pt.x, pt.y);
	}
	
	// ------------------------------------------------------------------------

	public boolean containsPt(Pt2D p) {
		val x = p.x, y = p.y;
		return (minx <= x && x <= maxx && //
				miny <= y && y <= maxy);
	}
	
	public double outerDistOr0ToPt(Pt2D p) {
		double res;
		val x = p.x, y = p.y;
		//cases
		//  0      1      2
		//     +-------+
		//  3  |   4   |  5
		//     +-------+
		//  6      7      8
		if (x < minx) {
			// 0,3,6
			val dx = minx-x;
			if (y < miny) {
				// 0
				val dy = miny-y;
				res = Math.sqrt(dx*dx + dy*dy);
			} else if (y <= maxy) {
				// 3
				res = dx;
			} else {
				// 6
				val dy = y-maxy;
				res = Math.sqrt(dx*dx + dy*dy);
			}
		} else if (x <= maxx) {
			// 1,4,7
			if (y < miny) {
				// 1
				val dy = miny-y;
				res = dy;
			} else if (y <= maxy) {
				// 4
				res = 0.0;
			} else {
				// 7
				val dy = y-maxy;
				res = dy;
			}
		} else {
			// 2,5,8
			val dx = x-maxx;
			if (y < miny) {
				// 2
				val dy = miny-y;
				res = Math.sqrt(dx*dx + dy*dy);
			} else if (y <= maxy) {
				// 5
				res = dx;
			} else {
				// 8
				val dy = y-maxy;
				res = Math.sqrt(dx*dx + dy*dy);
			}
		}
		return res;
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * Builder for BoundingRect2D
	 */
	public static class BoundingRect2DBuilder {

		private double minx;
		private double miny;
		private double maxx;
		private double maxy;
		
		public BoundingRect2DBuilder enclosingPt(Pt2D p) {
			this.minx = Math.min(minx, p.x);
			this.miny = Math.min(miny, p.y);
			this.maxx = Math.max(maxx, p.x);
			this.maxy = Math.max(maxy, p.y);
			return this;
		}

		public boolean isContainsPt(Pt2D p) {
			val x = p.x, y = p.y;
			return (minx <= x && x <= maxx && //
					miny <= y && y <= maxy);
		}
		
		public BoundingRect2DBuilder enclosingPts(Pt2D pt0, Pt2D pt1) {
			enclosingPt(pt0);
			enclosingPt(pt1);
			return this;
		}

		public BoundingRect2DBuilder enclosingPts(Collection<Pt2D> pts) {
			for(val pt : pts) {
				enclosingPt(pt);
			}
			return this;
		}

		public BoundingRect2DBuilder enclosingBoundingRect(BoundingRect2D rect) {
			this.minx = Math.min(minx, rect.minx);
			this.miny = Math.min(miny, rect.miny);
			this.maxx = Math.max(maxx, rect.maxx);
			this.maxy = Math.max(maxy, rect.maxy);
			return this;
		}

		public BoundingRect2DBuilder() {
			this.minx = Double.MAX_VALUE;
			this.miny = Double.MAX_VALUE;
			this.maxx = Double.MIN_VALUE;
			this.maxy = Double.MIN_VALUE;
		}

		public BoundingRect2DBuilder(double ptx, double pty) {
			this.minx = ptx;
			this.miny = pty;
			this.maxx = ptx;
			this.maxy = pty;
		}

		public BoundingRect2DBuilder(double minx, double miny, double maxx, double maxy) {
			this.minx = Math.min(minx, maxx);
			this.miny = Math.min(miny, maxy);
			this.maxx = Math.max(minx, maxx);
			this.maxy = Math.max(miny, maxy);
		}
		
		public BoundingRect2D build() {
			return new BoundingRect2D(minx, miny, maxx, maxy);
		}

		public void enclosingX(double x) {
			this.minx = Math.min(minx, x);
			this.maxx = Math.max(maxx, x);
		}

		public void enclosingY(double y) {
			this.miny = Math.min(miny, y);
			this.maxy = Math.max(maxy, y);
		}

	}

}
