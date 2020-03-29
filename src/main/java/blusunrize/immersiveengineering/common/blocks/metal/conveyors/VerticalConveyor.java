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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
		key += "b"+(renderBottomBelt(getTile(), getFacing())?("1"+(renderBottomWall(getTile(), getFacing(), 0)?"1": "0")+(renderBottomWall(getTile(), getFacing(), 1)?"1": "0")): "000");
		key += "c"+getDyeColour();
		if(allowCovers()&&cover!=Blocks.AIR)
			key += "s"+cover.getRegistryName();
		return key;
	}

	boolean renderBottomBelt(TileEntity tile, Direction facing)
	{
		TileEntity te = tile.getWorld().getTileEntity(tile.getPos().add(0, -1, 0));
		if(te instanceof IConveyorTile&&((IConveyorTile)te).getConveyorSubtype()!=null)
			for(Direction f : ((IConveyorTile)te).getConveyorSubtype().sigTransportDirections())
				if(f==Direction.UP)
					return false;
		for(Direction f : Direction.BY_HORIZONTAL_INDEX)
			if(f!=facing&&isInwardConveyor(tile, f))
				return true;
		return false;
	}

	protected boolean isInwardConveyor(TileEntity tile, Direction f)
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
	public Vec3d getDirection(Entity entity)
	{
		BlockPos posWall = getTile().getPos().offset(getFacing());
		double d = .625+entity.getWidth();
		double distToWall = Math.abs((getFacing().getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.posZ: entity.posX));
		if(distToWall > d)
			return super.getDirection(entity);

		double vBase = entity instanceof LivingEntity?1.5: 1.15;
		double distY = Math.abs(getTile().getPos().add(0, 1, 0).getY()+.5-entity.posY);
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
				if(entity.posZ > getTile().getPos().getZ()+0.65D)
					vZ = -0.1D*vBase;
				else if(entity.posZ < getTile().getPos().getZ()+0.35D)
					vZ = 0.1D*vBase;
			}
			else if(getFacing()==Direction.NORTH||getFacing()==Direction.SOUTH)
			{
				if(entity.posX > getTile().getPos().getX()+0.65D)
					vX = -0.1D*vBase;
				else if(entity.posX < getTile().getPos().getX()+0.35D)
					vX = 0.1D*vBase;
			}
		}
		//Little boost at the top of a conveyor to help players and minecarts to get off
		BlockPos upForward = getTile().getPos().add(0, 1, 0);
		if(contact&&!(Utils.getExistingTileEntity(getTile().getWorld(), upForward) instanceof IConveyorTile))
			vY *= 2.25;
		return new Vec3d(vX, vY, vZ);
	}

	//TODO memory leaks?
	private Map<TileEntity, CapabilityReference<IItemHandler>> inserters = new WeakHashMap<>();

	@Override
	public void onEntityCollision(Entity entity)
	{
		if(!isActive())
			return;

		BlockPos posWall = getTile().getPos().offset(getFacing());
		double d = .625+entity.getWidth();
		double distToWall = Math.abs((getFacing().getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.posZ: entity.posX));
		if(distToWall > d)
		{
			super.onEntityCollision(entity);
			return;
		}

		if(entity!=null&&entity.isAlive()&&!(entity instanceof PlayerEntity&&entity.isSneaking()))
		{
			double distY = Math.abs(getTile().getPos().add(0, 1, 0).getY()+.5-entity.posY);
			double treshold = .9;
			boolean contact = distY < treshold;

			entity.onGround = false;
			if(entity.fallDistance < 3)
				entity.fallDistance = 0;
			else
				entity.fallDistance *= .9;
			Vec3d vec = getDirection(entity);
			entity.setMotion(vec);

			if(!contact)
				ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)getTile());
			else
			{
				BlockPos posTop = getTile().getPos().add(0, 1, 0);
				if(!((getTile().getWorld().getTileEntity(posTop) instanceof IConveyorTile)||(getTile().getWorld().isAirBlock(posTop)&&(getTile().getWorld().getTileEntity(posTop.offset(getFacing())) instanceof IConveyorTile))))
					ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)getTile());
			}

			if(entity instanceof ItemEntity)
			{
				ItemEntity item = (ItemEntity)entity;
				if(!contact)
				{
					if(item.getAge() > item.lifespan-60*20)
						item.setAgeToCreativeDespawnTime();
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
								CapabilityReference<IItemHandler> insert = inserters.computeIfAbsent(getTile(),
										te -> CapabilityReference.forNeighbor(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN));
								ItemStack ret = Utils.insertStackIntoInventory(insert, stack, false);
								if(ret.isEmpty())
									entity.remove();
								else if(ret.getCount() < stack.getCount())
									item.setItem(ret);
							}
						}
					}
				}
			}
		}

		if(allowCovers()&&entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	static final AxisAlignedBB[] verticalBounds = {new AxisAlignedBB(0, 0, 0, 1, 1, .125f), new AxisAlignedBB(0, 0, .875f, 1, 1, 1), new AxisAlignedBB(0, 0, 0, .125f, 1, 1), new AxisAlignedBB(.875f, 0, 0, 1, 1, 1)};

	@Override
	public List<AxisAlignedBB> getSelectionBoxes()
	{
		ArrayList list = new ArrayList();
		if(getFacing().ordinal() > 1)
			list.add(verticalBounds[getFacing().ordinal()-2]);
		if(renderBottomBelt(getTile(), getFacing())||list.isEmpty())
			list.add(conveyorBounds);
		return list;
	}

	@Override
	public List<AxisAlignedBB> getColisionBoxes()
	{
		ArrayList list = new ArrayList();
		if(getFacing().ordinal() > 1)
			list.add(verticalBounds[getFacing().ordinal()-2]);
		if(renderBottomBelt(getTile(), getFacing())||list.isEmpty())
			list.add(conveyorBounds);
		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Matrix4f modifyBaseRotationMatrix(Matrix4f matrix)
	{
		return new Matrix4(matrix).translate(0, 1, 0).rotate(Math.PI/2, 1, 0, 0).toMatrix4f();
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
		if(getTile()!=null&&this.renderBottomBelt(getTile(), getFacing()))
		{
			TextureAtlasSprite sprite = ClientUtils.getSprite(isActive()?BasicConveyor.texture_on: BasicConveyor.texture_off);
			TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
			boolean[] walls = {renderBottomWall(getTile(), getFacing(), 0), renderBottomWall(getTile(), getFacing(), 1)};
			baseModel.addAll(ModelConveyor.getBaseConveyor(getFacing(), .875f, new Matrix4(getFacing()), ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
		}
		return baseModel;
	}
}
