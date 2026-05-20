package com.kalyzee.rctgstplayer.utils.manager;

import java.util.HashMap;
import java.util.Map;

public enum Command {

    setState, recreateView;

    private int index;
    public final int getIndex() {
        return index;
    }

    private static HashMap<String, Integer> commandMap = new HashMap<>();  // ← only change

    public static Map<String, Integer> getCommandsMap() {
        return commandMap;
    }

    public static boolean is(int commandType, Command command) {
        return Command.values()[commandType].getIndex() == command.getIndex();
    }

    static {
        for (int i = 0; i < Command.values().length; i++) {
            Command command = Command.values()[i];
            command.index = i;
            commandMap.put(command.name(), i);
        }
    }
}