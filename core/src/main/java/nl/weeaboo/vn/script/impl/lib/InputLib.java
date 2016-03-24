package nl.weeaboo.vn.script.impl.lib;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;

import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.core.impl.StaticEnvironment;
import nl.weeaboo.vn.core.impl.StaticRef;
import nl.weeaboo.vn.input.IInput;
import nl.weeaboo.vn.input.VKey;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.ScriptFunction;
import nl.weeaboo.vn.script.impl.lua.LuaScriptEnv;

public class InputLib extends LuaLib {

    private static final long serialVersionUID = 1L;

    private final StaticRef<IInput> inputRef = StaticEnvironment.INPUT;

    public InputLib() {
        super("Input");
    }

    @Override
    public void initEnv(LuaScriptEnv env) throws ScriptException {
        super.initEnv(env);

        LuaTable globals = env.getGlobals();

        // Create a table with standard VKeys
        LuaTable table = new LuaTable();
        for (VKey key : VKey.getStandardKeys()) {
            table.rawset(key.getId(), LuajavaLib.toUserdata(key, VKey.class));
        }
        globals.rawset("VKeys", table);
    }

    protected IInput getInput() {
        return inputRef.get();
    }

    private static VKey getVKey(Varargs args, int index) throws ScriptException {
        if (args.isstring(index)) {
            return VKey.fromString(args.tojstring(index));
        } else if (args.isuserdata(index)) {
            Object javaObject = args.touserdata(index);
            if (javaObject instanceof VKey) {
                return (VKey)javaObject;
            }
        }
        throw new ScriptException("Invalid key argument at index: " + index);
    }

    /**
     * @param args
     *        <ol>
     *        <li>vkey
     *        </ol>
     * @return {@code true} if a key press for the requested key was consumed by this method.
     */
    @ScriptFunction
    public Varargs consume(Varargs args) throws ScriptException {
        VKey key = getVKey(args, 1);

        return LuaBoolean.valueOf(getInput().consumePress(key));
    }

}
