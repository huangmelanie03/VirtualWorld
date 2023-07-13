import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;


public class Ghost extends ActivityEntity {

    public PathingStrategy newPath = new AStarPathingStrategy();
    private String id;
    private Point position;
    private double actionPeriod,animationPeriod;
    private List<PImage> images;

    private int imageIndex;

    public Ghost(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int imageIndex){
        super(id, position, images, actionPeriod, animationPeriod);
        this.id = id;
        this.position = position;
        this.actionPeriod = 1;
        this.animationPeriod = animationPeriod;
        this.images = images;
        this.imageIndex = imageIndex;
    }

    public void executeGhostActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> ghostTarget = super.findNearest(world, this.position, new ArrayList<>(List.of(Fairy.class)));

        if (ghostTarget.isPresent()) {

            if (this.moveTo(world, ghostTarget.get(), scheduler)) {

                world.removeEntity(ghostTarget.get());
                scheduler.unscheduleAllEvents(ghostTarget.get());
            }
        }

        scheduler.scheduleEvent( this, new Activity(this, world, imageStore), this.actionPeriod);
    }

    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (Position.adjacent(this.position, target.getPosition())) {
            world.removeEntity(target);
            return true;
        } else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!this.position.equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }

    public Point nextPosition(WorldModel world, Point destPos) {
        List<Point> newPos = newPath.computePath(getPosition(), destPos, canPass(world), inReach(world), PathingStrategy.CARDINAL_NEIGHBORS);
        if(newPos.size() == 0){
            return getPosition();
        }
        return newPos.get(0);
    }
    public Predicate<Point> canPass(WorldModel world){
        return point -> world.withinBounds(point) && !world.isOccupied(point);
    }

    public BiPredicate<Point, Point> inReach(WorldModel world){
        return (point, point2) -> point.adjacent(point2);
    }

    @Override
    public Point getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Point pos) {
        this.position = pos;
    }

    @Override
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }


    public String log() {
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.actionPeriod);
        scheduler.scheduleEvent(this, new AnimationAction(this,0), this.getAnimationPeriod());
    }


    @Override
    public Point Position(WorldModel world, Point destPos) {
        return null;
    }
}
