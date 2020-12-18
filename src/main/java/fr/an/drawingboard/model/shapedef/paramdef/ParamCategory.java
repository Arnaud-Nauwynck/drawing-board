package fr.an.drawingboard.model.shapedef.paramdef;

import lombok.Value;

@Value
public final class ParamCategory {

	public final String name;
	public final String descr;
	public final ParamTypeDef type;
	
	public ParamCategory(ParamCategoryRegistry owner, String name, String descr, ParamTypeDef type) {
		this.name = name;
		this.descr = descr;
		this.type = type;
	}
	
}
