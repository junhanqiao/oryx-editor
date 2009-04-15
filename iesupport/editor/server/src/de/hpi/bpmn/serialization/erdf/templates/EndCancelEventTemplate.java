package de.hpi.bpmn.serialization.erdf.templates;

import de.hpi.bpmn.DiagramObject;
import de.hpi.bpmn.EndCancelEvent;
import de.hpi.bpmn.serialization.erdf.ERDFSerializationContext;

public class EndCancelEventTemplate extends NonConnectorTemplate {

	private static BPMN2ERDFTemplate instance;

	public static BPMN2ERDFTemplate getInstance() {
		if (instance == null) {
			instance = new EndCancelEventTemplate();
		}
		return instance;
	}

	public StringBuilder getCompletedTemplate(DiagramObject diagramObject,
			ERDFSerializationContext transformationContext) {

		EndCancelEvent e = (EndCancelEvent) diagramObject;
		
		StringBuilder s = getResourceStartPattern(transformationContext.getResourceIDForDiagramObject(e));
		appendOryxField(s,"type",STENCIL_URI + "#EndCancelEvent");
		appendOryxField(s,"eventtype","End");
		appendNonConnectorStandardFields(e,s);
		appendOryxField(s,"result","Cancel");
		appendResourceEndPattern(s, e, transformationContext);
		
		return s;
	}

}
