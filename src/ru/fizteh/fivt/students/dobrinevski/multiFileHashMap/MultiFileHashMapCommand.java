package ru.fizteh.fivt.students.dobrinevski.multiFileHashMap;

import ru.fizteh.fivt.students.dobrinevski.shell.Command;

import java.io.File;

public abstract class MultiFileHashMapCommand extends Command {
    MyMultiHashMap parent;
    File root;

    MultiFileHashMapCommand(int argCount, MyMultiHashMap prnt, File realRoot) {
        super(argCount);
        parent = prnt;
        root = realRoot;
    }
}
