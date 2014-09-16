package org.eclipse.etools.ei18n.synchronize;


import org.eclipse.team.core.variants.ResourceVariantByteStore;

public class ZIPResourceVariantTree extends AbstractResourceVariantTree {
    // private final ZipFile file;

    protected ZIPResourceVariantTree(ZIPSubscriber subscriber, ResourceVariantByteStore store) {
        super(subscriber, store);
        // this.file=file;
    }

    @Override
    protected AbstractResourceVariant createResourceVariant(String name, String path) {
        return new ZIPResourceVariant((ZIPSubscriber) getSubscriber(), path);
    }
}
