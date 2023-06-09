/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.ai;

import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.common.ToolActions;

import java.util.EnumSet;

public abstract class ShieldCombatGoal<T extends AbstractIllager> extends Goal
{
	protected final T mob;
	protected final IntRange attackPhaseInterval;
	protected final IntRange attackInterval;
	protected int shieldCooldown = 0;

	protected ShieldCombatState combatState = ShieldCombatState.APPROACH;

	private final float attackRadiusSqr;
	private int attackTime = -1;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	protected ShieldCombatGoal(T mob, float attackRadius, IntRange attackPhaseInterval, IntRange attackInterval)
	{
		this.mob = mob;
		this.attackRadiusSqr = attackRadius*attackRadius;
		this.attackPhaseInterval = attackPhaseInterval;
		this.attackInterval = attackInterval;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	protected boolean isHoldingShield()
	{
		return this.mob.isHolding(is -> is.canPerformAction(ToolActions.SHIELD_BLOCK));
	}

	protected boolean hasTarget()
	{
		return this.mob.getTarget()!=null&&this.mob.getTarget().isAlive();
	}

	protected abstract boolean isHoldingWeapon();

	private boolean isEnemyNearby()
	{
		return this.mob.getTarget()!=null&&this.mob.getTarget().isAlive()&&this.mob.distanceToSqr(this.mob.getTarget()) < 128;
	}


	@Override
	public boolean canUse()
	{
		return this.isHoldingShield()&&this.isHoldingWeapon()&&this.hasTarget();
	}

	@Override
	public boolean requiresUpdateEveryTick()
	{
		return true;
	}

	@Override
	public void start()
	{
		super.start();
		this.combatState = ShieldCombatState.APPROACH;
		this.attackTime = this.attackPhaseInterval.getValue(this.mob.getRandom());
		this.shieldCooldown = 0;
		this.mob.setAggressive(true);
	}

	@Override
	public void stop()
	{
		super.stop();
		this.mob.setAggressive(false);
		this.seeTime = 0;
		this.attackTime = -1;
		this.mob.stopUsingItem();
	}

	protected boolean isUsingShield()
	{
		return mob.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK);
	}

	protected void startUsingShield()
	{
		if(this.shieldCooldown > 0)
			return;
		this.mob.startUsingItem(this.mob.getMainHandItem()
				.canPerformAction(ToolActions.SHIELD_BLOCK)?InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND
		);
	}

	public void disableShield()
	{
		if(this.combatState==ShieldCombatState.STRAFE)
		{
			this.mob.stopUsingItem();
			this.shieldCooldown = 100;
			this.combatState = ShieldCombatState.SHIELD_COOLDOWN;
			this.mob.level().broadcastEntityEvent(this.mob, (byte)30);
		}
	}

	abstract boolean performAttack();

	@Override
	public void tick()
	{
		LivingEntity target = this.mob.getTarget();
		if(target==null)
			return;

		double d0 = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
		boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
		boolean hasBeenSeeing = this.seeTime > 0;
		if(hasLineOfSight!=hasBeenSeeing)
			this.seeTime = 0;

		if(hasLineOfSight)
			++this.seeTime;
		else
			--this.seeTime;

		if(!(d0 > (double)this.attackRadiusSqr)&&this.seeTime >= 20)
		{
			this.mob.getNavigation().stop();
			++this.strafingTime;
			if(this.combatState==ShieldCombatState.APPROACH)
			{
				this.combatState = ShieldCombatState.STRAFE;
				this.startUsingShield();
			}
		}
		else
		{
			this.mob.getNavigation().moveTo(target, 1);
			this.strafingTime = -1;
			if(this.combatState==ShieldCombatState.STRAFE)
				this.combatState = ShieldCombatState.APPROACH;
		}

		float strafeSpeed = isUsingShield()?0.15f: 0.5f;

		if(this.strafingTime >= 20)
		{
			if((double)this.mob.getRandom().nextFloat() < 0.3D)
				this.strafingClockwise = !this.strafingClockwise;
			if((double)this.mob.getRandom().nextFloat() < 0.3D)
				this.strafingBackwards = !this.strafingBackwards;
			this.strafingTime = 0;
		}

		if(this.strafingTime > -1)
		{
			if(d0 > (double)(this.attackRadiusSqr*0.75F))
				this.strafingBackwards = false;
			else if(d0 < (double)(this.attackRadiusSqr*0.25F))
				this.strafingBackwards = true;

			this.mob.getMoveControl().strafe(this.strafingBackwards?-strafeSpeed: strafeSpeed, this.strafingClockwise?strafeSpeed: -strafeSpeed);
			this.mob.lookAt(target, 30.0F, 30.0F);
		}
		else
			this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);


		if(this.combatState==ShieldCombatState.SHIELD_COOLDOWN&&--this.shieldCooldown <= 0)
		{
			this.combatState = ShieldCombatState.STRAFE;
			this.startUsingShield();
		}

		if(--this.attackTime <= 0&&this.seeTime >= -60)
			switch(this.combatState)
			{
				case STRAFE ->
				{
					this.combatState = ShieldCombatState.ATTACK;
					this.attackTime = this.attackInterval.getValue(this.mob.getRandom());
					// stop using shield when attacking
					this.mob.releaseUsingItem();
				}
				case ATTACK ->
				{
					if(!this.performAttack())
						this.attackTime = this.attackInterval.getValue(this.mob.getRandom());
					else
					{
						// final attack of the phase returns true
						this.combatState = ShieldCombatState.STRAFE;
						this.attackTime = this.attackPhaseInterval.getValue(this.mob.getRandom());
						this.startUsingShield();
					}
				}
				default ->
				{
				}
			}
	}

	public record IntRange(int low, int high)
	{
		private int getValue(RandomSource rng)
		{
			return this.low+rng.nextInt(this.high-this.low);
		}
	}

	enum ShieldCombatState
	{
		APPROACH,
		STRAFE,
		ATTACK,
		SHIELD_COOLDOWN
	}

}
