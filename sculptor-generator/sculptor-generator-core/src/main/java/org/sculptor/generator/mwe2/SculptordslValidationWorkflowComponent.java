/*
 * Copyright 2013 The Sculptor Project Team, including the original 
 * author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sculptor.generator.mwe2;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.sculptor.dsl.sculptordsl.DslApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SculptordslValidationWorkflowComponent extends WorkflowComponentWithModelSlot {

	private static final Logger LOGGER = LoggerFactory.getLogger(SculptordslValidationWorkflowComponent.class);

	@SuppressWarnings("unchecked")
	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor m, Issues issues) {
		Collection<?> slotContent = (Collection<?>) ctx.get(getModelSlot());
		if (slotContent == null) {
			issues.addError(String.format("Slot %s is empty", getModelSlot()));
		} else {
		
			// retrieve models from model slot
			List<DslApplication> applications = Lists.newArrayList();
			for (DslApplication application : Iterables.filter(slotContent, DslApplication.class)) {
				applications.add(application);
			}
			if (applications.isEmpty()) {
				issues.addError(this, "No DslApplication instance found in model slot", slotContent, null, null);
			} else {

				// validate models
				for (DslApplication application : applications) {
					LOGGER.debug("Validating application '{}'", application.getName());
					Diagnostic diagnostic = Diagnostician.INSTANCE.validate(application);
					switch (diagnostic.getSeverity()) {
					case Diagnostic.ERROR:
						if (diagnostic.getChildren() !=null && diagnostic.getChildren().size() > 0) {
							for (Diagnostic d: diagnostic.getChildren()) {
								issues.addError(this, d.getMessage(), d.getData().get(0),
										d.getException(), (List<Object>) d.getData());
							}
						} else {
							issues.addError(this, diagnostic.getMessage(), diagnostic.getData().get(0),
									diagnostic.getException(), (List<Object>) diagnostic.getData());
						}
						break;
					case Diagnostic.WARNING:
						if (diagnostic.getChildren() !=null && diagnostic.getChildren().size() > 0) {
							for (Diagnostic d: diagnostic.getChildren()) {
								issues.addWarning(this, d.getMessage(), d.getData().get(0),
										d.getException(), (List<Object>) d.getData());
							}
						} else {
							issues.addWarning(this, diagnostic.getMessage(), diagnostic.getData().get(0),
									diagnostic.getException(), (List<Object>) diagnostic.getData());
						}
						break;
					}
				}
			}
		}
	}

}
