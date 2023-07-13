import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public interface Position {
    public Point Position(WorldModel world, Point destPos);
    default Optional<Entity> findNearest(WorldModel world, Point pos, List<Class> entity_classes) {
        List<Entity> ofType = new LinkedList<>();
        for (Class ent_class : entity_classes) {
            for (Entity entity : world.entities) {
                if (entity.getClass().equals(ent_class)) {
                    ofType.add(entity);
                }
            }
        }

        return pos.nearestEntity(ofType);
    }
    static boolean adjacent(Point p1, Point p2) {
        return (p1.x == p2.x && Math.abs(p1.y - p2.y) == 1) || (p1.y == p2.y && Math.abs(p1.x - p2.x) == 1);
    }
}