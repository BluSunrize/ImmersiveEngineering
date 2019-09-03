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
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/**
 * @author BluSunrize - 20.08.2016
 */
public class ConveyorVertical extends ConveyorBasic
{
	@Override
	public boolean renderWall(TileEntity tile, EnumFacing facing, int wall)
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
	public String getModelCacheKey(TileEntity tile, EnumFacing facing)
	{
		String key = ConveyorHandler.reverseClassRegistry.get(this.getClass()).toString();
		key += "f"+facing.ordinal();
		key += "a"+(isActive(tile)?1: 0);
		key += "b"+(renderBottomBelt(tile, facing)?("1"+(renderBottomWall(tile, facing, 0)?"1": "0")+(renderBottomWall(tile, facing, 1)?"1": "0")): "000");
		key += "c"+getDyeColour();
		return key;
	}

	boolean renderBottomBelt(TileEntity tile, EnumFacing facing)
	{
		TileEntity te = tile.getWorld().getTileEntity(tile.getPos().add(0, -1, 0));
		if(te instanceof IConveyorTile&&((IConveyorTile)te).getConveyorSubtype()!=null)
			for(EnumFacing f : ((IConveyorTile)te).getConveyorSubtype().sigTransportDirections(te, ((IConveyorTile)te).getFacing()))
				if(f==EnumFacing.UP)
					return false;
		for(EnumFacing f : EnumFacing.HORIZONTALS)
			if(f!=facing&&isInwardConveyor(tile, f))
				return true;
		return false;
	}

	protected boolean isInwardConveyor(TileEntity tile, EnumFacing f)
	{
		TileEntity te = tile.getWorld().getTileEntity(tile.getPos().offset(f));
		if(te instanceof IConveyorTile)
		{
			IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
			if(sub!=null)
				for(EnumFacing f2 : sub.sigTransportDirections(te, ((IConveyorTile)te).getFacing()))
					if(f2==EnumFacing.UP)
						break;
					else if(f==f2.getOpposite())
						return true;
		}
		te = tile.getWorld().getTileEntity(tile.getPos().add(0, -1, 0).offset(f));
		if(te instanceof IConveyorTile)
		{
			IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
			if(sub!=null)
			{
				int b = 0;
				for(EnumFacing f2 : sub.sigTransportDirections(te, ((IConveyorTile)te).getFacing()))
				{
					if(f==f2.getOpposite())
						b++;
					else if(EnumFacing.UP==f2)
						b++;
					if(b==2)
						return true;
				}
			}
		}
		return false;
	}

	protected boolean renderBottomWall(TileEntity tile, EnumFacing facing, int wall)
	{
		return super.renderWall(tile, facing, wall);
	}

	@Override
	public EnumFacing[] sigTransportDirections(TileEntity conveyorTile, EnumFacing facing)
	{
		return new EnumFacing[]{EnumFacing.UP, facing};
	}

