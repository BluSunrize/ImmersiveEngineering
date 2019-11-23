/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nonnull;
import java.util.List;

public class ConnectorProbeTileEntity extends ConnectorRedstoneTileEntity
{
	private DyeColor redstoneChannelSending = DyeColor.WHITE;
	private int lastOutput = 0;
	public static TileEntityType<ConnectorProbeTileEntity> TYPE;

	public ConnectorProbeTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(!world.isRemote&&world.getGameTime()%8!=((getPos().getX()^getPos().getZ())&8))
		{
			int out = getComparatorSignal();
			if(out!=lastOutput)
			{
				this.lastOutput = out;
				this.rsDirty = true;
			}
		}
		super.tick();
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
		BlockPos pos = this.getPos().offset(getFacing());
		BlockState state = world.getBlockState(pos);
		if(state.hasComparatorInputOverride())
			return state.getComparatorInputOverride(world, pos);
		else if(state.isNormalCube(world, pos))
		{
			pos = pos.offset(getFacing());
			state = world.getBlockState(pos);
			if(state.hasComparatorInputOverride())
				return state.getComparatorInputOverride(world, pos);
			else if(state.getMaterial()==Material.AIR)
			{
				ItemFrameEntity entityitemframe = this.findItemFrame(world, getFacing(), pos);
				if(entityitemframe!=null)
					return entityitemframe.getAnalogOutput();
			}
		}
		return 0;
	}

	private ItemFrameEntity findItemFrame(World world, final Direction facing, BlockPos pos)
	{
		List<ItemFrameEntity> list = world.getEntitiesWithinAABB(ItemFrameEntity.class, new AxisAlignedBB(pos), entity -> entity!=null&&entity.getHorizontalFacing()==facing);
		return list.size()==1?list.get(0): null;
	}

	//TODO
	//@Override
	//public void updateInput(byte[] signals)
	//{
	//	signals[redstoneChannelSending] = (byte)Math.max(lastOutput, signals[redstoneChannelSending]);
	//	rsDirty = false;
	//}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		if(player.isSneaking())
			redstoneChannel = DyeColor.byId(redstoneChannel.getId()+1);
		else
			redstoneChannelSending = DyeColor.byId(redstoneChannelSending.getId()+1);
		markDirty();
		//TODO wireNetwork.updateValues();
		//TODO onChange();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
		return true;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("redstoneChannelSending", redstoneChannelSending.getId());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		redstoneChannelSending = DyeColor.byId(nbt.getInt("redstoneChannelSending"));
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3d(.5+side.getXOffset()*(.375-conRadius), .5+side.getYOffset()*(.375-conRadius), .5+side.getZOffset()*(.375-conRadius));
	}

	@Override
	public float[] getBlockBounds()
	{
		float wMin = .28125f;
		float wMax = .71875f;
		switch(getFacing().getOpposite())
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		if("glass".equals(group))
			return MinecraftForgeClient.getRenderLayer()==BlockRenderLayer.TRANSLUCENT;
		return MinecraftForgeClient.getRenderLayer()==BlockRenderLayer.CUTOUT;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getRenderColour(BlockState object, String group)
	{
		if("colour_in".equals(group))
			return 0xff000000|redstoneChannel.colorValue;
		else if("colour_out".equals(group))
			return 0xff000000|redstoneChannelSending.colorValue;
		return 0xffffffff;
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return redstoneChannel+";"+redstoneChannelSending;
	}

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(!hammer)
			return null;
		return new String[]{
				I18n.format(Lib.DESC_INFO+"redstoneChannel.rec", I18n.format("item.fireworksCharge."+redstoneChannel.getTranslationKey())),
				I18n.format(Lib.DESC_INFO+"redstoneChannel.send", I18n.format("item.fireworksCharge."+redstoneChannelSending.getTranslationKey()))
		};
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}
}