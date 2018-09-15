/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.client.ClientUtils.putVertexData;
import static net.minecraft.util.EnumFacing.*;

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
		double lowerV = 16*lowerHeight;
		double upperV = 16*upperHeight;
		TextureAtlasSprite tas = quads.get(0).getSprite();
		quads = new ArrayList<>();
		Matrix4 mat = new Matrix4(facing);
		Vector3f[] vertices = {
				new Vector3f(0, upperHeight, 0),//0
				new Vector3f(0, lowerHeight, 1),//1
				new Vector3f(1, lowerHeight, 1),//2
				new Vector3f(1, upperHeight, 0),//3
				new Vector3f(0, 0, 0),//4
				new Vector3f(0, 0, 1),//5
				new Vector3f(1, 0, 1),//6
				new Vector3f(1, 0, 0),//7
		};
		for(int i = 0; i < vertices.length; i++)
		{
			vertices[i] = mat.apply(vertices[i]);
		}
		//TOP
		addCulledQuad(quads, DefaultVertexFormats.ITEM, Arrays.copyOf(vertices, 4),
				UP, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		//BOTTOM
		addCulledQuad(quads, DefaultVertexFormats.ITEM, getArrayByIndices(vertices, 7, 6, 5, 4),
				DOWN, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		//SIDES
		addSides(quads, vertices, tas, lowerV, upperV, false);
		addSides(quads, vertices, tas, lowerV, upperV, true);
		if(isAtEnd(true))
			//HIGHER END
			addCulledQuad(quads, DefaultVertexFormats.ITEM, getArrayByIndices(vertices, 0, 3, 7, 4),
					NORTH, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});

		return quads;
	}

	private void addCulledQuad(List<BakedQuad> quads, VertexFormat format, Vector3f[] vertices, EnumFacing side,
							   TextureAtlasSprite tas, double[] uvs, float[] alpha)
	{
		quads.add(ClientUtils.createBakedQuad(format, vertices, side, tas, uvs, alpha, false));
		quads.add(ClientUtils.createBakedQuad(format, vertices, side, tas, uvs, alpha, true));
	}

	private void addSides(List<BakedQuad> quads, Vector3f[] vertices, TextureAtlasSprite tas, double lowerV,
						  double upperV, boolean invert)
	{
		quads.add(createSide(DefaultVertexFormats.ITEM, getArrayByIndices(vertices, 5, 1, 0, 4),
				WEST, tas, new double[]{0, 0, 16}, lowerV, upperV, invert));
		quads.add(createSide(DefaultVertexFormats.ITEM, getArrayByIndices(vertices, 7, 3, 2, 6),
				EAST, tas, new double[]{0, 0, 16}, upperV, lowerV, invert));
	}

	@SideOnly(Side.CLIENT)
	private BakedQuad createSide(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite,
								 double[] uvs, double leftV, double rightV, boolean invert)
	{
		facing = Utils.rotateFacingTowardsDir(facing, this.facing);
		if(invert)
			facing = facing.getOpposite();
		float[] colour = {1, 1, 1, 1};
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setQuadOrientation(facing);
		builder.setTexture(sprite);
		Normal faceNormal = new Normal(facing.getDirectionVec().getX(), facing.getDirectionVec().getY(),
				facing.getDirectionVec().getZ());
		int vertexId = invert?3: 0;
		int u = vertexId > 1?2: 0;
		putVertexData(format, builder, vertices[vertexId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
		vertexId = invert?2: 1;
		u = vertexId > 1?2: 0;
		double v = invert?rightV: leftV;
		putVertexData(format, builder, vertices[vertexId], faceNormal, uvs[u], v, sprite, colour, 1);
		vertexId = invert?1: 2;
		u = vertexId > 1?2: 0;
		v = invert?leftV: rightV;
		putVertexData(format, builder, vertices[vertexId], faceNormal, uvs[u], v, sprite, colour, 1);
		vertexId = invert?0: 3;
		u = vertexId > 1?2: 0;
		putVertexData(format, builder, vertices[vertexId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
		return builder.build();
	}

	private Vector3f[] getArrayByIndices(Vector3f[] in, int... indices)
	{
		Vector3f[] ret = new Vector3f[indices.length];
		for(int i = 0; i < indices.length; i++)
		{
			ret[i] = in[indices[i]];
		}
		return ret;
	}

	@Override
	public String getCacheKey(IBlockState object)
	{
		return totalLength+","+slopePosition+","+facing.name();
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
