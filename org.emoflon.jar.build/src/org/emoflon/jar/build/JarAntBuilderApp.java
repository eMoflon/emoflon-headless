package org.emoflon.jar.build;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.app.MainApplicationLauncher;
import org.eclipse.jdt.internal.ui.jarpackagerfat.FatJarAntExporter;
import org.eclipse.jdt.internal.ui.jarpackagerfat.FatJarPackagerMessages;
import org.eclipse.jdt.internal.ui.jarpackagerfat.FatJarPackagerUtil;
import org.eclipse.jdt.internal.ui.jarpackagerfat.FatJarPackageWizardPage.ExtractLibraryHandler;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.debug.internal.core.LaunchConfiguration;

public class JarAntBuilderApp implements IApplication{

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] arguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		String projectName = getArgument(arguments, "-project");
		if(projectName == null) 
			throw new RuntimeException("No -project was specified!\n Entries: " + arguments);
		
		String launchPath = getArgument(arguments, "-launchPath");
		if(launchPath == null) 
			throw new RuntimeException("No -launchPath was specified!\n Entries: " + arguments);
		
		String jarPath = getArgument(arguments, "-jarPath");
		if(jarPath == null) 
			throw new RuntimeException("No -jarPath was specified!\n Entries: " + arguments);
		
		String antPath = getArgument(arguments, "-antPath");
		if(antPath == null) 
			throw new RuntimeException("No -antPath was specified!\n Entries: " + arguments );
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if(project == null) 
			throw new RuntimeException("Project " + projectName + " not found!");
		
		IFile launchFile = project.getFile(launchPath);
		if(!launchFile.exists()) 
			throw new RuntimeException(launchPath + " does not exist!");
		
		IFile jarIFile = project.getFile(jarPath);
		IFile antIFile = project.getFile(antPath);
		File antFile = new File(antIFile.getLocation().makeAbsolute().toOSString());
		if(!antFile.exists()) {
			antFile.getParentFile().mkdirs();
			antFile.createNewFile();
		}
		
		ILaunchConfiguration lConfig = new MyLaunchConfiguration(launchFile);
		
		ExtractLibraryHandler libraryHandler = new ExtractLibraryHandler();
		FatJarAntExporter antExporter= libraryHandler.getAntExporter(antIFile.getLocation().makeAbsolute(), jarIFile.getLocation(), lConfig);
		try {
			antExporter.run(new MultiStatus("org.emoflon.jar.build", 0, "No status"));
		} catch (CoreException e) {
			throw new RuntimeException(FatJarPackagerMessages.FatJarPackageWizardPage_error_ant_script_generation_failed, e);
		}
		return null;
	}

	@Override
	public void stop() {
		
	}
	
	
	private String getArgument(String[] arguments, String key) {
		for(int i=0; i < arguments.length; i++) {
			if(arguments[i].equals(key)) {
				if(i+1 >= arguments.length) {
					throw new RuntimeException("No project was specified!");
				}
				else {
					return arguments[i+1];
				}
			}
		}
		return null;
	}

	class MyLaunchConfiguration extends LaunchConfiguration {

		protected MyLaunchConfiguration(IFile file) {
			super(file);
		}
		
	}
}
