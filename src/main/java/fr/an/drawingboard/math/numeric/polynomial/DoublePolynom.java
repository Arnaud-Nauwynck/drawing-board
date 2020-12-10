package fr.an.drawingboard.math.numeric.polynomial;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Immutable class for double polynomial
 */
public final class DoublePolynom {
	
	public static final DoublePolynom POLY_0 = new DoublePolynom(ConstDoublePolynomImpl.IMPL_0);
	public static final DoublePolynom POLY_1 = new DoublePolynom(ConstDoublePolynomImpl.IMPL_1);
	public static final DoublePolynom POLY_X = new DoublePolynom(LinearDoublePolynomImpl.IMPL_X);
	public static final DoublePolynom POLY_X2 = new DoublePolynom(QuadDoublePolynomImpl.IMPL_X2);
	
	@Getter
	private final DoublePolynomImpl impl;

	private DoublePolynom(DoublePolynomImpl impl) {
		this.impl = impl;
	}

	public static DoublePolynom valueOf(DoublePolynomImpl impl) {
		if (impl == ConstDoublePolynomImpl.IMPL_0) return POLY_0;
		else if (impl == ConstDoublePolynomImpl.IMPL_1) return POLY_1; 
		else if (impl == LinearDoublePolynomImpl.IMPL_X) return POLY_X;
		else return new DoublePolynom(impl);
	}

	public static DoublePolynom create(double[] coefs) {
		int len = coefs.length;
		if (len == 0) {
			return POLY_0; 
		} else if (len == 1) {
			return valueOf(new ConstDoublePolynomImpl(coefs[0]));
		} else if (len == 2) {
			return valueOf(new LinearDoublePolynomImpl(coefs[1], coefs[0]));
		} else if (len == 3) {
			return valueOf(new QuadDoublePolynomImpl(coefs[2], coefs[1], coefs[0]));
		} else {
			return valueOf(new DenseArrayDoublePolynomImpl(coefs));
		}
	}
	
	// ------------------------------------------------------------------------

	public int getDegree() {
		return impl.getDegree();
	}
	
	public double getCoef(int i) {
		return impl.getCoef(i);
	}
	
	public double eval(double x) {
		return impl.eval(x);
	}
	
	public DoublePolynom derive() {
		DoublePolynomImpl deriveImpl = impl.derive();
		return valueOf(deriveImpl);
	}
	
	public String formatString(String varName) {
		return impl.formatString(varName);
	}

	@Override
	public String toString() {
		return formatString("X");
	}

	// ------------------------------------------------------------------------
	
	public static abstract class DoublePolynomImpl {
		
		public abstract double eval(double x);
		
		public abstract int getDegree();

		public abstract double getCoef(int i);

		public abstract DoublePolynomImpl derive();
		
		public abstract String formatString(String varName);

		@Override
		public String toString() {
			return formatString("X");
		}
		
	}
	
	// ------------------------------------------------------------------------


	/** constant  (degre 0) polynom: P[X] = a */
	public static class ConstDoublePolynomImpl extends DoublePolynomImpl {
		public static final ConstDoublePolynomImpl IMPL_0 = new ConstDoublePolynomImpl(0);
		public static final ConstDoublePolynomImpl IMPL_1 = new ConstDoublePolynomImpl(1.0);

		public final double a;
		
		private ConstDoublePolynomImpl(double a) {
			this.a = a;
		}

		public static ConstDoublePolynomImpl valueOf(double a) {
			if (a == 0.0) return IMPL_0;
			else if (a == 1.0) return IMPL_1;
			else return new ConstDoublePolynomImpl(a);
		}

		@Override
		public int getDegree() {
			return 0;
		}

		@Override
		public double getCoef(int i) {
			if (i > 0) throw new IllegalArgumentException();
			return a;
		}

		@Override
		public double eval(double x) {
			return a;
		}
		
		@Override
		public ConstDoublePolynomImpl derive() {
			return IMPL_0;
		}

		@Override
		public String formatString(String varName) {
			return Double.toString(a);
		}

	}
	
	// ------------------------------------------------------------------------
	
	/** linear : P[X] = a X + b X */
	public static class LinearDoublePolynomImpl extends DoublePolynomImpl {
		public static final LinearDoublePolynomImpl IMPL_X = new LinearDoublePolynomImpl(1.0, 0.0);

		public final double a, b;

