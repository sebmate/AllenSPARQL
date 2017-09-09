package de.unierlangen.med.imi.allensparql;

// Based on http://www.rgagnon.com/javadetails/java-0490.html
import java.util.*;
import java.io.*;

public abstract class FileWatcher extends TimerTask {

    private long timeStamp;
    private File file;

    public FileWatcher(File file) {
        this.file = file;
        this.timeStamp = file.lastModified();
    }

    public final void run() {
        long timeStamp = file.lastModified();

        if (this.timeStamp != timeStamp) {
            this.timeStamp = timeStamp;
            onChange(file);
        }
    }

    protected abstract void onChange(File file);
}
