package org.emoflon.sitexml;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.core.AntClassLoader;
import org.eclipse.core.internal.jobs.InternalJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.internal.framework.EquinoxBundle;
import org.eclipse.osgi.internal.framework.EquinoxContainer;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.url.EquinoxFactoryManager;
import org.eclipse.osgi.launch.Equinox;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.SiteBuildOperation;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;

public class SiteXMLBuilderApp implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] arguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		String projectName = getProject(arguments);

		if(projectName == null) {
			throw new RuntimeException("No project was specified!\n Entries: " + arguments);
		}
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if(project == null) 
			throw new RuntimeException("Project " + projectName + " not found!");
		
		AntClassLoader.getSystemClassLoader();
		AntClassLoader.getPlatformClassLoader();
		AntCorePlugin.getPlugin();
		
		IFile siteFile = project.getFile("site.xml");
		ISiteModel buildSiteModel = new WorkspaceSiteModel(siteFile);
		buildSiteModel.load();
		System.out.println();
		ISiteFeature[] features = buildSiteModel.getSite().getFeatures();
		IFeatureModel[] featureModels = getFeatureModels(features);

		try {
			SiteBuildOperation job = new SiteBuildOperation(featureModels, buildSiteModel, "");			
			job.setUser(true);
			job.schedule();
			job.join();
		}
		catch(Exception e) {
			
		}
		
		return null;
	}
	
	private String getProject(String[] arguments) {
		for(int i=0; i < arguments.length; i++) {
			if(arguments[i].equals("-project")) {
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

	private IFeatureModel[] getFeatureModels(ISiteFeature[] sFeatures) {
		ArrayList<IFeatureModel> list = new ArrayList<>();
		for (ISiteFeature siteFeature : sFeatures) {
			IFeatureModel model = PDECore.getDefault().getFeatureModelManager().findFeatureModelRelaxed(siteFeature.getId(), siteFeature.getVersion());
			if (model != null)
				list.add(model);
		}
		return list.toArray(new IFeatureModel[list.size()]);
	}

	@Override
	public void stop() {
		
	}

}
