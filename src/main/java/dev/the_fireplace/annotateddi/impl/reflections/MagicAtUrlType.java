package dev.the_fireplace.annotateddi.impl.reflections;

import org.reflections.vfs.Vfs;

import java.net.URL;
import java.util.Collections;

/**
 * I have no idea what this URL prefix is meant to indicate, but since it never seems to provide any actual URL, it should just be ignored so we don't crash
 */
public final class MagicAtUrlType implements Vfs.UrlType {
    @Override
    public boolean matches(URL url) throws Exception {
        return url.getProtocol().equals("magic-at");
    }

    @Override
    public Vfs.Dir createDir(URL url) throws Exception {
        return new Vfs.Dir() {
            @Override
            public String getPath() {
                return url.getPath();
            }

            @Override
            public Iterable<Vfs.File> getFiles() {
                return Collections.emptySet();
            }

            @Override
            public void close() {

            }
        };
    }
}
