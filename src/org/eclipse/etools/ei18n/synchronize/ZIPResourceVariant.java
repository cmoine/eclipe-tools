package org.eclipse.etools.ei18n.synchronize;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

import org.eclipse.etools.RemoveMe;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

import com.google.common.collect.Lists;

@RemoveMe
public class ZIPResourceVariant extends AbstractResourceVariant {
    private final ZIPSubscriber subscriber;

    public ZIPResourceVariant(ZIPSubscriber subscriber, String path) {
        super(subscriber, path);
        this.subscriber=subscriber;
    }

    @Override
    protected InputStream createInputStream() throws IOException {
        ZipEntry entry=subscriber.getFile().getEntry(getPath());
        return entry == null ? null : subscriber.getFile().getInputStream(entry);
    }

    @Override
    public IResourceVariant[] list() throws TeamException {
        List<ZIPResourceVariant> result=Lists.newArrayList();
        for (Enumeration<? extends ZipEntry> entries=subscriber.getFile().entries(); entries.hasMoreElements();) {
            ZipEntry entry=entries.nextElement();
            String entryName=entry.getName();
            if (subscriber.isSupervised(entry.getName())) {
                result.add(new ZIPResourceVariant(subscriber, entryName));
            }

        }
        return result.toArray(new IResourceVariant[] {});
    }
}
