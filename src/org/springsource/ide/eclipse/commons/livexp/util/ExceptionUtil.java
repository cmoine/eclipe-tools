/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.etools.Activator;

/**
 * Utility methods to convert exceptions into other types of exceptions, status
 * objects etc.
 *
 * @author Kris De Volder
 */
public class ExceptionUtil {

	public static CoreException coreException(int severity, String msg) {
		return coreException(status(severity, msg));
	}

	public static CoreException coreException(IStatus status) {
		if (status==null) {
			return coreException("Null status?");
		}
		Throwable e = status.getException();
		if (e == null) {
			return new CoreException(status);
		}
		else if (e instanceof CoreException) {
			return (CoreException) e;
		}
		return new CoreException(status);
	}

	public static CoreException coreException(String msg) {
		return coreException(IStatus.ERROR, msg);
	}

	public static Throwable coreException(String msg, Throwable error) {
		return new CoreException(status(error, msg));
	}

	public static CoreException coreException(Throwable e) {
		if (e instanceof CoreException) {
			return (CoreException) e;
		} else {
			return coreException(status(e));
		}
	}

	public static Throwable getDeepestCause(Throwable e) {
		Throwable cause = e;
		Throwable parent = e.getCause();
		while (parent != null && parent != e) {
			cause = parent;
			parent = cause.getCause();
		}
		return cause;
	}

	public static String getMessage(Throwable e) {
		// The message of nested exception is usually more interesting than the
		// one on top.
		Throwable cause = getDeepestCause(e);
		String msg = cause.getClass().getSimpleName() + ": " + cause.getMessage();
		return msg;
	}

	public static IllegalStateException notImplemented(String string) {
		return new IllegalStateException("Not implemented: " + string);
	}

	public static IStatus status(int severity, String msg) {
		return new Status(severity, Activator.PLUGIN_ID, msg);
	}

	public static IStatus status(Throwable e, String message) {
		if (message==null) {
			return status(e);
		}
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e);
	}

	public static boolean isCancelation(Throwable e) {
		return (
				e instanceof OperationCanceledException ||
				e instanceof InterruptedException ||
				e instanceof CancellationException ||
				(
						e instanceof CoreException &&
						((CoreException)e).getStatus().getSeverity()==IStatus.CANCEL
				)
		);
	}

	public static IStatus status(Throwable e) {
		if (isCancelation(e)) {
			return Status.CANCEL_STATUS;
		}
		return status(IStatus.ERROR, e);
	}

	public static IStatus status(int severity, Throwable e) {
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			if (status != null && status.getSeverity() == severity) {
				Throwable ee = status.getException();
				if (ee != null) {
					return status;
				}
			}
		}
		return new Status(severity, Activator.PLUGIN_ID, getMessage(e), e);
	}

	public static IStatus status(String msg) {
		return status(IStatus.ERROR, msg);
	}

	public static final IStatus OK_STATUS = status(IStatus.OK, "");

	public static Exception exception(Throwable e) {
		if (e instanceof Exception) {
			return (Exception)e;
		} else {
			return coreException(e);
		}
	}

	public static RuntimeException unchecked(Exception e) {
		return new RuntimeException(e);
	}

	public static String stacktrace() {
		return stacktrace(new Exception("Stacktrace"));
	}

	public static String stacktrace(Exception exception) {
		ByteArrayOutputStream dump = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(dump);
		try {
			exception.printStackTrace(out);
		} finally {
			out.close();
		}
		return dump.toString();
	}

}
