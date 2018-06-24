package info.bschambers.gridgeom;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Stack;
import java.util.Collection;
import java.util.Comparator;

/**
 * <p>A directed graph representing connections between points in two
 * dimensional space.</p>
 *
 * <p>Rules:</p>
 * <ul>
 * <li>A node cannot be connected to itself. It would not be useful for my intended usage.</li>
 * </ul>
 */
public class Digraph2D {

    private List<Node> nodes = new ArrayList<>();

    @Override
    public String toString() {
        String str = "Digraph: " + nodes.size() + " nodes";
        int count = 1;
        for (Node n : nodes) {
            str += "\n" + count++ + ": " + n;
        }
        return str;
    }

    public int getNumNodes() {
        return nodes.size();
    }

    /**
     * @return The {@code Node} at index {@code i}.
     */
    public Node getNode(int i) {
        return nodes.get(i);
    }

    /**
     * @return The node with co-ordinates of point {@code p}, or {@code null} if
     * there is no such node.
     */
    public Node getNode(Pt2Df p) {
        for (Node n : nodes)
            if (n.getPoint().equals(p))
                return n;
        return null;
    }

    /**
     * @return {@code true}, if digraph contains a node with the co-ordinates of point
     * {@code p}, otherwise {@code false}. */
    public boolean contains(Pt2Df p) {
        for (Node n : nodes)
            if (n.getPoint().equals(p))
                return true;
        return false;
    }

    /**
     * @return True, if node with co-ordinates of point {@code p1} has a forward
     * connection with node with co-ordinates of point {@code p2}. Returns false
     * if connection does not exist, or if no node exists for either point.
     */
    public boolean isConnected(Pt2Df p1, Pt2Df p2) {
        Node n1 = getNode(p1);
        Node n2 = getNode(p2);
        if (n1 == null || n2 == null)
            return false;
        return isConnected(n1, n2);
    }

    /**
     * @return True, if node with co-ordinates of point {@code p1} has a
     * backward connection with node with co-ordinates of point {@code p2}.
     * Returns false if connection does not exist, or if no node exists for
     * either point.
     */
    public boolean isConnectedBackward(Pt2Df p1, Pt2Df p2) {
        Node n1 = getNode(p1);
        Node n2 = getNode(p2);
        if (n1 == null || n2 == null)
            return false;
        return isConnectedBackward(n1, n2);
    }

    /**
     * @return True, if node {@code n1} has a forward connection with node
     * {@code n2}.
     */
    public boolean isConnected(Node n1, Node n2) {
        for (Connection c : n1.getConnectionsForward())
            if (c.getDestination().equals(n2))
                return true;
        return false;
    }
    
    /**
     * @return True, if node {@code n1} has a backward connection with node
     * {@code n2}.
     */
    public boolean isConnectedBackward(Node n1, Node n2) {
        for (Connection c : n1.getConnectionsBackward())
            if (c.getOrigin().equals(n2))
                return true;
        return false;
    }
    
    public void addLine(Line ln, int shapeID) {
        addLine(ln.start().toFloat(), ln.end().toFloat(), shapeID);
    }
    
    public void addLine(Pt2Df p1, Pt2Df p2, int shapeID) {
        System.out.println("addLine(p1=" + p1 + " p2=" + p2 + " id=" + shapeID + ")...");
        System.out.println("... " + this);

        Node n1 = getAndAddNode(p1);
        Node n2 = getAndAddNode(p2);
        SortedSet<Node> lineNodes
            = new TreeSet<>(new SmallestDistComparator(n1));
        lineNodes.add(n2);

        List<Connection> toSplit = new ArrayList<>();
        List<Pt2Df> splitPoints = new ArrayList<>();
        
        // check for intersections with existing lines...
        Linef l1 = new Linef(p1, p2);
        for (Node n : nodes) {
            for (Connection c : n.getConnectionsForward()) {
                Linef l2 = c.getLine();

                // DEAL WITH ALL POSSIBLE SCENARIOS...

                // SPECIAL CASES:

                // overlapping parallel lines

                if (l1.intersects(l2)) {

                    // INTERSECTION CASES:

                    // CASE: one shared end (no other intersection)
                    
                    // CASE: end point of l2 intersects l1

                    // CASE: end point of l1 intersects l2
                    
                    // CASE: mutual intersection
                
                    // add node at intersection point
                    Pt2Df iPt = l1.getIntersectionPoint(l2);
                    // only make new node if it's not an existing end point
                    if (!iPt.equals(p1) && !iPt.equals(p2)) {
                        Node intersection = new Node(iPt);
                        lineNodes.add(intersection);
                    }
                    
                    // divide at intersection point
                    if (!iPt.equals(c.getOrigin().getPoint()) &&
                        !iPt.equals(c.getDestination().getPoint())) {
                        toSplit.add(c);
                        splitPoints.add(iPt);
                    }
                }
            }   
        }

        // add all of the nodes on the stack connections to complete line
        Node origin = n1;
        for (Node dest : lineNodes) {
            addNode(dest);
            origin.addConnection(dest, shapeID);
            origin = dest;
        }

        // split and make new connections
        for (int i = 0; i < toSplit.size(); i++) {
            Connection c = toSplit.get(i);
            Pt2Df p = splitPoints.get(i);
            // get rid of old connection
            c.getOrigin().removeConnection(c.getDestination());
            // add two new connections
            Node iNode = getAndAddNode(p);
            c.getOrigin().addConnection(iNode, shapeID);
            iNode.addConnection(c.getDestination(), shapeID);
        }
    }

