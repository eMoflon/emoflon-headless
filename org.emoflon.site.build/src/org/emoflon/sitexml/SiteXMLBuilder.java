package org.emoflon.sitexml;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.SiteBuildOperation;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;

public class SiteXMLBuilder extends IncrementalProjectBuilder {

	private static final String SITEXML_ID = "org.moflon.emf.build.SiteXMLBuilder";
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		if(kind != FULL_BUILD) {
			return null;
		}
		IFile siteFile = getProject().getFile("site.xml");
		ISiteModel buildSiteModel = new WorkspaceSiteModel(siteFile);
		buildSiteModel.load();
		System.out.println();
		ISiteFeature[] features = buildSiteModel.getSite().getFeatures();
		IFeatureModel[] featureModels = getFeatureModels(features);

		Job job = new SiteBuildOperation(featureModels, buildSiteModel, "");
		job.setUser(true);
		job.schedule();
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

}
