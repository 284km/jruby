/*
 ***** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.truffle.parser.scope;

/**
 * Represents the the dynamic portion of scoping information.  The variableValues are the
 * values of assigned local or block variables.  The staticScope identifies which sort of
 * scope this is (block or local).
 * 
 * Properties of Dynamic Scopes:
 * 1. static and dynamic scopes have the same number of names to values
 * 2. size of variables (and thus names) is determined during parsing.  So those structured do
 *    not need to change
 *
 * FIXME: When creating dynamic scopes we sometimes accidentally pass in extra parents.  This
 * is harmless (other than wasting memory), but we should not do that.  We can fix this in two
 * ways:
 * 1. Fix all callers
 * 2. Check parent that is passed in and make if new instance is local, then its parent is not local
 */
public class ManyVarsDynamicScope extends DynamicScope {
    // Our values holder (name of variables are kept in staticScope)
    private Object[] variableValues;

    public ManyVarsDynamicScope(StaticScope staticScope, DynamicScope parent) {
        super(staticScope, parent);
        allocate();
    }

    public ManyVarsDynamicScope(StaticScope staticScope) {
        super(staticScope);
        allocate();
    }

    private void allocate() {
        if(variableValues == null) {
            int size = staticScope.getNumberOfVariables();
            variableValues = new Object[size];
        }
    }
    
    public DynamicScope cloneScope() {
        return new ManyVarsDynamicScope(staticScope, parent);
    }

    public Object[] getValues() {
        return variableValues;
    }
    
    /**
     * Get value from current scope or one of its captured scopes.
     * 
     * FIXME: block variables are not getting primed to nil so we need to null check those
     *  until we prime them properly.  Also add assert back in.
     * 
     * @param offset zero-indexed value that represents where variable lives
     * @param depth how many captured scopes down this variable should be set
     * @return the value here
     */
    public Object getValue(int offset, int depth) {
        if (depth > 0) {
            return parent.getValue(offset, depth - 1);
        }
        assertGetValue(offset, depth);
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        return variableValues[offset];
    }
    
    /**
     * Variation of getValue that checks for nulls, returning and setting the given value (presumably nil)
     */
    public Object getValueOrNil(int offset, int depth, Object nil) {
        if (depth > 0) {
            return parent.getValueOrNil(offset, depth - 1, nil);
        } else {
            return getValueDepthZeroOrNil(offset, nil);
        }
    }
    
    public Object getValueDepthZeroOrNil(int offset, Object nil) {
        assertGetValueDepthZeroOrNil(offset);
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        Object value = variableValues[offset];
        return value == null ? setValueDepthZero(nil, offset) : value;
    }
    
    public Object getValueZeroDepthZeroOrNil(Object nil) {
        assertGetValueZeroDepthZeroOrNil();
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        Object value = variableValues[0];
        return value == null ? setValueZeroDepthZero(nil) : value;
    }
    
    public Object getValueOneDepthZeroOrNil(Object nil) {
        assertGetValueOneDepthZeroOrNil();
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        Object value = variableValues[1];
        return value == null ? setValueOneDepthZero(nil) : value;
    }
    
    public Object getValueTwoDepthZeroOrNil(Object nil) {
        assertGetValueTwoDepthZeroOrNil();
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        Object value = variableValues[2];
        return value == null ? setValueTwoDepthZero(nil) : value;
    }
    
    public Object getValueThreeDepthZeroOrNil(Object nil) {
        assertGetValueThreeDepthZeroOrNil();
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        Object value = variableValues[3];
        return value == null ? setValueThreeDepthZero(nil) : value;
    }

    /**
     * Set value in current dynamic scope or one of its captured scopes.
     * 
     * @param offset zero-indexed value that represents where variable lives
     * @param value to set
     * @param depth how many captured scopes down this variable should be set
     */
    public Object setValue(int offset, Object value, int depth) {
        if (depth > 0) {
            assertParent();
            
            return parent.setValue(offset, value, depth - 1);
        } else {
            assertSetValue(offset, value);
            
            return setValueDepthZero(value, offset);
        }
    }

