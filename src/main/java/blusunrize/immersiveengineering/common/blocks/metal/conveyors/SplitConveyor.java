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
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
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

	public SplitConveyor(BlockEntity tile)
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
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getBlockPos().hashCode());
		if(entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
		{
			Direction redirect = Direction.values()[entity.getPersistentData().getInt(nbtKey)];
			BlockPos nextPos = getTile().getBlockPos().relative(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getZ(): entity.getX()));
			BlockEntity inventoryTile = getTile().getLevel().getBlockEntity(nextPos);
			if(distNext < .7&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
			{
				ItemStack stack = entity.getItem();
				if(!stack.isEmpty())
				{
					CapabilityReference<IItemHandler> insert = CapabilityReference.forNeighbor(getTile(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, redirect);
					ItemStack ret = Utils.insertStackIntoInventory(insert, stack, false);
					if(ret.isEmpty())
						entity.discard();
					else if(ret.getCount() < stack.getCount())
						entity.setItem(ret);
				}
			}
		}
	}

	@Override
	public void onEntityCollision(@Nonnull Entity entity)
	{
		if(!isActive())
			return;
		Direction redirect = null;
		if(entity.isAlive())
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getBlockPos().hashCode());
			if(entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
				redirect = Direction.values()[entity.getPersistentData().getInt(nbtKey)];
			else
			{
				redirect = getOutputFace();
				entity.getPersistentData().putInt(nbtKey, redirect.ordinal());
				BlockPos nextPos = getTile().getBlockPos().relative(this.getOutputFace().getOpposite());
				if(getTile().getLevel().hasChunkAt(nextPos))
				{
					BlockEntity nextTile = getTile().getLevel().getBlockEntity(nextPos);
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
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getBlockPos().hashCode());
			BlockPos nextPos = getTile().getBlockPos().relative(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getZ(): entity.getX()));
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
		return new Direction[]{getFacing().getClockWise(), getFacing().getCounterClockWise()};
	}

	@Override
	public Vec3 getDirection(Entity entity, boolean outputBlocked)
	{
		Vec3 vec = super.getDirection(entity, outputBlocked);
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getTile().getBlockPos().hashCode());
		if(!entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
			return vec;
		Direction redirect = Direction.from3DDataValue(entity.getPersistentData().getInt(nbtKey));
		BlockPos wallPos = getTile().getBlockPos().relative(getFacing());
		double distNext = Math.abs((getFacing().getAxis()==Axis.Z?wallPos.getZ(): wallPos.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getZ(): entity.getX()));
		if(distNext < 1.33)
		{
			double sideMove = Math.pow(1+distNext, 0.1)*.2;
			if(distNext < .8)
				vec = new Vec3(getFacing().getAxis()==Axis.X?0: vec.x, vec.y, getFacing().getAxis()==Axis.Z?0: vec.z);
			vec = vec.add(redirect.getStepX()*sideMove, 0, redirect.getStepZ()*sideMove);
		}
		return vec;
	}

	@Override
	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = super.writeConveyorNBT();
		nbt.putBoolean("nextLeft", nextOutputLeft);
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundTag nbt)
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
		Transformation tMatrix = matrix.toTransformationMatrix();
		float[] colour = {1, 1, 1, 1};

		Vec3[] vertices = {new Vec3(.0625f, 0, 0), new Vec3(.0625f, 0, 1), new Vec3(.9375f, 0, 1), new Vec3(.9375f, 0, 0)};

		// replace bottom with casing
		baseModel.set(0, ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, getFacing()), ClientUtils.getSprite(ModelConveyor.rl_casing[3]), new double[]{1, 0, 15, 16}, colour, true));

		vertices = new Vec3[]{new Vec3(.0625f, .1875f, 0), new Vec3(.0625f, .1875f, 1), new Vec3(.9375f, .1875f, 1), new Vec3(.9375f, .1875f, 0)};
		baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Direction.UP, tex_casing0, new double[]{1, 16, 15, 0}, colour, false));

		vertices = new Vec3[]{new Vec3(.0625f, 0, 0), new Vec3(.0625f, .1875f, 0), new Vec3(.9375f, .1875f, 0), new Vec3(.9375f, 0, 0)};
		baseModel.set(15, ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), getFacing(), ClientUtils.getSprite(ModelConveyor.rl_casing[1]), new double[]{1, 16, 15, 13}, colour, false));

		vertices = new Vec3[]{new Vec3(.0625f, .125f, 0), new Vec3(.0625f, .1875f, 0), new Vec3(.9375f, .1875f, 0), new Vec3(.9375f, .125f, 0)};
		Vec3[] vertices2 = new Vec3[]{new Vec3(.5f, .125f, 0), new Vec3(.5f, .125f, .5f), new Vec3(.5f, .1875f, .5f), new Vec3(.5f, .1875f, 0)};
		Vec3[] vertices3 = new Vec3[]{new Vec3(.5f, .125f, 0), new Vec3(.5f, .125f, .5f), new Vec3(.5f, .1875f, .5f), new Vec3(.5f, .1875f, 0)};
		for(int i = 0; i < 8; i++)
		{
			for(int iv = 0; iv < vertices.length; iv++)
			{
				vertices[iv] = withCoordinate(vertices[iv], Direction.Axis.Z, (i+1)*.0625f);
				vertices2[iv] = vertices2[iv].add(.0625f, 0, 0);
				vertices3[iv] = vertices3[iv].add(-.0625f, 0, 0);
			}
			double v = 16-i;
			baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), getFacing(), tex_casing0, new double[]{1, v-1, 15, v}, colour, true));
			if(i < 7)
			{
				double u = 8-i;
				baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices2), getFacing(), tex_casing0, new double[]{u-1, 16, u, 8}, colour, true));
				baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices3), getFacing(), tex_casing0, new double[]{u-1, 16, u, 8}, colour, false));
			}
		}
		super.modifyQuads(baseModel);
		return baseModel;
	}

	@Override
	public List<BlockPos> getNextConveyorCandidates()
	{
		BlockPos baseOutput = getTile().getBlockPos().relative(getOutputFace());
		return ImmutableList.of(
				baseOutput,
				baseOutput.below()
		);
	}

	@Override
	public boolean isOutputBlocked()
	{
		// Consider the belt blocked if at least one of the possible outputs is blocked
		Direction outputFace = getOutputFace();
		BlockPos here = getTile().getBlockPos();
		for(BlockPos outputPos : new BlockPos[]{
				here.relative(outputFace, 1),
				here.relative(outputFace, -1),
		})
		{
			BlockEntity tile = SafeChunkUtils.getSafeTE(getTile().getLevel(), outputPos);
			if(tile instanceof IConveyorTile&&((IConveyorTile)tile).getConveyorSubtype().isBlocked())
				return true;
		}
		return false;
	}

	private Direction getOutputFace()
	{
		if(nextOutputLeft)
			return getFacing().getCounterClockWise();
		else
			return getFacing().getClockWise();
	}
}
