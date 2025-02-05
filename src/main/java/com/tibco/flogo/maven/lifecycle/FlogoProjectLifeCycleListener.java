package com.tibco.flogo.maven.lifecycle;


import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.List;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class FlogoProjectLifeCycleListener extends AbstractMavenLifecycleParticipant {
	@Requirement
	private Logger logger;

	@Parameter(property = "crossPlatform")
	private boolean crossPlatform;

    public FlogoProjectLifeCycleListener() {
    }

    @Override
    public void afterSessionStart(MavenSession session) throws MavenExecutionException {
        super.afterSessionStart(session);
    }

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		logger.info("Starting Maven Build for Flogo App.................................");

		if (session.getRequest().getGoals().contains("install") || session.getRequest().getGoals().contains("deploy")) {
			throw new MavenExecutionException( "Goal not supported by the plugin", session.getRequest().getPom());
		}
		super.afterProjectsRead(session);
	}


	@Override
	public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
		super.afterSessionEnd(session);
	}
}
