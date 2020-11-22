package fr.an.drawingboard.model.shape;

import fr.an.drawingboard.model.var.ParamDef;

public class ParamValue {

	public final ParamDef paramDef;
	
	public double value;

	public ParamValue(ParamDef paramDef, double value) {
		this.paramDef = paramDef;
		this.value = value;
	}
	
}
