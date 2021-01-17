package blusunrize.immersiveengineering.api.wires.utils;

import net.minecraft.entity.Entity;

public interface IElectricDamageSource
{
	boolean apply(Entity e);
	float getDamage();
}
