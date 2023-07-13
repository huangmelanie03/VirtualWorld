import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Dude_Full extends Dude implements Move{
    public PathingStrategy newPath = new AStarPathingStrategy();
    private String id;
    private Point position;
    private int resourceLimit;
    private double actionPeriod, animationPeriod;
    private List<PImage> images;
    private int imageIndex;

    public Dude_Full(String id, Point position, double actionPeriod, List<PImage> images, double animationPeriod, int resourceLimit){
        super(id, position, images, actionPeriod, animationPeriod);
        this.resourceLimit = resourceLimit;
        this.id = id;
        this.position = position;
        this.actionPeriod = actionPeriod;
        this.images = images;
        this.animationPeriod = animationPeriod;
        //this.imageIndex = imageIndex;

    }




    private void transformFull(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        Dude_Not_Full dude = new Dude_Not_Full(this.getId(), this.getPosition(), this.getImages(), this.getActionPeriod(), this.getAnimationPeriod(), this.resourceLimit);

        world.removeEntity(this);

        world.addEntity(dude);
        dude.scheduleActions(scheduler, world, imageStore);
    }
    @Override
    public Point nextPosition(WorldModel world, Point destPos) {
        /*
        int horiz = Integer.signum(destPos.x - this.position.x);
        Point newPos = new Point(this.position.x + horiz, this.position.y);

        if (horiz == 0 || world.isOccupied(newPos) && world.getOccupancyCell(newPos).getClass() != Stump.class) {
            int vert = Integer.signum(destPos.y - this.position.y);
            newPos = new Point(this.position.x, this.position.y + vert);

            if (vert == 0 || world.isOccupied(newPos) && world.getOccupancyCell(newPos).getClass() != Stump.class) {
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
        if (Position.adjacent(this.getPosition(), target.getPosition())) {
            return true;
        } else {
            Point nextPos = this.nextPosition(world, target.getPosition());

            if (!this.getPosition().equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }


    @Override
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.getActionPeriod());
        scheduler.scheduleEvent(this, new AnimationAction(this,0), this.getAnimationPeriod());
    }
    public void executeDudeFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fullTarget = findNearest(world, this.getPosition(), new ArrayList<>(List.of(House.class)));

        if (fullTarget.isPresent() && this.moveTo( world, fullTarget.get(), scheduler)) {
            this.transformFull(world, scheduler, imageStore);
        } else {
            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.getActionPeriod());
        }
    }

    @Override
    public Point getPosition() {return position;}

    @Override
    public void setPosition(Point pos){
        this.position = pos;
    }
    @Override
    public String log(){
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.getId(), this.getPosition().x, this.getPosition().y, this.getIndex());
    }

    @Override
    public Point Position(WorldModel world, Point destPos) {
        return null;
    }
}

