package fr.an.drawingboard.model.var;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;
import lombok.Getter;

public abstract class ParametrizableEltDef {
	
	// ???
	@Getter
	private ParametrizableEltDef parent;
	
	@Getter
	private final Map<String,ParamDef> params = new LinkedHashMap<>();

	// --------------------------------------------------------------------------------------------

	public ParametrizableEltDef() {
	}

	public ParametrizableEltDef(ParametrizableEltDef parent) {
		this.parent = parent;
	}

	// --------------------------------------------------------------------------------------------

	public ParamDef addParamDef(String name) {
		ParamDef res = new ParamDef(this, name);
		params.put(name, res);
		return res;
	}
	
	public ParamDef getParam(String name) {
		return params.get(name);
	}

	public ParamDefExpr getParamExpr(String name) {
		return getParam(name).expr;
	}

}
