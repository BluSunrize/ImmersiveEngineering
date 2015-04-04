package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

public class IEDamageSources extends EntityDamageSourceIndirect
{
	public IEDamageSources(String tag, Entity shot, Entity shooter)
	{
		super(tag, shooter, shot);
	}
	
	
	public static DamageSource causeCasullDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources("revolver_casull", shot, shooter)).setProjectile();
    }
	public static DamageSource causePiercingDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources("revolver_armorPiercing", shot, shooter)).setDamageBypassesArmor().setProjectile();
    }
	public static DamageSource causeBuckshotDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources("revolver_buckshot", shot, shooter)).setProjectile();
    }
	public static DamageSource causeDragonsbreathDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources("revolver_dragonsbreath", shot, shooter)).setFireDamage().setProjectile();
    }
}
