package de.hpi.bpmn2execpn.converter;

import java.util.ArrayList;
import java.util.List;

import de.hpi.bpmn.BPMNDiagram;
import de.hpi.bpmn.IntermediateEvent;
import de.hpi.bpmn.SubProcess;
import de.hpi.bpmn.Task;
import de.hpi.bpmn2execpn.model.ExecTask;
import de.hpi.bpmn2pn.converter.Converter;
import de.hpi.bpmn2pn.model.ConversionContext;
import de.hpi.bpmn2pn.model.SubProcessPlaces;
import de.hpi.execpn.ExecPetriNet;
import de.hpi.execpn.impl.ExecPNFactoryImpl;
import de.hpi.petrinet.PetriNet;
import de.hpi.petrinet.Place;
import de.hpi.petrinet.Transition;

public class ExecConverter extends Converter {

	protected String modelURL;
	private List<ExecTask> taskMap;

	public ExecConverter(BPMNDiagram diagram, String modelURL) {
		super(diagram, new ExecPNFactoryImpl(modelURL));
		this.modelURL = modelURL;
		this.taskMap = new ArrayList<ExecTask>();
	}


	@Override
	protected void handleDiagram(PetriNet net, ConversionContext c) {
		((ExecPetriNet) net).setName(diagram.getTitle());
	}

	@Override
	protected void createStartPlaces(PetriNet net, ConversionContext c) {
		// do nothing...: we want start transitions instead of start places
	}

	// TODO this is a dirty hack...
	@Override
	protected void handleTask(PetriNet net, Task task, ConversionContext c) {
		ExecTask exTask = new ExecTask();
		exTask.setId(task.getId());
		exTask.setLabel(task.getLabel());
		
		exTask.startT = addLabeledTransition(net, "start_" + task.getId(), "start_"
				+ task.getId());
		exTask.running = addPlace(net, "running_" + task.getId());
		exTask.endT = addLabeledTransition(net, "end_" + task.getId(), "end_"
				+ task.getId());
		addFlowRelationship(net, c.map.get(getIncomingSequenceFlow(task)),
				exTask.startT);
		addFlowRelationship(net, exTask.endT, c.map
				.get(getOutgoingSequenceFlow(task)));
		addFlowRelationship(net, exTask.startT, exTask.running);
		addFlowRelationship(net, exTask.running, exTask.endT);

		//construct for suspend/resume
		exTask.suspend = addLabeledTransition(net, "suspend_" + task.getId(),
				"suspend_" + task.getId());
		exTask.resume = addLabeledTransition(net, "resume_" + task.getId(),
				"resume_" + task.getId());
		exTask.suspended = addPlace(net, "suspended_" + task.getId());
		addFlowRelationship(net, exTask.running, exTask.suspend);
		addFlowRelationship(net, exTask.suspend, exTask.suspended);
		addFlowRelationship(net, exTask.suspended, exTask.resume);
		addFlowRelationship(net, exTask.resume, exTask.running);

		taskMap.add(exTask);
		
		handleMessageFlow(net, task, exTask.startT, exTask.endT, c);
		if (c.ancestorHasExcpH)
			handleExceptions(net, task, exTask.endT, c);

		for (IntermediateEvent event : task.getAttachedEvents())
			handleAttachedIntermediateEventForTask(net, event, c);
	}

	@Override
	protected void handleSubProcess(PetriNet net, SubProcess process,
			ConversionContext c) {
		super.handleSubProcess(net, process, c);
		if (process.isAdhoc()) {
			handleSubProcessAdHoc(net, process, c);
		}
	}

	// TODO: Data dependencies
	protected void handleSubProcessAdHoc(PetriNet net, SubProcess process,
			ConversionContext c) {
		SubProcessPlaces pl = c.getSubprocessPlaces(process);

		// start and end transitions
		Transition startT = addTauTransition(net, "ad-hoc_start_"
				+ process.getId());
		Transition endT = addTauTransition(net, "ad-hoc_end_" + process.getId());
		Transition defaultEndT = addTauTransition(net, "ad-hoc_defaultEnd_"
				+ process.getId());
		addFlowRelationship(net, pl.startP, startT);
		addFlowRelationship(net, endT, pl.endP);
		addFlowRelationship(net, defaultEndT, pl.endP);

		if (process.isParallelOrdering()) {
			// parallel ad-hoc construct ------------------------------------------------------------------------

		} else {
			// sequential ad-hoc construct ---------------------------------------------------------------------

			// synchronization and completionCondition checks
			Place synch = addPlace(net, "ad-hoc_synch_" + process.getId());
			Place execState = addPlace(net, "ad-hoc_execState_"
					+ process.getId());
			addFlowRelationship(net, startT, synch);
			addFlowRelationship(net, startT, execState);
			addFlowRelationship(net, execState, endT);
			addFlowRelationship(net, synch, defaultEndT);
			addFlowRelationship(net, execState, defaultEndT);

			Place updatedState = addPlace(net, "ad-hoc_updatedState_"
					+ process.getId());
			Place ccStatus = addPlace(net, "ad-hoc_ccStatus_" + process.getId());
			Transition ccCheck = addLabeledTransition(net, "ad-hoc_ccCheck_"
					+ process.getId(), "ad-hoc_updatedState");
			Transition finalize = addLabeledTransition(net, "ad-hoc_finalize_"
					+ process.getId(), "ad-hoc_finalize");
			Transition resume = addLabeledTransition(net, "ad-hoc_resume_"
					+ process.getId(), "ad-hoc_resume");
			addFlowRelationship(net, updatedState, ccCheck);
			addFlowRelationship(net, execState, ccCheck);
			addFlowRelationship(net, ccCheck, execState);
			addFlowRelationship(net, ccCheck, ccStatus);
			addFlowRelationship(net, resume, synch);
			// TODO: add guard expressions
			addFlowRelationship(net, ccStatus, resume); //guard expression: ccStatus == false
			addFlowRelationship(net, ccStatus, finalize); // guard expression: ccStatus == true

			for (ExecTask exTask : taskMap) {
				// task execution
				Place enabled = addPlace(net, "ad-hoc_task_enabled_"
						+ exTask.getId());
				Place executed = addPlace(net, "ad-hoc_task_executed_"
						+ exTask.getId());
				addFlowRelationship(net, startT, enabled);
				addFlowRelationship(net, enabled, exTask.startT);
				addFlowRelationship(net, exTask.endT, executed);
				addFlowRelationship(net, executed, defaultEndT);
				addFlowRelationship(net, synch, exTask.startT);
				addFlowRelationship(net, exTask.endT, updatedState);

				// finish construct
				Place enableFinalize = addPlace(net,
						"ad-hoc_enable_finalize_task_" + exTask.getId());
				Place taskFinalized = addPlace(net, "ad-hoc_task_finalized_"
						+ exTask.getId());
				Transition skip = addTauTransition(net, "ad-hoc_skip_task_"
						+ exTask.getId());
				Transition finish = addTauTransition(net, "ad-hoc_finish_task_"
						+ exTask.getId());
				addFlowRelationship(net, finalize, enableFinalize);
				addFlowRelationship(net, enableFinalize, skip);
				addFlowRelationship(net, enabled, skip);
				addFlowRelationship(net, enableFinalize, finish);
				addFlowRelationship(net, executed, finish);
				addFlowRelationship(net, skip, taskFinalized);
				addFlowRelationship(net, finish, taskFinalized);
				addFlowRelationship(net, taskFinalized, endT);
			}
		}
	}
}
