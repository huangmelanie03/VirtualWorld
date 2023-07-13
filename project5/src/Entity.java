import processing.core.PImage;

import java.util.List;
import java.util.Random;

public abstract class Entity {
    public String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    public int resourceLimit;
    public int resourceCount;
    public double actionPeriod;
    public double animationPeriod;
    public int health;
    public int healthLimit;

    public static final String STUMP_KEY = "stump";
    public static final int PROPERTY_KEY = 0;
    public static final int STUMP_NUM_PROPERTIES = 0;
    public static AnimationEntity animate;

    public Entity(String id, Point position, List<PImage> images) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.health = health;
        this.healthLimit = healthLimit;
    }

    public String getId(){
        return this.id;
    }


    public int getIntFromRange(int max, int min) {
        Random rand = new Random();
        return min + rand.nextInt(max-min);
    }

    public double getNumFromRange(double max, double min) {
        Random rand = new Random();
        return min + rand.nextDouble() * (max - min);
    }
    public List<PImage> getImages(){

        return this.images;
    }
    public int getImageIndex(){
        return this.imageIndex;
    }
    public abstract Point getPosition();
    public abstract void setPosition(Point pos);
    public int getIndex(){
        return this.imageIndex;
    }
    public void nextImage() {
        this.imageIndex = this.getImageIndex() + 1;
    }

    /**
     * Helper method for testing. Preserve this functionality while refactoring.
     */
    public abstract String log();

    public PImage getCurrentImage(Object object) {
        if (object instanceof Background background) {
            return background.images.get(background.imageIndex);
        } else if (object instanceof Entity entity) {
            return this.images.get(this.imageIndex % this.images.size());
        } else {
            throw new UnsupportedOperationException(String.format("getCurrentImage not supported for %s", object));
        }
    }

}
