/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.api.tool.INoisyTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

public class NoisyToolSoundGroup
{
	private static final int attackDuration = 7-1;
	private final INoisyTool noisyToolItem;
	private final ItemStack noisyToolStack;
	private final LivingEntity holder;
	private final int harvestTimeoutGrace;

	private ToolMotorState currentMotorState = ToolMotorState.OFF;
	@Nullable
	private BlockPos currentTargetPos = null;
	private long lastTick = 0;

	public NoisyToolSoundGroup(ItemStack noisyToolStack, LivingEntity holder)
	{
		this.noisyToolStack = noisyToolStack;
		this.noisyToolItem = (INoisyTool)noisyToolStack.getItem();
		this.holder = holder;
		this.harvestTimeoutGrace = holder.equals(Minecraft.getInstance().player)?0: 2400; // shut off remote player's harvesting sound after 2 minutes
	}

	private static void play(AbstractTickableSoundInstance soundInstance)
	{
		Minecraft.getInstance().getSoundManager().queueTickingSound(soundInstance);
	}

	public INoisyTool getItem()
	{
		return noisyToolItem;
	}

	public boolean checkItemMatch(ItemStack handItem)
	{
		if(this.noisyToolItem!=handItem.getItem()||!noisyToolItem.ableToMakeNoise(handItem))
		{
			switchMotorOnOff(false);
			return false;
		}
		return true;
	}

	public enum ToolMotorState
	{
		OFF,
		IDLE,
		BUSY,
		ATTACK,
		ATTACK_END
	}

	public boolean triggerAttack()
	{
		return switchMotorState(true, true, true);
	}

	public boolean switchMotorOnOff(boolean motorOn)
	{
		return switchMotorState(motorOn, false, true);
	}

	private boolean switchMotorState(boolean motorOn, boolean attack, boolean propagate)
	{
		ToolMotorState newMotorState = motorOn?(attack?(ToolMotorState.ATTACK): (currentTargetPos==null?ToolMotorState.IDLE: ToolMotorState.BUSY)): ToolMotorState.OFF;

		if((currentMotorState==newMotorState||(propagate&&currentMotorState==ToolMotorState.ATTACK))&&newMotorState!=ToolMotorState.ATTACK)
			return false;

		currentMotorState = newMotorState;

		switch(newMotorState)
		{
			case OFF:
				if(propagate)
					updateHarvestState(null, false);
				break;
			case IDLE:
				play(new DieselToolMotorSound(noisyToolItem.getIdleSound(noisyToolStack).value(), newMotorState, true));
				break;
			case BUSY:
				play(new DieselToolMotorSound(noisyToolItem.getBusySound(noisyToolStack).value(), newMotorState, true));
				break;
			case ATTACK:
				if(propagate)
					updateHarvestState(null, false);
				lastTick = holder.level().getGameTime()+attackDuration;
				play(new DieselToolMotorSound(noisyToolItem.getAttackSound(noisyToolStack).value(), newMotorState, false));
				break;
		}
		return true;
	}

	public boolean updateHarvestState(@Nullable BlockPos newTargetPos)
	{
		return updateHarvestState(newTargetPos, true);
	}

	private boolean updateHarvestState(@Nullable BlockPos newTargetPos, boolean propagate)
	{
		lastTick = holder.level().getGameTime()+harvestTimeoutGrace;
		if(Objects.equals(currentTargetPos, newTargetPos))
			return false;

		currentTargetPos = newTargetPos;

		if(newTargetPos!=null)
		{
			if(propagate)
				switchMotorState(true, false, false);
			play(new DieselToolHarvestSound(newTargetPos));
		}
		return true;
	}

	public class DieselToolMotorSound extends AbstractTickableSoundInstance
	{
		private final ToolMotorState state;
		private final long lastTick;

		protected DieselToolMotorSound(SoundEvent sound, ToolMotorState state, boolean looping)
		{
			super(sound, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());

			this.x = holder.getX();
			this.y = holder.getY()+0.5d; //todo: get tool coordinates?
			this.z = holder.getZ();
			this.state = state;
			this.looping = looping;
			this.lastTick = looping?0: holder.level().getGameTime()+attackDuration;
		}

		@Override
		public void tick()
		{
			if(!isStopped())
			{
				if(currentMotorState==state)
				{
					this.x = holder.getX();
					this.y = holder.getY()+0.5d;
					this.z = holder.getZ();

					if(state==ToolMotorState.ATTACK) // only check if currentMotorState is still ATTACK
					{
						if(lastTick!=NoisyToolSoundGroup.this.lastTick) //second attack happened. I hate this.
							this.stop();
						else if(holder.level().getGameTime() > lastTick)
							currentMotorState = ToolMotorState.ATTACK_END;
					}
				}
				else
				{
					this.stop();
				}
			}
		}
	}

	public class DieselToolHarvestSound extends AbstractTickableSoundInstance
	{
		private final BlockPos targetBlockPos;

		protected DieselToolHarvestSound(BlockPos targetBlockPos)
		{
			super(noisyToolItem.getHarvestSound(noisyToolStack).value(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());//ApiUtils.RANDOM_SOURCE);

			this.targetBlockPos = targetBlockPos;
			this.x = targetBlockPos.getX()+0.5d;
			this.y = targetBlockPos.getY()+0.5d;
			this.z = targetBlockPos.getZ()+0.5d;
			this.looping = true;
		}

		@Override
		public void tick()
		{
			if(!isStopped())
			{
				if(holder.level().getGameTime() > lastTick||holder.level().getBlockState(currentTargetPos).isAir()) // air check is slapped on addition, because ofc creative
					currentTargetPos = null;
				if(currentTargetPos==null||!Objects.equals(targetBlockPos, currentTargetPos))
					this.stop();
			}
		}
	}
}
