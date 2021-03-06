function convertGraph(graph, id, hidden) {
    let glGraph = {};
    if (graph.type == 'automata') {
        visualizeAutomata(graph,id, hidden, glGraph);
    }
    return glGraph;
}
function visualizeAutomata(process, graphID, hidden, glGraph) {
    glGraph.interrupts = [];
    let lastBox = graphID;
    // add nodes in automaton to the graph
    const nodes = process.nodes;
    glGraph.nodes = [];
    glGraph.edges = [];
    let interruptId = 1;
    for(let i = 0; i < nodes.length; i++){
        const nid = 'n' + nodes[i].id;
        let type = "fsaState";
        // check if current node is the root node
        if(nodes[i].metaData.startNode){
            type = "fsaStartState";
        }
        if(nodes[i].metaData.isTerminal !== undefined) {
            type = "fsaEndState";
            if (nodes[i].metaData.isTerminal === 'ERROR') {
                type = "fsaErrorState";
            }
        }
        let tooltip;
        const vars = nodes[i].metaData.variables;
        if (vars && Object.keys(vars).length > 0) {
            tooltip = "";
            for (let i in vars) {
                tooltip+=i+"="+vars[i]+", ";
            }
            tooltip = "Variables: <span style='color:blue'>"+tooltip.substr(0,tooltip.length-2).replace(/\$/g,"")+"</span>";
        }
        glGraph.nodes.push({
            group:"nodes",
            data: {id: graphID+nid, label: nodes[i].metaData.label, type: type, tooltip: tooltip, parent: graphID},
        });
    }
    let toEmbed = [];
    // add the edges between the nodes in the automaton to the graph
    const edges = process.edges;
    for(let i = 0; i < edges.length; i++){
        let label = edges[i].label;
        const from = graphID+'n' + edges[i].from;
        const to = graphID+'n' + edges[i].to;
        let tooltip;
        let guard = edges[i].metaData.guard;
        if(guard && guard.hiddenGuardStr && guard.hiddenGuardStr.length > 0){
            label += " "+guard.hiddenGuardStr;
        }

        if (guard) {
            tooltip = "";
            if (guard.varStr.length > 0)
            tooltip += "Variables:<span style='color:blue'>" +guard.varStr+"</span><br/>";
            if (guard.guardStr.length > 0)
            tooltip += "Guard:<span style='color:blue'>" +guard.guardStr+"</span><br/>";
            if (guard.nextStr.length > 0)
            tooltip += "Assigned variables:<span style='color:blue'>" +guard.nextStr+"</span>";
        }
        if (tooltip == "") tooltip = undefined;
        if (edges[i].metaData.interrupt && hidden) {
            const toNode = process.nodeMap[edges[i].to];
            //Destroy all interrupt edges besides the last one.
            if (toNode.incomingEdges.indexOf(edges[i].id) != toNode.incomingEdges.length-1) {
                toEmbed.push(from);
                continue;
            }
            lastBox = _box(glGraph, toEmbed, graphID+"."+(interruptId++),graphID);
            //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
            toEmbed = [ lastBox, _link(lastBox,to, label,tooltip, glGraph, lastBox), to];
            continue;
        }
        toEmbed.push(_link(from,to, label,tooltip,glGraph, lastBox));
        toEmbed.push(from);
        toEmbed.push(to);
    }
}

function _link(source, target, label, tooltip, glGraph, lastBox) {
    glGraph.edges.push({
        group: "edges",
        data: {id:source+"-"+label+"->"+target,label: label, tooltip: tooltip,source: source,target: target, parent: lastBox},
    });
    return source+"->"+target;
}
function _box(glGraph, toEmbed, name, graphID) {
    glGraph.interrupts.push("boxNode"+name);
    //we need to use unshift here, as the parents need to load before the children.
    glGraph.nodes.unshift({
        group:"nodes",
        data: {id: "boxNode"+name, type: 'interrupt', parent: graphID, label: name},
    });
    //Remove embedded cells from the parent and add them to the box
    toEmbed.forEach(embed => {
        let el = _.findWhere(glGraph.nodes,{data:{id: embed}});
        if (!el) {
            el = _.findWhere(glGraph.edges,{data:{id: embed}});
        }
        el.data.parent = "boxNode"+name;
    });
    return "boxNode"+name;
}
function first(data) {
    for(let key in data){
        if(data.hasOwnProperty(key)){
            return key;
        }
    }
}
function getCytoscapeStyle() {
    return [
        {
            selector: 'node',
            style: {
                'background-color': Colours.grey,
                'label': 'data(label)',
                "text-valign" : "center",
                "text-halign" : "center",
                'font-size': '15',
                'font-weight': 'bold',
                'border-width': '3px',
                'width':"label",
                'height':"label",
                'padding': '10px',
            }
        },
        {
            selector: 'node[type=\'fsaStartState\']',
            style: {
                'border-style': 'double',
                'background-color': Colours.blue,
                'border-width': '10px',
            }
        },
        {
            selector: 'node[type=\'fsaEndState\']',
            style: {
                'border-style': 'double',
                'background-color': Colours.green,
                'border-width': '10px',
            }
        },
        {
            selector: 'node[type=\'fsaErrorState\']',
            style: {
                'border-style': 'double',
                'background-color': Colours.red,
                'border-width': '10px',
            }
        },
        {
            selector: ':parent',
            style: {
                'background-opacity': 0.333,
                "text-valign" : "top",
            }
        },
        {
            selector: 'edge',
            style: {
                'width': 3,
                'line-color': 'black',
                'label': 'data(label)',
                'curve-style': 'bezier',
                'font-size': '15',
                'font-weight': 'bold',
                'target-arrow-color': 'black',
                'target-arrow-shape': 'triangle',
                'text-background-opacity': 1,
                'text-background-color': '#ffffff',
                'text-background-shape': 'rectangle',
                'text-rotation': 'autorotate'
            }
        }
    ]
}
