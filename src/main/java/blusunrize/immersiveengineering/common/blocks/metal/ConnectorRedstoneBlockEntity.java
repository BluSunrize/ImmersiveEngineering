/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.wires.WireType.REDSTONE_CATEGORY;

public class ConnectorRedstoneBlockEntity extends ImmersiveConnectableBlockEntity implements IEServerTickableBE, IStateBasedDirectional,
		IRedstoneOutput, IScrewdriverInteraction, IBlockBounds, IBlockOverlayText, IRedstoneConnector
{
	public IOSideConfig ioMode = IOSideConfig.INPUT;
	public DyeColor redstoneChannel = DyeColor.WHITE;
	public boolean rsDirty = false;
	//Only write to this in wire network updates!
	protected int output;

	public ConnectorRedstoneBlockEntity(BlockPos pos, BlockState state)
	{
		this(IEBlockEntities.CONNECTOR_REDSTONE.get(), pos, state);
	}

	public ConnectorRedstoneBlockEntity(BlockEntityType<? extends ConnectorRedstoneBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void tickServer()
	{
		if(rsDirty)
			globalNet.getLocalNet(worldPosition)
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
					.updateValues();
	}

	@Override
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		if(side==this.getFacing().getOpposite())
			return getWeakRSOutput(side);
		return 0;
	}

	@Override
	public int getWeakRSOutput(@Nonnull Direction side)
	{
		if(!isRSOutput()||side==this.getFacing())
			return 0;
		return output;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return side!=getFacing().getOpposite();
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!level.isClientSide&&SafeChunkUtils.isChunkSafe(level, worldPosition))
		{
			output = handler.getValue(redstoneChannel.getId());
			if(!isRemoved()&&isRSOutput())
			{
				setChanged();
				markContainingBlockForUpdate(getBlockState());
				BlockPos offsetPos = worldPosition.relative(getFacing());
				level.updateNeighborsAt(offsetPos, level.getBlockState(offsetPos).getBlock());
			}
		}
	}

	public boolean isRSInput()
	{
		return ioMode==IOSideConfig.INPUT;
	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		if(isRSInput())
			signals[redstoneChannel.getId()] = (byte)getMaxRSInput();
		rsDirty = false;
	}

	protected boolean acceptSignalFrom(Direction side)
	{
		BlockPos offset = worldPosition.relative(side);
		BlockEntity te = SafeChunkUtils.getSafeBE(level, offset);
		// If it's not a connector, exit early
		if(!(te instanceof ConnectorRedstoneBlockEntity rsConnector))
			return true;
		// If it's not the same net, exit early
		if(!this.getLocalNet(0).getConnectors().contains(offset))
			return true;
		// Allow connection if they are different colors
		return rsConnector.redstoneChannel!=this.redstoneChannel;
	}

	@Override
	protected int getRSInput(Direction from)
	{
		if(acceptSignalFrom(from))
			return super.getRSInput(from);
		return 0;
	}

	public boolean isRSOutput()
	{
		return ioMode==IOSideConfig.OUTPUT;
	}

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide)
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneConnector, this);
		return ItemInteractionResult.SUCCESS;
	}

	protected void updateAfterConfigure()
	{
		setChanged();
		globalNet.getLocalNet(worldPosition)
				.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
				.updateValues();
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return ConnectorBlock.DEFAULT_FACING_PROP;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("ioMode"))
			ioMode = IOSideConfig.VALUES[message.getInt("ioMode")];
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getInt("redstoneChannel"));
		updateAfterConfigure();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putInt("ioMode", ioMode.ordinal());
		nbt.putInt("redstoneChannel", redstoneChannel.getId());
		nbt.putInt("output", output);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		ioMode = IOSideConfig.VALUES[nbt.getInt("ioMode")];
		redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel"));
		output = nbt.getInt("output");
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = type.getRenderDiameter()/2;
		return new Vec3(.5-conRadius*side.getStepX(), .5-conRadius*side.getStepY(), .5-conRadius*side.getStepZ());
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return EnergyConnectorBlockEntity.getConnectorBounds(getFacing(), .625f);
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return null;
		return new Component[]{
				getChannelComponent("", redstoneChannel),
				Component.translatable(Lib.DESC_INFO+"blockSide.io."+this.ioMode.getSerializedName())
		};
	}

	protected static Component getChannelComponent(String subtype, DyeColor channel)
	{
		return Component.empty()
				.append(Component.translatable(Lib.GUI_CONFIG+"redstone_color"+subtype))
				.append(Component.literal(" "))
				.append(Lib.getRedstoneColorComponent(channel));
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers()
	{
		return ImmutableList.of(RedstoneNetworkHandler.ID);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		int oldRSIn = getMaxRSInput();
		super.onNeighborBlockChange(otherPos);
		if(isRSInput()&&oldRSIn!=getMaxRSInput())
			rsDirty = true;
	}
}