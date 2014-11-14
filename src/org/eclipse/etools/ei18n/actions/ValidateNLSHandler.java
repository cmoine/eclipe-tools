package org.eclipse.etools.ei18n.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.etools.Activator;
import org.eclipse.etools.SelectionUtils;
import org.eclipse.etools.ei18n.search.EI18NTextSearchResult;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.LineElement;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class ValidateNLSHandler extends AbstractHandler implements IHandler {
    public static final String LABEL="Validate NLS Messages";

    public Object execute(ExecutionEvent event) throws ExecutionException {
        //        Shell shell=HandlerUtil.getActiveShellChecked(event);
        final ISelection selection=HandlerUtil.getCurrentSelectionChecked(event);
        //        ProgressMonitorDialog dialog=new ProgressMonitorDialog(shell);
        //        try {
        // check option PREF_PB_NON_EXTERNALIZED_STRINGS
        if (checkParameters(event)) {
            NewSearchUI.runQueryInBackground(new ISearchQuery() {
                private final List<IResource> resources=SelectionUtils.getResources(selection);

                private final EI18NTextSearchResult searchResult=new EI18NTextSearchResult(this);

                public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
                    process(monitor, searchResult, resources);
                    return Status.OK_STATUS;
                }

                public String getLabel() {
                    return LABEL;
                }

                public boolean canRerun() {
                    return true;
                }

                public boolean canRunInBackground() {
                    return true;
                }

                public ISearchResult getSearchResult() {
                    return searchResult;
                }

            });
            //                dialog.run(true, true, new IRunnableWithProgress() {
            //                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            //                        
            //                });
        }
        //        } catch (InvocationTargetException e) {
        //            Activator.logError("Failed finding ", e); //$NON-NLS-1$
        //        } catch (InterruptedException e) {
        //            Activator.logError("", e); //$NON-NLS-1$
        //        }
        return null;
    }

    private void process(IProgressMonitor monitor, final EI18NTextSearchResult searchResult, List<IResource> resources) {
        for (final IResource res : resources) {
            try {
                res.accept(new IResourceVisitor() {
                    private final List<MappingPreference> list=MappingPreference.list(res.getProject());

                    public boolean visit(IResource resource) throws CoreException {
                        if (resource instanceof IFile) {
                            IFile file=(IFile) resource;
                            // Find markers
                            try {
                                DefaultLineTracker tracker=new DefaultLineTracker();
                                String content=FileUtils.readFileToString(org.eclipse.etools.FileUtils.getResource(file));
                                tracker.set(content);
                                //                                List<String> lines=FileUtils.readLines(org.eclipse.etools.FileUtils.getResource(file));
                                for (IMarker marker : file.findMarkers("org.eclipse.jdt.core.problem", false, IResource.DEPTH_ZERO)) { //$NON-NLS-1$
                                    if (((Integer) marker.getAttribute(IJavaModelMarker.ID)).intValue() == IProblem.NonExternalizedStringLiteral) {
                                        String message=(String) marker.getAttribute(IMarker.MESSAGE);
                                        int lineNumber=((Integer) marker.getAttribute(IMarker.LINE_NUMBER)).intValue();
                                        int charStart=((Integer) marker.getAttribute(IMarker.CHAR_START)).intValue();
                                        int charEnd=((Integer) marker.getAttribute(IMarker.CHAR_END)).intValue();
                                        lineNumber--;
                                        IRegion region=tracker.getLineInformation(lineNumber);
                                        searchResult.addMatch(
                                                new FileMatch(file, charStart, charEnd - charStart, new LineElement(file, lineNumber, tracker
                                                        .getLineOffset(lineNumber), content.substring(region.getOffset(),
                                                        region.getOffset() + region.getLength())))/*tracker.lines.get(lineNumber - 1)))*/, file, message);
                                        //                                    System.out.println(file.toString() + '(' + lineNumber + ')' + ':' + message);
                                    }
                                }
                            } catch (IOException e) {
                                Activator.logError("Failed reading lines " + file, e); //$NON-NLS-1$
                            } catch (BadLocationException e) {
                                Activator.logError("Bad location when reading lines " + file, e); //$NON-NLS-1$
                            }
                            // Find brokens messages
                            Comparator<IFile> comparator=new Comparator<IFile>() {
                                public int compare(IFile o1, IFile o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            };
                            Multimap<String, IFile> multimap=TreeMultimap.create(Ordering.natural(), comparator);
                            Set<IFile> fs=new TreeSet<IFile>(comparator);
                            for (final MappingPreference mapping : list) {
                                if (mapping.getJavaFile().equals(file)) {
                                    ICompilationUnit cu=(ICompilationUnit) JavaCore.create(file);
                                    for (IField field : cu.getTypes()[0].getFields()) {
                                        multimap.put(field.getElementName(), file);
                                    }
                                    final List<IFile> files=new ArrayList<IFile>();
                                    files.add(mapping.getPropertyFile());
                                    mapping.getPropertyFile().getParent().accept(new IResourceVisitor() {
                                        public boolean visit(IResource resource) throws CoreException {
                                            if (resource instanceof IFile) {
                                                Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(resource.getName());
                                                if (matcher.matches()) {
                                                    // TODO
                                                    //                                                                if (StringUtils.remove(resource.getName(), matcher.group(1)).equals(
                                                    //                                                                        mapping.getPropertyFile().getName())) {
                                                    files.add((IFile) resource);
                                                    //                                                                }
                                                }
                                            }
                                            return true;
                                        }
                                    }, IResource.DEPTH_ONE, IResource.NONE);
                                    fs.add(file);

                                    for (IFile f : files) {
                                        fs.add(f);
                                        InputStream is=null;
                                        try {
                                            is=f.getContents();
                                            Properties props=new Properties();
                                            props.load(is);
                                            for (Entry<Object, Object> entry : props.entrySet()) {
                                                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                                                    String value=(String) entry.getValue();
                                                    if (StringUtils.isNotEmpty(value)) {
                                                        String key=(String) entry.getKey();
                                                        multimap.put(key, f);
                                                    }
                                                }
                                            }
                                        } catch (IOException e) {
                                            Activator.logError("Failed reading " + mapping.getPropertyFile(), e); //$NON-NLS-1$
                                        } finally {
                                            IOUtils.closeQuietly(is);
                                        }
                                    }
                                }
                            }
                            for (String key : multimap.keySet()) {
                                Collection<IFile> files=multimap.get(key);
                                if (files.size() != fs.size()) {
                                    Set<IFile> copy=new TreeSet<IFile>(comparator);
                                    copy.addAll(fs);
                                    copy.removeAll(files);
                                    for (IFile f : copy) {
                                        searchResult.addMatch(new FileMatch(f, 0, 0, new LineElement(f, 0, 0, " ")), f, "Key " + key + " is missing");
                                    }
                                    //                                    System.out.println("Key " + key + " is missing from " + Joiner.on(" and ").join(copy)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                }
                            }
                        }
                        return true;
                    }
                });
            } catch (CoreException e) {
                Activator.logError("Failed visiting " + res, e); //$NON-NLS-1$
            }
        }
    }

    //    private LineElement getLineElement(int offset, TextSearchMatchAccess matchRequestor) {
    //        int lineNumber=1;
    //        int lineStart=0;
    //        //        if (!fCachedMatches.isEmpty()) {
    //        //            // match on same line as last?
    //        //            FileMatch last= (FileMatch) fCachedMatches.get(fCachedMatches.size() - 1);
    //        //            LineElement lineElement= last.getLineElement();
    //        //            if (lineElement.contains(offset)) {
    //        //                return lineElement;
    //        //            }
    //        //            // start with the offset and line information from the last match
    //        //            lineStart= lineElement.getOffset() + lineElement.getLength();
    //        //            lineNumber= lineElement.getLine() + 1;
    //        //        }
    //        if (offset < lineStart) {
    //            return null; // offset before the last line
    //        }
    //
    //        int i=lineStart;
    //        int contentLength=matchRequestor.getFileContentLength();
    //        while (i < contentLength) {
    //            char ch=matchRequestor.getFileContentChar(i++);
    //            if (ch == '\n' || ch == '\r') {
    //                if (ch == '\r' && i < contentLength && matchRequestor.getFileContentChar(i) == '\n') {
    //                    i++;
    //                }
    //                if (offset < i) {
    //                    String lineContent=getContents(matchRequestor, lineStart, i); // include line delimiter
    //                    return new LineElement(matchRequestor.getFile(), lineNumber, lineStart, lineContent);
    //                }
    //                lineNumber++;
    //                lineStart=i;
    //            }
    //        }
    //        if (offset < i) {
    //            String lineContent=getContents(matchRequestor, lineStart, i); // until end of file
    //            return new LineElement(matchRequestor.getFile(), lineNumber, lineStart, lineContent);
    //        }
    //        return null; // offset outside of range
    //    }
    //
    //    private static String getContents(TextSearchMatchAccess matchRequestor, int start, int end) {
    //        StringBuffer buf=new StringBuffer();
    //        for (int i=start; i < end; i++) {
    //            char ch=matchRequestor.getFileContentChar(i);
    //            if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
    //                buf.append(' ');
    //            } else {
    //                buf.append(ch);
    //            }
    //        }
    //        return buf.toString();
    //    }

    //    private class ProjectSubsetBuildAction extends BuildAction {
    //        private IProject[] projectsToBuild=new IProject[0];
    //
    //        public ProjectSubsetBuildAction(IShellProvider shellProvider, int type, IProject[] projects) {
    //            super(shellProvider, type);
    //            this.projectsToBuild=projects;
    //        }
    //
    //        @Override
    //        protected List getSelectedResources() {
    //            return Arrays.asList(this.projectsToBuild);
    //        }
    //    }

    private boolean checkParameters(ExecutionEvent event) throws ExecutionException {
        //        final MutableObject<Key> myKey=new MutableObject<Key>();
        //        for (Key key : ProblemSeveritiesConfigurationBlock.getKeys()) {
        //            if (key.getQualifier().equals(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL)) {
        //                myKey.setValue(key);
        //            }
        //        }
        Object object=JavaCore.getOptions().get(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL);
        if ("ignore".equals(object)) { //$NON-NLS-1$
            //            Display.getDefault().asyncExec(new Runnable() {
            //                @Override
            //                public void run() {
            Shell shell=PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
            if (!MessageDialog.openQuestion(shell, "Question", "You must enable a JDT option, whould you like to set it automatically ?"))
                return false;

            //            System.out.println("Activator.checkParameters().new Runnable() {...}.run()");
            Hashtable newOptions=JavaCore.getOptions();
            newOptions.put(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL, "warning");
            JavaCore.setOptions(newOptions);
            Set<IProject> projects=new HashSet<IProject>();
            for (IResource res : SelectionUtils.getResources(HandlerUtil.getCurrentSelectionChecked(event))) {
                projects.add(res.getProject());
            }
            for (IProject project : projects) {
                try {
                    project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
                } catch (CoreException e) {
                    Activator.logError("Failed to rebuild " + project, e); //$NON-NLS-1$
                }
            }

            //            ProjectSubsetBuildAction projectBuild=new ProjectSubsetBuildAction(HandlerUtil.getActiveWorkbenchWindowChecked(event),
            //                    IncrementalProjectBuilder.INCREMENTAL_BUILD, projects.toArray(new IProject[projects.size()]));
            //            projectBuild.runInBackground(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
            //
            //            while (Job.getJobManager().currentJob() != null) {
            //                try {
            //                    System.out.println("ValidateNLSHandler.checkParameters()");
            //                    Thread.sleep(DateUtils.MILLIS_PER_SECOND);
            //                } catch (InterruptedException e) {
            //                    Activator.logInfo("", e); //$NON-NLS-1$
            //                }
            //            }

            //            PreferencesUtil.createPreferenceDialogOn(shell, ProblemSeveritiesPreferencePage.PREF_ID, null, null).open();
            //                }
            //            });
        }
        return true;
    }
}
