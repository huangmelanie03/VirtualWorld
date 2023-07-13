import processing.core.PImage;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Dude_Not_Full extends Dude implements Move{
    public PathingStrategy newPath = new AStarPathingStrategy();
    private String id;
    private Point position;
    private double actionPeriod, animationPeriod;
    private int resourceLimit, resourceCount;
    private List<PImage> images;
    private int imageIndex;
    public Dude_Not_Full(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int resourceLimit) {
        super(id, position, images, actionPeriod, animationPeriod);
        this.resourceLimit = resourceLimit;
        this.position = position;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.images = images;
        this.id =id;
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
    private boolean transformNotFull(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.resourceCount >= this.resourceLimit) {
            Dude_Full dude = new Dude_Full(this.getId(), this.getPosition(), this.getActionPeriod(), this.getImages(), this.getAnimationPeriod(), this.resourceLimit);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents( this);

            world.addEntity(dude);
            dude.scheduleActions(scheduler, world, imageStore);

            return true;
        }

        return false;
    }
    @Override
    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (Position.adjacent(this.getPosition(), target.getPosition())) {
            this.resourceCount += 1;
            if(target instanceof Tree){
                ((Tree) target).setHealth( ((Tree) target).getHealth() -1);
            }
            else if( target instanceof Sapling){
                ((Sapling) target).setHealth( ((Sapling) target).getHealth() -1);
            }

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
        this.imageIndex = this.getImageIndex() + 1;
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.getActionPeriod());
        scheduler.scheduleEvent(this, new AnimationAction(this, 0), this.getAnimationPeriod());
    }
    public void executeDudeNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> target = findNearest(world, this.getPosition(), new ArrayList<>(Arrays.asList(Tree.class, Sapling.class)));

        if (target.isEmpty() || !this.moveTo(world, target.get(), scheduler) || !this.transformNotFull( world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, new Activity(this,  world, imageStore), this.getActionPeriod());
        }
    }
    @Override
    public String log(){
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.getId(), this.getPosition().x, this.getPosition().y, this.getIndex());
    }

    public PImage getCurrentImage(){
        return this.images.get(this.imageIndex % this.images.size());
    }
    public List<PImage> getImages(){return this.images;}
    //public int getimageIndex(){return this.imageIndex;}


    @Override
    public Point Position(WorldModel world, Point destPos) {
        return null;
    }
}
