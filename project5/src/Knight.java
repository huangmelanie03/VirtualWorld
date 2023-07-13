import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Knight extends ActivityEntity{

    public PathingStrategy newPath = new AStarPathingStrategy();
    private String id;
    private Point position;
    private double actionPeriod,animationPeriod;
    private List<PImage> images;
    private int imageIndex;
    public static final String FAIRY_KEY = "fairy";
    private final int FAIRY_ANIMATION_PERIOD = 0;
    private final int FAIRY_ACTION_PERIOD = 1;
    private final int FAIRY_NUM_PROPERTIES = 2;

    public Knight(String id, Point position, List<PImage> images, double animationPeriod, double actionPeriod, int imageIndex){
        super(id, position, images, animationPeriod, actionPeriod);
        this.id = id;
        this.position = position;
        this.actionPeriod = 1;
        this.animationPeriod = animationPeriod;
        this.images = images;
        this.imageIndex = imageIndex;
    }

    public void executeKnightActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> knightTarget = super.findNearest(world, this.position, new ArrayList<>(List.of(Ghost.class)));

        if (knightTarget.isPresent()) {
            Point tgtPos = knightTarget.get().getPosition();

            if (this.moveTo(world, knightTarget.get(), scheduler)) {

                world.removeEntity(knightTarget.get());
                scheduler.unscheduleAllEvents(knightTarget.get());
                /*
                if(!world.isOccupied(tgtPos)){

                    Fairy fairy = new Fairy( FAIRY_KEY + "_" + knightTarget.get().id, tgtPos, FAIRY_ACTION_PERIOD, FAIRY_ANIMATION_PERIOD, imageStore.getImageList(WorldModel.FAIRY_KEY), 0);

                    world.addEntity(fairy);
                    fairy.scheduleActions(scheduler, world, imageStore);
                }

                 */
            }
        }

        scheduler.scheduleEvent( this, new Activity(this, world, imageStore), this.actionPeriod);
    }

    private boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
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

    @Override
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
