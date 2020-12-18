package fr.an.drawingboard.model.shapedef.paramdef;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.drawingboard.model.shapedef.paramdef.ParamTypeDef.RangeDoubleParamTypeDef;

public class ParamCategoryRegistry {

	public static final ParamCategoryRegistry INSTANCE = new ParamCategoryRegistry();
	
	public final ParamCategory STD_X;
	public final ParamCategory STD_W;
	public final ParamCategory STD_Y;
	public final ParamCategory STD_H;
	public final ParamCategory STD_RADIUS;

	private final Map<String,ParamCategory> paramCategories = new LinkedHashMap<>();
	
	public ParamCategoryRegistry() {
		RangeDoubleParamTypeDef xUnitType = new RangeDoubleParamTypeDef("x-unit", -200, +5000, 0.1);
		STD_X = of("x", "horyzontal position", xUnitType);
		STD_W = of("w", "width", xUnitType);
		RangeDoubleParamTypeDef yUnitType = new RangeDoubleParamTypeDef("y-unit", -200, +5000, 0.1);
		STD_Y = of("y", "vertical position", yUnitType);
		STD_H = of("h", "vertical height", yUnitType);
		
		STD_RADIUS = of("r", "radius", xUnitType);
	}

	public ParamCategory get(String name) {
		ParamCategory found = paramCategories.get(name);
		if (found == null) {
			throw new IllegalArgumentException("paramCategory name not found");
		}
		return found;
	}

	public ParamCategory of(String name, String descr, ParamTypeDef type) {
		ParamCategory found = paramCategories.get(name);
		if (found != null) {
			if (found.type.equals(type)) {
				return found;
			} else {
				throw new IllegalArgumentException("paramCategory name already exists");
			}
		}
		ParamCategory res = new ParamCategory(this, name, descr, type);
		paramCategories.put(name, res);
		return res;
	}
	
 
}
