package blusunrize.immersiveengineering.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;

public class IEDamageSources extends EntityDamageSourceIndirect
{
	public IEDamageSources(String tag, Entity shot, Entity shooter)
	{
		super(tag, shooter, shot);
	}
	
	
	public static DamageSource causeCasullDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources(Lib.DMG_RevolverCasull, shot, shooter)).setProjectile();
    }
	public static DamageSource causePiercingDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources(Lib.DMG_RevolverAP, shot, shooter)).setDamageBypassesArmor().setProjectile();
    }
	public static DamageSource causeBuckshotDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources(Lib.DMG_RevolverBuck, shot, shooter)).setProjectile();
    }
	public static DamageSource causeDragonsbreathDamage(EntityRevolvershot shot, Entity shooter)
    {
        return (new IEDamageSources(Lib.DMG_RevolverDragon, shot, shooter)).setFireDamage().setProjectile();
    }
	
	public static DamageSource causeGrinderDamage()
    {
        return new DamageSource(Lib.DMG_Grinder);
    }
}
