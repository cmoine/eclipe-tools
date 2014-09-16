package org.eclipse.etools.ei18n.synchronize;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.core.variants.IResourceVariantTree;
import org.eclipse.team.core.variants.ResourceVariantTree;
import org.eclipse.team.core.variants.ResourceVariantTreeSubscriber;
import org.eclipse.team.core.variants.SessionResourceVariantByteStore;
import org.eclipse.team.internal.core.mapping.LocalResourceVariant;

import com.google.common.collect.Lists;

abstract class AbstractSubscriber extends ResourceVariantTreeSubscriber {

	private final IResource[] roots;

    public AbstractSubscriber(IResource[] roots) throws ZipException, IOException {
		this.roots=roots;
	}


	@Override
    protected IResourceVariantTree getBaseTree() {
		return new ResourceVariantTree(new SessionResourceVariantByteStore()) {
			class MyLocalResourceVariant extends LocalResourceVariant {
				private final IResource resource;

				public MyLocalResourceVariant(IResource resource) {
					super(resource);
					this.resource=resource;
				}
			}


			public IResource[] roots() {
				return roots;
			}


			public IResourceVariant getResourceVariant(IResource resource) throws TeamException {
				if (resource.exists())
					return new MyLocalResourceVariant(resource);
				else
					return null;
			}


			@Override
            protected IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException {
				List<IResourceVariant> result=Lists.newArrayList();
				IResource resource=((MyLocalResourceVariant) variant).resource;
				if (resource instanceof IContainer) {
					try {
						for (IResource res : ((IContainer) resource).members()) {
                            if (isSupervised(res))
                                result.add(new MyLocalResourceVariant(res));
						}
					} catch (CoreException e) {
						Activator.log(IStatus.ERROR, "Failed to fecth member " + resource, e); //$NON-NLS-1$
					}
				}
				return result.toArray(new IResourceVariant[] {});
			}


			@Override
            protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
				return new MyLocalResourceVariant(resource);
			}
		};
	}


    @Override
    protected IResourceVariantTree getRemoteTree() {
        return createResourceVariantTree(new SessionResourceVariantByteStore());
    }

    protected abstract ResourceVariantTree createResourceVariantTree(SessionResourceVariantByteStore sessionResourceVariantByteStore);


	@Override
    public String getName() {
		return "Ei18n"; //$NON-NLS-1$
	}


	@Override
    public boolean isSupervised(IResource resource) throws TeamException {
		return isSupervised(resource.getName());
	}

	public boolean isSupervised(String name) throws TeamException {
        return EI18NConstants.PATTERN.matcher(name).matches() || EI18NConstants.LOCALE_PATTERN.matcher(name).matches();
	}


	@Override
    public IResource[] roots() {
		return roots;
	}


	@Override
    public IResourceVariantComparator getResourceComparator() {
		return new EI18NResourceVariantComparator();
	}
}
