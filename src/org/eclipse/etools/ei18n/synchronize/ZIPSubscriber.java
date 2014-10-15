package org.eclipse.etools.ei18n.synchronize;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.etools.RemoveMe;
//import org.eclipse.team.core.variants.ResourceVariantTree;
//import org.eclipse.team.core.variants.SessionResourceVariantByteStore;
import org.eclipse.team.core.variants.ResourceVariantTree;
import org.eclipse.team.core.variants.SessionResourceVariantByteStore;

@RemoveMe
public class ZIPSubscriber extends AbstractSubscriber {
    // private final IResource[] roots;

	private final ZipFile file;

	public ZIPSubscriber(IResource[] roots, File file) throws ZipException, IOException {
        super(roots);
		this.file=new ZipFile(file);
	}

    // @Override
    // protected IResourceVariantTree getBaseTree() {
    // return new ResourceVariantTree(new SessionResourceVariantByteStore()) {
    // class MyLocalResourceVariant extends LocalResourceVariant {
    // private final IResource resource;
    //
    // public MyLocalResourceVariant(IResource resource) {
    // super(resource);
    // this.resource=resource;
    // }
    // }
    // @Override
    // public IResource[] roots() {
    // return roots;
    // }
    //
    // @Override
    // public IResourceVariant getResourceVariant(IResource resource) throws TeamException {
    // if (resource.exists())
    // return new MyLocalResourceVariant(resource);
    // else
    // return null;
    // }
    //
    // @Override
    // protected IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException {
    // List<IResourceVariant> result=Lists.newArrayList();
    // IResource resource=((MyLocalResourceVariant) variant).resource;
    // if (resource instanceof IContainer) {
    // try {
    // for (IResource res : ((IContainer) resource).members()) {
    // if (isSupervised(res))
    // result.add(new MyLocalResourceVariant(res));
    // }
    // } catch (CoreException e) {
    //						Activator.log(IStatus.ERROR, "Failed to fecth member " + resource, e); //$NON-NLS-1$
    // }
    // }
    // return result.toArray(new IResourceVariant[] {});
    // }
    //
    // @Override
    // protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
    // return new MyLocalResourceVariant(resource);
    // }
    // };
    // }
    public ZipFile getFile() {
        return file;
    }

    @Override
    protected ResourceVariantTree createResourceVariantTree(SessionResourceVariantByteStore sessionResourceVariantByteStore) {
        return new ZIPResourceVariantTree(this, sessionResourceVariantByteStore);
    }