		public LinearDoublePolynomImpl(double a, double b) {
			this.a = a; // a should not be 0.0 (not normalized degree 1 polynom)
			this.b = b;
		}

		@Override
		public int getDegree() {
			return 1;
		}

		@Override
		public double getCoef(int i) {
			if (i > 1) throw new IllegalArgumentException();
			return (i == 1)? a : b;
		}

		@Override
		public double eval(double x) {
			return a * x + b;
		}
		
		@Override
		public DoublePolynomImpl derive() {
			return new ConstDoublePolynomImpl(a);
		}

		@Override
		public String formatString(String varName) {
			return a + "*" + varName + " + " + b;
		}

	}
	
	// ------------------------------------------------------------------------
	
	/** quadratic polynomial: P[X] = a X^2 + b X + c */
	@AllArgsConstructor
	public static class QuadDoublePolynomImpl extends DoublePolynomImpl {
		public static final QuadDoublePolynomImpl IMPL_X2 = new QuadDoublePolynomImpl(1.0, 0.0, 0.0);
		
		public final double a, b, c;
		
		@Override
		public int getDegree() {
			return 2;
		}

		@Override
		public double getCoef(int i) {
			if (i > 2) throw new IllegalArgumentException();
			return (i == 2)? a : (i == 1)? b : c;
		}

		@Override
		public double eval(double x) {
			return c + x * (b + x * c);
		}
		
		@Override
		public DoublePolynomImpl derive() {
			return new LinearDoublePolynomImpl(2.0*a, b);
		}

		@Override
		public String formatString(String varName) {
			return a + "*" + varName + "^2 + " + b + "*" + varName + " + " + c;
		}

	}
	
	// ------------------------------------------------------------------------
	
	public static class DenseArrayDoublePolynomImpl extends DoublePolynomImpl {
		
		private final double[] coefs;
		
		// private: reuse (unsafe otherwise) array ref
		private DenseArrayDoublePolynomImpl(double[] coefs) {
			this.coefs = coefs;
		}
	
		public DenseArrayDoublePolynomImpl create(double[] coefs) {
			return new DenseArrayDoublePolynomImpl(Arrays.copyOf(coefs, coefs.length));
		}

		@Override
		public int getDegree() {
			return coefs.length - 1;
		}

		@Override
		public double getCoef(int i) {
			if (i >= coefs.length) throw new IllegalArgumentException();
			return coefs[i];
		}

		@Override
		public double eval(double x) {
			double res = coefs[0];
			final int len = coefs.length;
			double xpow = 1.0;
			for(int i = 1; i < len; i++) {
				xpow *= x;
				res += coefs[i] * xpow;
			}
			return res;
		}
		
		@Override
		public DoublePolynomImpl derive() {
			final int len = coefs.length;
			if (len >= 5) {
				// general case degree >= 4, derive to degree >= 3
				final int resLen = len-1;
				double[] deriveCoefs = new double[resLen];
				for(int i = 0; i < resLen; i++) {
					deriveCoefs[i] = (i+1) * coefs[i+1];
				}
				return new DenseArrayDoublePolynomImpl(deriveCoefs);
				
			} else if (len == 4) {
				// 4 coefs (degree 3) => derive to degree 2
				double a = 3 * coefs[3];
				double b = 2 * coefs[2];
				double c = coefs[1];
				return new QuadDoublePolynomImpl(a, b, c);
			} else {
				// should not occur
				if (len == 3) {
					// 3 coefs (already Quad) => derive to degree 1
					double a = 2 * coefs[2];
					double b = coefs[1];
					return new LinearDoublePolynomImpl(a, b);
				} else if (len == 2) {
					// 2 coefs (already Linear) => derive to degree 0
					double a = coefs[1];
					return ConstDoublePolynomImpl.valueOf(a);
				} else {
					return ConstDoublePolynomImpl.IMPL_0;
				}
			}
		}
	
		@Override
		public String formatString(String varName) {
			StringBuilder sb = new StringBuilder();
			final int len = coefs.length;
			sb.append(coefs[len-1] + "*" + varName + "^" + (len-1));
			for(int pow = len-1; pow > 1; pow--) {
				sb.append(" + " + coefs[pow] + "*" + varName + "^" + pow);
			}
			sb.append(" + " + coefs[0]);
			return sb.toString();
		}

	}
	
}
