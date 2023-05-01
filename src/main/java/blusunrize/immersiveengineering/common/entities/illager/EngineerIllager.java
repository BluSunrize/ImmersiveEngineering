/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.illager;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.Level;

public abstract class EngineerIllager extends AbstractIllager
{
	protected EngineerIllager(EntityType<? extends AbstractIllager> entityType, Level level)
	{
		super(entityType, level);
	}

	@Override
	public boolean isAlliedTo(Entity entity)
	{
		if(super.isAlliedTo(entity))
			return true;
		else if(entity instanceof LivingEntity&&((LivingEntity)entity).getMobType()==MobType.ILLAGER)
			return this.getTeam()==null&&entity.getTeam()==null;
		return false;
	}

	@Override
	protected float getEquipmentDropChance(EquipmentSlot slot)
	{
		return 0;
	}

	@Override
	public IllagerArmPose getArmPose()
	{
		return this.isCelebrating()?IllagerArmPose.CELEBRATING: IllagerArmPose.NEUTRAL;
	}

}
