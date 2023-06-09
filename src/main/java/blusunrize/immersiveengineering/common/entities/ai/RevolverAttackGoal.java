/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.ai;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.entities.illager.Commando;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.IESounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class RevolverAttackGoal<T extends Commando> extends ShieldCombatGoal<T>
{
	private int bulletCount = 0;
	private int maxBullets;

	public RevolverAttackGoal(T mob, float attackRadius, int maxBullets)
	{
		super(mob, attackRadius, new IntRange(120,180), new IntRange(20,40));
		this.maxBullets = maxBullets;
	}

	public void setMaxBullets(int maxBullets)
	{
		this.maxBullets = maxBullets;
	}

	@Override
	protected boolean isHoldingWeapon()
	{
		return this.mob.isHolding(is -> is.getItem() instanceof RevolverItem);
	}

	@Override
	public void tick()
	{
		super.tick();
		if(this.combatState==ShieldCombatState.STRAFE&&bulletCount > 0&&this.mob.tickCount%20==0)
			if(--bulletCount==0)
				this.mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), IESounds.revolverReload.get(), SoundSource.PLAYERS, 1f, 1f);
		if(this.combatState==ShieldCombatState.ATTACK && !this.mob.isAiming())
			this.mob.setAiming(true);
		if(this.combatState!=ShieldCombatState.ATTACK && this.mob.isAiming())
			this.mob.setAiming(false);
	}

	@Override
	boolean performAttack()
	{
		ItemStack revolver = this.mob.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack bulletStack = this.mob.getRevolverAmmo();
		IBullet bulletType = ((BulletItem)bulletStack.getItem()).getType();
		RevolverItem.fireProjectile(this.mob.level(), this.mob, revolver, bulletType, bulletStack);
		bulletCount++;
		return bulletCount >= maxBullets;
	}

}
