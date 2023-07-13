import processing.core.PImage;

import java.util.List;

public abstract class ActivityEntity extends AnimationEntity {
    private double actionPeriod;
    private int imageIndex;
    private List<PImage> images;

    public ActivityEntity(String id, Point position, List<PImage> images,  double actionPeriod, double animationPeriod){
        super(id, position, images, animationPeriod);
        this.actionPeriod = actionPeriod;
        //this.imageIndex = imageIndex;

    }

    public Point nextPosition(WorldModel world, Point destPos) {
        return null;
    }

    public double getActionPeriod() {
        return actionPeriod;
    }

    @Override
    public abstract void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore);



}
