package nl.weeaboo.vn.script.impl.lib;

import java.io.IOException;
import java.util.Collection;

import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.lua2.luajava.LuajavaLib;
import nl.weeaboo.lua2.vm.LuaBoolean;
import nl.weeaboo.lua2.vm.LuaConstants;
import nl.weeaboo.lua2.vm.LuaInteger;
import nl.weeaboo.lua2.vm.LuaTable;
import nl.weeaboo.lua2.vm.LuaThread;
import nl.weeaboo.lua2.vm.Varargs;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.INovel;
import nl.weeaboo.vn.core.impl.StaticEnvironment;
import nl.weeaboo.vn.core.impl.StaticRef;
import nl.weeaboo.vn.save.ISaveFile;
import nl.weeaboo.vn.save.ISaveFileHeader;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.save.IStorage;
import nl.weeaboo.vn.save.impl.SaveParams;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.ScriptFunction;
import nl.weeaboo.vn.script.impl.lua.ILuaStorage;
import nl.weeaboo.vn.script.impl.lua.LuaConvertUtil;
import nl.weeaboo.vn.script.impl.lua.LuaStorage;

public class SaveLib extends LuaLib {

    private static final long serialVersionUID = 1L;

    private final StaticRef<INovel> novelRef = StaticEnvironment.NOVEL;
    private final IEnvironment env;

    public SaveLib(IEnvironment env) {
        super("Save");

        this.env = env;
    }

    /**
     * @param args
     *        <ol>
     *        <li>(optional) start index
     *        <li>(optional) max result count
     *        </ol>
     * @return All used save slots in the range {@code [start, start + maxResultCount)}.
     */
    @ScriptFunction
    public Varargs getSaves(Varargs args) {
        int offset = args.optint(1, 0);
        int maxResults = args.optint(2, 10);

        ISaveModule saveModule = env.getSaveModule();
        Collection<ISaveFile> saves = saveModule.getSaves(offset, maxResults);
        LuaTable table = new LuaTable(saves.size(), 0);
        for (ISaveFile saveInfo : saves) {
            table.rawset(saveInfo.getSlot(), LuajavaLib.toUserdata(saveInfo, ISaveFileHeader.class));
        }
        return table;
    }

    /**
     * @param args
     *        <ol>
     *        <li>save slot (int)
     *        <li>(optional) userdata table
     *        </ol>
     */
    @ScriptFunction
    public Varargs save(Varargs args) throws ScriptException {
        int slot = args.checkint(1);
        IStorage userData = LuaConvertUtil.toStorage(args.opttable(2, new LuaTable()));

        // Gather required params
        ISaveModule saveModule = env.getSaveModule();
        INovel novel = novelRef.get();
        SaveParams saveParams = new SaveParams();
        saveParams.setUserData(userData);

        // TODO: Store screenshot in the save data

        // Save
        final LuaRunState lrs = LuaRunState.getCurrent();
        final LuaThread thread = lrs.getRunningThread();
        Varargs result = thread.yield(LuaConstants.NONE);
        try {
            saveModule.save(novel, slot, saveParams, null);
        } catch (IOException e) {
            throw new ScriptException("Error saving to slot " + slot, e);
        }
        return result;
    }

    @ScriptFunction
    public Varargs load(Varargs args) throws ScriptException {
        int slot = args.checkint(1);
        ISaveModule saveModule = env.getSaveModule();
        INovel novel = novelRef.get();

        final LuaRunState lrs = LuaRunState.getCurrent();
        final LuaThread thread = lrs.getRunningThread();
        Varargs result = thread.yield(LuaConstants.NONE);
        lrs.destroy();
        try {
            saveModule.load(novel, slot, null);
        } catch (IOException e) {
            throw new ScriptException("Error loading save slot: " + slot, e);
        }
        return result;
    }

    /**
     * @param args
     *        <ol>
     *        <li>save slot index
     *        </ol>
     * @return {@code true} if the save slot exists, {@code false} otherwise.
     */
    @ScriptFunction
    public Varargs getSaveExists(Varargs args) {
        int slot = args.checkint(1);
        ISaveModule saveModule = env.getSaveModule();

        return LuaBoolean.valueOf(saveModule.getSaveExists(slot));
    }

    /**
     * @param args Not used.
     * @return The index of a free save slot.
     */
    @ScriptFunction
    public Varargs getNextFreeSlot(Varargs args) {
        ISaveModule saveModule = env.getSaveModule();
        return LuaInteger.valueOf(saveModule.getNextFreeSlot());
    }

    /**
     * Deletes a save file.
     *
     * @param args
     *        <ol>
     *        <li>save slot index
     *        </ol>
     */
    @ScriptFunction
    public Varargs delete(Varargs args) throws ScriptException {
        int slot = args.checkint(1);
        ISaveModule saveModule = env.getSaveModule();

        try {
            saveModule.delete(slot);
            return LuaConstants.NONE;
        } catch (IOException e) {
            throw new ScriptException("Unable to delete save slot: " + slot, e);
        }
    }

    // TODO: Implement the rest of the save lib functions
    /*
    protected Varargs getSavepointStorage(Varargs args) {
        return LuajavaLib.toUserdata(saveHandler.getSavepointStorage(), IStorage.class);
    }

    protected Varargs getQuickSaveSlot(Varargs args) {
        return valueOf(saveHandler.getQuickSaveSlot(args.optint(1, 1)));
    }

    protected Varargs getAutoSaveSlot(Varargs args) {
        return valueOf(saveHandler.getAutoSaveSlot(args.optint(1, 1)));
    }

    protected Varargs getFreeSaveSlot(Varargs args) {
        return valueOf(saveHandler.getNextFreeSlot());
    }
    */

    /**
     * Returns an {@link IStorage} object that's shared between all save files.
     *
     * @param args Not used.
     */
    @ScriptFunction
    public Varargs getSharedGlobals(Varargs args) {
        ISaveModule saveModule = env.getSaveModule();

        IStorage sharedGlobals = saveModule.getSharedGlobals();
        return LuajavaLib.toUserdata(LuaStorage.from(sharedGlobals), ILuaStorage.class);
    }
}
