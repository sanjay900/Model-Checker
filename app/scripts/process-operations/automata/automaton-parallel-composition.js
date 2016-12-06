'use strict';

	/**
	 * Performs parallel composition of the two specified automata. Constructs
	 * a new automaton with the specified id representing the composition. Returns
	 * the automaton constructed by the composition.
	 *
	 * @param {int} id - the id of the composition
	 * @param {automaton} net1 - the first automaton
	 * @param {automaton} net2 - the second automaton
	 * @return {automaton} - the automaton formed by the composition
	 */
function automataParallelComposition(id, automaton1, automaton2){
	var graph = new Automaton(id);
	var nodes1 = automaton1.nodes;
	var nodes2 = automaton2.nodes;
	combineStates(graph, nodes1, nodes2);
	var alphabet1 = automaton1.alphabet;
	var alphabet2 = automaton2.alphabet;
	var alphabet = alphabetUnion(alphabet1, alphabet2);

	// loop through combined node states and form the parallel composition
	for(var i = 0; i < nodes1.length; i++){
		var node1 = nodes1[i];
		for(var j = 0; j < nodes2.length; j++){
			var node2 = nodes2[j];
			// the id for the current combined state
			var fromId = node1.id + '.' + node2.id;

			for(var action in alphabet){
				var coaccessible1 = automaton1.coaccessible(node1, action);
				coaccessible1 = (coaccessible1.length !== 0) ? coaccessible1 : [undefined];
				var coaccessible2 = automaton2.coaccessible(node2, action);
				coaccessible2 = (coaccessible2.length !== 0) ? coaccessible2 : [undefined];

				for(var x = 0; x < coaccessible1.length; x++) {
          var c1 = coaccessible1[x];
          for (var y = 0; y < coaccessible2.length; y++) {
            var c2 = coaccessible2[y];
            if (c1 !== undefined && c2 !== undefined) {
              var toId = c1.node.id + '.' + c2.node.id;
              var edge = graph.addEdge(graph.nextEdgeId, action, graph.getNode(fromId), graph.getNode(toId));
              edge.locations = locationUnion(node1.locations, node2.locations);
            }
            else if (c1 !== undefined && alphabet2[action] === undefined) {
              var toId = c1.node.id + '.' + node2.id;
              var edge = graph.addEdge(graph.nextEdgeId, action, graph.getNode(fromId), graph.getNode(toId));
              edge.locations = node1.locations
            }
            else if (c2 !== undefined && alphabet1[action] === undefined) {
              var toId = node1.id + '.' + c2.node.id;
              var edge = graph.addEdge(graph.nextEdgeId, action, graph.getNode(fromId), graph.getNode(toId));
              edge.locations = node2.locations;
            }
            if (c2 !== undefined && c2.edge.metaData.broadcaster) {
              const receivers = automaton1.edges.filter(e => e.label === action && e.metaData.receiver);
              receivers.forEach(function (receiver) {
                nodes2.forEach(function(node){
                  var fromId = receiver.from + '.' + node.id;
                  var toId = receiver.to + '.' + node.id;
                  if(node.id !== node2.id) {
                    graph.addEdge(graph.nextEdgeId, action, graph.getNode(fromId), graph.getNode(toId));
                  }
                });
              });
            }
          }
          if(c1 !== undefined && c1.edge.metaData.broadcaster) {
            const receivers = automaton2.edges.filter(e => e.label === action && e.metaData.receiver);
            receivers.forEach(function (receiver) {
              nodes1.forEach(function(node){
                var fromId = node.id + '.' + receiver.from;
                var toId = node.id + '.' + receiver.to;
                if(node.id !== node1.id) {
                  graph.addEdge(graph.nextEdgeId, action, graph.getNode(fromId), graph.getNode(toId));
                }
              });
            });
          }

				}
			}
		}
	}

	// remove unreachable nodes from the composition
	graph.trim();
    graph.removeDuplicateEdges();
	return graph;

	/**
	 * Helper function for automataParallelComposition that creates a new
	 * node in the specified automaton for each combined state from the specified
	 * node arrays. Each node from the first array is combined with each node from
	 * the second array.
	 *
	 * @param {automaton} graph - the automaton to add nodes to
	 * @param {node[]} nodes1 - the first array of nodes
	 * @param {node[]} nodes2 - the second array of nodes
	 */
	function combineStates(graph, nodes1, nodes2){
		for(var i = 0; i < nodes1.length; i++){
			var locations1 = nodes1[i].locations;

			for(var j = 0; j < nodes2.length; j++){
				var id = nodes1[i].id + '.' + nodes2[j].id;
				var metaData = metaDataIntersection(nodes1[i].metaData, nodes2[j].metaData);
				var node = graph.addNode(id, metaData);

				var locations2 = nodes2[j].locations;
				node.locations = locationUnion(locations1, locations2);

				// check if this is the first node constructed
				if(i === 0 && j === 0) {
          graph.rootId = id;
        }
			}
		}
	}

	function locationUnion(loc1, loc2){
		for(var x in loc1){
			loc2[x] = true;
		}

		return loc2;
	}
}
