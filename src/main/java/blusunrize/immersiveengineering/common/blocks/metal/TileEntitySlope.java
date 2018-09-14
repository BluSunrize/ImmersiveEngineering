/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.UP;

public class TileEntitySlope extends TileEntityIEBase implements IOBJModelCallback<IBlockState>, INeighbourChangeTile,
		IDirectionalTile, IBlockBounds
{
	private int totalLength = 1;
	private int slopePosition = 0;
	private EnumFacing facing = NORTH;
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		int oldLength = totalLength, oldPos = slopePosition;
		totalLength = nbt.getInteger("totalLength");
		slopePosition = nbt.getInteger("slopePosition");
		if(world!=null&&world.isRemote&&(oldLength!=totalLength||slopePosition!=oldPos))
		{
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
		facing = EnumFacing.VALUES[nbt.getInteger("facing")];
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("totalLength", totalLength);
		nbt.setInteger("slopePosition", slopePosition);
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		if (world.isRemote)
			return;
		boolean positive;
		if(otherPos.equals(pos.offset(facing, 1)))
			positive = true;
		else if(otherPos.equals(pos.offset(facing, -1)))
			positive = false;
		else
			return;
		TileEntitySlope slope = null;
		{
			TileEntity atOther = world.getTileEntity(otherPos);
			if(atOther instanceof TileEntitySlope)
			{
				TileEntitySlope tmp = (TileEntitySlope)atOther;
				if(tmp.facing==this.facing)
					slope = (TileEntitySlope)atOther;
			}
		}
		boolean atEnd = isAtEnd(positive);
		if(atEnd==(slope==null))
			return;
		if(slope==null)
		{
			int toEnd = blocksToEnd(positive);
			forEachSlopeBlockBeyond(positive, false, true, other->{
				other.totalLength = toEnd-1;
				if (positive)
					other.slopePosition -= slopePosition+1;
				updateNoNeighbours(other.pos);
			});
			forEachSlopeBlockBeyond(!positive, true, true, other->{
				other.totalLength = totalLength-toEnd;
				if (!positive)
					other.slopePosition -= this.slopePosition;
				updateNoNeighbours(other.pos);
			});

		} else {
			int oldLength = totalLength;
			if (!positive)
				slopePosition += slope.totalLength;
			totalLength += slope.totalLength;
			forEachSlopeBlockBeyond(positive, false, false, other->{
				other.totalLength = totalLength;
				if (positive)
					other.slopePosition += oldLength;
				updateNoNeighbours(other.pos);
			});
			forEachSlopeBlockBeyond(!positive, false, false, other->{
				other.totalLength = totalLength;
				if (!positive)
					other.slopePosition += totalLength-oldLength;
				updateNoNeighbours(other.pos);
			});
		}
		updateNoNeighbours(pos);
	}

	private boolean isAtEnd(boolean positive)
	{
		if(positive)
			return slopePosition==totalLength-1;
		else
			return slopePosition==0;
	}

	private int blocksToEnd(boolean positive)
	{
		if (positive)
			return totalLength-slopePosition-1;
		else
			return slopePosition;
	}

	private void forEachSlopeBlockBeyond(boolean positive, boolean includeThis, boolean removing,
										 Consumer<TileEntitySlope> out)
	{
		if(positive)
			for(int i = 1; i < totalLength-slopePosition; i++)
				acceptIfValid(i, removing, out);
		else
			for (int i = -1;i>=-slopePosition;i--)
				acceptIfValid(i, removing, out);
		if (includeThis)
			out.accept(this);
	}

	private void acceptIfValid(int offsetToHere, boolean removing, Consumer<TileEntitySlope> out) {
		BlockPos posI = pos.offset(facing, offsetToHere);
		TileEntity teAtI = world.getTileEntity(posI);
		if(teAtI instanceof TileEntitySlope)
		{
			TileEntitySlope slope = (TileEntitySlope)teAtI;
			int offsetAtPos = slopePosition+offsetToHere;
			if((!removing||(slope.totalLength==this.totalLength&&slope.slopePosition==offsetAtPos))
					&&slope.facing==this.facing)
				out.accept(slope);
		}
	}

	private void updateNoNeighbours(BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
		totalLength = 1;
		slopePosition = 0;
		if (world!=null)
			world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return side.getAxis()==Axis.Y;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return axis.getAxis()==Axis.Y;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(IBlockState object, List<BakedQuad> quads)
	{
		float lowerHeight = slopePosition/(float)totalLength;
		float upperHeight = (slopePosition+1F)/totalLength;
		TextureAtlasSprite tas = quads.get(0).getSprite();
		quads = new ArrayList<>();
		//TODO transform!
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, new Vector3f[]{
				new Vector3f(0, upperHeight, 0),
				new Vector3f(0, lowerHeight, 1),
				new Vector3f(1, lowerHeight, 1),
				new Vector3f(1, upperHeight, 0)
		}, UP, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1}, false));
		return quads;
	}

	@Override
	public String getCacheKey(IBlockState object)
	{
		return totalLength+","+slopePosition;
	}

	@Override
	public float[] getBlockBounds()
	{
		float height = (.5F+slopePosition)/totalLength;
		return new float[]{
				0, 0, 0, 1, height, 1
		};
	}
}
