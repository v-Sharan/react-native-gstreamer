package com.kalyzee.rctgstplayer.utils.manager;

import java.util.HashMap;
import java.util.Map;

public enum Command {

    setState, recreateView;

    private int index;
    public final int getIndex() {
        return index;
    }

    // ← FIXED: added <> to avoid raw type warning
    private static HashMap<String, Integer> commandMap = new HashMap<>();
    public static Map<String, Integer> getCommandsMap() {
        return commandMap;
    }

    // Kept for reference but no longer called (receiveCommand now uses String)
    public static boolean is(int commandType, Command command) {
        return Command.values()[commandType].getIndex() == command.getIndex();
    }

    // String-based check — used by updated receiveCommand
    public static final String setState = "setState";  // ← ADD this constant

    static {
        for (int i = 0; i < Command.values().length; i++) {
            Command command = Command.values()[i];
            command.index = i;
            commandMap.put(command.name(), i);
        }
    }
}