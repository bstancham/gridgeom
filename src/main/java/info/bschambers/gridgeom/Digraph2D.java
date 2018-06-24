package info.bschambers.gridgeom;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Stack;
import java.util.Collection;

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

        // collect all points of new line (except start) into a sorted set so
        // that they will be in the correct order to make connections at the end
        SortedSet<Pt2Df> linePoints
            = new TreeSet<>(new Pt2Df.SmallestDistComparator(p1));
        linePoints.add(p2);

        // keep track of what splits need to be made in existing connections
        List<SplitDirective> splitters = new ArrayList<>();
        
        // check for intersections with existing lines...
        Linef newLine = new Linef(p1, p2);
        for (Node n : nodes) {
            for (Connection c : n.getConnectionsForward()) {
                Linef oldLine = c.getLine();

                // DO LINES INTERSECT?

                List<Pt2Df> ipts = new ArrayList<>();
                if (newLine.intersects(oldLine)) {
                    // normal intersection

                    Pt2Df ip = newLine.getIntersectionPoint(oldLine);

                    // for new line
                    ipts.add(ip);
                    // linePoints.add(ip);

                    // split-directive for old line
                    if (!ip.equals(oldLine.start()) &&
                        !ip.equals(oldLine.end())) {
                        
                        splitters.add(new SplitDirective(c.getOrigin().getPoint(),
                                                         c.getDestination().getPoint(),
                                                         ip));
                    }

                } else if (newLine.isParallel(oldLine)) {

                    // new line split
                    // ... easy - just add to line-points
                    
                    if (newLine.contains(oldLine.start()) &&
                        !newLine.hasVertex(oldLine.start()))
                        ipts.add(oldLine.start());
                    
                    if (newLine.contains(oldLine.end()) &&
                        !newLine.hasVertex(oldLine.end()))
                        ipts.add(oldLine.end());


                    
                    // old line split
                    // ... 

                    boolean ns =
                        !oldLine.hasVertex(newLine.start()) &&
                        oldLine.contains(newLine.start());
                    boolean ne =
                        !oldLine.hasVertex(newLine.end()) &&
                        oldLine.contains(newLine.end());

                    // both?
                    if (ns && ne) {

                        Pt2Df s = c.getOrigin().getPoint();
                        Pt2Df i1 = newLine.start();
                        Pt2Df i2 = newLine.end();
                        Pt2Df e = c.getDestination().getPoint();
                        if (Geom2D.distSquared(s, i2) < Geom2D.distSquared(s, i1)) {
                            // i2 is nearer to start - swap order
                            i1 = newLine.end();
                            i2 = newLine.start();
                        }
                        splitters.add(new SplitDirective(s, e, i1));
                        splitters.add(new SplitDirective(i1, e, i2));
                        
                    } else if (ns) {
                        splitters.add(new SplitDirective(c.getOrigin().getPoint(),
                                                         c.getDestination().getPoint(),
                                                         newLine.start()));
                    } else if (ne) {
                        splitters.add(new SplitDirective(c.getOrigin().getPoint(),
                                                         c.getDestination().getPoint(),
                                                         newLine.end()));
                    }

                }

                for (Pt2Df ip : ipts) {
                    // add node for new line
                    // ... only if point is not same as start point or end
                    if (!ip.equals(newLine.start()) &&
                        !ip.equals(newLine.end())) {
                        linePoints.add(ip);
                    }
                }
            }   
        }

        // add new line (segmented as required)
        Pt2Df origin = p1;
        for (Pt2Df dest : linePoints) {
            addConnection(origin, dest, shapeID);
            origin = dest;
        }

        for (SplitDirective sd : splitters) {
            boolean result = splitConnection(sd);
        }
    }

    private boolean splitConnection(SplitDirective sd) {
        Connection c = getConnection(sd.start, sd.end);
        if (c == null)
            return false;

        // get rid of old connection
        c.getOrigin().removeConnection(c.getDestination());
        // add two new connections
        Node inode = getAndAddNode(sd.splitPoint);
        c.getOrigin().addConnection(inode, c.getIDs());
        inode.addConnection(c.getDestination(), c.getIDs());
        return true;
    }

    /**
     * <p>Gets the {@code Connection} from {@code origin} to {@code
     * destination}, if it exists - otherwise returns {@code null}.</p>
     */
    private Connection getConnection(Pt2Df origin, Pt2Df destination) {
        Node n = getNode(origin);
        if (n == null)
            return null;
        return n.getConnection(destination);
    }

    private class SplitDirective {
        public final Pt2Df start;
        public final Pt2Df end;
        public final Pt2Df splitPoint;
        public SplitDirective(Pt2Df start, Pt2Df end, Pt2Df splitPoint) {
            this.start = start;
            this.end = end;
            this.splitPoint = splitPoint;
        }
    }
    
    /**
     * <p>Adds a connection between {@code Node} at point {@code p1} and {@code
     * Node} at point {@code p2}, creating each {@code Node} if it doesn't
     * already exist.</p>
     */
    private void addConnection(Pt2Df p1, Pt2Df p2, int shapeID) {
        Node n1 = getAndAddNode(p1);
        Node n2 = getAndAddNode(p2);
        n1.addConnection(n2, shapeID);
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
            if (c.getDestination() == this)
                throw new IllegalArgumentException("Node may not be connected to itself");
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
         * @return The {@code Connection} from this {@code Node} to the {@code
         * Node} at point {@code p}, or returns {@code null} if no such {@code
         * Connection} exists.
         */
        private Connection getConnection(Pt2Df p) {
            for (Connection c : connections)
                if (c.getDestination().getPoint().equals(p))
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
