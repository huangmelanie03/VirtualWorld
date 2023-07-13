import processing.core.PImage;

import java.util.List;

public abstract class Dude extends ActivityEntity {
    public double actionPeriod;
    private Point position;
    private String id;
    private List<PImage> images;
    private int imageIndex;

    public Dude(String id, Point position, List<PImage> images,  double actionPeriod, double animationPeriod){
        super(id,  position, images, actionPeriod, animationPeriod);

        this.id = id;
        this.position = position;
        this.images = images;
        this.actionPeriod = actionPeriod;
        //this.imageIndex = imageIndex;
        this.animationPeriod = animationPeriod;
    }


}