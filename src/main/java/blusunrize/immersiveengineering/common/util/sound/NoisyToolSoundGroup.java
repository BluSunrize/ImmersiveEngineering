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

import static blusunrize.immersiveengineering.common.util.sound.NoisyToolSoundGroup.ToolMotorState.*;

public class NoisyToolSoundGroup
{
	private static final int ATTACK_DURATION = 6-1; // in ticks. -1 cause first tick is free
	private static final int FADE_DURATION = 20-1; // in ticks. -1 cause first tick is free
	private final INoisyTool noisyToolItem;
	private ItemStack noisyToolStack;
	private final int hotbarSlot;
	private final LivingEntity noisyToolHolder;
	private final int harvestTimeoutGrace;

	private ToolMotorState currentMotorState = OFF;
	@Nullable
	private BlockPos currentTargetPos = null;
	private long groupLastTickHelper = 0;

	public NoisyToolSoundGroup(ItemStack noisyToolStack, LivingEntity noisyToolHolder, int hotbarSlot)
	{
		this.noisyToolStack = noisyToolStack;
		this.noisyToolItem = (INoisyTool)noisyToolStack.getItem();
		this.noisyToolHolder = noisyToolHolder;
		this.hotbarSlot = hotbarSlot;
		// shut off remote player's harvesting sound after 2 minutes
		// grace for local player to deal with hard 5 tick delay between LeftClickBlock.START and .CLIENT_HOLD action
		this.harvestTimeoutGrace = noisyToolHolder.equals(Minecraft.getInstance().player)?(5-1): 2400;
	}

	private static void play(AbstractTickableSoundInstance soundInstance)
	{
		Minecraft.getInstance().getSoundManager().play(soundInstance);
	}

	public INoisyTool getItem()
	{
		return noisyToolItem;
	}

	public boolean checkItemValid(ItemStack handItemStack, int hotbarSlot)
	{
		if(this.hotbarSlot!=hotbarSlot||!checkItemMatch(handItemStack)||!noisyToolItem.ableToMakeNoise(handItemStack))
		{
			switchMotorOnOff(false);
			return false;
		}
		return true;
	}

	private boolean checkItemMatch(ItemStack handItemStack)
	{
		if (noisyToolStack == handItemStack)
		{
			return true;
		}
		else if (noisyToolItem.noisySameStack(noisyToolStack, handItemStack))
		{
			noisyToolStack = handItemStack;
			return true;
		}
		return false;
	}

	public enum ToolMotorState
	{
		OFF,
		IDLE,
		BUSY,
		FADING,
		ATTACK,
		TRANSITION // transient helper state to bridge away from timed sounds associated with FADING and ATTACK, cause transitioning directly gives a tick of no sound with current implementation
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
		ToolMotorState newMotorState = motorOn?
				(attack?
						ATTACK
						: (currentTargetPos==null?
						(currentMotorState==BUSY||currentMotorState==FADING?
								FADING
								: IDLE)
						: BUSY))
				: OFF;

		if((currentMotorState==newMotorState||(propagate&&currentMotorState==ATTACK))&&newMotorState!=ATTACK)
			return false;

		currentMotorState = newMotorState;

		switch(newMotorState)
		{
			case OFF:
				if(propagate)
					updateHarvestState(null, false);
				break;
			case IDLE:
				play(new NoisyToolMotorSoundLooping(noisyToolItem.getIdleSound(noisyToolStack).value(), newMotorState));
				break;
			case BUSY:
				play(new NoisyToolMotorSoundLooping(noisyToolItem.getBusySound(noisyToolStack).value(), newMotorState));
				break;
			case FADING:
				play(new NoisyToolMotorSoundFinite(noisyToolItem.getFadingSound(noisyToolStack).value(), newMotorState, FADE_DURATION));
				break;
			case ATTACK:
				if(propagate)
					updateHarvestState(null, false); // todo: check if this is really necessary. Not that it hurts.
				play(new NoisyToolMotorSoundFinite(noisyToolItem.getAttackSound(noisyToolStack).value(), newMotorState, ATTACK_DURATION));
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
		groupLastTickHelper = noisyToolHolder.level().getGameTime();
		if(currentMotorState!=BUSY)
			groupLastTickHelper += harvestTimeoutGrace; //initial start needs grace period before stopping for remote AND local players

		if(Objects.equals(currentTargetPos, newTargetPos))
			return false;

		currentTargetPos = newTargetPos;

		if(newTargetPos!=null)
		{
			if(propagate)
				switchMotorState(true, false, false);
			play(new NoisyToolHarvestSound(newTargetPos));
		}
		return true;
	}

