import processing.core.PImage;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Fairy extends ActivityEntity implements Move{
    public PathingStrategy newPath = new AStarPathingStrategy();
    private String id;
    private Point position;
    public static final String SAPLING_KEY = "sapling";
    private double actionPeriod,animationPeriod;
    private List<PImage> images;
    private final double SAPLING_ACTION_ANIMATION_PERIOD = 1.000; // have to be in sync since grows and gains health at same time
    private final int SAPLING_HEALTH_LIMIT = 5;
    private int imageIndex;


    public Fairy(String id, Point position, double actionPeriod, double animationPeriod, List<PImage> images, int imageIndex ){
        super(id, position, images, actionPeriod, animationPeriod);
        this.id = id;
        this.position = position;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.images = images;
        this.imageIndex = imageIndex;
    }

    @Override
    public Point getPosition(){
        return this.position;
    }
    @Override
    public void setPosition(Point pos){
        this.position = pos;
    }
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }
    public PImage getCurrentImage(){
        return this.images.get(this.imageIndex % this.images.size());
    }
    public List<PImage> getImages(){
        return this.images;
    }



    @Override
    public Point nextPosition(WorldModel world, Point destPos) {
        /*
        int horiz = Integer.signum(destPos.x - this.position.x);
        Point newPos = new Point(this.position.x + horiz, this.position.y);

        if (horiz == 0 || world.isOccupied(newPos)) {
            int vert = Integer.signum(destPos.y - this.position.y);
            newPos = new Point(this.position.x, this.position.y + vert);

            if (vert == 0 || world.isOccupied(newPos)) {
                newPos = this.position;
            }
        }

         */

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

    public void executeFairyActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fairyTarget = super.findNearest(world, this.position, new ArrayList<>(List.of(Stump.class)));

        if (fairyTarget.isPresent()) {
            Point tgtPos = fairyTarget.get().getPosition();

            if (this.moveTo(world, fairyTarget.get(), scheduler)) {

                Sapling sapling = new Sapling( SAPLING_KEY + "_" + fairyTarget.get().id, tgtPos, imageStore.getImageList(WorldModel.SAPLING_KEY), 0,SAPLING_ACTION_ANIMATION_PERIOD, SAPLING_ACTION_ANIMATION_PERIOD, 0, SAPLING_HEALTH_LIMIT);

                world.addEntity(sapling);
                sapling.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent( this, new Activity(this, world, imageStore), this.actionPeriod);
    }
    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.actionPeriod);
        scheduler.scheduleEvent(this, new AnimationAction(this,0), this.getAnimationPeriod());
    }
    public String log(){
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }

    @Override
    public Point Position(WorldModel world, Point destPos) {
        return null;
    }
}
