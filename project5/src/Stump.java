import processing.core.PImage;

import java.util.List;

public class Stump extends Entity{

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;

    public Stump(String id, Point position, List<PImage> images, int imageIndex){
        super(id, position,images);
        this.id = id;
        this.position = position;
        this.imageIndex = imageIndex;
        this.images = images;

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



}
