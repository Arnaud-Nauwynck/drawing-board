package fr.an.drawingboard.geom2d.bezier;

/**
 * https://pomax.github.io/BezierInfo-2/#reordering
 * cf https://pomax.github.io/BezierInfo-2/chapters/reordering/reorder.js
 *
 */
public class RaiseLowerBezierDegreeUtil {

//	raise() {
//		  const p = points,
//		        np = [p[0]],
//		        k = p.length;
//
//		  // raising the order of a curve is lossless:
//		  for (let i = 1, pi, pim; i < k; i++) {
//		      pi = p[i];
//		      pim = p[i - 1];
//		      np[i] = {
//		          x: ((k - i) / k) * pi.x + (i / k) * pim.x,
//		          y: ((k - i) / k) * pi.y + (i / k) * pim.y,
//		      };
//		  }
//		  np[k] = p[k - 1];
//		  points = np;
//
//		  resetMovable(points);
//		  redraw();
//		}
//
//		lower() {
//		  // Based on https://www.sirver.net/blog/2011/08/23/degree-reduction-of-bezier-curves/
//
//		  // TODO: FIXME: this is the same code as in the old codebase,
//		  //              and it does something odd to the either the
//		  //              first or last point... it starts to travel
//		  //              A LOT more than it looks like it should... O_o
//
//		  const p = points,
//		      k = p.length,
//		      data = [],
//		      n = k-1;
//
//		  if (k <= 3) return;
//
//		  // build M, which will be (k) rows by (k-1) columns
//		  for(let i=0; i<k; i++) {
//		    data[i] = (new Array(k - 1)).fill(0);
//		    if(i===0) { data[i][0] = 1; }
//		    else if(i===n) { data[i][i-1] = 1; }
//		    else {
//		      data[i][i-1] = i / k;
//		      data[i][i] = 1 - data[i][i-1];
//		    }
//		  }
//
//		  // Apply our matrix operations:
//		  const M = new Matrix(data);
//		  const Mt = M.transpose(M);
//		  const Mc = Mt.multiply(M);
//		  const Mi = Mc.invert();
//
//		  if (!Mi) {
//		    return console.error('MtM has no inverse?');
//		  }
//
//		  // And then we map our k-order list of coordinates
//		  // to an n-order list of coordinates, instead:
//		  const V = Mi.multiply(Mt);
//		  const x = new Matrix(points.map(p => [p.x]));
//		  const nx = V.multiply(x);
//		  const y = new Matrix(points.map(p => [p.y]));
//		  const ny = V.multiply(y);
//
//		  points = nx.data.map((x,i) => ({
//		      x: x[0],
//		      y: ny.data[i][0]
//		  }));
//
//		  resetMovable(points);
//		  redraw();
//		}
}
