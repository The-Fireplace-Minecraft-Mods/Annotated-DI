package dev.the_fireplace.annotateddi.impl;

import net.fabricmc.loader.impl.util.UrlConversionException;

import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UrlUtil
{
    public static URL getSource(String filename, URL resourceURL) throws Exception {
        URL codeSourceURL;

        try {
            URLConnection connection = resourceURL.openConnection();

            if (connection instanceof JarURLConnection) {
                codeSourceURL = ((JarURLConnection) connection).getJarFileURL();
            } else {
                String path = resourceURL.getPath();

                if (path.endsWith(filename)) {
                    codeSourceURL = new URL(resourceURL.getProtocol(), resourceURL.getHost(), resourceURL.getPort(), path.substring(0, path.length() - filename.length()));
                } else {
                    throw new UrlConversionException("Could not figure out code source for file '" + filename + "' and URL '" + resourceURL + "'!");
                }
            }
        } catch (Exception e) {
            throw new UrlConversionException(e);
        }

        return codeSourceURL;
    }

    public static Path asPath(URL url) throws URISyntaxException {
        return Paths.get(url.toURI());
    }
}
