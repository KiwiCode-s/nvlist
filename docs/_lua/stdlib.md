---
id: stdlib.lua
title: stdlib.lua
---

<!--excerpt-->

{% include sourcecode.html id="textnotation" lang="lua" class="full-screen" content="
---Standard library of utility functions
-- @module stdlib

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

---Takes a list of tables and generates a new table containing SHALLOW copies
-- of all attributes.
function extend(...)
    local result = &#123;}
    for n=1,arg.n do
        local tbl = arg[n]
        if tbl ~= nil then
            for k,v in pairs(tbl) do
                result[k] = v
            end
        end
    end
    return result
end

---Creates a shallow copy of <code>x</code>
function shallowCopy(x)
    if type(x) ~= \"table\" then
        return x
    end
    return addAll(&#123;}, x)
end

---Adds all key/value pairs in <code>values</code> to <code>tbl</code>.
function addAll(tbl, values)
    for k,v in pairs(values) do
        tbl[k] = v
    end
    return tbl
end

---Removes all key/value pairs from <code>tbl</code> for which <code>value == val</code>.
function removeAll(tbl, val)
    local result = &#123;}
    for k,v in pairs(tbl) do
        if v ~= val then
            result[k] = v
        end
    end
    return result
end

---Calls the destroy method on all values of table <code>tbl</code>.
function destroyValues(tbl)
    for _,v in pairs(tbl) do    
        if v ~= nil then
            v:destroy()
        end
    end
end

---Return a new table returning all non-nil values of tbl inserted with sequential numerical indices.
function values(...)
    local result = &#123;}
    local d = 1
    local tbl = getTableOrVarArg(...)
    for _,v in pairs(tbl) do
        if v ~= nil then
            result[d] = v
            d = d + 1
        end
    end
    return result
end

---Returns the sign of the given number (<code>-1</code> for negative values,
-- <code>1</code> for positive, and <code>0</code> for zero).
function signum(x)
    if x > 0 then
        return 1
    elseif x < 0 then
        return -1
    end
    return 0
end

---Converts a table or vararg argument and returns it as a table.
function getTableOrVarArg(...)
    if arg.n == 1 and type(arg[1]) == \"table\" then
        return arg[1]
    end
    
    local result = &#123;}
    local d = 1
    for s=1,arg.n do
        result[d] = arg[s]
        d = d + 1
    end
    return result
end

---Calls the update method on each argument, then calls <code>join</code>.
-- @see join
function update1join(...)
    local threads = getTableOrVarArg(...)

    for _,thread in ipairs(threads) do
        if thread:isRunnable() then
            thread:update()
        end
    end

    join(threads)
end

---Blocks until all threads passed as an argument are finished.
function join(...)
    local threads = getTableOrVarArg(...)

    while true do
        local finished = true
        for _,thread in ipairs(threads) do
            if thread:isRunnable() then
                finished = false
                break
            end
        end
        if finished then
            break
        end
        yield()
    end
end

---Trims the whitespace from the edges of the given string. 
function trim(str)
    return str:match(\"^%s*(.-)%s*$\")
end

---Splits <code>str</code> based on the regular expression <code>pattern</code>.
function split(str, pattern)
    local result = &#123;}
    local pattern = string.format(\"([^%s]+)\", pattern)
    str:gsub(pattern, function(c) table.insert(result, c) end)
    return result
end

---Returns the script file and line.
-- @param callOffset Offset in the call stack to determine the script file and line of.
function getScriptPos(callOffset)
    callOffset = callOffset or 0

    if debug ~= nil and debug.getinfo ~= nil then
        local info = debug.getinfo(callOffset + 2, 'Sl')
        if info ~= nil then
            return (info.short_src or \"undefined\") .. \":\" .. (info.currentline or 0)
        end
    end
    return \"undefined:0\"
end

---Returns a 'deep' field from table <code>t</code>. The given <code>name</code>
-- will be split into '.'-separated chunks which are used to recursively
-- traverses tables to find the matching value.
function getDeepField(t, name)
    for _,part in ipairs(split(name, \".\")) do
        if t == nil then
            return nil
        end
        t = t[part]
    end
    return t
end

---Returns the value of the local (or upval) variable at the specified level
-- @param level The depth in the callstack to get the locals from (1=current function)
-- @param max The maximum number of local variables to return.
function getLocalVars(level, max)
    level = level or 2
    max = max or 999

    local result = &#123;}
    
    local func = debug.getinfo(level, \"f\").func
    for i=1,max do
        local ln, lv = debug.getupvalue(func, i)
        if ln == nil then
            break
        end
        result[ln] = lv
      end
    
    for i=1,max do
        local ln, lv = debug.getlocal(level, i)
        if ln == nil then
            break
        end
        result[ln] = lv
    end
        
    return result
end

---Calls the <code>setProperty</code> function for each key/value pair in <code>props</code>.
-- @param obj The object to set the property value on.
-- @param props The table of properties to set.
function setProperties(obj, props)
    if props ~= nil then
        for k,v in pairs(props) do
            setProperty(obj, k, v)
        end
    end
end

---Calls the default setter function corresponding to a property called <code>name</code>.
-- @param obj The object to set the property value on.
-- @param name The name of the property to change.
-- @param ... The parameters to pass to the setter function (the new value for the property).
-- @see getProperty
function setProperty(obj, name, ...)
    local vals = getTableOrVarArg(...)

    Log.trace(\"setProperty(&#123;}, &#123;}, &#123;})\", obj, name, ...)

    -- First, try to access a direct accessor function with the given name
    local property = obj[name]
    if type(property) == \"function\" then
        --Property is an accessor method
        return property(obj, unpack(vals))
    end

    local setter = obj[\"set\" .. string.upper(string.sub(name, 1, 1)) .. string.sub(name, 2)]
    if setter ~= nil then
        return setter(obj, unpack(vals))
    end

    -- Set value directly. This needs to be our last alternative, because in Lua we can't
    -- distinguish between an undefined property and a property whose value is currently nil.
    obj[name] = unpack(vals)
end

---Calls the default getter function corresponding to a property called <code>name</code>.
-- @param obj The object to get the property value of.
-- @param name The name of the property to get the value of.
-- @return The value of the property (result of calling the property getter function).
-- @see setProperty
function getProperty(obj, name)
    -- First, try to access a direct field with the given name
    local property = obj[name]
    if property ~= nil then
        local result
        if type(property) == \"function\" then
            --Property is an accessor method
            result = property(obj)
        else
            result = property
        end
        Log.trace(\"getProperty(&#123;}, &#123;}) => &#123;}\", obj, name, result)
        return result
    else
        -- If no direct field exists, try a function named \"get$&#123;Name}\"
        local getter = obj[\"get\" .. string.upper(string.sub(name, 1, 1)) .. string.sub(name, 2)]
        if getter ~= nii then
            local result = getter(obj)
            Log.trace(\"getProperty(&#123;}, &#123;}) => &#123;}\", obj, name, result)
            return result
        end
    end
    
    Log.warn(\"Invalid property: &#123;}\", name)
end

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------
" %}
                