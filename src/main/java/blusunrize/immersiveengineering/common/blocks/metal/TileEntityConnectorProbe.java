/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TileEntityConnectorProbe extends TileEntityConnectorRedstone
{
	private int redstoneChannelSending = 0;
	private int lastOutput = 0;

	@Override
	public void update()
	{
		if(!world.isRemote&&world.getTotalWorldTime()%8!=((getPos().getX()^getPos().getZ())&8))
		{
			int out = getComparatorSignal();
			if(out!=lastOutput)
			{
				this.lastOutput = out;
				this.rsDirty = true;
			}
		}
		super.update();
	}

	@Override
	public boolean isRSInput()
	{
		return true;
	}

	@Override
	public boolean isRSOutput()
	{
		return true;
	}

	private int getComparatorSignal()
	{
		BlockPos pos = this.getPos().offset(facing);
		IBlockState state = world.getBlockState(pos);
		if(state.hasComparatorInputOverride())
			return state.getComparatorInputOverride(world, pos);
		else if(state.isNormalCube())
		{
			pos = pos.offset(facing);
			state = world.getBlockState(pos);
			if(state.hasComparatorInputOverride())
				return state.getComparatorInputOverride(world, pos);
			else if(state.getMaterial()==Material.AIR)
			{
				EntityItemFrame entityitemframe = this.findItemFrame(world, facing, pos);
				if(entityitemframe!=null)
					return entityitemframe.getAnalogOutput();
			}
		}
		return 0;
	}

	private EntityItemFrame findItemFrame(World world, final EnumFacing facing, BlockPos pos)
	{
		List<EntityItemFrame> list = world.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pos), entity -> entity!=null&&entity.getHorizontalFacing()==facing);
		return list.size()==1?list.get(0): null;
	}

	@Override
	public void updateInput(byte[] signals)
	{
		signals[redstoneChannelSending] = (byte)Math.max(lastOutput, signals[redstoneChannelSending]);
		rsDirty = false;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
			redstoneChannel = (redstoneChannel+1)%16;
		else
			redstoneChannelSending = (redstoneChannelSending+1)%16;
		markDirty();
		wireNetwork.updateValues();
		onChange();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
		return true;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("redstoneChannelSending", redstoneChannelSending);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		redstoneChannelSending = nbt.getInteger("redstoneChannelSending");
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5+side.getXOffset()*(.375-conRadius), .5+side.getYOffset()*(.375-conRadius), .5+side.getZOffset()*(.375-conRadius));
	}

	@Override
	public float[] getBlockBounds()
	{
		float wMin = .28125f;
		float wMax = .71875f;
		switch(facing.getOpposite())
		{
			case UP:
			case DOWN:
				return new float[]{wMin, 0, wMin, wMax, 1, wMax};
			case SOUTH:
			case NORTH:
				return new float[]{wMin, wMin, 0, wMax, wMax, 1};
			case EAST:
			case WEST:
				return new float[]{0, wMin, wMin, 1, wMax, wMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		if("glass".equals(group))
			return MinecraftForgeClient.getRenderLayer()==BlockRenderLayer.TRANSLUCENT;
		return MinecraftForgeClient.getRenderLayer()==BlockRenderLayer.CUTOUT;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColour(IBlockState object, String group)
	{
		if("colour_in".equals(group))
			return 0xff000000|EnumDyeColor.byMetadata(this.redstoneChannel).getColorValue();
		else if("colour_out".equals(group))
			return 0xff000000|EnumDyeColor.byMetadata(this.redstoneChannelSending).getColorValue();
		return 0xffffffff;
	}

	@Override
	public String getCacheKey(IBlockState object)
	{
		return redstoneChannel+";"+redstoneChannelSending;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(!hammer)
			return null;
		return new String[]{
				I18n.format(Lib.DESC_INFO+"redstoneChannel.rec", I18n.format("item.fireworksCharge."+EnumDyeColor.byMetadata(redstoneChannel).getTranslationKey())),
				I18n.format(Lib.DESC_INFO+"redstoneChannel.send", I18n.format("item.fireworksCharge."+EnumDyeColor.byMetadata(redstoneChannelSending).getTranslationKey()))
		};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}
}