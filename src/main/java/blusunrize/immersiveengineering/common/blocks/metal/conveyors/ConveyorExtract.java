/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * @author BluSunrize - 19.05.2017
 */
public class ConveyorExtract extends ConveyorBasic
{
	private EnumFacing extractDirection;
	private int transferCooldown = -1;
	private int transferTickrate = 8;
	private float extension = -1;

	public ConveyorExtract(EnumFacing conveyorDir)
	{
		this.extractDirection = conveyorDir.getOpposite();
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

//	private static final TextureAtlasSprite texture_steel = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/storage_steel"));
//	private static final TextureAtlasSprite texture_casing = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/wooden_device_turntable_bottom"));
//	private static final TextureAtlasSprite texture_curtain = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/cloth_device_stripcurtain"));
//	private static final TextureAtlasSprite texture_assembler = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/metal_multiblock_assembler"));

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing)
	{
		final TextureAtlasSprite texture_steel = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/storage_steel"));
		final TextureAtlasSprite texture_casing = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/wooden_device_turntable_bottom"));
		final TextureAtlasSprite texture_curtain = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/cloth_device_stripcurtain"));
		final TextureAtlasSprite texture_assembler = ClientUtils.getSprite(new ResourceLocation("immersiveengineering:blocks/metal_multiblock_assembler"));

		float[] colour = {1, 1, 1, 1};
		Matrix4 matrix = new Matrix4(this.extractDirection);
		final float extend = getExtensionIntoBlock(tile);
		this.extension = extend;

		Function<EnumFacing, TextureAtlasSprite> getCasingSprite = enumFacing -> enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

		Function<Vector3f[], Vector3f[]> vertexTransformer = vertices -> {
			if(extend==0)
				return vertices;
			Vector3f[] ret = new Vector3f[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vector3f(vertices[i].x, vertices[i].y, vertices[i].z-extend);
			return ret;
		};
		Function<Vector3f[], Vector3f[]> casingTransformer = vertices -> {
			Vector3f[] ret = new Vector3f[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vector3f(vertices[i].x, vertices[i].y-.25f, vertices[i].z-.625f-extend);
			return ret;
		};

		baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.0625f, .375f, .625f), new Vector3f(.1875f, 1f, 1f), matrix, facing, casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.8125f, .375f, .625f), new Vector3f(.9375f, 1f, 1f), matrix, facing, casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.1875f, .875f, .625f), new Vector3f(.8125f, 1f, 1f), matrix, facing, casingTransformer, getCasingSprite, colour));

		if(tile!=null&&extend > 0)
		{
			TextureAtlasSprite tex_conveyor = ClientUtils.getSprite(isActive(tile)?ConveyorBasic.texture_on: ConveyorBasic.texture_off);
			Function<EnumFacing, TextureAtlasSprite> getExtensionSprite = enumFacing -> enumFacing.getAxis()==Axis.Y?null: enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

			Vector3f[] vertices = {new Vector3f(.0625f, 0, -extend), new Vector3f(.0625f, 0, 0), new Vector3f(.9375f, 0, 0), new Vector3f(.9375f, 0, -extend)};
			baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, facing), tex_conveyor, new double[]{15, extend*16, 1, 0}, colour, true));
			for(Vector3f vec : vertices)
				vec.setY(.125f);
			baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_conveyor, new double[]{15, (1-extend)*16, 1, 16}, colour, false));
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.0625f, .25f, .625f), new Vector3f(.9375f, .375f, .625f+extend), matrix, facing, casingTransformer, getExtensionSprite, colour));
		}


		Vector3f[] vertices = new Vector3f[]{new Vector3f(.8125f, .625f, .03125f), new Vector3f(.8125f, .125f, .03125f), new Vector3f(.1875f, .125f, .03125f), new Vector3f(.1875f, .625f, .03125f)};
		baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.NORTH, facing), texture_assembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));
		for(Vector3f vec : vertices)
			vec.setZ(.0625f);
		baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.SOUTH, facing), texture_assembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));

		for(int i = 0; i < 5; i++)
		{
			float off = i*.125f;
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.203125f+off, .1875f, .09375f), new Vector3f(.296875f+off, .625f, .125f), matrix, facing, vertexTransformer, (facing1) -> texture_curtain, colour));
		}
		return baseModel;
	}

	@Override
	public String getModelCacheKey(TileEntity tile, EnumFacing facing)
	{
		String key = super.getModelCacheKey(tile, facing);
		key += "e"+this.extractDirection.ordinal();
		key += "ex"+getExtensionIntoBlock(tile);
		return key;
	}

	@Override
	public boolean renderWall(TileEntity tile, EnumFacing facing, int wall)
	{
		EnumFacing side = wall==0?facing.rotateYCCW(): facing.rotateY();
		return side!=this.extractDirection&&super.renderWall(tile, facing, wall);
	}

	private float getExtensionIntoBlock(TileEntity tile)
	{
		float extend = 0;
		if(tile==null||!tile.hasWorld())
			return extend;

		World world = tile.getWorld();
		BlockPos neighbour = tile.getPos().offset(this.extractDirection);
		if(!world.isAirBlock(neighbour))
		{
			IBlockState connected = world.getBlockState(neighbour);
			TileEntity connectedTile = world.getTileEntity(neighbour);
			if(connectedTile!=null&&connectedTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.extractDirection.getOpposite()))
				if(connected.getBlockFaceShape(world, neighbour, this.extractDirection.getOpposite())!=BlockFaceShape.SOLID)
				{
					AxisAlignedBB aabb = connected.getBoundingBox(world, neighbour);
					switch(extractDirection)
					{
						case NORTH:
							extend = (float)(1-aabb.maxZ);
							break;
						case SOUTH:
							extend = (float)aabb.minZ;
							break;
						case WEST:
							extend = (float)(1-aabb.maxX);
							break;
						case EAST:
							extend = (float)aabb.minX;
							break;
					}
					if(extend > .25f)
						return .25f;
					float round = extend%.0625f;
					if(round < extend)
						extend = round+.0625f;
				}
		}
		return extend;
	}

	@Override
	public boolean isActive(TileEntity tile)
	{
		return true;
	}

	private boolean isPowered(TileEntity tile)
	{
		return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) > 0;
	}

	@Override
	public boolean isTicking(TileEntity tile)
	{
		return true;
	}

	@Override
	public void onUpdate(TileEntity tile, EnumFacing facing)
	{
		if(!tile.getWorld().isRemote)
		{
			if(this.transferCooldown > 0)
			{
				this.transferCooldown--;
			}
			if(!isPowered(tile) && this.transferCooldown <= 0)
			{
				World world = tile.getWorld();
				BlockPos neighbour = tile.getPos().offset(this.extractDirection);
				if(!world.isAirBlock(neighbour))
				{
					TileEntity neighbourTile = world.getTileEntity(neighbour);
					if(neighbourTile!=null&&neighbourTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.extractDirection.getOpposite()))
					{
						IItemHandler itemHandler = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.extractDirection.getOpposite());
						for(int i = 0; i < itemHandler.getSlots(); i++)
						{
							ItemStack extractItem = itemHandler.extractItem(i, 1, true);
							if(!extractItem.isEmpty())
							{
								extractItem = itemHandler.extractItem(i, 1, false);
								EntityItem entity = new EntityItem(world, tile.getPos().getX()+.5, tile.getPos().getY()+.1875, tile.getPos().getZ()+.5, extractItem);
								entity.motionX = 0;
								entity.motionY = 0;
								entity.motionZ = 0;
								world.spawnEntity(entity);
								this.onItemDeployed(tile, entity, facing);
								this.transferCooldown = this.transferTickrate;
								return;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side)
	{
		if(Utils.isHammer(heldItem)&&player.isSneaking())
		{
			EnumFacing dir = this.extractDirection.rotateY();
			if(dir==((IConveyorTile)tile).getFacing())
				dir = dir.rotateY();
			this.extractDirection = dir;
			return true;
		}
		if(Utils.isWirecutter(heldItem))
		{
			if(this.transferTickrate==4)
				this.transferTickrate = 8;
			else if(this.transferTickrate==8)
				this.transferTickrate = 16;
			else if(this.transferTickrate==16)
				this.transferTickrate = 20;
			else if(this.transferTickrate==20)
				this.transferTickrate = 4;
			player.sendStatusMessage(new TextComponentTranslation(Lib.CHAT_INFO+"tickrate", this.transferTickrate), true);
			return true;
		}
		return false;
	}

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		if(this.extension < 0)
			this.extension = getExtensionIntoBlock(tile);
		switch(extractDirection)
		{
			case NORTH:
				list.add(new AxisAlignedBB(.0625, .125, -extension, .9375, .75, .375-extension));
				break;
			case SOUTH:
				list.add(new AxisAlignedBB(.0625, .125, .625+extension, .9375, .75, 1+extension));
				break;
			case WEST:
				list.add(new AxisAlignedBB(-extension, .125, .0625, .375-extension, .75, .9375));
				break;
			case EAST:
				list.add(new AxisAlignedBB(.625+extension, .125, .0625, 1+extension, .75, .9375));
				break;
		}
		return list;
	}

	@Override
	public NBTTagCompound writeConveyorNBT()
	{
		NBTTagCompound nbt = super.writeConveyorNBT();
		nbt.setInteger("extractDirection", extractDirection.ordinal());
		nbt.setInteger("transferCooldown", transferCooldown);
		nbt.setInteger("transferTickrate", transferTickrate);
		return nbt;
	}

	@Override
	public void readConveyorNBT(NBTTagCompound nbt)
	{
		super.readConveyorNBT(nbt);
		extractDirection = EnumFacing.values()[nbt.getInteger("extractDirection")];
		transferCooldown = nbt.getInteger("transferCooldown");
		transferTickrate = nbt.getInteger("transferTickrate");
	}
}
