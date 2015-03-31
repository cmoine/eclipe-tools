package org.eclipse.etools.ei18n.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.ei18n.extensions.ImpexExtension;
import org.eclipse.etools.ei18n.extensions.ImpexExtensionManager;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EI18NFile {
	private final Map<String, String[]> input;

	public EI18NFile(File file) throws ZipException, IOException {
		ZipFile zipFile=new ZipFile(file);

		locales=populateLocales(zipFile);
		input=Maps.newLinkedHashMap();

		for (Enumeration<? extends ZipEntry> entries=zipFile.entries(); entries.hasMoreElements();) {
			ZipEntry entry=entries.nextElement();
			Properties props=new Properties();
			InputStream inputStream=zipFile.getInputStream(entry);
			props.load(inputStream);
			IOUtils.closeQuietly(inputStream);
			int i=0;
			String locale=getLocale(entry);
			if (locale != null) {
				i=locales.indexOf(locale) + 1;
			}
			for (Object key : props.keySet()) {
				String value=props.getProperty((String) key);
				String mapKey=StringUtils.replace(entry.getName(), locale == null ? StringUtils.EMPTY : "_" + locale, StringUtils.EMPTY) + "#" + key; //$NON-NLS-1$ //$NON-NLS-2$
				String[] values=input.get(mapKey);
				if (values == null) {
					values=new String[locales.size() + 1];
					input.put(mapKey, values);
				}
				values[i]=value;
			}
		}
	}

	public Object getInput() {
		return input.keySet();
	}

	public List<String> getLocales() {
		return locales;
	}

	private final List<String> locales;

	private List<String> populateLocales(ZipFile zipFile) {
		Set<String> locales=Sets.newHashSet();
		for (Enumeration<? extends ZipEntry> entries=zipFile.entries(); entries.hasMoreElements();) {
			ZipEntry entry=entries.nextElement();
			String locale=getLocale(entry);
			if (locale != null) {
				locales.add(locale);
			}
		}
		return ImmutableList.copyOf(locales);
	}

	protected String getLocale(ZipEntry entry) {
        Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(StringUtils.substringAfterLast(entry.getName(), "/")); //$NON-NLS-1$
		if (matcher.matches())
            return matcher.group(EI18NConstants.LOCALE_GROUP);

		return null;
	}

	public void setValue(String key, String property, String value) {
		input.get(key)[locales.indexOf(property) + 1]=value;
	}

	public String getValue(Object key, int index) {
		return input.get(key)[index];
	}

	public int getLocale(String property) {
		return locales.indexOf(property) + 1;
	}

	public void saveTo(File file, IProgressMonitor monitor) throws IOException {
		ZipOutputStream out=null;
		try {
			Map<String, Properties> result=getProperties();
			monitor.beginTask("Write " + file, result.keySet().size()); //$NON-NLS-1$
			out=new ZipOutputStream(new FileOutputStream(file));
			for (String key : result.keySet()) {
				Properties props=result.get(key);
				out.putNextEntry(new ZipEntry(key));
				props.store(out, StringUtils.EMPTY);
				out.closeEntry();
				monitor.worked(1);
			}
		} finally {
			IOUtils.closeQuietly(out);
			monitor.done();
		}
	}

	public Map<String, Properties> getProperties() {
		Map<String, Properties> result=Maps.newHashMap();
		for (String key : input.keySet()) {
			String[] strs=input.get(key);
			for (int i=0; i < strs.length; i++) {
				String entryName=StringUtils.substringBefore(key, "#"); //$NON-NLS-1$
				if (i > 0)
					entryName=StringUtils.removeEndIgnoreCase(entryName, ".properties") + "_" + locales.get(i - 1) + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				Properties props=result.get(entryName);
				if (props == null) {
					props=new Properties();
					result.put(entryName, props);
				}
				props.put(StringUtils.substringAfter(key, "#"), StringUtils.defaultString(strs[i])); //$NON-NLS-1$
			}
		}
		return result;
	}

	public static File promptFile(Shell shell) {
		FileDialog dialog=new FileDialog(shell);
        String extensions=Joiner.on(';').join(
                Iterables.transform(ImpexExtensionManager.getInstance().getApplications(), new Function<ImpexExtension, String>() {
                    public String apply(ImpexExtension ext) {
                        return "*." + ext.getFileExtension(); //$NON-NLS-1$
                    }
                }));
        dialog.setFilterExtensions(new String[] { extensions, "*.*" }); //$NON-NLS-1$ 
		String filePath=dialog.open();
		if (filePath != null) {
			return new File(filePath);
		}
		return null;
	}

	public String[] getValues(String key) {
		return input.get(key);
	}
}