    /**
     * <p>Gets the node representing the point {@code p}. If such a node doesn't
     * exist, it will be created and added to the list of nodes.</p>
     */
    private Node getAndAddNode(Pt2Df p) {
        Node n = getNode(p);
        if (n == null) {
            n = new Node(p);
            nodes.add(n);
        }
        return n;
    }

    /**
     * <p>Adds node {@code n} only if there is no existing node at the location.</p>
     *
     * @return True, if a new {@code Node} was added.
     */
    public boolean addNode(Node n) {
        boolean found = false;
        for (Node nn : nodes)
            if (nn.getPoint().equals(n.getPoint()))
                found = true;
        if (!found)
            nodes.add(n);
        return !found;
    }

    public boolean remove(Pt2Df p) {
        Node n = getNode(p);
        if (n == null)
            return false;
        n.removeAllConnections();
        nodes.remove(n);
        return true;
    }



    /*------------------------- HELPER CLASSES -------------------------*/

    /**
     * <p>Connections are directional, although they can be traversed backwards
     * if need be.</p>
     */
    public class Node {
        
        private Pt2Df point;
        private List<Connection> connections = new ArrayList<>();
        private List<Connection> backwardConnections = new ArrayList<>();

        public Node(Pt2Df p) {
            this.point = p;
        }

        @Override
        public String toString() {
            String str = "Node: " + connections.size() + " forward / "
                + backwardConnections.size() + " backward";
            for (Connection c : connections)
                str += "\n... forward ---> " + c;
            for (Connection c : backwardConnections)
                str += "\n... backward --> " + c;
            return str;
        }

        public Pt2Df getPoint() {
            return point;
        }

        public int getNumConnectionsForward() {
            return connections.size();
        }

        public int getNumConnectionsBackward() {
            return backwardConnections.size();
        }

        public List<Connection> getConnectionsForward() {
            return connections;
        }        

        public List<Connection> getConnectionsBackward() {
            return backwardConnections;
        }

        public void addConnection(Node n, int shapeID) {
            // Connection c = getAndAddConnection(n);
            Connection c = getConnection(n);
            if (c == null) {
                c = new Connection(this, n, shapeID);
                addConnection(c);
            } else {
                c.addID(shapeID);
            }
        }
        
        public void addConnection(Node n, Collection<Integer> shapeIDs) {
            Connection c = getConnection(n);
            if (c == null) {
                c = new Connection(this, n, shapeIDs);
                addConnection(c);
            } else {
                for (Integer id : shapeIDs)
                    c.addID(id);
            }
        }

        public void addConnection(Connection c) {
            connections.add(c);
            c.getDestination().backwardConnections.add(c);
        }

        private Connection getConnection(Node n) {
            // does connection exist already?
            for (Connection c : connections)
                if (c.getDestination().equals(n))
                    return c;
            return null;
        }

        /**
         * <p>Removes forward connection to point {@code dest} (if it exists),
         * and also the associated backward-connection in the destination
         * node.</p>
         */
        public void removeConnection(Node n) {
            Connection c = getConnection(n);
            if (c != null) {
                connections.remove(c);
                c.getDestination().backwardConnections.remove(c);
            }
        }

        public void removeAllConnections() {
            for (Connection c : connections)
                c.getDestination().backwardConnections.remove(c);
            for (Connection c : backwardConnections)
                c.getOrigin().connections.remove(c);
            connections.clear();
        }
    }

    public class SmallestDistComparator implements Comparator<Node> {

        private Pt2Df p;

        public SmallestDistComparator(Node n) {
            p = n.getPoint();
        }

        @Override
        public int compare(Node a, Node b) {
            double distA = getDist(a);
            double distB = getDist(b);
            if (distA < distB) return -1;
            if (distA > distB) return 1;
            else return 0;
        }

        private double getDist(Node n) {
            return Geom2D.distSquared(p, n.getPoint());
        }
        
    }

    /**
     * <p>A directed connection from one node to another.</p>
     */
    public class Connection {
        private Node origin;
        private Node dest;
        private Set<Integer> ids = new HashSet<>();
        private Linef line;

        public Connection(Node origin, Node destination, int shapeID) {
            this.origin = origin;
            this.dest = destination;
            ids.add(shapeID);
            line = new Linef(origin.getPoint(), destination.getPoint());
        }
        
        public Connection(Node origin, Node destination, Collection<Integer> ids) {
            this.origin = origin;
            this.dest = destination;
            for (Integer i : ids)
                this.ids.add(i);
            line = new Linef(origin.getPoint(), destination.getPoint());
        }

        @Override
        public String toString() {
            String str = "<Connection: <IDs: ";
            for (Integer i : ids)
                str += i + ", ";
            return str + "> " + line + ">";
        }
        
        public Node getOrigin() {
            return origin;
        }
        
        public Node getDestination() {
            return dest;
        }

        public Linef getLine() {
            return line;
        }

        public void addID(int id) {
            ids.add(id);
        }
        
        public Set<Integer> getIDs() {
            return ids;
        }
    }

}
