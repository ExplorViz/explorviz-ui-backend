package net.explorviz.server.repository;

import net.explorviz.model.Application;
import net.explorviz.model.Communication;
import net.explorviz.model.Component;
import net.explorviz.model.Landscape;
import net.explorviz.model.Node;
import net.explorviz.model.NodeGroup;
import net.explorviz.model.System;

public class LandscapePreparer {
	
	private static int idCounter = 1;
	
	public static Landscape prepareLandscape(final Landscape landscape) {
		if (landscape == null) {
			Landscape l = new Landscape();
			l.setId(String.valueOf(idCounter));
			idCounter = 1;
			return l;
		}
		
		landscape.setId(String.valueOf(idCounter++));

		for (final System system : landscape.getSystems()) {
			system.setId(String.valueOf(idCounter++));
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				nodeGroup.setId(String.valueOf(idCounter++));
				for (final Node node : nodeGroup.getNodes()) {
					node.setId(String.valueOf(idCounter++));
					for (final Application application : node.getApplications()) {
						application.setId(String.valueOf(idCounter++));
						final Component foundationComponent = new Component();
						foundationComponent.setFoundation(true);
						foundationComponent.setOpened(true);
						foundationComponent.setName(application.getName());
						foundationComponent.setFullQualifiedName(application.getName());
						foundationComponent.setBelongingApplication(application);
						//foundationComponent.setColor(ColorDefinitions.componentFoundationColor);

						foundationComponent.getChildren().addAll(application.getComponents());
						
						foundationComponent.setId(String.valueOf(idCounter++));

						for (final Component child : foundationComponent.getChildren()) {
							setComponentAttributes(child, 0, true);
							child.setId(String.valueOf(idCounter++));
						}

						application.getComponents().clear();
						application.getComponents().add(foundationComponent);
					}
				}

				if (nodeGroup.getNodes().size() == 1) {
					nodeGroup.setOpened(true);
				} else {
					nodeGroup.setOpened(false);
				}
				nodeGroup.updateName();
			}
		}

		for (final Communication commu : landscape.getApplicationCommunication()) {
			createApplicationInAndOutgoing(commu);
			commu.setId(String.valueOf(idCounter++));
		}
		
		idCounter = 1;

		return landscape;
	}

	private static void setComponentAttributes(final Component component, final int index,
			final boolean shouldBeOpened) {
		boolean openNextLevel = shouldBeOpened;

		if (!openNextLevel) {
			component.setOpened(false);
		} else if (component.getChildren().size() == 1) {
			component.setOpened(true);
		} else {
			component.setOpened(true);
			openNextLevel = false;
		}

		if ((index % 2) == 1) {
			if (component.isSynthetic()) {
				//component.setColor(ColorDefinitions.componentSyntheticColor);
			} else {
			//	component.setColor(ColorDefinitions.componentFirstColor);
			}
		} else {
			if (component.isSynthetic()) {
			//	component.setColor(ColorDefinitions.componentSyntheticSecondColor);
			} else {
				//component.setColor(ColorDefinitions.componentSecondColor);
			}
		}

		for (final Component child : component.getChildren()) {
			setComponentAttributes(child, index + 1, openNextLevel);
			child.setId(String.valueOf(idCounter++));
		}
	}

	private static final void createApplicationInAndOutgoing(final Communication communication) {
		final Application sourceApp = communication.getSource();
		if (sourceApp != null) {
			sourceApp.getOutgoingCommunications().add(communication);
		}

		final Application targetApp = communication.getTarget();
		if (targetApp != null) {
			targetApp.getIncomingCommunications().add(communication);
		}
	}
}
