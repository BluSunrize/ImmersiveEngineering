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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration2;
import static net.minecraft.util.EnumFacing.*;

public class TileEntityStructuralArm extends TileEntityIEBase implements IOBJModelCallback<IBlockState>, INeighbourChangeTile,
		IDirectionalTile, IAdvancedCollisionBounds, IAdvancedSelectionBounds
{
	private int totalLength = 1;
	private int slopePosition = 0;
	private EnumFacing facing = NORTH;
	private boolean onCeiling = false;
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		int oldLength = totalLength, oldPos = slopePosition;
		totalLength = nbt.getInteger("totalLength");
		slopePosition = nbt.getInteger("slopePosition");
		onCeiling = nbt.getBoolean("onCeiling");
		if(world!=null&&world.isRemote&&(oldLength!=totalLength||slopePosition!=oldPos))
		{
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			bounds = null;
		}
		facing = EnumFacing.VALUES[nbt.getInteger("facing")];
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("totalLength", totalLength);
		nbt.setInteger("slopePosition", slopePosition);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("onCeiling", onCeiling);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		if(world.isRemote)
			return;
		boolean positive;
		if(otherPos.equals(pos.offset(facing, 1)))
			positive = true;
		else if(otherPos.equals(pos.offset(facing, -1)))
			positive = false;
		else
			return;
		TileEntityStructuralArm slope = null;
		{
			TileEntity atOther = world.getTileEntity(otherPos);
			if(atOther instanceof TileEntityStructuralArm)
			{
				TileEntityStructuralArm tmp = (TileEntityStructuralArm)atOther;
				IBlockState stateHere = world.getBlockState(pos);
				IBlockState stateThere = world.getBlockState(otherPos);
				BlockTypes_MetalDecoration2 typeHere = stateHere.getValue(blockMetalDecoration2.property);
				BlockTypes_MetalDecoration2 typeOther = stateThere.getValue(blockMetalDecoration2.property);
				if(tmp.facing==this.facing&&typeHere==typeOther)
					slope = (TileEntityStructuralArm)atOther;
			}
		}
		boolean atEnd = isAtEnd(positive);
		if(atEnd==(slope==null))
			return;
		if(slope==null)
		{
			int toEnd = blocksToEnd(positive);
			forEachSlopeBlockBeyond(positive, false, true, other -> {
				other.totalLength = toEnd-1;
				if(positive)
					other.slopePosition -= slopePosition+2;
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});
			forEachSlopeBlockBeyond(!positive, true, true, other -> {
				other.totalLength = totalLength-toEnd;
				if(!positive)
					other.slopePosition -= this.slopePosition;
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});

		}
		else
		{
			int oldLength = totalLength;
			if(!positive)
				slopePosition += slope.totalLength;
			totalLength += slope.totalLength;
			forEachSlopeBlockBeyond(positive, false, false, other -> {
				other.totalLength = totalLength;
				if(positive)
					other.slopePosition += oldLength;
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});
			forEachSlopeBlockBeyond(!positive, false, false, other -> {
				other.totalLength = totalLength;
				if(!positive)
					other.slopePosition += totalLength-oldLength;
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});
			bounds = null;
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
		if(positive)
			return totalLength-slopePosition-1;
		else
			return slopePosition;
	}

	private void forEachSlopeBlockBeyond(boolean positive, boolean includeThis, boolean removing,
										 Consumer<TileEntityStructuralArm> out)
	{
		if(positive)
			for(int i = 1; i < totalLength-slopePosition; i++)
				acceptIfValid(i, removing, out);
		else
			for(int i = -1; i >= -slopePosition; i--)
				acceptIfValid(i, removing, out);
		if(includeThis)
			out.accept(this);
	}

	private void acceptIfValid(int offsetToHere, boolean removing, Consumer<TileEntityStructuralArm> out)
	{
		BlockPos posI = pos.offset(facing, offsetToHere);
		TileEntity teAtI = world.getTileEntity(posI);
		if(teAtI instanceof TileEntityStructuralArm)
		{
			TileEntityStructuralArm slope = (TileEntityStructuralArm)teAtI;
			int offsetAtPos = slopePosition+offsetToHere;
			IBlockState stateHere = world.getBlockState(pos);
			IBlockState otherState = world.getBlockState(posI);
			BlockTypes_MetalDecoration2 typeHere = stateHere.getValue(blockMetalDecoration2.property);
			BlockTypes_MetalDecoration2 typeOther = otherState.getValue(blockMetalDecoration2.property);
			if((!removing||(slope.totalLength==this.totalLength&&slope.slopePosition==offsetAtPos&&slope.onCeiling==this.onCeiling))
					&&typeHere==typeOther
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
		bounds = null;
		if(world!=null)
			world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
	}

	@Override
	public EnumFacing getFacingForPlacement(EntityLivingBase placer, BlockPos pos, EnumFacing side, float hitX, float hitY,
											float hitZ)
	{
		onCeiling = (side==DOWN)||(side!=UP&&hitY > .5);
		return IDirectionalTile.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
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

	private List<AxisAlignedBB> bounds = null;

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		return getBounds();
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getBounds();
	}

	private List<AxisAlignedBB> getBounds()
	{
		if(bounds==null)
		{
			double lowerH = (slopePosition+.5)/totalLength;
			double upperH = (slopePosition+1.)/totalLength;
			List<AxisAlignedBB> basic;
			if(!onCeiling)
				basic = ImmutableList.of(
						new AxisAlignedBB(0, 0, 0, 1, lowerH, 1),
						new AxisAlignedBB(0, lowerH, 0, 1, upperH, .5)
				);
			else
				basic = ImmutableList.of(
						new AxisAlignedBB(0, 1-lowerH, 0, 1, 1, 1),
						new AxisAlignedBB(0, 1-upperH, 0, 1, 1-lowerH, .5)
				);
			bounds = basic.stream()
					.map(aabb -> Utils.transformAABB(aabb, facing).offset(pos))
					.collect(ImmutableList.toImmutableList());
		}
		return bounds;
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
		VertexFormat format = quads.get(0).getFormat();
		quads = new ArrayList<>();
		Matrix4 mat = new Matrix4(facing);

		Vector3f[] vertices;
		{
			float y03 = onCeiling?1: upperHeight;
			float y12 = onCeiling?1: lowerHeight;
			float y47 = onCeiling?1-upperHeight: 0;
			float y56 = onCeiling?1-lowerHeight: 0;
			vertices = new Vector3f[]{
					new Vector3f(0, y03, 0),//0
					new Vector3f(0, y12, 1),//1
					new Vector3f(1, y12, 1),//2
					new Vector3f(1, y03, 0),//3
					new Vector3f(0, y47, 0),//4
					new Vector3f(0, y56, 1),//5
					new Vector3f(1, y56, 1),//6
					new Vector3f(1, y47, 0),//7
			};
		}
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = mat.apply(vertices[i]);
		//TOP
		addCulledQuad(quads, format, Arrays.copyOf(vertices, 4),
				UP, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		//BOTTOM
		addCulledQuad(quads, format, getArrayByIndices(vertices, 7, 6, 5, 4),
				DOWN, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		//SIDES
		addSides(quads, vertices, tas, lowerV, upperV, false);
		addSides(quads, vertices, tas, lowerV, upperV, true);
		if(isAtEnd(true))
			//HIGHER END
			addCulledQuad(quads, format, getArrayByIndices(vertices, 0, 3, 7, 4),
					NORTH, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		return quads;
	}

	private static final Matrix4 SHRINK;

	static
	{
		SHRINK = new Matrix4();
		SHRINK.translate(.5, .5, .5);
		SHRINK.scale(.999, .999, .999);
		SHRINK.translate(-.5, -.5, -.5);
	}

	private void addCulledQuad(List<BakedQuad> quads, VertexFormat format, Vector3f[] vertices, EnumFacing side,
							   TextureAtlasSprite tas, double[] uvs, float[] alpha)
	{
		side = Utils.rotateFacingTowardsDir(side, this.facing);
		quads.add(ClientUtils.createBakedQuad(format, vertices, side, tas, uvs, alpha, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = SHRINK.apply(vertices[i]);
		quads.add(ClientUtils.createBakedQuad(format, vertices, side.getOpposite(), tas, uvs, alpha, true));
	}

	private void addSides(List<BakedQuad> quads, Vector3f[] vertices, TextureAtlasSprite tas, double lowerV,
						  double upperV, boolean invert)
	{
		if(invert)
		{
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = SHRINK.apply(vertices[i]);
		}
		quads.add(createSide(DefaultVertexFormats.ITEM, getArrayByIndices(vertices, 5, 1, 0, 4),
				WEST, tas, lowerV, upperV, invert));
		quads.add(createSide(DefaultVertexFormats.ITEM, getArrayByIndices(vertices, 7, 3, 2, 6),
				EAST, tas, upperV, lowerV, invert));
	}

	@SideOnly(Side.CLIENT)
	private BakedQuad createSide(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite,
								 double leftV, double rightV, boolean invert)
	{
		facing = Utils.rotateFacingTowardsDir(facing, this.facing);
		if(invert)
		{
			double tmp = leftV;
			leftV = rightV;
			rightV = tmp;
		}
		if(invert)
			facing = facing.getOpposite();
		float[] colour = {1, 1, 1, 1};
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setQuadOrientation(facing);
		builder.setTexture(sprite);
		Normal faceNormal = new Normal(facing.getDirectionVec().getX(), facing.getDirectionVec().getY(),
				facing.getDirectionVec().getZ());
		int vertexId = invert?3: 0;
		double v = onCeiling?16-leftV: 0;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?2: 1;
		v = onCeiling?16: leftV;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?1: 2;
		v = onCeiling?16: rightV;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?0: 3;
		v = onCeiling?16-rightV: 0;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
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
		return totalLength+","+slopePosition+","+facing.name()+","+(onCeiling?"1": "0");
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{
				0, 0, 0, 1, (slopePosition+.5F)/totalLength, 1
		};
	}
}
