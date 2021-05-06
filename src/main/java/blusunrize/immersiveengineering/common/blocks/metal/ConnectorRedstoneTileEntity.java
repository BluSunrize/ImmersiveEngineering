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
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.wires.WireType.REDSTONE_CATEGORY;

public class ConnectorRedstoneTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IStateBasedDirectional,
		IRedstoneOutput, IScrewdriverInteraction, IBlockBounds, IBlockOverlayText, IOBJModelCallback<BlockState>,
		IRedstoneConnector
{
	public IOSideConfig ioMode = IOSideConfig.INPUT;
	public DyeColor redstoneChannel = DyeColor.WHITE;
	public boolean rsDirty = false;
	//Only write to this in wire network updates!
	private int output;

	public ConnectorRedstoneTileEntity()
	{
		this(IETileTypes.CONNECTOR_REDSTONE.get());
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
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		if(!isRSOutput()||side!=this.getFacing().getOpposite())
			return 0;
		return output;
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
			signals[redstoneChannel.getId()] = (byte)getMaxRSInput();
		rsDirty = false;
	}

	protected boolean acceptSignalFrom(Direction side)
	{
		BlockPos offset = pos.offset(side);
		TileEntity te = SafeChunkUtils.getSafeTE(world, offset);
		// If it's not a connector, exit early
		if(!(te instanceof ConnectorRedstoneTileEntity))
			return true;
		// If it's not the same net, exit early
		if(!this.getLocalNet(0).getConnectors().contains(offset))
			return true;
		// Allow connection if they are different colors
		return ((ConnectorRedstoneTileEntity)te).redstoneChannel!=this.redstoneChannel;
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
	public ActionResultType screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
	{
		if(world.isRemote)
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneConnector, this);
		return ActionResultType.SUCCESS;
	}

	protected void updateAfterConfigure()
	{
		markDirty();
		globalNet.getLocalNet(pos)
				.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
				.updateValues();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		return REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return MiscConnectableBlock.DEFAULT_FACING_PROP;
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("ioMode"))
			ioMode = IOSideConfig.VALUES[message.getInt("ioMode")];
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getInt("redstoneChannel"));
		updateAfterConfigure();
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
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		ioMode = IOSideConfig.VALUES[nbt.getInt("ioMode")];
		redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel"));
		output = nbt.getInt("output");
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vector3d(.5-conRadius*side.getXOffset(), .5-conRadius*side.getYOffset(), .5-conRadius*side.getZOffset());
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
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getHeldItem(Hand.MAIN_HAND)))
			return null;
		return new ITextComponent[]{
				new TranslationTextComponent(Lib.DESC_INFO+"redstoneChannel", I18n.format("item.minecraft.firework_star."+redstoneChannel.getTranslationKey())),
				new TranslationTextComponent(Lib.DESC_INFO+"blockSide.io."+this.ioMode.getString())
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
		int oldRSIn = getMaxRSInput();
		super.onNeighborBlockChange(otherPos);
		if(isRSInput()&&oldRSIn!=getMaxRSInput())
			rsDirty = true;
	}
}