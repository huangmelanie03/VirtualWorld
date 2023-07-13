import processing.core.PImage;

import java.util.*;

/**
 * Represents the 2D World in which this simulation is running.
 * Keeps track of the size of the world, the background image for each
 * location in the world, and the entities that populate the world.
 */
final class WorldModel {

    public int numRows;
    public int numCols;
    private final int SAPLING_NUM_PROPERTIES = 1;
    private final int SAPLING_HEALTH = 0;
    private final double SAPLING_ACTION_ANIMATION_PERIOD = 1000; // have to be in sync since grows and gains health at same time
    private final int SAPLING_HEALTH_LIMIT = 5;
    public static final String SAPLING_KEY = "sapling";

    public Background[][] background;
    public Entity[][] occupancy;
    public Set<Entity> entities;
    public static final String TREE_KEY = "tree";

    private final int TREE_ANIMATION_PERIOD = 0;
    private final int TREE_ACTION_PERIOD = 1;
    private final int TREE_HEALTH = 2;
    private final int TREE_NUM_PROPERTIES = 3;
    public static final String OBSTACLE_KEY = "obstacle";
    private final int OBSTACLE_ANIMATION_PERIOD = 0;
    private final int OBSTACLE_NUM_PROPERTIES = 1;
    public static final String HOUSE_KEY = "house";
    private final int HOUSE_NUM_PROPERTIES = 0;
    private Background rainbow;

    public static final String DUDE_KEY = "dude";
    private final int DUDE_ACTION_PERIOD = 0;
    private final int DUDE_ANIMATION_PERIOD = 1;
    private final int DUDE_LIMIT = 2;
    private final int DUDE_NUM_PROPERTIES = 3;
    public static final String FAIRY_KEY = "fairy";
    private final int FAIRY_ANIMATION_PERIOD = 0;
    private final int FAIRY_ACTION_PERIOD = 1;
    private final int FAIRY_NUM_PROPERTIES = 2;
    public static final String GHOST_KEY = "ghost";
    private static final int GHOST_NUM_PROPERTIES = 6;
    private static final int GHOST_ANIMATION_PERIOD = 0;
    private static final int GHOST_ACTION_PERIOD = 1;

    public static final String KNIGHT_KEY = "knight";
    private static final int KNIGHT_NUM_PROPERTIES = 6;
    private static final int KNIGHT_ANIMATION_PERIOD = 0;
    private static final int KNIGHT_ACTION_PERIOD = 1;

    private final int PROPERTY_ID = 1;
    private final int PROPERTY_COL = 2;
    private final int PROPERTY_ROW = 3;
    private final int ENTITY_NUM_PROPERTIES = 4;
    private final Random rand = new Random();
    private final List<String> PATH_KEYS = new ArrayList<>(Arrays.asList("bridge", "dirt", "dirt_horiz", "dirt_vert_left", "dirt_vert_right", "dirt_bot_left_corner", "dirt_bot_right_up", "dirt_vert_left_bot"));

    public WorldModel() {
        this.rainbow = rainbow;
    }

