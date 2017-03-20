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

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.dataflow.core.dsl.ComposedTaskParser;
import org.springframework.cloud.dataflow.core.dsl.ParseException;
import org.springframework.cloud.dataflow.core.dsl.graph.Graph;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for integrating with frontend tools.
 *
 * @author Andy Clement
 */
@RestController
@RequestMapping("/tools")
public class ToolsController {
	
	private final static String COMPOSED_TASK_NAME_KEY = "name";
	
	private final static String COMPOSED_TASK_DSL_TEXT_KEY = "dsl";

	private final static String RESULT_GRAPH_KEY = "graph";
	
	private final static String RESULT_ERROR_KEY = "error";
	
	private final static String RESULT_DSL_TEXT_KEY = "text";

	/**
	 * Parse a single composed task definition into a graph structure. The definition map is expected to have
	 * a 'dsl' key containing the composed task DSL and optionally a 'name' key indicating the name of the
	 * composed task.
	 * 
	 * @return a map with either a 'graph' or 'error' key set 
	 */
	@RequestMapping(value = "/parseComposedTaskTextToGraph", method = RequestMethod.POST)
	public Map<String, Object> parseComposedTaskTextToGraph(@RequestBody Map<String, String> definition) {
		Map<String, Object> response = new HashMap<>();
		ComposedTaskParser composedTaskParser = new ComposedTaskParser();
		try {
			Graph graph = composedTaskParser.parse(
					definition.get(COMPOSED_TASK_NAME_KEY),
					definition.get(COMPOSED_TASK_DSL_TEXT_KEY)).toGraph();
			response.put(RESULT_GRAPH_KEY, graph);
		}
		catch (ParseException pe) {
			response.put(RESULT_ERROR_KEY, pe.toString());
		}
		return response;
	}
	
	/**
	 * Convert a graph format into DSL text format.
	 */
	@RequestMapping(value = "/convertComposedTaskGraphToText", method = RequestMethod.POST)
	public Map<String, Object> convertComposedTaskGraphToText(@RequestBody Graph graph) {
		Map<String, Object> response = new HashMap<>();
		try {
			String dslText = graph.toDSLText();
			response.put(RESULT_DSL_TEXT_KEY, dslText);
		}
		catch (Throwable e) {
			response.put(RESULT_ERROR_KEY, e.toString());
		}
		return response;
	}

}
