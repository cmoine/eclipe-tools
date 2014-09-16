package org.eclipse.etools.ei18n.synchronize;

import org.eclipse.team.core.variants.ResourceVariantByteStore;

public class XLSResourceVariantTree extends AbstractResourceVariantTree {
    protected XLSResourceVariantTree(AbstractSubscriber subscriber, ResourceVariantByteStore store) {
        super(subscriber, store);
    }

    @Override
    protected AbstractResourceVariant createResourceVariant(String name, String path) {
        return new XLSResourceVariant((XLSSubscriber) getSubscriber(), path);
    }
}