    public Object setValueDepthZero(Object value, int offset) {
        assertSetValueDepthZero(offset, value);

        return variableValues[offset] = value;
    }
    public Object setValueZeroDepthZero(Object value) {
        assertSetValueZeroDepthZero(value);

        return variableValues[0] = value;
    }
    public Object setValueOneDepthZero(Object value) {
        assertSetValueOneDepthZero(value);

        return variableValues[1] = value;
    }
    public Object setValueTwoDepthZero(Object value) {
        assertSetValueTwoDepthZero(value);

        return variableValues[2] = value;
    }
    public Object setValueThreeDepthZero(Object value) {
        assertSetValueThreeDepthZero(value);

        return variableValues[3] = value;
    }

    /**
     * 
     * Make a larger dynamic scope if the static scope grew.
     * 
     * Eval's with bindings require us to possibly change the size of the dynamic scope if
     * things like 'eval "b = 2", binding' happens.
     *
     */
    public void growIfNeeded() {
        int dynamicSize = variableValues == null ? 0: variableValues.length;
        
        if (staticScope.getNumberOfVariables() > dynamicSize) {
            Object values[] = new Object[staticScope.getNumberOfVariables()];
            
            if (dynamicSize > 0) {
                copy(variableValues, 0, values, 0, dynamicSize);
            }
            
            variableValues = values;
        }
    }

    public static void copy(Object[] src, int srcOff, Object[] dst, int dstOff, final int length) {
        switch (length) {
            case 0: return;
            case 1: dst[dstOff] = src[srcOff]; return;
            case 2: dst[dstOff] = src[srcOff]; dst[++dstOff] = src[srcOff + 1]; return;
            case 3: dst[dstOff] = src[srcOff]; dst[++dstOff] = src[srcOff + 1]; dst[++dstOff] = src[srcOff + 2]; return;
        }
        System.arraycopy(src, srcOff, dst, dstOff, length);
    }

    private void assertGetValue(int offset, int depth) {
        Object[] values = variableValues;
        assert values != null && offset < values.length : "No variables or index to big for getValue off: " + offset + ", Dep: " + depth + ", O: " + this;
    }

    private void assertGetValueDepthZeroOrNil(int offset) {
        Object[] values = variableValues;
        assert values != null && offset < values.length : "No variables or index too big for getValue off: " + offset + ", Dep: " + 0 + ", O: " + this;
    }

    private void assertGetValueZeroDepthZeroOrNil() {
        Object[] values = variableValues;
        assert values != null && 0 < values.length : "No variables or index to big for getValue off: " + 0 + ", Dep: " + 0 + ", O: " + this;
    }

    private void assertGetValueOneDepthZeroOrNil() {
        Object[] values = variableValues;
        assert values != null && 1 < values.length : "No variables or index to big for getValue off: " + 1 + ", Dep: " + 0 + ", O: " + this;
    }

    private void assertGetValueTwoDepthZeroOrNil() {
        Object[] values = variableValues;
        assert values != null && 3 < values.length : "No variables or index to big for getValue off: " + 3 + ", Dep: " + 0 + ", O: " + this;
    }

    private void assertGetValueThreeDepthZeroOrNil() {
        Object[] values = variableValues;
        assert values != null && 2 < values.length : "No variables or index to big for getValue off: " + 2 + ", Dep: " + 0 + ", O: " + this;
    }

    private void assertParent() {
        assert parent != null : "If depth > 0, then parent should not ever be null";
    }

    private void assertSetValue(int offset, Object value) {
        assert offset < variableValues.length : "Setting " + offset + " to " + value + ", O: " + this;
    }

    private void assertSetValueDepthZero(int offset, Object value) {
        assert offset < variableValues.length : "Setting " + offset + " to " + value + ", O: " + this;
    }

    private void assertSetValueZeroDepthZero(Object value) {
        assert 0 < variableValues.length : "Setting " + 0 + " to " + value + ", O: " + this;
    }

    private void assertSetValueOneDepthZero(Object value) {
        assert 1 < variableValues.length : "Setting " + 1 + " to " + value + ", O: " + this;
    }

    private void assertSetValueThreeDepthZero(Object value) {
        assert 3 < variableValues.length : "Setting " + 3 + " to " + value + ", O: " + this;
    }

    private void assertSetValueTwoDepthZero(Object value) {
        assert 2 < variableValues.length : "Setting " + 2 + " to " + value + ", O: " + this;
    }
}
