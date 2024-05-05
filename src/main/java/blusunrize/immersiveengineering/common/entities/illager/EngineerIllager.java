/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.illager;

import blusunrize.immersiveengineering.common.entities.ai.ShieldCombatGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.Level;

public abstract class EngineerIllager extends AbstractIllager
{
	protected EngineerIllager(EntityType<? extends AbstractIllager> entityType, Level level)
	{
		super(entityType, level);
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

	@Override
	protected void blockUsingShield(LivingEntity entity)
	{
		super.blockUsingShield(entity);
		if(entity.getMainHandItem().canDisableShield(this.useItem, this, entity))
			disableShield();
	}

	public void disableShield()
	{
		// shield disabling is supposed to be a random chance, with sprinting adding 75% to the chance
		// however, this has been broken in vanilla Minecraft for years, so I won't bother with implementing it here
		for(WrappedGoal goal : this.goalSelector.getAvailableGoals())
			if(goal.getGoal() instanceof ShieldCombatGoal<?> shieldGoal)
				shieldGoal.disableShield();
	}


}
