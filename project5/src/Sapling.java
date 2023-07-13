import processing.core.PImage;

import java.util.List;


public class Sapling extends ActivityEntity {
    public double actionPeriod, animationPeriod;
    private int health;

    private final double TREE_ACTION_MAX = 1.400;
    public static final String TREE_KEY = "tree";

    private final double TREE_ACTION_MIN = 1.000;
    private final double TREE_ANIMATION_MAX = 0.600;
    private final double TREE_ANIMATION_MIN = 0.050;

    private final int TREE_HEALTH_MAX = 3;
    private final int TREE_HEALTH_MIN = 1;
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;

    public Sapling(String id, Point position, List<PImage> images, int imageIndex, double actionPeriod, double animationPeriod, int health, int healthLimit){
        super(id, position, images, actionPeriod, animationPeriod);
        this.id = id;
        this.position = position;
        this.images = images;
        this.health = health;
        this.healthLimit = healthLimit;
        this.imageIndex = imageIndex;
        this.animationPeriod = animationPeriod;
        this.actionPeriod = actionPeriod;

    }

    public int getimageIndex(){
        return this.imageIndex;
    }

    public List<PImage> getImages(){
        return this.images;
    }
    public PImage getCurrentImage(){
        return this.images.get(this.imageIndex % this.images.size());
    }
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }
    public boolean transformSapling(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.health <= 0) {
            Stump stump = new Stump(STUMP_KEY + "_" + this.id, this.position, imageStore.getImageList(STUMP_KEY), 0);

            world.removeEntity(this);

            world.addEntity(stump);

            return true;
        } else if (this.health >= this.healthLimit) {
            Tree tree = new Tree( TREE_KEY + "_" + this.id, this.position, imageStore.getImageList(WorldModel.TREE_KEY), 0,super.getNumFromRange(TREE_ACTION_MAX, TREE_ACTION_MIN), super.getNumFromRange(TREE_ANIMATION_MAX, TREE_ANIMATION_MIN), super.getIntFromRange(TREE_HEALTH_MAX, TREE_HEALTH_MIN));

            world.removeEntity(this);

            world.addEntity(tree);
            tree.scheduleActions(scheduler, world, imageStore);

            return true;
        }

        return false;
    }
    public int getHealth(){

        return this.health;
    }
    public void setHealth(int health){
        this.health = health;
    }

    public void executeSaplingActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        this.health++;
        if (!this.transformSapling(world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.actionPeriod);
        }
    }
    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(this,new Activity(this, world, imageStore), this.actionPeriod);
        scheduler.scheduleEvent(this, new AnimationAction(this,0), this.getAnimationPeriod());
    }


    @Override
    public Point getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Point pos) {
        this.position = pos;
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
