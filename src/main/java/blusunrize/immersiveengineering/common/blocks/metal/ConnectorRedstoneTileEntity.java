/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import blusunrize.immersiveengineering.common.util.SafeChunkUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.wires.WireType.REDSTONE_CATEGORY;

public class ConnectorRedstoneTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IStateBasedDirectional,
		IRedstoneOutput, IHammerInteraction, IBlockBounds, IBlockOverlayText, IOBJModelCallback<BlockState>,
		IRedstoneConnector, INeighbourChangeTile
{
	public IOSideConfig ioMode = IOSideConfig.INPUT;
	public DyeColor redstoneChannel = DyeColor.WHITE;
	public boolean rsDirty = false;
	//Only write to this in wire network updates!
	private int output;
	public static TileEntityType<ConnectorRedstoneTileEntity> TYPE;

	public ConnectorRedstoneTileEntity()
	{
		this(TYPE);
	}

	public ConnectorRedstoneTileEntity(TileEntityType<? extends ConnectorRedstoneTileEntity> type)
	{
		super(type);
	}

	@Override
	public void tick()
	{
		if(hasWorld()&&!world.isRemote&&rsDirty)
			globalNet.getLocalNet(pos)
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
					.updateValues();
	}

	@Override
	public int getStrongRSOutput(BlockState state, Direction side)
	{
		if(!isRSOutput()||side!=this.getFacing().getOpposite())
			return 0;
		return output;
	}

	@Override
	public int getWeakRSOutput(BlockState state, Direction side)
	{
		if(!isRSOutput())
			return 0;
		return output;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, Direction side)
	{
		return true;
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!world.isRemote&&SafeChunkUtils.isChunkSafe(world, pos))
		{
			output = handler.getValue(redstoneChannel.getId());
			if(!isRemoved()&&isRSOutput())
			{
				markDirty();
				BlockState stateHere = world.getBlockState(pos);
				markContainingBlockForUpdate(stateHere);
				markBlockForUpdate(pos.offset(getFacing()), world.getBlockState(pos.offset(getFacing())));
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
			signals[redstoneChannel.getId()] = (byte)Math.max(getLocalRS(), signals[redstoneChannel.getId()]);
		rsDirty = false;
	}

	protected int getLocalRS()
	{
		int val = SafeChunkUtils.getRedstonePowerFromNeighbors(world, pos);
		if(val==0)
		{
			for(Direction f : Direction.BY_HORIZONTAL_INDEX)
			{
				BlockState state = SafeChunkUtils.getBlockState(world, pos.offset(f));
				if(state.getBlock()==Blocks.REDSTONE_WIRE&&state.get(RedstoneWireBlock.POWER) > val)
					val = state.get(RedstoneWireBlock.POWER);
			}
		}
		return val;
	}

	public boolean isRSOutput()
	{
		return ioMode==IOSideConfig.OUTPUT;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		if(!world.isRemote)
		{
			//Sneaking iterates through colours, normal hammerign toggles in and out
			if(player.isSneaking())
				redstoneChannel = DyeColor.byId(redstoneChannel.getId()+1);
			else
				ioMode = ioMode==IOSideConfig.INPUT?IOSideConfig.OUTPUT: IOSideConfig.INPUT;
			markDirty();
			globalNet.getLocalNet(pos)
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
					.updateValues();
			this.markContainingBlockForUpdate(null);
			world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
		}
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return MiscConnectorBlock.DEFAULT_FACING_PROP;
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
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}


	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("ioMode", ioMode.ordinal());
		nbt.putInt("redstoneChannel", redstoneChannel.getId());
		nbt.putInt("output", output);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		ioMode = IOSideConfig.VALUES[nbt.getInt("ioMode")];
		redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel"));
		output = nbt.getInt("output");
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3d(.5-conRadius*side.getXOffset(), .5-conRadius*side.getYOffset(), .5-conRadius*side.getZOffset());
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		float length = .625f;
		float wMin = .3125f;
		return EnergyConnectorTileEntity.getConnectorBounds(getFacing(), wMin, length);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		if("io_out".equals(group))
			return this.ioMode==IOSideConfig.OUTPUT;
		else if("io_in".equals(group))
			return this.ioMode==IOSideConfig.INPUT;
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Vector4f getRenderColor(BlockState object, String group, Vector4f original)
	{
		if("coloured".equals(group))
		{
			float[] rgb = redstoneChannel.getColorComponentValues();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		return original;
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return redstoneChannel+";"+ioMode;
	}

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(!hammer)
			return null;
		return new String[]{
				I18n.format(Lib.DESC_INFO+"redstoneChannel", I18n.format("item.minecraft.firework_star."+redstoneChannel.getTranslationKey())),
				I18n.format(Lib.DESC_INFO+"blockSide.io."+this.ioMode.getName())
		};
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
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
		if(isRSInput())
			rsDirty = true;
	}
}