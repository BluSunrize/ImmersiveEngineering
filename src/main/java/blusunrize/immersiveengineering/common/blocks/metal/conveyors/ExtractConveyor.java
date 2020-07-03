/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.function.Function;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 19.05.2017
 */
public class ExtractConveyor extends BasicConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "extract");
	private int transferCooldown = -1;
	private int transferTickrate = 8;
	private float extension = -1;
	private Rotation relativeExtractDir = Rotation.CLOCKWISE_180;

	public ExtractConveyor(TileEntity tile)
	{
		super(tile);
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
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		final TextureAtlasSprite texture_steel = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/metal/storage_steel"));
		final TextureAtlasSprite texture_casing = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/turntable_bottom"));
		final TextureAtlasSprite texture_curtain = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/stripcurtain"));
		final TextureAtlasSprite texture_assembler = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/multiblocks/assembler"));

		float[] colour = {1, 1, 1, 1};
		Matrix4 matrix = new Matrix4(this.getExtractDirection());
		TransformationMatrix tMatrix = matrix.toTransformationMatrix();
		final float extend = getExtensionIntoBlock(getTile());
		this.extension = extend;

		Function<Direction, TextureAtlasSprite> getCasingSprite = enumFacing -> enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

		Function<Vector3d[], Vector3d[]> vertexTransformer = vertices -> {
			if(extend==0)
				return vertices;
			Vector3d[] ret = new Vector3d[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vector3d(vertices[i].x, vertices[i].y, vertices[i].z-extend);
			return ret;
		};
		Function<Vector3d[], Vector3d[]> casingTransformer = vertices -> {
			Vector3d[] ret = new Vector3d[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vector3d(vertices[i].x, vertices[i].y-.25f, vertices[i].z-.625f-extend);
			return ret;
		};

		baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.0625f, .375f, .625f), new Vector3d(.1875f, 1f, 1f), matrix, getFacing(), casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.8125f, .375f, .625f), new Vector3d(.9375f, 1f, 1f), matrix, getFacing(), casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.1875f, .875f, .625f), new Vector3d(.8125f, 1f, 1f), matrix, getFacing(), casingTransformer, getCasingSprite, colour));

		if(getTile()!=null&&extend > 0)
		{
			TextureAtlasSprite tex_conveyor = ClientUtils.getSprite(isActive()?BasicConveyor.texture_on: BasicConveyor.texture_off);
			Function<Direction, TextureAtlasSprite> getExtensionSprite = enumFacing -> enumFacing.getAxis()==Axis.Y?null: enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

			Vector3d[] vertices = {new Vector3d(.0625f, 0, -extend), new Vector3d(.0625f, 0, 0), new Vector3d(.9375f, 0, 0), new Vector3d(.9375f, 0, -extend)};
			baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, getFacing()), tex_conveyor, new double[]{15, extend*16, 1, 0}, colour, true));
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = Utils.withCoordinate(vertices[i], Axis.Y, .125);
			baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, getFacing()), tex_conveyor, new double[]{15, (1-extend)*16, 1, 16}, colour, false));
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.0625f, .25f, .625f), new Vector3d(.9375f, .375f, .625f+extend), matrix, getFacing(), casingTransformer, getExtensionSprite, colour));
		}


		Vector3d[] vertices = new Vector3d[]{new Vector3d(.8125f, .625f, .03125f), new Vector3d(.8125f, .125f, .03125f), new Vector3d(.1875f, .125f, .03125f), new Vector3d(.1875f, .625f, .03125f)};
		baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, getFacing()), texture_assembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = Utils.withCoordinate(vertices[i], Axis.Z, .0625);
		baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, getFacing()), texture_assembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));

		for(int i = 0; i < 5; i++)
		{
			float off = i*.125f;
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.203125f+off, .1875f, .09375f), new Vector3d(.296875f+off, .625f, .125f), matrix, getFacing(), vertexTransformer, (facing1) -> texture_curtain, colour));
		}

		super.modifyQuads(baseModel);

		return baseModel;
	}

	@Override
	public String getModelCacheKey()
	{
		String key = super.getModelCacheKey();
		key += "e"+this.getExtractDirection().ordinal();
		key += "ex"+getExtensionIntoBlock(getTile());
		return key;
	}

	@Override
	public boolean renderWall(Direction facing, int wall)
	{
		Direction side = wall==0?facing.rotateYCCW(): facing.rotateY();
		return side!=this.getExtractDirection()&&super.renderWall(facing, wall);
	}

	private float getExtensionIntoBlock(TileEntity tile)
	{
		float extend = 0;
		if(tile==null||!tile.hasWorld())
			return extend;

		World world = tile.getWorld();
		BlockPos neighbour = tile.getPos().offset(this.getExtractDirection());
		if(!world.isAirBlock(neighbour))
		{
			BlockState connected = world.getBlockState(neighbour);
			TileEntity connectedTile = world.getTileEntity(neighbour);
			if(connectedTile!=null&&connectedTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getExtractDirection().getOpposite()).isPresent())
				if(Block.doesSideFillSquare(connected.getShape(world, neighbour), this.getExtractDirection().getOpposite()))
				{
					AxisAlignedBB aabb = connected.getShape(world, neighbour).getBoundingBox();
					switch(getExtractDirection())
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
	public boolean isActive()
	{
		return true;
	}

	private boolean isPowered(TileEntity tile)
	{
		return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) > 0;
	}

	@Override
	public boolean isTicking()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		if(!getTile().getWorld().isRemote)
		{
			if(this.transferCooldown > 0)
			{
				this.transferCooldown--;
			}
			if(!isPowered(getTile())&&this.transferCooldown <= 0)
			{
				World world = getTile().getWorld();
				BlockPos neighbour = getTile().getPos().offset(this.getExtractDirection());
				if(!world.isAirBlock(neighbour))
				{
					LazyOptional<IItemHandler> cap = CapabilityUtils.findItemHandlerAtPos(world, neighbour, this.getExtractDirection().getOpposite(), true);
					cap.ifPresent(itemHandler ->
					{
						for(int i = 0; i < itemHandler.getSlots(); i++)
						{
							ItemStack extractItem = itemHandler.extractItem(i, 1, true);
							if(!extractItem.isEmpty())
							{
								extractItem = itemHandler.extractItem(i, 1, false);
								ItemEntity entity = new ItemEntity(world,
										getTile().getPos().getX()+.5,
										getTile().getPos().getY()+.1875,
										getTile().getPos().getZ()+.5, extractItem);
								entity.setMotion(Vector3d.ZERO);
								world.addEntity(entity);
								this.onItemDeployed(entity);
								this.transferCooldown = this.transferTickrate;
								return;
							}
						}
					});
				}
			}
		}
	}

	@Override
	public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		if(Utils.isHammer(heldItem)&&player.isSneaking())
		{
			do
			{
				relativeExtractDir = Rotation.values()[(relativeExtractDir.ordinal()+1)%Rotation.values().length];
			} while(relativeExtractDir==Rotation.NONE);
			return true;
		}
		if(Utils.isScrewdriver(heldItem))
		{
			if(this.transferTickrate==4)
				this.transferTickrate = 8;
			else if(this.transferTickrate==8)
				this.transferTickrate = 16;
			else if(this.transferTickrate==16)
				this.transferTickrate = 20;
			else if(this.transferTickrate==20)
				this.transferTickrate = 4;
			player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_INFO+"tickrate", this.transferTickrate), true);
			return true;
		}
		return false;
	}

	@Override
	public VoxelShape getSelectionShape()
	{
		VoxelShape ret = conveyorBounds;
		if(this.extension < 0)
			this.extension = getExtensionIntoBlock(getTile());
		VoxelShape extensionShape = null;
		switch(getExtractDirection())
		{
			case NORTH:
				extensionShape = VoxelShapes.create(.0625, .125, -extension, .9375, .75, .375-extension);
				break;
			case SOUTH:
				extensionShape = VoxelShapes.create(.0625, .125, .625+extension, .9375, .75, 1+extension);
				break;
			case WEST:
				extensionShape = VoxelShapes.create(-extension, .125, .0625, .375-extension, .75, .9375);
				break;
			case EAST:
				extensionShape = VoxelShapes.create(.625+extension, .125, .0625, 1+extension, .75, .9375);
				break;
		}
		if(extensionShape!=null)
			ret = VoxelShapes.combineAndSimplify(ret, extensionShape, IBooleanFunction.OR);
		return ret;
	}

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		nbt.putInt("transferCooldown", transferCooldown);
		nbt.putInt("transferTickrate", transferTickrate);
		nbt.putInt("relativeExtractDir", relativeExtractDir.ordinal());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		transferCooldown = nbt.getInt("transferCooldown");
		transferTickrate = nbt.getInt("transferTickrate");
		relativeExtractDir = Rotation.values()[nbt.getInt("relativeExtractDir")];
		if(relativeExtractDir==Rotation.NONE)
			relativeExtractDir = Rotation.CLOCKWISE_180;
	}

	private Direction getExtractDirection()
	{
		return relativeExtractDir.rotate(getFacing());
	}
}
