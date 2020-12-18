package fr.an.drawingboard.model.shapedef.paramdef;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.VarDef;
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

	public ParamDef addParamDef(String name, ParamCategory paramCategory) {
		ParamDef res = new ParamDef(this, name, paramCategory); 
		params.put(name, res);
		return res;
	}
	
	public ParamDef getParam(String name) {
		ParamDef res = params.get(name);
		if (res == null && parent != null) {
			res = parent.getParam(name);
		}
		return res;
	}

	public VariableExpr getParamExpr(String name) {
		return getParam(name).varDef.expr;
	}

}
