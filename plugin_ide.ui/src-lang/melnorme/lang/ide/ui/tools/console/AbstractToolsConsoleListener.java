/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.ui.tools.console;

import static melnorme.utilbox.core.CoreUtil.array;

import java.io.IOException;
import java.util.List;

import melnorme.lang.ide.core.operations.DaemonEnginePreferences;
import melnorme.lang.ide.core.operations.ILangOperationsListener;
import melnorme.lang.ide.ui.utils.ConsoleUtils;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessNotifyingHelper;
import melnorme.utilbox.process.ExternalProcessNotifyingHelper.IProcessOutputListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IOConsoleOutputStream;


public abstract class AbstractToolsConsoleListener implements ILangOperationsListener {
	
	public ToolsConsole getOrRecreateMessageConsole(String name, boolean recreateConsole) {
		ToolsConsole console = ConsoleUtils.findConsole(name, ToolsConsole.class);
		if(console != null) {
			if(!recreateConsole) {
				return console;
			}
			
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(array(console));
		}
		// create a new one
		console = createConsole(name);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(array(console));
		return console;
	}
	
	public AbstractToolsConsoleListener() {
		super();
	}
	
	protected ToolsConsole getOperationConsole(IProject project, boolean clearConsole) {
		String operationConsoleName = getOperationConsoleName(project);
		// We recreate a message console to have a clear console. 
		// console.clearConsole() is not used because of poor concurrency behavior: if more than one cleanConsole
		// is requested per a console lifetime, these aditional clears may appear out of order with regards
		// to input written to the console output streams.
		// since org.eclipse.ui.console_3.5.200.v20130514-0954
		return getOrRecreateMessageConsole(operationConsoleName, clearConsole);
	}
	
	protected abstract String getOperationConsoleName(IProject project);
	
	protected abstract ToolsConsole createConsole(String name);
	
	protected String getProjectNameSuffix(IProject project) {
		if(project == null) {
			return " (Global)";
		}
		return " ["+ project.getName() +"]";
	}
	
	/* -----------------  ----------------- */
	
	protected static void default_printProcessStartResult(IOConsoleOutputStream outStream, String prefix, 
			ProcessBuilder pb, CommonException ce) {
		List<String> commandLine = pb.command();
		String text = prefix + StringUtil.collToString(commandLine, " ") + "\n";
		
		if(ce != null) {
			text += "  FAILED: " + ce.getMessage();
			Throwable cause = ce.getCause();
			if(cause != null) {
				text += "   Reason: " + cause.getMessage() + "\n";
			}
		}
		
		try {
			outStream.write(text);
		} catch (IOException e) {
			// Do nothing
		}
	}
	
	public class ProcessUIConsoleHandler {
		
		protected ProcessBuilder pb;
		protected IProject project;
		protected String prefixText;
		protected ExternalProcessNotifyingHelper processHelper;
		protected CommonException ce;
		protected boolean clearConsole = false;
		
		public void handle(ProcessBuilder pb, IProject project, String prefixText, boolean clearConsole,
				ExternalProcessNotifyingHelper processHelper, CommonException ce) {
			init(pb, project, prefixText, clearConsole, processHelper, ce).handle();
		}
		
		public ProcessUIConsoleHandler init(ProcessBuilder pb, IProject project, String prefixText, 
				boolean clearConsole, ExternalProcessNotifyingHelper processHelper, CommonException ce) {
			this.pb = pb;
			this.project = project;
			this.prefixText = prefixText;
			this.processHelper = processHelper;
			this.ce = ce;
			
			this.clearConsole = clearConsole;
			
			return this;
		}
		
		public void handle() {
			ToolsConsole console = getConsole();
			handle(console);
		}
		
		public void handle(ToolsConsole console) {
			printProcessStartResult(console.infoOut);
			
			if(processHelper != null) {
				processHelper.getOutputListenersHelper().addListener(createOutputListener(console));
			}
		}
		
		protected void printProcessStartResult(IOConsoleOutputStream outStream) {
			default_printProcessStartResult(outStream, prefixText, pb, ce);
		}
		
		protected ToolsConsole getConsole() {
			return getOperationConsole(project, clearConsole);
		}
		
		protected IProcessOutputListener createOutputListener(final ToolsConsole console) {
			return new ConsoleOutputProcessListener(console.stdOut, console.stdErr) {
				@Override
				public void notifyProcessTerminatedAndRead(int exitCode) {
					super.notifyProcessTerminatedAndRead(exitCode);
					handleProcessTerminated(console, exitCode);
				}
			};
		}
		
		protected void handleProcessTerminated(ToolsConsole console, int exitCode) {
			try {
				console.infoOut.write(getProcessTerminatedMessage(exitCode));
				console.infoOut.flush();
			} catch (IOException e) {
				// Ignore
			}
		}
		
		protected String getProcessTerminatedMessage(int exitCode) {
			return "  ^^^ Terminated, exit code: " + exitCode +  " ^^^\n";
		}
		
	}
	
	@Override
	public void handleProcessStartResult(ProcessBuilder pb, IProject project,
			ExternalProcessNotifyingHelper processHelper, CommonException ce) {
		new ProcessUIConsoleHandler().handle(pb, project, ">> Running: ", false, processHelper, ce);
	}
	
	public class EngineServerProcessUIConsoleHandler extends ProcessUIConsoleHandler {
		
		protected DaemonToolMessageConsole console;
		
		@Override
		public void handle() {
			if(DaemonEnginePreferences.DAEMON_CONSOLE_ENABLE.get() == false) {
				return;
			}
			console = DaemonToolMessageConsole.getConsole();
			handle(console);
		}
		
		@Override
		protected ConsoleOutputProcessListener createOutputListener(ToolsConsole console_) {
			return new ConsoleOutputProcessListener(console.serverStdOut, console.serverStdErr);
		}
	
	}
	
	public class EngineClientProcessUIConsoleHandler extends EngineServerProcessUIConsoleHandler {
		@Override
		protected ConsoleOutputProcessListener createOutputListener(ToolsConsole console_) {
			return new ConsoleOutputProcessListener(console.stdOut, console.stdErr);
		}
	}
	
}