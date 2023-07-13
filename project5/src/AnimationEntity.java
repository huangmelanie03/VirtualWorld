import processing.core.PImage;

import java.util.List;

public abstract class AnimationEntity extends Entity implements Position{

    public AnimationEntity(String id, Point position, List<PImage> images, double animationPeriod){
        super(id, position, images);
        this.animationPeriod = animationPeriod;
    }
    public double getAnimationPeriod(){
        return this.animationPeriod;
    }

    public abstract void nextImage();

    public abstract void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore);


    public abstract Point nextPosition(WorldModel world, Point destPos);
}
