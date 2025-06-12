/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Map;

public class RedstoneTimerBlockEntity extends ConnectorRedstoneBlockEntity
{
	public static final int TIMER_MIN = 8;
	public static final int TIMER_MAX = 200;

	public int timerSetting = 80;
	int currentTimer = 0;
	public boolean requireControlSignal = false;
	boolean hasControlSignal = false;

	public DyeColor redstoneChannelControl = DyeColor.WHITE;

	public RedstoneTimerBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.REDSTONE_TIMER.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(this.currentTimer > 0&&this.output > 0)
			setOutput(0);

		if(++this.currentTimer >= timerSetting)
		{
			setOutput(isActive()?15: 0);
			resetTimer();
		}
		super.tickServer();
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!level.isClientSide&&SafeChunkUtils.isChunkSafe(level, worldPosition))
		{
			final boolean hadSignal = hasControlSignal;
			hasControlSignal = handler.getValue(redstoneChannelControl.getId()) > 0;
			if(hasControlSignal&&!hadSignal)
				resetTimer();
		}
	}

	private boolean isActive()
	{
		return !requireControlSignal||hasControlSignal;
	}

	private void resetTimer()
	{
		if(isActive())
			this.currentTimer = 0;
	}

	private void setOutput(int value)
	{
		if(this.output!=value)
		{
			this.output = value;
			this.rsDirty = true;
		}
	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		signals[redstoneChannel.ordinal()] = (byte)this.output;
		rsDirty = false;
	}

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide)
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneTimer, this);
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("timerSetting"))
			timerSetting = message.getInt("timerSetting");
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getInt("redstoneChannel"));
		if(message.contains("redstoneChannelControl"))
			redstoneChannelControl = DyeColor.byId(message.getInt("redstoneChannelControl"));
		if(message.contains("requireControlSignal"))
			requireControlSignal = message.getBoolean("requireControlSignal");
		updateAfterConfigure();
	}

	@Override
	protected void updateAfterConfigure()
	{
		resetTimer();
		super.updateAfterConfigure();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putInt("timerSetting", timerSetting);
		nbt.putInt("currentTimer", currentTimer);
		nbt.putBoolean("requireControlSignal", requireControlSignal);
		nbt.putBoolean("hasControlSignal", hasControlSignal);
		nbt.putInt("redstoneChannelControl", redstoneChannelControl.getId());
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		timerSetting = nbt.getInt("timerSetting");
		currentTimer = nbt.getInt("currentTimer");
		requireControlSignal = nbt.getBoolean("requireControlSignal");
		hasControlSignal = nbt.getBoolean("hasControlSignal");
		redstoneChannelControl = DyeColor.byId(nbt.getInt("redstoneChannelControl"));
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = type.getRenderDiameter()/2;
		return switch(side)
		{
			case UP -> new Vec3(.5, .375-conRadius, .1875-conRadius);
			case DOWN -> new Vec3(.5, .625-conRadius, .8125-conRadius);
			default -> new Vec3(
					.5-side.getStepX()*(.125-conRadius),
					.1875-conRadius,
					.5-side.getStepZ()*(.125-conRadius)
			);
		};
	}

	@Override
	public boolean isRSInput()
	{
		return false;
	}

	@Override
	public boolean isRSOutput()
	{
		return false;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return false;
	}

	private static final Pair<DyeColor, Byte>[] NO_OVERRIDE = new Pair[0];
	@Override
	public Pair<DyeColor, Byte>[] overrideVoltmeterRead()
	{
		return NO_OVERRIDE;
	}

	private static final Map<Direction, VoxelShape> SHAPES = Util.make(
			new EnumMap<>(Direction.class), map -> {
				final double wMin = .25;
				final double wMax = .75;
				map.put(Direction.NORTH, Shapes.box(wMin, 0, 0, wMax, 1, .5));
				map.put(Direction.SOUTH, Shapes.box(wMin, 0, .5, wMax, 1, 1));
				map.put(Direction.WEST, Shapes.box(0, 0, wMin, .5, 1, wMax));
				map.put(Direction.EAST, Shapes.box(.5, 0, wMin, 1, 1, wMax));
				map.put(Direction.DOWN, Shapes.box(wMin, 0, 0, wMax, .5, 1));
				map.put(Direction.UP, Shapes.box(wMin, .5, 0, wMax, 1, 1));
			}
	);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(getFacing());
	}


	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return null;
		return new Component[]{
				getTimeFormatted(this.timerSetting),
				getChannelComponent("_output", redstoneChannel),
		};
	}

	public static Component getTimeFormatted(int ticks)
	{
		if(ticks < 20)
			return Component.translatable(Lib.DESC_INFO+"ticks", ticks);
		double seconds = ticks/20.0;
		DecimalFormat format = new DecimalFormat("0.00");
		return Component.translatable(Lib.DESC_INFO+"seconds", format.format(seconds));
	}

}