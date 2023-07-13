import processing.core.PImage;

import java.util.List;

public class Tree extends ActivityEntity {


    private int health;
    private Point position;
    private double actionPeriod, animationPeriod;

    private List<PImage> images;
    private String id;
    private int imageIndex;


    public Tree(String id, Point position, List<PImage> images, int imageIndex, double actionPeriod, double animationPeriod, int health){
        super(id, position, images, actionPeriod, animationPeriod);
        this.health = health;
        this.position = position;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.images = images;
        this.id = id;
        this.imageIndex = imageIndex;


    }
    public int getHealth(){
        return this.health;
    }
    public void setHealth(int health){
        this.health = health;
    }
    public void nextImage() {

        this.imageIndex = this.imageIndex + 1;
    }
    public boolean transformTree(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.health <= 0) {
            Stump stump = new Stump(STUMP_KEY + "_" + this.id, this.position, imageStore.getImageList(STUMP_KEY), 0);

            world.removeEntity(this);

            world.addEntity(stump);

            return true;
        }

        return false;
    }
    public int getimageIndex(){
        return this.imageIndex;
    }
    public PImage getCurrentImage(){
        return this.images.get(this.imageIndex % this.images.size());
    }
    public List<PImage> getImages(){
        return this.images;
    }

    public void executeTreeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
//        Sapling ent = new Sapling( id, position,images, actionPeriod, animationPeriod, health, healthLimit);
//
        if (!this.transformTree(world, scheduler, imageStore)) {

            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.actionPeriod);
            //        }
        }
    }
    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.actionPeriod);
        scheduler.scheduleEvent(this, new AnimationAction(this,0), this.getAnimationPeriod());
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
    public String log(){
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }


    @Override
    public Point Position(WorldModel world, Point destPos) {
        return null;
    }
}
