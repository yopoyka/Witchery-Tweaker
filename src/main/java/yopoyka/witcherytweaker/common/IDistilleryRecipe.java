package yopoyka.witcherytweaker.common;

public interface IDistilleryRecipe {
    @Inject.Access(value = "wtw_cookTime", create = true)
    public int getCookTime();

    @Inject.Access(value = "wtw_cookTime", create = true)
    public void setCookTime(int time);
}