	@Override
	public Vec3d getDirection(TileEntity conveyorTile, Entity entity, EnumFacing facing)
	{
		BlockPos posWall = conveyorTile.getPos().offset(facing);
		double d = .625+entity.width;
		double distToWall = Math.abs((facing.getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(facing.getAxis()==Axis.Z?entity.posZ: entity.posX));
		if(distToWall > d)
			return super.getDirection(conveyorTile, entity, facing);

		double vBase = entity instanceof EntityLivingBase?1.5: 1.15;
		double distY = Math.abs(conveyorTile.getPos().add(0, 1, 0).getY()+.5-entity.posY);
		double treshold = .9;
		boolean contact = distY < treshold;

		double vX = entity.motionX;
		double vY = 0.1*vBase;
		double vZ = entity.motionZ;
		if(entity.motionY < 0)
			vY += entity.motionY*.9;

		if(!(entity instanceof EntityPlayer))
		{
			vX = 0.05*facing.getXOffset();
			vZ = 0.05*facing.getZOffset();
			if(facing==EnumFacing.WEST||facing==EnumFacing.EAST)
			{
				if(entity.posZ > conveyorTile.getPos().getZ()+0.65D)
					vZ = -0.1D*vBase;
				else if(entity.posZ < conveyorTile.getPos().getZ()+0.35D)
					vZ = 0.1D*vBase;
			}
			else if(facing==EnumFacing.NORTH||facing==EnumFacing.SOUTH)
			{
				if(entity.posX > conveyorTile.getPos().getX()+0.65D)
					vX = -0.1D*vBase;
				else if(entity.posX < conveyorTile.getPos().getX()+0.35D)
					vX = 0.1D*vBase;
			}
		}
		//Little boost at the top of a conveyor to help players and minecarts to get off
		BlockPos upForward = conveyorTile.getPos().add(0, 1, 0);
		if(contact&&!(Utils.getExistingTileEntity(conveyorTile.getWorld(), upForward) instanceof IConveyorTile))
			vY *= 2.25;
		return new Vec3d(vX, vY, vZ);
	}

	@Override
	public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing)
	{
		if(!isActive(tile))
			return;

		BlockPos posWall = tile.getPos().offset(facing);
		double d = .625+entity.width;
		double distToWall = Math.abs((facing.getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(facing.getAxis()==Axis.Z?entity.posZ: entity.posX));
		if(distToWall > d)
		{
			super.onEntityCollision(tile, entity, facing);
			return;
		}

		if(entity!=null&&!entity.isDead&&!(entity instanceof EntityPlayer&&entity.isSneaking()))
		{
			double distY = Math.abs(tile.getPos().add(0, 1, 0).getY()+.5-entity.posY);
			double treshold = .9;
			boolean contact = distY < treshold;

			entity.onGround = false;
			if(entity.fallDistance < 3)
				entity.fallDistance = 0;
			else
				entity.fallDistance *= .9;
			Vec3d vec = getDirection(tile, entity, facing);
			entity.motionX = vec.x;
			entity.motionY = vec.y;
			entity.motionZ = vec.z;

			if(!contact)
				ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
			else
			{
				BlockPos posTop = tile.getPos().add(0, 1, 0);
				if(!((tile.getWorld().getTileEntity(posTop) instanceof IConveyorTile)||(tile.getWorld().isAirBlock(posTop)&&(tile.getWorld().getTileEntity(posTop.offset(facing)) instanceof IConveyorTile))))
					ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
			}

			if(entity instanceof EntityItem)
			{
				EntityItem item = (EntityItem)entity;
				if(!contact)
					item.setNoDespawn(); //misnamed, actually sets despawn timer to 10 minutes
				else
				{
					TileEntity inventoryTile;
					inventoryTile = tile.getWorld().getTileEntity(tile.getPos().add(0, 1, 0));
					if(!tile.getWorld().isRemote)
					{
						if(inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
						{
							ItemStack stack = item.getItem();
							if(!stack.isEmpty())
							{
								ItemStack ret = Utils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.DOWN);
								if(ret.isEmpty())
									entity.setDead();
								else if(ret.getCount() < stack.getCount())
									item.setItem(ret);
							}
						}
					}
				}
			}
		}
	}

	static final AxisAlignedBB[] verticalBounds = {new AxisAlignedBB(0, 0, 0, 1, 1, .125f), new AxisAlignedBB(0, 0, .875f, 1, 1, 1), new AxisAlignedBB(0, 0, 0, .125f, 1, 1), new AxisAlignedBB(.875f, 0, 0, 1, 1, 1)};

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing)
	{
		ArrayList list = new ArrayList();
		if(facing.ordinal() > 1)
			list.add(verticalBounds[facing.ordinal()-2]);
		if(renderBottomBelt(tile, facing)||list.isEmpty())
			list.add(conveyorBounds);
		return list;
	}

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing)
	{
		ArrayList list = new ArrayList();
		if(facing.ordinal() > 1)
			list.add(verticalBounds[facing.ordinal()-2]);
		if(renderBottomBelt(tile, facing)||list.isEmpty())
			list.add(conveyorBounds);
		return list;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Matrix4f modifyBaseRotationMatrix(Matrix4f matrix, TileEntity tile, EnumFacing facing)
	{
		return new Matrix4(matrix).translate(0, 1, 0).rotate(Math.PI/2, 1, 0, 0).toMatrix4f();
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:blocks/conveyor_vertical");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:blocks/conveyor_vertical_off");

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
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing)
	{
		if(tile!=null&&this.renderBottomBelt(tile, facing))
		{
			TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(tile)?ConveyorBasic.texture_on: ConveyorBasic.texture_off);
			TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
			boolean[] walls = {renderBottomWall(tile, facing, 0), renderBottomWall(tile, facing, 1)};
			baseModel.addAll(ModelConveyor.getBaseConveyor(facing, .875f, new Matrix4(facing), ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
		}
		return baseModel;
	}
}