	private abstract class NoisyToolSound extends AbstractTickableSoundInstance
	{

		protected NoisyToolSound(SoundEvent sound)
		{
			super(sound, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
		}

		@Override
		public boolean canStartSilent()
		{
			return true;
		}
	}

	private abstract class NoisyToolMotorSound extends NoisyToolSound
	{
		protected final ToolMotorState state;

		protected NoisyToolMotorSound(SoundEvent sound, ToolMotorState state)
		{
			super(sound);

			this.x = noisyToolHolder.getX();
			this.y = noisyToolHolder.getY()+0.5d; //todo: get tool coordinates?
			this.z = noisyToolHolder.getZ();
			this.state = state;
			this.volume = INoisyTool.TEST_VOLUME_ADJUSTMENT; //TODO: remove me
		}

		protected void updateCoordinates()
		{
			this.x = noisyToolHolder.getX();
			this.y = noisyToolHolder.getY()+0.5d;
			this.z = noisyToolHolder.getZ();
		}

		@Override
		public void tick()
		{
			if(!isStopped())
			{
				if(currentMotorState==state)
				{
					updateCoordinates();
				}
				else
				{
					this.stop();
				}
			}
		}
	}

	private class NoisyToolMotorSoundLooping extends NoisyToolMotorSound
	{

		protected NoisyToolMotorSoundLooping(SoundEvent sound, ToolMotorState state)
		{
			super(sound, state);

			looping = true;
		}
	}

	private class NoisyToolMotorSoundFinite extends NoisyToolMotorSound
	{
		private final long thisSoundsLastTick;

		protected NoisyToolMotorSoundFinite(SoundEvent sound, ToolMotorState state, int duration)
		{
			super(sound, state);
			this.thisSoundsLastTick = noisyToolHolder.level().getGameTime()+duration;
			groupLastTickHelper = thisSoundsLastTick;

		}

		@Override
		public void updateCoordinates()
		{
			super.updateCoordinates();

			if(state==ATTACK||state==FADING) // only check if currentMotorState==state
			{
				if(thisSoundsLastTick!=NoisyToolSoundGroup.this.groupLastTickHelper) //second attack happened. I hate this.
					this.stop();
				else if(noisyToolHolder.level().getGameTime() > thisSoundsLastTick)
					currentMotorState = ToolMotorState.TRANSITION;
			}
		}
	}

	private class NoisyToolHarvestSound extends NoisyToolSound
	{
		private final BlockPos targetBlockPos;

		protected NoisyToolHarvestSound(BlockPos targetBlockPos)
		{
			super(noisyToolItem.getHarvestSound(noisyToolStack).value());//ApiUtils.RANDOM_SOURCE);

			this.targetBlockPos = targetBlockPos;
			this.x = targetBlockPos.getX()+0.5d;
			this.y = targetBlockPos.getY()+0.5d;
			this.z = targetBlockPos.getZ()+0.5d;
			this.looping = true;
			this.volume = INoisyTool.TEST_VOLUME_ADJUSTMENT; //TODO: remove me
		}

		@Override
		public void tick()
		{
			if(!isStopped())
			{
				if(currentTargetPos!=null&&(noisyToolHolder.level().getGameTime() > groupLastTickHelper||noisyToolHolder.level().getBlockState(currentTargetPos).isAir())) // air check is slapped on addition, because of creative insta break
					currentTargetPos = null;
				if(currentTargetPos==null||!Objects.equals(targetBlockPos, currentTargetPos))
					this.stop();
			}
		}
	}
}