    /**
     * Helper method for testing. Don't move or modify this method.
     */
    public List<String> log(){
        List<String> list = new ArrayList<>();
        for (Entity entity : entities) {
            String log = entity.log();
            if(log != null) list.add(log);
        }
        return list;
    }
    public  Optional<PImage> getBackgroundImage(Point pos) {
        if (this.withinBounds(pos)) {
            return Optional.of(this.background[pos.y][pos.x].getCurrentImage(this.getBackgroundCell(pos)));
        } else {
            return Optional.empty();
        }
    }
    public Entity getOccupancyCell(Point pos) {

        return this.occupancy[pos.y][pos.x];
    }
    public void setOccupancyCell(Point pos, Entity entity) {
        this.occupancy[pos.y][pos.x] = entity;
    }
    public void parseBackgroundRow(String line, int row, ImageStore imageStore) {
        String[] cells = line.split(" ");
        if(row < this.numRows){
            int rows = Math.min(cells.length, this.numCols);
            for (int col = 0; col < rows; col++){
                this.background[row][col] = new Background(cells[col], imageStore.getImageList( cells[col]));
            }
        }
    }
    public void parseTree(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == TREE_NUM_PROPERTIES) {
            Tree entity = new Tree(id, pt, imageStore.getImageList(TREE_KEY), 0,Double.parseDouble(properties[TREE_ACTION_PERIOD]), Double.parseDouble(properties[TREE_ANIMATION_PERIOD]), Integer.parseInt(properties[TREE_HEALTH]));
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", TREE_KEY, TREE_NUM_PROPERTIES));
        }
    }
    public void parseObstacle(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == OBSTACLE_NUM_PROPERTIES) {
            Obstacle entity = new Obstacle(id, pt, Double.parseDouble(properties[OBSTACLE_ANIMATION_PERIOD]), imageStore.getImageList(OBSTACLE_KEY), 0);
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", OBSTACLE_KEY, OBSTACLE_NUM_PROPERTIES));
        }
    }
    public void parseHouse(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == HOUSE_NUM_PROPERTIES) {
            House entity = new House(id, pt, 0,imageStore.getImageList( HOUSE_KEY));
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", HOUSE_KEY, HOUSE_NUM_PROPERTIES));
        }
    }
    public void load(Scanner saveFile, ImageStore imageStore, Background defaultBackground) {
        this.parseSaveFile(saveFile, imageStore, defaultBackground);
        if (this.background == null) {
            this.background = new Background[this.numRows][this.numCols];
            for (Background[] row : this.background)
                Arrays.fill(row, defaultBackground);
        }
        if (this.occupancy == null) {
            this.occupancy = new Entity[this.numRows][this.numCols];
            this.entities = new HashSet<>();
        }
    }
    public boolean isOccupied(Point pos) {
        return this.withinBounds(pos) && this.getOccupancyCell(pos) != null;
    }
    public boolean withinBounds(Point pos) {
        return pos.y >= 0 && pos.y < this.numRows && pos.x >= 0 && pos.x < this.numCols;
    }
    public void moveEntity(EventScheduler scheduler, Entity entity, Point pos) {
        Point oldPos = entity.getPosition();
        if (this.withinBounds(pos) && !pos.equals(oldPos)) {
            this.setOccupancyCell(oldPos, null);
            Optional<Entity> occupant = this.getOccupant(pos);
            occupant.ifPresent(target -> this.removeEntity(target));
            this.setOccupancyCell(pos, entity);
            entity.setPosition(pos);
        }
    }
    public void removeEntity(Entity entity) {
        //scheduler.unscheduleAllEvents(entity);
        this.removeEntityAt(entity.getPosition());
    }
    public void removeEntityAt(Point pos) {
        if (this.withinBounds(pos) && this.getOccupancyCell(pos) != null) {
            Entity entity = this.getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            this.entities.remove(entity);
            this.setOccupancyCell(pos, null);
        }
    }
    public void setBackgroundCell(Point pos, Background background) {
        this.background[pos.y][pos.x] = background;
    }
    public Optional<Entity> getOccupant(Point pos) {
        if (this.isOccupied(pos)) {
            return Optional.of(this.getOccupancyCell(pos));
        } else {
            return Optional.empty();
        }
    }
    public void parseSapling(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == SAPLING_NUM_PROPERTIES) {
            int health = Integer.parseInt(properties[SAPLING_HEALTH]);
            Sapling entity = new Sapling(id, pt, imageStore.getImageList(SAPLING_KEY),0,SAPLING_ACTION_ANIMATION_PERIOD, SAPLING_ACTION_ANIMATION_PERIOD, health, SAPLING_HEALTH_LIMIT);
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", SAPLING_KEY, SAPLING_NUM_PROPERTIES));
        }
    }
    public void addEntity(Entity entity) {
        if (this.withinBounds(entity.getPosition())) {
            this.setOccupancyCell(entity.getPosition(), entity);
            this.entities.add(entity);
        }
    }
    public void parseDude(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == DUDE_NUM_PROPERTIES) {
            Dude_Not_Full entity = new Dude_Not_Full(id, pt, imageStore.getImageList(DUDE_KEY) ,Double.parseDouble(properties[DUDE_ACTION_PERIOD]), Double.parseDouble(properties[DUDE_ANIMATION_PERIOD]), Integer.parseInt(properties[DUDE_LIMIT]));
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", DUDE_KEY, DUDE_NUM_PROPERTIES));
        }
    }
    public void parseFairy(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == FAIRY_NUM_PROPERTIES) {
            Fairy entity = new Fairy(id, pt, Double.parseDouble(properties[FAIRY_ACTION_PERIOD]), Double.parseDouble(properties[FAIRY_ANIMATION_PERIOD]), imageStore.getImageList(FAIRY_KEY), 0);
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", FAIRY_KEY, FAIRY_NUM_PROPERTIES));
        }
    }

    public void parseGhost(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == GHOST_NUM_PROPERTIES) {
            Ghost entity = new Ghost(id, pt, imageStore.getImageList(GHOST_KEY), Double.parseDouble(properties[GHOST_ACTION_PERIOD]), Double.parseDouble(properties[GHOST_ANIMATION_PERIOD]), 0);
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", GHOST_KEY, GHOST_NUM_PROPERTIES));
        }
    }

    public void parseKnight(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == KNIGHT_NUM_PROPERTIES) {
            Knight entity = new Knight(id, pt, imageStore.getImageList(KNIGHT_KEY), Double.parseDouble(properties[KNIGHT_ANIMATION_PERIOD]), Double.parseDouble(properties[KNIGHT_ACTION_PERIOD]), 0);
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", KNIGHT_KEY, KNIGHT_NUM_PROPERTIES));
        }
    }

    private void tryAddEntity(Entity entity) {
        if (this.isOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        this.addEntity(entity);
    }

    private void parseSaveFile(Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        String lastHeader = "";
        int headerLine = 0;
        int lineCounter = 0;
        while(saveFile.hasNextLine()){
            lineCounter++;
            String line = saveFile.nextLine().strip();
            if(line.endsWith(":")){
                headerLine = lineCounter;
                lastHeader = line;
                switch (line){
                    case "Backgrounds:" -> this.background = new Background[this.numRows][this.numCols];
                    case "Entities:" -> {
                        this.occupancy = new Entity[this.numRows][this.numCols];
                        this.entities = new HashSet<>();
                    }
                }
            }else{
                switch (lastHeader){
                    case "Rows:" -> this.numRows = Integer.parseInt(line);
                    case "Cols:" -> this.numCols = Integer.parseInt(line);
                    case "Backgrounds:" -> this.parseBackgroundRow(line, lineCounter-headerLine-1, imageStore);
                    case "Entities:" -> parseEntity(this, line, imageStore);
                }
            }
        }
    }
    private void parseStump(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Entity.STUMP_NUM_PROPERTIES) {
            Stump entity = new Stump(id, pt, imageStore.getImageList( Entity.STUMP_KEY), 0);
            this.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Entity.STUMP_KEY, Entity.STUMP_NUM_PROPERTIES));
        }
    }
    private Background getBackgroundCell(Point pos) {
        return this.background[pos.y][pos.x];
    }
    private void parseEntity(WorldModel world, String line, ImageStore imageStore) {
        String[] properties = line.split(" ", ENTITY_NUM_PROPERTIES + 1);
        if (properties.length >= ENTITY_NUM_PROPERTIES) {
            String key = properties[Entity.PROPERTY_KEY];
            String id = properties[PROPERTY_ID];
            Point pt = new Point(Integer.parseInt(properties[PROPERTY_COL]), Integer.parseInt(properties[PROPERTY_ROW]));

            properties = properties.length == ENTITY_NUM_PROPERTIES ?
                    new String[0] : properties[ENTITY_NUM_PROPERTIES].split(" ");

            switch (key) {
                case WorldModel.OBSTACLE_KEY -> world.parseObstacle(properties, pt, id, imageStore);
                case WorldModel.DUDE_KEY -> world.parseDude(properties, pt, id, imageStore);
                case WorldModel.FAIRY_KEY -> world.parseFairy(properties, pt, id, imageStore);
                case WorldModel.HOUSE_KEY -> world.parseHouse(properties, pt, id, imageStore);
                case WorldModel.TREE_KEY -> world.parseTree(properties, pt, id, imageStore);
                case WorldModel.SAPLING_KEY -> world.parseSapling(properties, pt, id, imageStore);
                case WorldModel.GHOST_KEY -> world.parseGhost(properties, pt, id, imageStore);
                case WorldModel.KNIGHT_KEY -> world.parseKnight(properties, pt, id, imageStore);
                case Entity.STUMP_KEY -> world.parseStump(properties, pt, id, imageStore);
                default -> throw new IllegalArgumentException("Entity key is unknown");
            }
        } else {
            throw new IllegalArgumentException("Entity must be formatted as [key] [id] [x] [y] ...");
        }
    }


}
