/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author BluSunrize - 20.08.2016
 */
public class ConveyorSplit extends ConveyorBasic
{
	Direction outputFace = Direction.NORTH;

	public ConveyorSplit(Direction startingOutputFace)
	{
		this.outputFace = startingOutputFace.rotateY();
	}

	@Override
	public ConveyorDirection getConveyorDirection()
	{
		return ConveyorDirection.HORIZONTAL;
	}

	@Override
	public boolean changeConveyorDirection()
	{
		return false;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		return false;
	}

	@Override
	public void afterRotation(Direction oldDir, Direction newDir)
	{
		this.outputFace = newDir.rotateY();
	}

	@Override
	public void handleInsertion(TileEntity tile, ItemEntity entity, Direction facing, ConveyorDirection conDir, double distX, double distZ)
	{
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(tile.getPos().hashCode());
		if(entity.getEntityData().hasKey(nbtKey))
		{
			Direction redirect = Direction.values()[entity.getEntityData().getInt(nbtKey)];
			BlockPos nextPos = tile.getPos().offset(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.posZ: entity.posX));
			if(distNext < .7)
				super.handleInsertion(tile, entity, redirect, conDir, distX, distZ);
		}
	}

	@Override
	public void onEntityCollision(TileEntity tile, Entity entity, Direction facing)
	{
		if(!isActive(tile))
			return;
		Direction redirect = null;
		if(entity!=null&&!entity.isDead)
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(tile.getPos().hashCode());
			if(entity.getEntityData().hasKey(nbtKey))
				redirect = Direction.values()[entity.getEntityData().getInt(nbtKey)];
			else
			{
				redirect = this.outputFace;
				entity.getEntityData().setInt(nbtKey, redirect.ordinal());
				BlockPos nextPos = tile.getPos().offset(this.outputFace.getOpposite());
				if(tile.getWorld().isBlockLoaded(nextPos))
				{
					TileEntity nextTile = tile.getWorld().getTileEntity(nextPos);
					if(!(nextTile instanceof IConveyorTile))
						this.outputFace = outputFace.getOpposite();
					else if(((IConveyorTile)nextTile).getFacing()!=this.outputFace)
						this.outputFace = outputFace.getOpposite();
				}
			}
		}
		super.onEntityCollision(tile, entity, facing);
		if(redirect!=null)
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(tile.getPos().hashCode());
			BlockPos nextPos = tile.getPos().offset(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.posZ: entity.posX));
			double treshold = .4;
			boolean contact = distNext < treshold;
			if(contact)
				entity.getEntityData().removeTag(nbtKey);
		}
	}

	@Override
	public boolean renderWall(TileEntity tile, Direction facing, int wall)
	{
		return false;
	}

	@Override
	public Direction[] sigTransportDirections(TileEntity conveyorTile, Direction facing)
	{
		return new Direction[]{facing.rotateY(), facing.rotateYCCW()};
	}

	@Override
	public Vec3d getDirection(TileEntity conveyorTile, Entity entity, Direction facing)
	{
		Vec3d vec = super.getDirection(conveyorTile, entity, facing);
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(conveyorTile.getPos().hashCode());
		if(!entity.getEntityData().hasKey(nbtKey))
			return vec;
		Direction redirect = Direction.byIndex(entity.getEntityData().getInt(nbtKey));
		BlockPos wallPos = conveyorTile.getPos().offset(facing);
		double distNext = Math.abs((facing.getAxis()==Axis.Z?wallPos.getZ(): wallPos.getX())+.5-(facing.getAxis()==Axis.Z?entity.posZ: entity.posX));
		if(distNext < 1.33)
		{
			double sideMove = Math.pow(1+distNext, 0.1)*.2;
			if(distNext < .8)
				vec = new Vec3d(facing.getAxis()==Axis.X?0: vec.x, vec.y, facing.getAxis()==Axis.Z?0: vec.z);
			vec = vec.add(redirect.getXOffset()*sideMove, 0, redirect.getZOffset()*sideMove);
		}
		return vec;
	}

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		nbt.setInt("outputFace", outputFace.ordinal());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		outputFace = Direction.values()[nbt.getInt("outputFace")];
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:blocks/conveyor_split");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:blocks/conveyor_split_off");
	public static ResourceLocation texture_casing = new ResourceLocation("immersiveengineering:blocks/conveyor_split_wall");

	@Override
	public ResourceLocation getActiveTexture()
	{
		return texture_on;
	}

	@Override
	public ResourceLocation getInactiveTexture()
	{
		return texture_off;
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, Direction facing)
	{
		TextureAtlasSprite tex_casing0 = ClientUtils.getSprite(texture_casing);
		Matrix4 matrix = new Matrix4(facing);
		float[] colour = {1, 1, 1, 1};
		Vector3f[] vertices = {new Vector3f(.0625f, .1875f, 0), new Vector3f(.0625f, .1875f, 1), new Vector3f(.9375f, .1875f, 1), new Vector3f(.9375f, .1875f, 0)};
		baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Direction.UP, tex_casing0, new double[]{1, 16, 15, 0}, colour, false));

		vertices = new Vector3f[]{new Vector3f(.0625f, 0, 0), new Vector3f(.0625f, .1875f, 0), new Vector3f(.9375f, .1875f, 0), new Vector3f(.9375f, 0, 0)};
		baseModel.set(15, ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, ClientUtils.getSprite(ModelConveyor.rl_casing[1]), new double[]{1, 16, 15, 13}, colour, false));

		vertices = new Vector3f[]{new Vector3f(.0625f, .125f, 0), new Vector3f(.0625f, .1875f, 0), new Vector3f(.9375f, .1875f, 0), new Vector3f(.9375f, .125f, 0)};
		Vector3f[] vertices2 = new Vector3f[]{new Vector3f(.5f, .125f, 0), new Vector3f(.5f, .125f, .5f), new Vector3f(.5f, .1875f, .5f), new Vector3f(.5f, .1875f, 0)};
		Vector3f[] vertices3 = new Vector3f[]{new Vector3f(.5f, .125f, 0), new Vector3f(.5f, .125f, .5f), new Vector3f(.5f, .1875f, .5f), new Vector3f(.5f, .1875f, 0)};
		for(int i = 0; i < 8; i++)
		{
			for(int iv = 0; iv < vertices.length; iv++)
			{
				vertices[iv].setZ((i+1)*.0625f);
				vertices2[iv].setX(vertices2[iv].getX()+.0625f);
				vertices3[iv].setX(vertices3[iv].getX()-.0625f);
			}
			double v = 16-i;
			baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_casing0, new double[]{1, v-1, 15, v}, colour, true));
			if(i < 7)
			{
				double u = 8-i;
				baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices2), facing, tex_casing0, new double[]{u-1, 16, u, 8}, colour, true));
				baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices3), facing, tex_casing0, new double[]{u-1, 16, u, 8}, colour, false));
			}
		}
		return baseModel;
	}
}
