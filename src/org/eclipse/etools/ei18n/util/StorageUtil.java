package org.eclipse.etools.ei18n.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IPackageFragment;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class StorageUtil {
    private StorageUtil() {
    }

    public static Charset getCharset(IStorage storage) throws CoreException {
        if (storage instanceof IFile)
            return Charset.forName(((IFile) storage).getCharset(true));

        return Charsets.UTF_8;
    }

    public static Collection<IStorage> members(IStorage storage) throws CoreException {
        if (storage instanceof IFile) {
            return Lists.newArrayList(Iterables.filter(Arrays.asList(((IFile) storage).getParent().members()), IStorage.class));
        }
        if (storage instanceof IJarEntryResource) {
            Object parent=((IJarEntryResource) storage).getParent();
            if (parent instanceof IPackageFragment) {
                IPackageFragment packageFragment=(IPackageFragment) parent;
                //                IPackageFragmentRoot root=(IPackageFragmentRoot) packageFragment.getParent();
                //                String bundleName=StringUtils.substringBefore(root.getPath().lastSegment(), "_"); //$NON-NLS-1$
                //                Bundle bundle=Platform.getBundle(bundleName);

                ArrayList<IStorage> result=Lists.newArrayList(Iterables.filter(Arrays.asList(packageFragment.getNonJavaResources()), IStorage.class));
                //                if (bundle != null) {
                //                    for (Bundle fragment : TargetPlatform.getFragments(bundle)) {
                //                        JavaCore.create(fragment.get)
                //                    }
                //                }

                return result;
            }
        }
        return null;
    }
}
