package org.eclipse.etools.ei18n.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ResourceVariantByteStore;
import org.eclipse.team.core.variants.ResourceVariantTree;

abstract class AbstractResourceVariantTree extends ResourceVariantTree {
    private final AbstractSubscriber subscriber;

    protected AbstractResourceVariantTree(AbstractSubscriber subscriber, ResourceVariantByteStore store) {
        super(store);
        this.subscriber=subscriber;
    }

    public AbstractSubscriber getSubscriber() {
        return subscriber;
    }


    public IResource[] roots() {
        return subscriber.roots();
    }


    public IResourceVariant getResourceVariant(final IResource resource) throws TeamException {
        String path=resource.getFullPath().toString();
        return createResourceVariant(resource.getProject().getName(), path);
    }

    protected abstract AbstractResourceVariant createResourceVariant(String name, String path);


    @Override
    protected IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException {
        return ((AbstractResourceVariant) variant).list();
    }


    @Override
    protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
        String path=resource.getFullPath().toString();
        return createResourceVariant(resource.getProject().getName(), path);
    }

}
