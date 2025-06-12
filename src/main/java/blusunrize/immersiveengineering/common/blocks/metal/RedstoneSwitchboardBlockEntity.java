/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static blusunrize.immersiveengineering.api.wires.WireType.REDSTONE_CATEGORY;

public class RedstoneSwitchboardBlockEntity extends ImmersiveConnectableBlockEntity
		implements IEServerTickableBE, IStateBasedDirectional, IScrewdriverInteraction,
		IBlockBounds, IBlockOverlayText, IRedstoneConnector
{
	protected static final int RIGHT_INDEX = 0;
	protected static final int LEFT_INDEX = 1;
	public boolean rsDirty = false;

	public List<SwitchboardSetting> settings = new ArrayList<>();
	byte[] output = new byte[16];

	public RedstoneSwitchboardBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.REDSTONE_SWITCHBOARD.get(), pos, state);
	}

	public void addSetting(SwitchboardSetting newSetting)
	{
		// remove any conflicting ones
		this.settings.removeIf(oldSetting -> oldSetting.input().equals(newSetting.input())||oldSetting.output().equals(newSetting.output()));
		this.settings.add(newSetting);
		this.rsDirty = true;
	}

	public void removeSetting(DyeColor output)
	{
		this.settings.removeIf(oldSetting -> oldSetting.output().equals(output));
		this.rsDirty = true;
	}

	@Override
	public void tickServer()
	{
		if(rsDirty)
		{
			RedstoneNetworkHandler rightHander = globalNet.getLocalNet(new ConnectionPoint(worldPosition, RIGHT_INDEX))
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
			if(rightHander!=null)
				rightHander.updateValues();
		}
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(cp.index()==LEFT_INDEX)
		{
			this.settings.forEach(setting -> output[setting.output().getId()] = setting.getOutput(
					handler.getValue(setting.input().getId())
			));
			this.rsDirty = true;
		}

	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		if(cp.index()==RIGHT_INDEX)
		{
			for(DyeColor dye : DyeColor.values())
				signals[dye.getId()] = (byte)Math.max(signals[dye.getId()], this.output[dye.getId()]);
			this.rsDirty = false;
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		ListTag settingsTag = new ListTag();
		this.settings.forEach(switchboardSetting -> settingsTag.add(switchboardSetting.writeToNBT()));
		nbt.put("settings", settingsTag);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		ListTag settingsTag = nbt.getList("settings", 10);
		this.settings.clear();
		for(int i = 0; i < settingsTag.size(); i++)
			this.settings.add(new SwitchboardSetting(settingsTag.getCompound(i)));
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("remove"))
			removeSetting(DyeColor.byId(message.getInt("remove")));
		else
			addSetting(new SwitchboardSetting(message));
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(worldPosition, RIGHT_INDEX), new ConnectionPoint(worldPosition, LEFT_INDEX));
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		boolean right = here.index()==RIGHT_INDEX;
		double conRadius = type.getRenderDiameter()/2;
		if(getFacing()==Direction.NORTH)
			return new Vec3(right?.6875-conRadius: .3125+conRadius, .875-conRadius, .1875);
		if(getFacing()==Direction.SOUTH)
			return new Vec3(right?.3125+conRadius: .6875-conRadius, .875-conRadius, .8125);
		if(getFacing()==Direction.WEST)
			return new Vec3(.1875, .875-conRadius, right?.3125+conRadius: .6875-conRadius);
		if(getFacing()==Direction.EAST)
			return new Vec3(.8125, .875-conRadius, right?.6875-conRadius: .3125+conRadius);
		return new Vec3(.5, .5, .5);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers()
	{
		return ImmutableList.of(RedstoneNetworkHandler.ID);
	}

	@Nullable
	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return null;
		Vec3 target = mop.getLocation().subtract(Vec3.atLowerCornerOf(getPosition()));
		if(target.y <= .75)
			return null;
		Direction facing = getFacing();
		double hitPos;
		if(facing.getAxis()==Axis.X)
			hitPos = target.z;
		else
			hitPos = 1-target.x;

		if((hitPos < .5)==(facing.getAxisDirection()==AxisDirection.POSITIVE))
			return new Component[]{Component.translatable(Lib.DESC_INFO+"blockSide.io.input")};
		else
			return new Component[]{Component.translatable(Lib.DESC_INFO+"blockSide.io.output")};
	}

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide)
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneSwitchboard, this);
		return ItemInteractionResult.SUCCESS;
	}

	CachedVoxelShapes<Direction> SHAPES = new CachedVoxelShapes<>(
			key -> ImmutableList.of(
					ShapeUtils.transformAABB(new AABB(.125, 0, 0, .875, .75, .375), key),
					ShapeUtils.transformAABB(new AABB(.1875, .75, .0625, .4375, 1, .3125), key),
					ShapeUtils.transformAABB(new AABB(.5625, .75, .0625, .8125, 1, .3125), key)
			)
	);

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(getFacing());
	}


	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo target, Vec3i offset)
	{
		ConnectionPoint leftCP = new ConnectionPoint(worldPosition, LEFT_INDEX);
		ConnectionPoint rightCP = new ConnectionPoint(worldPosition, RIGHT_INDEX);
		boolean leftEmpty = getLocalNet(LEFT_INDEX).getConnections(leftCP).stream().allMatch(Connection::isInternal);
		boolean rightEmpty = getLocalNet(RIGHT_INDEX).getConnections(rightCP).stream().allMatch(Connection::isInternal);
		// Special case: If one side is already connected, target the other side
		if(leftEmpty&&!rightEmpty)
			return leftCP;
		else if(!leftEmpty&&rightEmpty)
			return rightCP;
		Direction facing = getFacing();
		double hitPos;
		if(facing.getAxis()==Axis.X)
			hitPos = target.hitZ;
		else
			hitPos = 1-target.hitX;

		if((hitPos < .5)==(facing.getAxisDirection()==AxisDirection.POSITIVE))
			return leftCP;
		else
			return rightCP;
	}

	public record SwitchboardSetting(DyeColor input, boolean invert, DyeColor output)
	{
		private SwitchboardSetting(CompoundTag nbt)
		{
			this(
					DyeColor.byId(nbt.getInt("input")),
					nbt.getBoolean("invert"),
					DyeColor.byId(nbt.getInt("output"))
			);
		}

		public CompoundTag writeToNBT()
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("input", input.getId());
			nbt.putBoolean("invert", invert());
			nbt.putInt("output", output.getId());
			return nbt;
		}

		public byte getOutput(byte input)
		{
			if(this.invert())
				return (byte)(15-input);
			return input;
		}
	}
}


