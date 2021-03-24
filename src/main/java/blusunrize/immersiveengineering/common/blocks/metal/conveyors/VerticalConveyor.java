/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 20.08.2016
 */
public class VerticalConveyor extends BasicConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "vertical");

	public VerticalConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	public boolean renderWall(Direction facing, int wall)
	{
		return true;
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
	public String getModelCacheKey()
	{
		String key = ConveyorHandler.reverseClassRegistry.get(this.getClass()).toString();
		key += "f"+getFacing().ordinal();
		key += "a"+(isActive()?1: 0);
		if(renderBottomBelt(getTile(), getFacing()))
		{
			key += "b";
			key += isInwardConveyor(getTile(), getFacing().getOpposite())?"1": "0";
			key += renderBottomWall(getTile(), getFacing(), 0)?"1": "0";
			key += renderBottomWall(getTile(), getFacing(), 1)?"1": "0";
		}
		key += "c"+getDyeColour();
		if(isCovered()&&cover!=Blocks.AIR)
			key += "s"+cover.getRegistryName();
		return key;
	}

	static boolean renderBottomBelt(TileEntity tile, Direction facing)
	{
		TileEntity te = tile.getWorld().getTileEntity(tile.getPos().add(0, -1, 0));
		if(te instanceof IConveyorTile&&((IConveyorTile)te).getConveyorSubtype()!=null)
			for(Direction f : ((IConveyorTile)te).getConveyorSubtype().sigTransportDirections())
				if(f==Direction.UP)
					return false;
		for(Direction f : DirectionUtils.BY_HORIZONTAL_INDEX)
			if(f!=facing&&isInwardConveyor(tile, f))
				return true;
		return false;
	}

	protected static boolean isInwardConveyor(TileEntity tile, Direction f)
	{
		TileEntity te = tile.getWorld().getTileEntity(tile.getPos().offset(f));
		if(te instanceof IConveyorTile)
		{
			IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
			if(sub!=null)
				for(Direction f2 : sub.sigTransportDirections())
					if(f==f2.getOpposite())
						return true;
		}
		te = tile.getWorld().getTileEntity(tile.getPos().add(0, -1, 0).offset(f));
		if(te instanceof IConveyorTile)
		{
			IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
			if(sub!=null)
			{
				int b = 0;
				for(Direction f2 : sub.sigTransportDirections())
				{
					if(f==f2.getOpposite())
						b++;
					else if(Direction.UP==f2)
						b++;
					if(b==2)
						return true;
				}
			}
		}
		return false;
	}

	protected boolean renderBottomWall(TileEntity tile, Direction facing, int wall)
	{
		return super.renderWall(facing, wall);
	}

	@Override
	public Direction[] sigTransportDirections()
	{
		return new Direction[]{Direction.UP, getFacing()};
	}

	@Override
	public Vector3d getDirection(Entity entity, boolean outputBlocked)
	{
		BlockPos posWall = getTile().getPos().offset(getFacing());
		double d = .625+entity.getWidth();
		double distToWall = Math.abs((getFacing().getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getPosZ(): entity.getPosX()));
		if(distToWall > d)
			return super.getDirection(entity, outputBlocked);

		double vBase = entity instanceof LivingEntity?1.5: 1.15;
		double distY = Math.abs(getTile().getPos().add(0, 1, 0).getY()+.5-entity.getPosY());
		double treshold = .9;
		boolean contact = distY < treshold;

		double vX = entity.getMotion().x;
		double vY = 0.1*vBase;
		double vZ = entity.getMotion().z;
		if(entity.getMotion().y < 0)
			vY += entity.getMotion().y*.9;

		if(!(entity instanceof PlayerEntity))
		{
			vX = 0.05*getFacing().getXOffset();
			vZ = 0.05*getFacing().getZOffset();
			if(getFacing()==Direction.WEST||getFacing()==Direction.EAST)
			{
				if(entity.getPosZ() > getTile().getPos().getZ()+0.65D)
					vZ = -0.1D*vBase;
				else if(entity.getPosZ() < getTile().getPos().getZ()+0.35D)
					vZ = 0.1D*vBase;
			}
			else if(getFacing()==Direction.NORTH||getFacing()==Direction.SOUTH)
			{
				if(entity.getPosX() > getTile().getPos().getX()+0.65D)
					vX = -0.1D*vBase;
				else if(entity.getPosX() < getTile().getPos().getX()+0.35D)
					vX = 0.1D*vBase;
			}
		}
		//Little boost at the top of a conveyor to help players and minecarts to get off
		BlockPos upForward = getTile().getPos().add(0, 1, 0);
		if(contact&&!(Utils.getExistingTileEntity(getTile().getWorld(), upForward) instanceof IConveyorTile))
			vY *= 2.25;
		return new Vector3d(vX, vY, vZ);
	}

	private CapabilityReference<IItemHandler> inserter;

	@Override
	public void onEntityCollision(@Nonnull Entity entity)
	{
		collisionTracker.onEntityCollided(entity);
		if(!isActive()||!entity.isAlive())
			return;
		if(entity instanceof PlayerEntity&&entity.isSneaking())
			return;

		BlockPos posWall = getTile().getPos().offset(getFacing());
		double d = .625+entity.getWidth();
		double distToWall = Math.abs((getFacing().getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getPosZ(): entity.getPosX()));
		if(distToWall > d)
		{
			super.onEntityCollision(entity);
			return;
		}

		boolean outputBlocked = isOutputBlocked();
		double distY = Math.abs(getTile().getPos().add(0, 1, 0).getY()+.5-entity.getPosY());
		double treshold = .9;
		boolean contact = distY < treshold;

		entity.setOnGround(false);
		if(entity.fallDistance < 3)
			entity.fallDistance = 0;
		else
			entity.fallDistance *= .9;
		Vector3d vec = getDirection(entity, outputBlocked);
		boolean hasBeenHandled = !ConveyorHandler.markEntityAsHandled(entity);
		if(outputBlocked&&entity.getPosY() >= getTile().getPos().getY()+0.25)
		{
			double my;
			if(entity.getPosY() < entity.lastTickPosY)
				my = entity.lastTickPosY-entity.getPosY();
			else
				my = entity.getMotion().y;
			if(hasBeenHandled)
				vec = new Vector3d(vec.x, my, vec.z);
			else
				vec = new Vector3d(0, my, 0);
		}
		entity.setMotion(vec);

		if(!contact)
			ConveyorHandler.applyMagnetSuppression(entity, (IConveyorTile)getTile());
		else
		{
			BlockPos posTop = getTile().getPos().add(0, 1, 0);
			if(!((getTile().getWorld().getTileEntity(posTop) instanceof IConveyorTile)||(getTile().getWorld().isAirBlock(posTop)&&(getTile().getWorld().getTileEntity(posTop.offset(getFacing())) instanceof IConveyorTile))))
				ConveyorHandler.revertMagnetSuppression(entity, (IConveyorTile)getTile());
		}

		if(entity instanceof ItemEntity)
		{
			ItemEntity item = (ItemEntity)entity;
			if(!contact)
			{
				ItemEntityAccess access = (ItemEntityAccess)item;
				if(access.getAgeNonsided() > item.lifespan-60*20)
					access.setAge(item.lifespan-60*20);
			}
			else
			{
				TileEntity inventoryTile;
				inventoryTile = getTile().getWorld().getTileEntity(getTile().getPos().add(0, 1, 0));
				if(!getTile().getWorld().isRemote)
				{
					if(inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
					{
						ItemStack stack = item.getItem();
						if(!stack.isEmpty())
						{
							if(inserter==null)
								inserter = CapabilityReference.forNeighbor(getTile(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
							ItemStack ret = Utils.insertStackIntoInventory(inserter, stack, false);
							if(ret.isEmpty())
								entity.remove();
							else if(ret.getCount() < stack.getCount())
								item.setItem(ret);
						}
					}
				}
			}
		}

		if(isCovered()&&entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	@Override
	public BlockPos getOutputInventory()
	{
		return getTile().getPos().up();
	}

	@Override
	public List<BlockPos> getNextConveyorCandidates()
	{
		BlockPos pos = getTile().getPos();
		return ImmutableList.of(
				pos.up(),
				pos.up().offset(getFacing())
		);
	}

	private static final CachedShapesWithTransform<Boolean, Direction> SHAPES =
			CachedShapesWithTransform.createDirectional(VerticalConveyor::getShapes);

	@Override
	public VoxelShape getSelectionShape()
	{
		return getCollisionShape();
	}

	private static List<AxisAlignedBB> getShapes(Boolean bottomBelt)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, 1, .125f));
		if(bottomBelt)
			list.add(conveyorBounds.getBoundingBox());
		return list;
	}

	@Override
	public VoxelShape getCollisionShape()
	{
		return SHAPES.get(Pair.of(renderBottomBelt(getTile(), getFacing()), getFacing()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TransformationMatrix modifyBaseRotationMatrix(TransformationMatrix matrix)
	{
		return matrix
				.compose(new TransformationMatrix(
						new Vector3f(0, 1, 0),
						new Quaternion((float)Math.PI/2, 0, 0, false),
						null,
						null
				));
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/vertical");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/vertical_off");

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
		if(getTile()!=null&&renderBottomBelt(getTile(), getFacing()))
		{
			TextureAtlasSprite sprite = ClientUtils.getSprite(isActive()?BasicConveyor.texture_on: BasicConveyor.texture_off);
			TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
			boolean[] walls = {renderBottomWall(getTile(), getFacing(), 0), renderBottomWall(getTile(), getFacing(), 1)};
			baseModel.addAll(ModelConveyor.getBaseConveyor(
					getFacing(),
					.875f,
					ClientUtils.rotateTo(getFacing()),
					ConveyorDirection.HORIZONTAL,
					sprite,
					walls,
					new boolean[]{true, false},
					spriteColour,
					getDyeColour()
			));
		}
		return baseModel;
	}
}
