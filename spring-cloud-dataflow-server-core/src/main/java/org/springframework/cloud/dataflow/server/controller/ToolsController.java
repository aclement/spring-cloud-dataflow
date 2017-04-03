/*
 * Copyright 2017 the original author or authors.
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
 *
 *
 */
package org.springframework.cloud.dataflow.server.controller;

import java.util.Map;

import org.springframework.cloud.dataflow.core.dsl.ParseException;
import org.springframework.cloud.dataflow.core.dsl.TaskParser;
import org.springframework.cloud.dataflow.core.dsl.graph.Graph;
import org.springframework.cloud.dataflow.rest.resource.TaskDslResource;
import org.springframework.cloud.dataflow.rest.resource.TaskGraphResource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for integrating task parsing with frontend tools.
 *
 * @author Andy Clement
 */
@RestController
@RequestMapping("/tools")
public class ToolsController {
	
	private final static String TASK_NAME_KEY = "name";
	
	private final static String TASK_DSL_TEXT_KEY = "dsl";
	
	private TaskGraphAssembler taskGraphAssembler = new TaskGraphAssembler();
	
	private TaskDslAssembler taskDslAssembler = new TaskDslAssembler();
	
	/**
	 * Parse a task definition into a graph structure. The definition map is expected to have
	 * a 'dsl' key containing the composed task DSL and optionally a 'name' key indicating the name of the
	 * composed task.
	 * 
	 * @return a map with either a 'graph' or 'error' key set 
	 */
	@RequestMapping(value = "/parseTaskTextToGraph", method = RequestMethod.POST)
	public TaskGraphResource parseTaskTextToGraph(@RequestBody Map<String, String> definition) {
		Graph graph = null;
		String errorMessage = null;
		try {
			TaskParser taskParser = new TaskParser(definition.get(TASK_NAME_KEY),definition.get(TASK_DSL_TEXT_KEY), true, true);
			graph = taskParser.parse().toGraph();
		}
		catch (ParseException pe) {
			errorMessage = pe.toString();
		}
		return taskGraphAssembler.toResource(new ParsedGraphOutput(graph,errorMessage));
	}
	
	/**
	 * Convert a graph format into DSL text format.
	 */
	@RequestMapping(value = "/convertTaskGraphToText", method = RequestMethod.POST)
	public TaskDslResource convertTaskGraphToText(@RequestBody Graph graph) {
		String dsl = null;
		String errorMessage = null;
		try {
			dsl = graph.toDSLText();
		}
		catch (Throwable e) {
			errorMessage = e.toString();
		}
		return taskDslAssembler.toResource(new GraphToDslOutput(dsl, errorMessage));
	}
	
	private static class ParsedGraphOutput {
		final Graph graph;
		
		final String errorMessage;
		
		public ParsedGraphOutput(Graph graph, String errorMessage) {
			this.graph = graph;
			this.errorMessage = errorMessage;
		}
	}

	private static class GraphToDslOutput {
		final String dsl;
		
		final String errorMessage;
		
		public GraphToDslOutput(String dsl, String errorMessage) {
			this.dsl = dsl;
			this.errorMessage = errorMessage;
		}
	}

	/**
	 * {@link org.springframework.hateoas.ResourceAssembler} implementation
	 * that converts a {@link ParsedGraphOutput} to a {@link GraphResource}.
	 */
	static class TaskGraphAssembler extends ResourceAssemblerSupport<ParsedGraphOutput, TaskGraphResource> {

		public TaskGraphAssembler() {
			super(ToolsController.class, TaskGraphResource.class);
		}

		@Override
		public TaskGraphResource toResource(ParsedGraphOutput graph) {
			return new TaskGraphResource(graph.graph, graph.errorMessage);
		}
	}

	/**
	 * {@link org.springframework.hateoas.ResourceAssembler} implementation
	 * that converts a {@link GraphToDslOutput} to a {@link TaskDslResource}.
	 */
	static class TaskDslAssembler extends ResourceAssemblerSupport<GraphToDslOutput, TaskDslResource> {

		public TaskDslAssembler() {
			super(ToolsController.class, TaskDslResource.class);
		}

		@Override
		public TaskDslResource toResource(GraphToDslOutput output) {
			return new TaskDslResource(output.dsl, output.errorMessage);
		}
	}

}
