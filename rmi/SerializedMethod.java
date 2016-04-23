package rmi;

import java.io.Serializable;
import java.lang.reflect.Method;

public class SerializedMethod implements Serializable{
	
    private static final long serialVersionUID = -4724209998802410954L;
	public String name;
	Class[] paramTypes;
	Object[] parameters;
	
	public SerializedMethod(Method method, Object[] params){
		name = method.getName();
		paramTypes = method.getParameterTypes();
		parameters = params;
	}
}
