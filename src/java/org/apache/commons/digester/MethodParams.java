/* Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 


package org.apache.commons.digester;

import org.apache.commons.logging.Log;

/**
 * Used to encapsulate a "delayed" invocation of some arbitrary method using
 * some set of parameters. This object is created when the target of the
 * method call is known; the parameter values for the call are populated as
 * they become available, and when all parameters are available, the method 
 * is invoked.
 * <p>
 * This class is not used directly by most Digester users; it is declared public
 * only for the use of programmers extending standard Digester behaviour.
 */

public class MethodParams {

    private InvokeMethodRule rule;
    private Object target;
    private int nParams;
    
    private Object[] params;
    private boolean[] paramsSet;
    private int nParamsLeftToSet;
    
    /**
     * When setParam has been called once for each parameter 0..nParams-1, 
     * the specified rule's doInvocation method will be invoked passing the
     * target object and the parameter values.
     */
    public MethodParams(
    InvokeMethodRule rule,
    Object target,
    int nParams) {
        this.rule = rule;
        this.target = target;
        this.nParams = nParams;
        
        this.params = new Object[nParams];
        this.paramsSet = new boolean[nParams];
        this.nParamsLeftToSet = nParams;
    }

    /**
     * Set the value of the nth parameter to the method that this object
     * encapsulates.
     *
     * Note that the method is only invoked if all params are available.
     * Is this what CallMethodRule does? Is this what we want?
     */
    public void setParam(int index, Object value) throws Exception {
        Log log = rule.getDigester().getLogger();

        log.debug("in setParam");
        log.debug("nParams is:" + nParams);
        log.debug("params length=" + params.length);
        
        if ((index < 0) || (index >= nParams)) {
            throw new ArrayIndexOutOfBoundsException(
                "bad index [" + index + "] for parameter array"
                + " of size [" + nParams + "]"
                + " associated with rule [" + rule.toString() + "]");
        }
        
        params[index] = value;
        
        if (paramsSet[index] == false) {
            log.debug("first time this param has been set.");
            paramsSet[index] = true;
            --nParamsLeftToSet;
            if (nParamsLeftToSet == 0) {
                log.debug("invoking method...");
                rule.doInvocation(target, params);
                log.debug("invoked method.");
            }
        } else {
            // Else we are *resetting* the value of a param.
            // this is normally an error.
            //
            // Note that the first value is not overridden.
            log.warn(
                "Reassigning to parameter [" + index + "]"
                + " associated with rule [" + rule.toString() + "]");
        }
    }
    
    /**
     * Set a group of parameter values at once. Each value (including nulls)
     * in the array is assigned to the corresponding parameter value,
     * provided the parameter hasn't already been assigned a value. Any
     * objects in the array which are references to the special object
     * InvokeParamRule.NO_DEFAULT are skipped.
     * <p>
     * Normally, a call to this method would cause the target method to
     * be invoked, because values had been assigned to all its parameters.
     * There are two cases where this isn't the case:
     * <ul>
     * <li>When the defaults array is shorter than the params array</li>
     * <li>When the defaults array contains NO_DEFAULT values, and those
     * parameters have not been set via any other mechanism.</li>
     * </ul>
     */
    public void setDefaults(Object[] defaults) throws Exception {
        Log log = rule.getDigester().getLogger();
        int nDefaults = defaults.length;
        for(int i=0; i<nDefaults; ++i) {
            Object param = defaults[i];
            if (!paramsSet[i] &&
                (param != InvokeParamRule.NO_DEFAULT)) {
                setParam(i, param);
            }
        }
    }
}
