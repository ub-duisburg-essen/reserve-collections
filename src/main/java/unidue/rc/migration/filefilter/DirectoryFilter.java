package unidue.rc.migration.filefilter;

/**
 * A <code>FileFilter</code> that accepts directories.
 * 
 * @author Marcus Koesters
 */

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter {

    @Override
    public boolean accept(File file) {

        return file.isDirectory();

    }

}
