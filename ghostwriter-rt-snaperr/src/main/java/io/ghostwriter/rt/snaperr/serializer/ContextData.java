package io.ghostwriter.rt.snaperr.serializer;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

/*

{
				"class" : "OpenSessionInViewFilter",
				"method" : "doFilter",
				"variables" : [{
					"value" : "Easy as 123?",
					"type" : "Question",
					"name" : "question"
				}, {
					"value" : 1,
					"type" : "Integer",
					"name" : "iterator"
				}
				]
}

 */

public class ContextData {

    @SerializedName("class")
    private String clazz;

    private String method;

    /**
     * We use linkedlist because we only need sequential access and we don't
     * know the size beforehand.
     */
    private List<Variable> variables = new LinkedList<>();

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

}