    // @Override
    // protected IResourceVariantTree getRemoteTree() {
    // return new ResourceVariantTree(new SessionResourceVariantByteStore()) {
    // class ZipEntryResourceVariant extends CachedResourceVariant {
    // private final String path;
    // private final String project;
    //
    // public ZipEntryResourceVariant(String project, String path) {
    // this.project=project;
    // this.path=path;
    // }
    //
    // @Override
    // public boolean isContainer() {
    //            return !path.endsWith(".properties"); //$NON-NLS-1$
    // }
    //
    // @Override
    // public String getName() {
    //            String result=/* "/" + project + "/" + */StringUtils.removeStart(path, "/" + project + "/"); //$NON-NLS-1$ //$NON-NLS-2$
    // return result;
    // }
    //
    // @Override
    // public String getContentIdentifier() {
    // return getCachePath();
    // }
    //
    // @Override
    // public byte[] asBytes() {
    // return getContentIdentifier().getBytes();
    // }
    //
    // @Override
    // protected String getCachePath() {
    // return path;
    // }
    //
    // @Override
    // protected String getCacheId() {
    //            return "com.cmoine.ei18n"; //$NON-NLS-1$
    // }
    //
    // @Override
    // protected void fetchContents(IProgressMonitor monitor) throws TeamException {
    // InputStream is=null;
    // try {
    // String path=getCachePath();
    // IFile iFile=null;
    // for (IResource res : roots()) {
    //                    path=StringUtils.removeStart(path, res.getFullPath().toString() + "/"); //$NON-NLS-1$
    // if (res instanceof IContainer && iFile == null) {
    // IContainer container=(IContainer) res;
    // IResource member=container.findMember(path);
    // if (member != null && member instanceof IFile) {
    // iFile=(IFile) member;
    // }
    // }
    // }
    // ZipEntry entry=file.getEntry(path);
    // if (entry != null) {
    // List<String> actualLines=Collections.emptyList();
    // if (iFile != null) {
    // InputStream stream=null;
    // try {
    // stream=iFile.getContents();
    // actualLines=Arrays.asList(StringUtils.splitPreserveAllTokens(IOUtils.toString(stream), '\n'));
    // } catch (CoreException e) {
    //                            Activator.log(IStatus.ERROR, "Failed to read " + iFile, e); //$NON-NLS-1$
    // } finally {
    // IOUtils.closeQuietly(stream);
    // }
    // }
    // is=file.getInputStream(entry);
    // List<String> lines=IOUtils.readLines(is);
    // List<String> modifiedLines=Lists.newArrayList();
    // for (String actualLine : actualLines) {
    // boolean found=false;
    // for (String line : lines) {
    // if (equals(actualLine, line)) {
    // lines.remove(line);
    // modifiedLines.add(line);
    // found=true;
    // break;
    // }
    // }
    // if (!found) {
    // modifiedLines.add(actualLine);
    // }
    // }
    // for (String line : lines) {
    //                        if (!line.trim().startsWith("#")) //$NON-NLS-1$
    // modifiedLines.add(line);
    // }
    // setContents(new ByteArrayInputStream(StringUtils.join(modifiedLines.toArray(), IOUtils.LINE_SEPARATOR).getBytes()), monitor);
    // } else {
    //                    setContents(new ByteArrayInputStream("ERROR".getBytes()), monitor); //$NON-NLS-1$
    // }
    // } catch (IOException e) {
    //                Activator.log(IStatus.ERROR, "Failed fetching member", e); //$NON-NLS-1$
    // } finally {
    // IOUtils.closeQuietly(is);
    // }
    // }
    //
    // private boolean equals(String actualLine, String line) {
    // return getKey(actualLine).equals(getKey(line));
    // }
    //
    // private String getKey(String line) {
    //            return StringUtils.substringBefore(StringUtils.substringBefore(line, ":"), "=").trim(); //$NON-NLS-1$ //$NON-NLS-2$
    // }
    // }
    //
    // @Override
    // public IResource[] roots() {
    // return getRoots();
    // }
    //
    // @Override
    // public IResourceVariant getResourceVariant(final IResource resource) throws TeamException {
    // String path=resource.getFullPath().toString();
    // return new ZipEntryResourceVariant(resource.getProject().getName(), path);
    // }
    //
    // @Override
    // protected IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException {
    // String project=((ZipEntryResourceVariant) variant).project;
    // // String path=((ZipEntryResourceVariant) variant).path;
    // List<IResourceVariant> result=Lists.newArrayList();
    // for (Enumeration<? extends ZipEntry> entries=file.entries(); entries.hasMoreElements();) {
    // ZipEntry entry=entries.nextElement();
    // String entryName=entry.getName();
    // if (isSupervised(entry.getName())) {
    // result.add(new ZipEntryResourceVariant(project, entryName));
    // }
    //
    // }
    // return result.toArray(new IResourceVariant[] {});
    // }
    //
    // @Override
    // protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
    //                return new ZipEntryResourceVariant(resource.getProject().getName(), ""); //$NON-NLS-1$
    // }
    // };
    // }

    // @Override
    // public String getName() {
    //		return "Ei18n"; //$NON-NLS-1$
    // }
    //
    // @Override
    // public boolean isSupervised(IResource resource) throws TeamException {
    // return isSupervised(resource.getName());
    // }
    //
    // public boolean isSupervised(String name) throws TeamException {
    // return EI18NConstants.PATTERN.matcher(name).matches();
    // }
    //
    // @Override
    // public IResource[] roots() {
    // return roots;
    // }
    //
    // @Override
    // public IResourceVariantComparator getResourceComparator() {
    // return new EI18NResourceVariantComparator();
    // }
}
