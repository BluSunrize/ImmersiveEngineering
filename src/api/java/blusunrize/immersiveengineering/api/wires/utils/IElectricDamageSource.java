package blusunrize.immersiveengineering.api.wires.utils;

import net.minecraft.world.entity.Entity;

public interface IElectricDamageSource
{
	boolean apply(Entity e);
	float getDamage();
}
