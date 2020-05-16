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
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.common.util.Utils.withCoordinate;

/**
 * @author BluSunrize - 20.08.2016
 */
public class SplitConveyor extends BasicConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "splitter");
	boolean nextOutputLeft = true;

	public SplitConveyor(TileEntity tile)
	{
		super(tile);
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
	public void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getPos().hashCode());
		if(entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
		{
			Direction redirect = Direction.values()[entity.getPersistentData().getInt(nbtKey)];
			BlockPos nextPos = getTile().getPos().offset(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getPosZ(): entity.getPosX()));
			if(distNext < .7)
				super.handleInsertion(entity, conDir, distX, distZ);
		}
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		if(!isActive())
			return;
		Direction redirect = null;
		if(entity!=null&&entity.isAlive())
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getPos().hashCode());
			if(entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
				redirect = Direction.values()[entity.getPersistentData().getInt(nbtKey)];
			else
			{
				redirect = getOutputFace();
				entity.getPersistentData().putInt(nbtKey, redirect.ordinal());
				BlockPos nextPos = getTile().getPos().offset(this.getOutputFace().getOpposite());
				if(getTile().getWorld().isBlockLoaded(nextPos))
				{
					TileEntity nextTile = getTile().getWorld().getTileEntity(nextPos);
					if(!(nextTile instanceof IConveyorTile))
						nextOutputLeft = !nextOutputLeft;
					else if(((IConveyorTile)nextTile).getFacing()!=this.getOutputFace())
						nextOutputLeft = !nextOutputLeft;
				}
			}
		}
		super.onEntityCollision(entity);
		if(redirect!=null)
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getPos().hashCode());
			BlockPos nextPos = getTile().getPos().offset(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getPosZ(): entity.getPosX()));
			double treshold = .4;
			boolean contact = distNext < treshold;
			if(contact)
				entity.getPersistentData().remove(nbtKey);
		}
	}

	@Override
	public boolean renderWall(Direction facing, int wall)
	{
		return false;
	}

	@Override
	public Direction[] sigTransportDirections()
	{
		return new Direction[]{getFacing().rotateY(), getFacing().rotateYCCW()};
	}

	@Override
	public Vec3d getDirection(Entity entity)
	{
		Vec3d vec = super.getDirection(entity);
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getPos().hashCode());
		if(!entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
			return vec;
		Direction redirect = Direction.byIndex(entity.getPersistentData().getInt(nbtKey));
		BlockPos wallPos = getTile().getPos().offset(getFacing());
		double distNext = Math.abs((getFacing().getAxis()==Axis.Z?wallPos.getZ(): wallPos.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getPosZ(): entity.getPosX()));
		if(distNext < 1.33)
		{
			double sideMove = Math.pow(1+distNext, 0.1)*.2;
			if(distNext < .8)
				vec = new Vec3d(getFacing().getAxis()==Axis.X?0: vec.x, vec.y, getFacing().getAxis()==Axis.Z?0: vec.z);
			vec = vec.add(redirect.getXOffset()*sideMove, 0, redirect.getZOffset()*sideMove);
		}
		return vec;
	}

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		nbt.putBoolean("nextLeft", nextOutputLeft);
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		nextOutputLeft = nbt.getBoolean("nextLeft");
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/split");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/split_off");
	public static ResourceLocation texture_casing = new ResourceLocation("immersiveengineering:block/conveyor/split_wall");

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
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		TextureAtlasSprite tex_casing0 = ClientUtils.getSprite(texture_casing);
		Matrix4 matrix = new Matrix4(getFacing());
		TransformationMatrix tMatrix = matrix.toTransformationMatrix();
		float[] colour = {1, 1, 1, 1};

		Vec3d[] vertices = {new Vec3d(.0625f, 0, 0), new Vec3d(.0625f, 0, 1), new Vec3d(.9375f, 0, 1), new Vec3d(.9375f, 0, 0)};

		// replace bottom with casing
		baseModel.set(0, ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, getFacing()), ClientUtils.getSprite(ModelConveyor.rl_casing[3]), new double[]{1, 0, 15, 16}, colour, true));

		vertices = new Vec3d[]{new Vec3d(.0625f, .1875f, 0), new Vec3d(.0625f, .1875f, 1), new Vec3d(.9375f, .1875f, 1), new Vec3d(.9375f, .1875f, 0)};
		baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Direction.UP, tex_casing0, new double[]{1, 16, 15, 0}, colour, false));

		vertices = new Vec3d[]{new Vec3d(.0625f, 0, 0), new Vec3d(.0625f, .1875f, 0), new Vec3d(.9375f, .1875f, 0), new Vec3d(.9375f, 0, 0)};
		baseModel.set(15, ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), getFacing(), ClientUtils.getSprite(ModelConveyor.rl_casing[1]), new double[]{1, 16, 15, 13}, colour, false));

		vertices = new Vec3d[]{new Vec3d(.0625f, .125f, 0), new Vec3d(.0625f, .1875f, 0), new Vec3d(.9375f, .1875f, 0), new Vec3d(.9375f, .125f, 0)};
		Vec3d[] vertices2 = new Vec3d[]{new Vec3d(.5f, .125f, 0), new Vec3d(.5f, .125f, .5f), new Vec3d(.5f, .1875f, .5f), new Vec3d(.5f, .1875f, 0)};
		Vec3d[] vertices3 = new Vec3d[]{new Vec3d(.5f, .125f, 0), new Vec3d(.5f, .125f, .5f), new Vec3d(.5f, .1875f, .5f), new Vec3d(.5f, .1875f, 0)};
		for(int i = 0; i < 8; i++)
		{
			for(int iv = 0; iv < vertices.length; iv++)
			{
				vertices[iv] = withCoordinate(vertices[iv], Direction.Axis.Z, (i+1)*.0625f);
				vertices2[iv] = vertices2[iv].add(.0625f, 0, 0);
				vertices3[iv] = vertices3[iv].add(-.0625f, 0, 0);
			}
			double v = 16-i;
			baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), getFacing(), tex_casing0, new double[]{1, v-1, 15, v}, colour, true));
			if(i < 7)
			{
				double u = 8-i;
				baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices2), getFacing(), tex_casing0, new double[]{u-1, 16, u, 8}, colour, true));
				baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices3), getFacing(), tex_casing0, new double[]{u-1, 16, u, 8}, colour, false));
			}
		}
		return baseModel;
	}

	private Direction getOutputFace()
	{
		if(nextOutputLeft)
			return getFacing().rotateYCCW();
		else
			return getFacing().rotateY();
	}
}
