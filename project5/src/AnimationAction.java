public class AnimationAction implements Action {
    private final AnimationEntity entity;
    private final int repeatCount;

    public AnimationAction(AnimationEntity entity, int repeatCount)
    {
        this.entity = entity;
        this.repeatCount = repeatCount;
    }
    @Override
    public void executeAction(EventScheduler scheduler) {
        if(entity instanceof AnimationEntity) {
            (this.entity).nextImage();
        }
        if (this.repeatCount != 1) {
            scheduler.scheduleEvent(this.entity, new AnimationAction( entity,Math.max(this.repeatCount - 1, 0)), ((AnimationEntity)this.entity).getAnimationPeriod());
        }
    }

}
