/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// Haiti project  - searching the nearest road
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Int2D;

@SuppressWarnings("restriction")
public class AStar {
    // static private HashMap<int[], ArrayList<Location>> cache = new HashMap(1000);

    /**
     * Assumes that both the start and end location are NODES as opposed to LOCATIONS
     * @param ebolaSim
     * @param start
     * @param goal
     * @return
     */
    static public HashSet<EbolaBuilder.Node> astarPath(EbolaABM ebolaSim, EbolaBuilder.Node start, EbolaBuilder.Node goal) {
//        int[] cacheKey = new int[] {start.location.xLoc, start.location.yLoc, goal.location.xLoc, goal.location.yLoc};
//        if (cache.containsKey(cacheKey))
//            return cache.get(cacheKey);
//        
        // initial check
        long startTime = System.currentTimeMillis();
        if (start == null || goal == null) {
            System.out.println("Error: invalid node provided to AStar");
        }

        // containers for the metainformation about the Nodes relative to the 
        // A* search
        HashMap<EbolaBuilder.Node, AStarNodeWrapper> foundNodes =
                new HashMap<EbolaBuilder.Node, AStarNodeWrapper>();


        AStarNodeWrapper startNode = new AStarNodeWrapper(start);
        AStarNodeWrapper goalNode = new AStarNodeWrapper(goal);
        foundNodes.put(start, startNode);
        foundNodes.put(goal, goalNode);

        startNode.gx = 0;
        startNode.hx = heuristic(start, goal);
        startNode.fx = heuristic(start, goal);

        // A* containers: allRoadNodes to be investigated, allRoadNodes that have been investigated
        HashSet<AStarNodeWrapper> closedSet = new HashSet<>(),
                openSet = new HashSet<>();
        HashSet<EbolaBuilder.Node> returnVal = new HashSet<>();
        openSet.add(startNode);


        while(openSet.size() > 0){ // while there are reachable allRoadNodes to investigate

            AStarNodeWrapper x = findMin(openSet); // find the shortest path so far
            if(x == null)
            {
                AStarNodeWrapper n = findMin(openSet);
            }
            if(x.node == goal ){ // we have found the shortest possible path to the goal!
                // Reconstruct the path and send it back.
                //return reconstructPath(goalNode);
                return null;
            }
            openSet.remove(x); // maintain the lists
            closedSet.add(x);
            returnVal.add(x.node);
            EbolaBuilder.nodeToSet.put(x.node, returnVal);

            // check all the neighbors of this location
            for(Edge l: x.node.links){

                EbolaBuilder.Node n = (EbolaBuilder.Node) l.from();
                if( n == x.node )
                    n = (EbolaBuilder.Node) l.to();

                // get the A* meta information about this Node
                AStarNodeWrapper nextNode;
                if( foundNodes.containsKey(n))
                    nextNode = foundNodes.get(n);
                else{
                    nextNode = new AStarNodeWrapper(n);
                    foundNodes.put( n, nextNode );
                }

                if(closedSet.contains(nextNode)) // it has already been considered
                    continue;

                // otherwise evaluate the cost of this node/edge combo
                double tentativeCost = x.gx + (Integer) l.info;
                boolean better = false;

                if(! openSet.contains(nextNode)){
                    openSet.add(nextNode);
                    nextNode.hx = heuristic(n, goal);
                    better = true;
                }
                else if(tentativeCost < nextNode.gx){
                    better = true;
                }

                // store A* information about this promising candidate node
                if(better){
                    nextNode.cameFrom = x;
                    nextNode.gx = tentativeCost;
                    nextNode.fx = nextNode.gx + nextNode.hx;
                }
            }

//            if(foundNodes.size()%10000 == 0)
//                System.out.println("Time = " + System.currentTimeMillis());
        }
        System.out.println("Searched " + foundNodes.size() + " nodes but could not find it");
        return returnVal;
    }

    /**
     * Takes the information about the given node n and returns the path that
     * found it.
     * @param n the end point of the path
     * @return an ArrayList of allRoadNodes that lead from the
     * given Node to the Node from which the search began 
     */
    static ArrayList<EbolaBuilder.Location> reconstructPath(AStarNodeWrapper n) {
        ArrayList<EbolaBuilder.Location> result = new ArrayList<EbolaBuilder.Location>();
        AStarNodeWrapper x = n;
        while (x.cameFrom != null) {
            result.add(0, x.node.location); // add this edge to the front of the list
            x = x.cameFrom;
        }

        return result;
    }

    /**
     * Measure of the estimated distance between two Nodes.
     * @return notional "distance" between the given allRoadNodes.
     */
    static double heuristic(EbolaBuilder.Node x, EbolaBuilder.Node y) {
        return x.location.distanceTo(y.location);
    }

    /**
     *  Considers the list of Nodes open for consideration and returns the node 
     *  with minimum fx value
     * @param set list of open Nodes
     * @return
     */
    static AStarNodeWrapper findMin(Collection<AStarNodeWrapper> set) {
        double min = Double.MAX_VALUE;
        AStarNodeWrapper minNode = null;
        for (AStarNodeWrapper n : set) {
            if (n.fx < min) {
                min = n.fx;
                minNode = n;
            }
        }
        return minNode;
    }

    /**
     * A wrapper to contain the A* meta information about the Nodes
     *
     */
    static class AStarNodeWrapper {

        // the underlying Node associated with the metainformation
        EbolaBuilder.Node node;
        // the Node from which this Node was most profitably linked
        AStarNodeWrapper cameFrom;
        double gx, hx, fx;

        public AStarNodeWrapper(EbolaBuilder.Node n) {
            node = n;
            gx = 0;
            hx = 0;
            fx = 0;
            cameFrom = null;
        }
    }
}