import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public final class VirtualWorld extends PApplet
{
    private static String[] ARGS;

    public static final int VIEW_WIDTH = 640;
    public static final int VIEW_HEIGHT = 480;
    public static final int TILE_WIDTH = 32;
    public static final int TILE_HEIGHT = 32;

    public static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
    public static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;

    public static final String IMAGE_LIST_FILE_NAME = "imagelist";
    public static final String DEFAULT_IMAGE_NAME = "background_default";
    public static final int DEFAULT_IMAGE_COLOR = 0x808080;

    public static final String FAST_FLAG = "-fast";
    public static final String FASTER_FLAG = "-faster";
    public static final String FASTEST_FLAG = "-fastest";
    public static final double FAST_SCALE = 0.5;
    public static final double FASTER_SCALE = 0.25;
    public static final double FASTEST_SCALE = 0.10;

    public String loadFile = "world.sav";
    public long startTimeMillis = 0;
    private static final int GHOST_ANIMATION_PERIOD = 200;
    private static final int GHOST_ACTION_PERIOD = 1;

    private static final int KNIGHT_ANIMATION_PERIOD = 200;
    private static final int KNIGHT_ACTION_PERIOD = 1;
    public double timeScale = 1.0;

    public ImageStore imageStore;
    public WorldModel world;
    public WorldView view;
    public EventScheduler scheduler;
    public int imageIndex;

    public void settings() {
        size(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /*
       Processing entry point for "sketch" setup.
    */
    public void setup() {
        parseCommandLine(ARGS);
        loadImages(IMAGE_LIST_FILE_NAME);
        loadWorld(loadFile, this.imageStore);

        this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world, TILE_WIDTH, TILE_HEIGHT);
        this.scheduler = new EventScheduler();
        this.startTimeMillis = System.currentTimeMillis();
        this.scheduleActions(world, scheduler, imageStore);
    }

    public void draw() {
        double appTime = (System.currentTimeMillis() - startTimeMillis) * 0.001;
        double frameTime = (appTime - scheduler.currentTime)/timeScale;
        this.update(frameTime);
        view.drawViewport();
    }

    public void update(double frameTime){
        scheduler.updateOnTime( frameTime);
    }

    // Just for debugging and for P5
    // Be sure to refactor this method as appropriate
    public void mousePressed() {
        Point pressed = mouseToPoint(mouseX, mouseY);

        Point p2 = new Point(pressed.x+1, pressed.y);
        Point p3 = new Point(pressed.x-1, pressed.y);
        Point p4 = new Point(pressed.x, pressed.y-1);
        Point p5 = new Point(pressed.x, pressed.y+1);
        Point p6 = new Point(pressed.x+2, pressed.y-1);
        Point p7 = new Point(pressed.x+2, pressed.y+1);
        Point p8 = new Point(pressed.x-2, pressed.y-1);
        Point p9 = new Point(pressed.x-2, pressed.y+1);

        List<Point> points = new ArrayList<>();
        //points.add(pressed);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        points.add(p5);
        points.add(p6);
        points.add(p7);
        points.add(p8);
        points.add(p9);
        List<Point> valid = points.stream().filter(p -> world.withinBounds(p)).collect(Collectors.toList());

        for (Point p: valid) {

            world.setBackgroundCell(p, new Background("stone", imageStore.getImageList("stone")));

            if (world.isOccupied(p) && world.getOccupancyCell(p).getClass() == Tree.class) {
                ((Tree) world.getOccupant(p).get()).setHealth(0);
            }

            if (world.isOccupied(p) && world.getOccupancyCell(p).getClass() == Dude_Full.class
                    || world.isOccupied(p) && world.getOccupancyCell(p).getClass() == Dude_Not_Full.class)  {
                Knight knight = new Knight("knight", p, imageStore.getImageList("knight"), KNIGHT_ANIMATION_PERIOD, KNIGHT_ACTION_PERIOD, imageIndex );
                world.removeEntity(world.getOccupant(p).get());

                world.addEntity(knight);
                knight.scheduleActions(scheduler, world, imageStore);
            }
        }

       eventNewEntity(pressed);
    }

    private void eventNewEntity(Point pressed) {
        Ghost ghost = new Ghost("ghost", pressed, imageStore.getImageList("ghost"), GHOST_ACTION_PERIOD, GHOST_ANIMATION_PERIOD, imageIndex);
        world.addEntity(ghost);
        ghost.scheduleActions(scheduler, world, imageStore);
    }

    public void scheduleActions(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        for (Entity entity : world.entities) {
            if(entity instanceof Obstacle){
                ((Obstacle)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Fairy){
                ((Fairy)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Dude_Not_Full){
                ((Dude_Not_Full)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Dude_Full){
                ((Dude_Full)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Sapling){
                ((Sapling)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Tree){
                ((Tree)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Knight){
                ((Knight)entity).scheduleActions(scheduler, world, imageStore);
            }
            else if(entity instanceof Ghost){
                ((Ghost)entity).scheduleActions(scheduler, world, imageStore);
            }

        }
    }

    private Point mouseToPoint(int x, int y) {
        return view.getViewport().viewportToWorld(mouseX / TILE_WIDTH, mouseY / TILE_HEIGHT);
    }

    public void keyPressed() {
        if (key == CODED) {
            int dx = 0;
            int dy = 0;

            switch (keyCode) {
                case UP -> dy -= 1;
                case DOWN -> dy += 1;
                case LEFT -> dx -= 1;
                case RIGHT -> dx += 1;
            }
            view.shiftView( dx, dy);
        }
    }

    public static Background createDefaultBackground(ImageStore imageStore) {
        return new Background(DEFAULT_IMAGE_NAME, imageStore.getImageList(DEFAULT_IMAGE_NAME));
    }

    public static PImage createImageColored(int width, int height, int color) {
        PImage img = new PImage(width, height, RGB);
        img.loadPixels();
        Arrays.fill(img.pixels, color);
        img.updatePixels();
        return img;
    }

    public void loadImages(String filename) {
        this.imageStore = new ImageStore(createImageColored(TILE_WIDTH, TILE_HEIGHT, DEFAULT_IMAGE_COLOR));
        try {
            Scanner in = new Scanner(new File(filename));
            imageStore.loadImages(in, this);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public void loadWorld(String file, ImageStore imageStore) {
        this.world = new WorldModel();
        try {
            Scanner in = new Scanner(new File(file));
            world.load(in, imageStore, createDefaultBackground(imageStore));
        } catch (FileNotFoundException e) {
            Scanner in = new Scanner(file);
            world.load(in, imageStore, createDefaultBackground(imageStore));
        }
    }

    public void parseCommandLine(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case FAST_FLAG -> timeScale = Math.min(FAST_SCALE, timeScale);
                case FASTER_FLAG -> timeScale = Math.min(FASTER_SCALE, timeScale);
                case FASTEST_FLAG -> timeScale = Math.min(FASTEST_SCALE, timeScale);
                default -> loadFile = arg;
            }
        }
    }


    public static void main(String[] args) {
        VirtualWorld.ARGS = args;
        PApplet.main(VirtualWorld.class);
    }
}
