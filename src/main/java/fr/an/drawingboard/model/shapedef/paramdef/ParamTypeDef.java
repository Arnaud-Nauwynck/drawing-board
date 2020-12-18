package fr.an.drawingboard.model.shapedef.paramdef;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
public abstract class ParamTypeDef {

	public final String name;
	public final String descr;
	
	// ------------------------------------------------------------------------

	@Value
	public static class DoubleParamTypeDef extends ParamTypeDef {
		public final double precision;
		
		public DoubleParamTypeDef(String name, double precision) {
			super(name, "double, precision:" + precision);
			this.precision = precision;
		}
		
	}

	// ------------------------------------------------------------------------
	
	@Value
	public static class RangeDoubleParamTypeDef extends ParamTypeDef {
		public final double minValue;
		public final double maxValue;
		public final double precision;
		
		public RangeDoubleParamTypeDef(String name, double minValue, double maxValue, double precision) {
			super(name, "double range [" + minValue + ";" + maxValue + "], precision:" + precision);
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.precision = precision;
		}
		
	}

	// ------------------------------------------------------------------------
	
	@Value
	public static class IntParamTypeDef extends ParamTypeDef {
		
		public IntParamTypeDef(String name) {
			super(name, "int");
		}
		
	}
	
	// ------------------------------------------------------------------------
	
	@Value
	public static class RangeIntParamTypeDef extends ParamTypeDef {
		public final int minValue;
		public final int maxValue;
		
		public RangeIntParamTypeDef(String name, int minValue, int maxValue) {
			super(name, "int range [" + minValue + ";" + maxValue + "]");
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
	}

	// ------------------------------------------------------------------------
	
	@Value
	public static class EnumParamTypeDef<T> extends ParamTypeDef {
		public final ImmutableSet<T> values;
		
		public EnumParamTypeDef(String name, Set<T> values) {
			super(name, "enum " + name);
			this.values = ImmutableSet.copyOf(values);
		}
		
	}

	// ------------------------------------------------------------------------
	
	@Value
	public static class StringParamTypeDef<T> extends ParamTypeDef {
		
		public StringParamTypeDef(String name) {
			super(name, "string");
		}
		
	}

	// ------------------------------------------------------------------------
	
	@Value
	public static class PatternStringParamTypeDef<T> extends ParamTypeDef {
		public final Pattern pattern;
		
		public PatternStringParamTypeDef(String name, Pattern pattern) {
			super(name, "string pattern:" + pattern);
			this.pattern = pattern;
		}
		
	}

}