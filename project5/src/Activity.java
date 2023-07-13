public class Activity implements Action{
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private Point position;
    public Activity(Entity entity, WorldModel world, ImageStore imageStore){
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;

    }

    @Override
    public void executeAction(EventScheduler scheduler) {

        if(entity instanceof Sapling) {
            ((Sapling)entity).executeSaplingActivity(this.world, this.imageStore, scheduler);
        }
        else if(entity instanceof Tree) {
            ((Tree) entity).executeTreeActivity(this.world, this.imageStore, scheduler);
        }
        else if(entity instanceof Fairy) {
            ((Fairy) entity).executeFairyActivity(this.world, this.imageStore, scheduler);
        }
        else if(entity instanceof Dude_Not_Full) {
            ((Dude_Not_Full) entity).executeDudeNotFullActivity(this.world, this.imageStore, scheduler);
        }
        else if(entity instanceof Dude_Full) {
            ((Dude_Full) entity).executeDudeFullActivity(this.world, this.imageStore, scheduler);
        }
        else if(entity instanceof Ghost){
            ((Ghost) entity).executeGhostActivity(this.world, this.imageStore, scheduler);
        }
        else if(entity instanceof Knight){
            ((Knight) entity).executeKnightActivity(this.world, this.imageStore, scheduler);
        }
        else{
            throw new UnsupportedOperationException(String.format("executeActivityAction not supported for %s", entity.getClass()));
        }

    }



}
