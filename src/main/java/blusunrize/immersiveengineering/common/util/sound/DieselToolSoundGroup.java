/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.common.items.DieselToolItem;
import li.cil.oc2.common.ConfigManager.Min;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

public class DieselToolSoundGroup
{
	private final DieselToolItem dieselToolItem;
	private final LivingEntity holder;
	private final int harvestTimeoutGrace;
	private final EquipmentSlot slot;

	private ToolMotorState currentMotorState = ToolMotorState.OFF;
	@Nullable
	private BlockPos currentTargetPos = null;
	private long lastTick = 0;

	public DieselToolSoundGroup(DieselToolItem dieselToolItem, LivingEntity holder, EquipmentSlot slot)
	{
		this.dieselToolItem = dieselToolItem;
		this.holder = holder;
		this.harvestTimeoutGrace = holder.equals(Minecraft.getInstance().player)?0: 600; // shut off remote player's harvesting sound after 600 ticks
		this.slot = slot;
	}

	private static void play(AbstractTickableSoundInstance soundInstance)
	{
		Minecraft.getInstance().getSoundManager().queueTickingSound(soundInstance);
	}

	public DieselToolItem getItem()
	{
		return dieselToolItem;
	}

	public boolean checkItemMatch(ItemStack handItem)
	{
		if(this.dieselToolItem!=handItem.getItem()||!dieselToolItem.canToolBeUsed(handItem))
		{
			switchMotorOnOff(false); // go to sounds-off state (propagates)
			return false;
		}
		return true;
	}

//	public enum ToolHarvestState
//	{
//		NON,
//		HARVESTING
//	}

	public boolean triggerMotorAttack()
	{
		return switchMotorState(true, true, true);
	}

	public boolean switchMotorOnOff(boolean motorOn)
	{
		return switchMotorState(motorOn, false, true);
	}

	public boolean switchMotorState(boolean motorOn, boolean attack, boolean propagate)
	{
		ToolMotorState newMotorState = motorOn?(attack?(ToolMotorState.ATTACK): (currentTargetPos==null?ToolMotorState.IDLE: ToolMotorState.BUSY)): ToolMotorState.OFF;

		if(currentMotorState==newMotorState || (propagate && currentMotorState==ToolMotorState.ATTACK))
			return false;

		currentMotorState = newMotorState;
		//todo: motorSound = switch(newMotorState) case -> ?
		switch(newMotorState)
		{
			case OFF:
				if(propagate)
					updateHarvestState(null, false);
				break;
			case IDLE:
				play(new DieselToolMotorSound(dieselToolItem.getIdleSound().value(), newMotorState, true));
				break;
			case BUSY:
				play(new DieselToolMotorSound(dieselToolItem.getBusySound().value(), newMotorState, true));
				break;
			case ATTACK:
				if(propagate)
					updateHarvestState(null, false);
				lastTick = holder.level().getGameTime()+12;
				play(new DieselToolMotorSound(dieselToolItem.getAttackSound().value(), newMotorState, false));
				break;
//				System.out.println("attaaaaack!"); //TODO: remove me
//				motorSound = Optional.of(new DieselToolMotorSound(dieselToolItem.getBusySound()));
//				break;
		}
		return true;
	}

	public boolean updateHarvestState(@Nullable BlockPos newTargetPos)
	{
		return updateHarvestState(newTargetPos, true);
	}

	public boolean updateHarvestState(@Nullable BlockPos newTargetPos, boolean propagate)
	{
		lastTick = holder.level().getGameTime()+harvestTimeoutGrace;
		if(Objects.equals(currentTargetPos, newTargetPos))
			return false;

		System.out.println(currentTargetPos+" ::: "+newTargetPos);
		currentTargetPos = newTargetPos;

		// TODO: simplify/remove null branch. See if it does anything with/without propagation in case of no target
		if(newTargetPos!=null)
		{
			if(propagate)
				switchMotorState(true, false, false);
			play(new DieselToolHarvestSound(newTargetPos));
		}
		return true;
	}

	public enum ToolMotorState
	{
		OFF,
		IDLE,
		BUSY,
		ATTACK
	}

	public class DieselToolMotorSound extends AbstractTickableSoundInstance
	{
		private final ToolMotorState state;
		private final SoundManager soundManager = Minecraft.getInstance().getSoundManager();

		protected DieselToolMotorSound(SoundEvent sound, ToolMotorState state, boolean looping)
		{
			super(sound, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
			//todo: get tool coordinates?
			this.x = holder.getX();
			this.y = holder.getY()+0.5d;
			this.z = holder.getZ();
			this.state = state;
			this.volume = state==ToolMotorState.IDLE?0.4f: 0.5f;
			this.pitch = state==ToolMotorState.IDLE?0.5f: 0.8f;
			this.looping = looping;
		}

		@Override
		public void tick()
		{
			if(currentMotorState==state)
			{
				this.x = holder.getX();
				this.y = holder.getY()+0.5d;
				this.z = holder.getZ();

				if(state==ToolMotorState.ATTACK && holder.level().getGameTime() > lastTick) // only check if currentMotorState is still ATTACK
				{
					switchMotorState(true, false, false);
				}
			}
			else if(!isStopped())
			{
				this.stop();
			}
		}
	}

	public class DieselToolHarvestSound extends AbstractTickableSoundInstance
	{
		private final BlockPos targetBlockPos;

		protected DieselToolHarvestSound(BlockPos targetBlockPos)
		{
			super(dieselToolItem.getHarvestSound().value(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());//ApiUtils.RANDOM_SOURCE);

			this.targetBlockPos = targetBlockPos;
			this.x = targetBlockPos.getX()+0.5d;
			this.y = targetBlockPos.getY()+0.5d;
			this.z = targetBlockPos.getZ()+0.5d;
			this.looping = true;
			this.volume = 1.2f;
		}

		@Override
		public void tick()
		{
			if(holder.level().getGameTime() > lastTick) // TODO: I think this might make the repeat try on the same block fucky for remote players
				currentTargetPos = null;
			if((currentTargetPos==null||!Objects.equals(targetBlockPos, currentTargetPos))&&!isStopped()) //todo: doesn't die if currentTargetPos changed
				this.stop();
		}
	}
}
