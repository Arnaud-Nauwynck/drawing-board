package fr.an.drawingboard.model.shapedef.paramdef;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;

public class ParamDef {

	public final ParametrizableEltDef owner;
	
	public final String name;
	// TODO add namespace for printing unambiguously ??
	
	public final ParamCategory paramCategory;
	
	public final VarDef varDef;
	public final Expr expr;
	
	public ParamDef(ParametrizableEltDef owner, String name, ParamCategory paramCategory) {
		super();
		this.owner = owner;
		this.name = name;
		this.paramCategory = paramCategory;
		this.varDef = new VarDef(name);
		this.expr = varDef.expr; 
	}

	@Override
	public String toString() {
		return "ParamDef[" + name + "]";
	}

	
}
