/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityConnectorRedstone extends TileEntityImmersiveConnectable implements ITickable, IDirectionalTile,
		IRedstoneOutput, IHammerInteraction, IBlockBounds, IBlockOverlayText, IOBJModelCallback<IBlockState>,
		IRedstoneConnector
{
	public EnumFacing facing = EnumFacing.DOWN;
	public int ioMode = 0; // 0 - input, 1 -output
	public int redstoneChannel = 0;
	public boolean rsDirty = false;
	//Only write to this in wire network updates!
	private int output;

	@Override
	public void update()
	{
		if(hasWorld()&&!world.isRemote&&rsDirty)
			globalNet.getLocalNet(pos)
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
					.updateValues();
	}

	@Override
	public int getStrongRSOutput(IBlockState state, EnumFacing side)
	{
		if(!isRSOutput()||side!=this.facing.getOpposite())
			return 0;
		return output;
	}

	@Override
	public int getWeakRSOutput(IBlockState state, EnumFacing side)
	{
		if(!isRSOutput())
			return 0;
		return output;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, EnumFacing side)
	{
		return true;
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		output = handler.getValue(redstoneChannel);
		if(!isInvalid()&&isRSOutput())
		{
			markDirty();
			IBlockState stateHere = world.getBlockState(pos);
			markContainingBlockForUpdate(stateHere);
			markBlockForUpdate(pos.offset(facing), stateHere);
		}
	}

	public boolean isRSInput()
	{
		return ioMode==0;
	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		if(isRSInput())
			signals[redstoneChannel] = (byte)Math.max(getLocalRS(), signals[redstoneChannel]);
		rsDirty = false;
	}

	protected int getLocalRS()
	{
		int val = world.getRedstonePowerFromNeighbors(pos);
		if(val==0)
		{
			for(EnumFacing f : EnumFacing.HORIZONTALS)
			{
				IBlockState state = world.getBlockState(pos.offset(f));
				if(state.getBlock()==Blocks.REDSTONE_WIRE&&state.getValue(BlockRedstoneWire.POWER) > val)
					val = state.getValue(BlockRedstoneWire.POWER);
			}
		}
		return val;
	}

	public boolean isRSOutput()
	{
		return ioMode==1;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		//Sneaking iterates through colours, normal hammerign toggles in and out
		if(player.isSneaking())
			redstoneChannel = (redstoneChannel+1)%16;
		else
			ioMode = ioMode==0?1: 0;
		markDirty();
		globalNet.getLocalNet(pos)
				.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
				.updateValues();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}


	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInt("facing", facing.ordinal());
		nbt.setInt("ioMode", ioMode);
		nbt.setInt("redstoneChannel", redstoneChannel);
		nbt.setInt("output", output);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.byIndex(nbt.getInt("facing"));
		ioMode = nbt.getInt("ioMode");
		redstoneChannel = nbt.getInt("redstoneChannel");
		output = nbt.getInt("output");
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3d(.5-conRadius*side.getXOffset(), .5-conRadius*side.getYOffset(), .5-conRadius*side.getZOffset());
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		int inc = getRenderRadiusIncrease();
		return new AxisAlignedBB(this.pos.getX()-inc, this.pos.getY()-inc, this.pos.getZ()-inc, this.pos.getX()+inc+1, this.pos.getY()+inc+1, this.pos.getZ()+inc+1);
	}

	int getRenderRadiusIncrease()
	{
		return WireType.REDSTONE.getMaxLength();
	}

	@Override
	public float[] getBlockBounds()
	{
		float length = .625f;
		float wMin = .3125f;
		float wMax = .6875f;
		switch(facing.getOpposite())
		{
			case UP:
				return new float[]{wMin, 0, wMin, wMax, length, wMax};
			case DOWN:
				return new float[]{wMin, 1-length, wMin, wMax, 1, wMax};
			case SOUTH:
				return new float[]{wMin, wMin, 0, wMax, wMax, length};
			case NORTH:
				return new float[]{wMin, wMin, 1-length, wMax, wMax, 1};
			case EAST:
				return new float[]{0, wMin, wMin, length, wMax, wMax};
			case WEST:
				return new float[]{1-length, wMin, wMin, 1, wMax, wMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		if("io_out".equals(group))
			return this.ioMode==1;
		else if("io_in".equals(group))
			return this.ioMode==0;
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColour(IBlockState object, String group)
	{
		if("coloured".equals(group))
			return 0xff000000|EnumDyeColor.byMetadata(this.redstoneChannel).getColorValue();
		return 0xffffffff;
	}

	@Override
	public String getCacheKey(IBlockState object)
	{
		return redstoneChannel+";"+ioMode;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(!hammer)
			return null;
		return new String[]{
				I18n.format(Lib.DESC_INFO+"redstoneChannel", I18n.format("item.fireworksCharge."+EnumDyeColor.byMetadata(redstoneChannel).getTranslationKey())),
				I18n.format(Lib.DESC_INFO+"blockSide.io."+this.ioMode)
		};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers()
	{
		return ImmutableList.of(RedstoneNetworkHandler.ID);
	}
}