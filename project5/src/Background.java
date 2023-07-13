import java.util.List;

import processing.core.PImage;

/**
 * Represents a background for the 2D world.
 */
public final class Background
{
    public final String id;
    public final List<PImage> images;
    public int imageIndex;

    public Background(String id, List<PImage> images) {
        this.id = id;
        this.images = images;
    }
    public PImage getCurrentImage() {
        return images.get(imageIndex);
    }
    public  PImage getCurrentImage(Object object) {
        if (object instanceof Background background) {
            return this.images.get(this.imageIndex);
        } else if (object instanceof Entity entity) {
            return entity.getImages().get(entity.getIndex() % entity.getImages().size());
        } else {
            throw new UnsupportedOperationException(String.format("getCurrentImage not supported for %s", object));
        }
    }
}
