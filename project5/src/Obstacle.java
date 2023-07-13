import processing.core.PImage;

import java.util.*;

public class Obstacle extends AnimationEntity {
    private String id;
    private Point position;
    double animationPeriod;
    private List<PImage> images;
    private int imageIndex;

    public Obstacle(String id, Point position, double animationPeriod, List<PImage> images, int imageIndex){
        super(id, position, images, animationPeriod);
        this.id = id;
        this.position = position;
        this.images = images;
        this.animationPeriod = animationPeriod;
        this.imageIndex = imageIndex;

    }

    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(this, new AnimationAction(this,0), this.getAnimationPeriod());

    }
    public void nextImage() {

        this.imageIndex = this.imageIndex + 1;
    }
    @Override
    public Point getPosition(){

        return this.position;
    }
    @Override
    public void setPosition(Point pos){

        this.position = pos;
    }
    public String log(){
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }
    public PImage getCurrentImage(){

        return this.images.get(this.imageIndex % this.images.size());
    }
    public List<PImage> getImages(){

        return this.images;
    }
    public int getImageIndex(){

        return this.imageIndex;
    }




    @Override
    public Point nextPosition(WorldModel world, Point destPos) {
        return null;
    }

    @Override
    public Point Position(WorldModel world, Point destPos) {
        return null;
    }
}
