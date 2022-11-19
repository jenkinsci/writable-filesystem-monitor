package org.jenkinsci.plugins.writablefilesystem_monitor;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.node_monitors.AbstractNodeMonitorDescriptor;
import hudson.node_monitors.NodeMonitor;
import hudson.remoting.Callable;
import hudson.slaves.OfflineCause;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;

public class RWFSMonitor extends NodeMonitor {

    @DataBoundConstructor
    public RWFSMonitor() {
    }

    @Extension
    public static final AbstractNodeMonitorDescriptor<Boolean> DESCRIPTOR = new AbstractNodeMonitorDescriptor<Boolean>() {
        public String getDisplayName() {
            return "FileSystem";
        }

        @Override
        protected Boolean monitor(Computer c) throws IOException, InterruptedException {
            FilePath p = c.getNode().getRootPath();
            if(p==null) return null;
            Boolean rw = p.act(new CanWrite());
            if (!rw.booleanValue()) {
                markOffline(c, new ReadOnlyFileSystem());
            }
            return rw;
        }

    };

    protected static final class CanWrite implements Callable<Boolean, IOException> {

        public Boolean call() throws IOException {
            File f = Files.createTempFile("monitor", "empty").toFile();
            f.delete();
            return TRUE;
        }
        private static final long serialVersionUID = 1L;
    }

    public static final class ReadOnlyFileSystem extends OfflineCause implements Serializable {

        public String toString() {
            return "Remote filesystem is read-only.";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(RWFSMonitor.class.getName());
}

