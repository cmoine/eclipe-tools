package org.eclipse.etools.ei18n.builder;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.ei18n.markers.IMarkerConstants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class I18NBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID="com.cmoine.ei18n.I18NBuilder"; //$NON-NLS-1$

    //    private abstract class CommonVisitor implements IResourceVisitor {
    //		@Override
    //		public final boolean visit(IResource resource) {
    //			if (resource instanceof IFolder && resource.getName().equals("bin")) //$NON-NLS-1$
    //				return false;
    //			if (resource instanceof IFile)
    //				visit((IFile) resource);
    //
    //			return true;
    //		}
    //
    //		public abstract void visit(IFile file);
    //	}

    //	private class CounterResourceVisitor extends CommonVisitor {
    //        @Override
    //		public void visit(IFile file) {
    //            Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(file.getName());
    //			if (matcher.matches()) {
    //				String locale=matcher.group(1);
    //				locales.add(locale);
    //			}
    //		}
    //	}
    //
    //	private final CounterResourceVisitor counterVisitor=new CounterResourceVisitor();
    //
    //	private class EI18NResourceVisitor extends CommonVisitor {
    //		@Override
    //		public void visit(IFile file) {
    //			String[] filesToIgnore=Activator.getDefault().getPreferenceStore().getString(EI18NPreferenceConstants.FILES_TO_IGNORE_STR).split(","); //$NON-NLS-1$
    //			String fileName=file.getName();
    //            if ((!ArrayUtils.contains(filesToIgnore, fileName)) && EI18NConstants.PATTERN.matcher(fileName).matches()) {
    //				deleteMarkers(file);
    ////				InputStream stream=null;
    //				try {
    //                    LineProperties properties=new LineProperties(file);
    //                    // Properties properties=new Properties();
    //                    // properties.load(stream);
    //                    // List<String> oKeys=listPropertiesKeys(new File(file.getLocationURI()));
    //                    for (String key : properties) {
    //						String message=properties.getProperty(key);
    //						String strMessage=StringUtils.trimToEmpty(message);
    //						Collection<IFile> files=MESSAGES.get(strMessage);
    //						boolean found=false;
    //						if (files != null) {
    //							for (IFile otherFile : files) {
    //								if (!otherFile.equals(file)) {
    //									addMarker(file, "Found duplicate message " + key + "='" + message + "' in '" + otherFile.getFullPath() + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    //                                            properties.getLineNumber(key), IMarker.SEVERITY_WARNING, null);
    //								} else
    //									found=true;
    //							}
    //						}
    //						if (!found)
    //							MESSAGES.put(strMessage, file);
    //					}
    //
    //                    Pattern pattern=Pattern.compile(file.getName() + EI18NConstants.SUFFIX_LOCALE_PATTERN.pattern());
    //					List<String> ls=Lists.newArrayList(locales);
    //					for (IResource res : file.getParent().members()) {
    //						Matcher matcher=pattern.matcher(res.getName());
    //						if (res instanceof IFile && matcher.matches()) {
    //							String locale=matcher.group(1);
    //							ls.remove(locale);
    ////							Properties p=new Properties();
    ////							InputStream s=null;
    //							try {
    //								// deleteMarkers((IFile) res);
    //								LineProperties p=new LineProperties((IFile) res);
    //
    //								// Compare here
    //								List<String> keys=Lists.newArrayList(p);
    //								for (String key : properties) {
    //									String v1=StringUtils.defaultString(properties.getProperty(key));
    //									String v2=StringUtils.defaultString(p.getProperty(key));
    //									try {
    //										if (StringUtils.isNotEmpty(v1) && ((StringUtils.isEmpty(v2)) | (!keys.remove(key)))) { // FIXME use an iterator.remove
    //											// instead
    //											addMarker(file, "Missing key '" + key + "' for locale '" + locale + "'", properties.getLineNumber(key), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //													IMarker.SEVERITY_WARNING, null);
    //										} else if (new MessageFormat(v1).getFormats().length != new MessageFormat(v2).getFormats().length) {
    //											addMarker(file, "Number of arguments does not match for key '" + key + "' for locale '" + locale + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //											        properties.getLineNumber(key), IMarker.SEVERITY_ERROR, null);
    //										}
    //									} catch (IllegalArgumentException e) {
    //										addMarker(file, e.getMessage(), properties.getLineNumber(key), IMarker.SEVERITY_ERROR, null);
    //									}
    //								}
    //								for (Object key : keys) {
    //									addMarker((IFile) res, "Key '" + key + "' is unused.", 0, IMarker.SEVERITY_WARNING, null); //$NON-NLS-1$ //$NON-NLS-2$
    //								}
    //                            } catch (Exception e) {
    //								Activator.log(IStatus.ERROR, "Failed to read " + res, e); //$NON-NLS-1$
    //                                // } finally {
    //                                // IOUtils.closeQuietly(s);
    //							}
    //						}
    //					}
    //					for (String locale : ls) {
    //						addMarker(file, "Missing locale '" + locale + "'", 0, IMarker.SEVERITY_ERROR, locale); //$NON-NLS-1$ //$NON-NLS-2$
    //					}
    //				} catch (CoreException e) {
    //					Activator.log(IStatus.ERROR, "Failed to list " + file.getParent(), e); //$NON-NLS-1$
    //				} catch (IOException e) {
    //					Activator.log(IStatus.ERROR, "Failed to read " + file, e); //$NON-NLS-1$
    //                } catch (BadLocationException e) {
    //                    Activator.log(IStatus.ERROR, "Failed to parse " + file, e); //$NON-NLS-1$
    ////				} finally {
    ////					IOUtils.closeQuietly(stream);
    //                }
    //			}
    //		}
    //	}
    //
    //	private final EI18NResourceVisitor visitor=new EI18NResourceVisitor();

	private final Set<String> locales=Sets.newHashSet();
	private static final Multimap<String, IFile> MESSAGES=HashMultimap.create();

	private void addMarker(IFile file, String message, int lineNumber, int severity, String locale) {
		try {
			IMarker marker = file.createMarker(IMarkerConstants.MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarkerConstants.LOCALE_ATT, locale);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		locales.clear();
		// messages.clear();
        // TODO CME
        //		getProject().accept(counterVisitor, IResource.DEPTH_INFINITE, false);
        //		getProject().accept(visitor);
		return null;
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(IMarkerConstants.MARKER_TYPE, false, IResource.DEPTH_ONE);
		} catch (CoreException ce) {
		}
	}

    // private List<String> listPropertiesKeys(File file){
    // try {
    // List<String> lines = FileUtils.readLines(file);
    // List<String> properties = Lists.newArrayList();
    // for(String property : lines){
    //				if(property.isEmpty() | property.trim().startsWith("#")){ //$NON-NLS-1$
    // properties.add(null);
    // }else{
    //					properties.add(StringUtils.substringBefore(property, "=")); //$NON-NLS-1$
    // }
    // }
    // return properties;
    // } catch (IOException e) {
    //			Activator.log(IStatus.ERROR, "Failed to read " + file, e); //$NON-NLS-1$
    // }
    // return Collections.emptyList();
    // }
}
