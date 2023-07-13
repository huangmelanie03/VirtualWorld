public class Node{

    private Point current;
    private int g;
    private double h;
    private double f;
    private Node prior;


    public Node(Point current, int g, double h, double f, Node prior) {
        this.current = current;
        this.g = g;
        this.h = h;
        this.f = g+h;
        this.prior = prior;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        return ((Node)other).current.equals(this.current);
    }

    public int getG(){return g;}
    public double getH(){return h;}
    public double getF(){return f;}
    public Node getPrior(){return prior;}
    public Point getCurrent() { return current;}

    @Override
    public String toString() {
        return "Point: " + current + " g: " + g + " h: " + h + " f: " + f + " previous: " +prior;
    }
}
