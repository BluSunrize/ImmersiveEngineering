/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.ai;

import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RailgunAttackGoal<T extends Fusilier> extends Goal
{
	public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
	private final T mob;
	private RailgunState railgunState = RailgunState.COOLDOWN;
	private final double speedModifier;
	private final float attackRadiusSqr;
	private int seeTime;
	private int attackDelay;
	private int cooldownDelay;
	private int updatePathDelay;

	public RailgunAttackGoal(T mob, double speedModifier, float attackRadius)
	{
		this.mob = mob;
		this.speedModifier = speedModifier;
		this.attackRadiusSqr = attackRadius*attackRadius;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse()
	{
		return this.isValidTarget()&&this.isHoldingRailgun();
	}

	private boolean isHoldingRailgun()
	{
		return this.mob.isHolding(is -> is.getItem() instanceof RailgunItem);
	}


	@Override
	public boolean canContinueToUse()
	{
		return this.isValidTarget()&&(this.canUse()||!this.mob.getNavigation().isDone())&&this.isHoldingRailgun();
	}

	private boolean isValidTarget()
	{
		return this.mob.getTarget()!=null&&this.mob.getTarget().isAlive();
	}


	@Override
	public void stop()
	{
		super.stop();
		this.mob.setAggressive(false);
		this.mob.setTarget(null);
		this.seeTime = 0;
		if(this.mob.isUsingItem())
		{
			this.mob.stopUsingItem();
			this.mob.setAimingRailgun(false);
		}

	}

	public boolean requiresUpdateEveryTick()
	{
		return true;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick()
	{
		LivingEntity livingentity = this.mob.getTarget();
		if(livingentity!=null)
		{
			boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(livingentity);
			boolean hasBeenSeeing = this.seeTime > 0;
			if(hasLineOfSight!=hasBeenSeeing)
				this.seeTime = 0;

			if(hasLineOfSight)
				++this.seeTime;
			else
				--this.seeTime;

			double d0 = this.mob.distanceToSqr(livingentity);
			boolean shouldAttack = (d0 > (double)this.attackRadiusSqr||this.seeTime < 5)&&this.attackDelay==0;
			if(shouldAttack)
			{
				--this.updatePathDelay;
				if(this.updatePathDelay <= 0)
				{
					this.mob.getNavigation().moveTo(livingentity, this.canRun()?this.speedModifier: this.speedModifier*0.5D);
					this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
				}
			}
			else
			{
				this.updatePathDelay = 0;
				this.mob.getNavigation().stop();
			}

			this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
			if(this.railgunState==RailgunState.COOLDOWN)
			{
				if(!shouldAttack&&--this.cooldownDelay <= 0)
				{
					this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof RailgunItem));
					RailgunItem.playChargeSound(this.mob, this.mob.getUseItem());
					this.railgunState = RailgunState.CHARGING;
					this.mob.setAimingRailgun(true);
				}
			}
			else if(this.railgunState==RailgunState.CHARGING)
			{
				if(!this.mob.isUsingItem())
					this.railgunState = RailgunState.COOLDOWN;

				int i = this.mob.getTicksUsingItem();
				ItemStack itemstack = this.mob.getUseItem();
				if(i >= RailgunItem.getChargeTime(itemstack))
				{
					this.railgunState = RailgunState.CHARGED;
					this.attackDelay = 20+this.mob.getRandom().nextInt(20);
				}
			}
			else if(this.railgunState==RailgunState.CHARGED)
			{
				--this.attackDelay;
				if(this.attackDelay==0)
					this.railgunState = RailgunState.READY_TO_ATTACK;
			}
			else if(this.railgunState==RailgunState.READY_TO_ATTACK&&hasLineOfSight)
			{
				ItemStack itemstack = this.mob.getUseItem();
				Entity shot = RailgunItem.fireProjectile(itemstack, this.mob.level(), this.mob, new ItemStack(Ingredients.STICK_STEEL));
				if(shot instanceof Projectile projectile)
					this.shootProjectile(this.mob, livingentity, projectile, 3f);
				this.mob.releaseUsingItem();
				ItemNBTHelper.remove(itemstack, "inUse");
				this.mob.setAimingRailgun(false);
				this.railgunState = RailgunState.COOLDOWN;
				this.cooldownDelay = 40;
			}

		}
	}

	private void shootProjectile(LivingEntity user, LivingEntity target, Projectile projectile, float velocity)
	{
		double dx = target.getX()-user.getX();
		double dz = target.getZ()-user.getZ();
		double distSqrt = Math.sqrt(dx*dx+dz*dz);
		double dy = target.getY(0.3333333333333333D)-projectile.getY()+distSqrt*(double)0.2F;
		Vec3 vec3 = new Vec3(dx, dy, dz).normalize();
		projectile.shoot(vec3.x(), vec3.y(), vec3.z(), velocity, (float)(14-user.level().getDifficulty().getId()*4));
	}

	private boolean canRun()
	{
		return this.railgunState==RailgunState.COOLDOWN;
	}

	enum RailgunState
	{
		COOLDOWN,
		CHARGING,
		CHARGED,
		READY_TO_ATTACK;
	}
}
