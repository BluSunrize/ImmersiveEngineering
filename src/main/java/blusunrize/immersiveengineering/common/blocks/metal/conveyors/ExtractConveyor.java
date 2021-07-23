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
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.OptionalDouble;
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
	private double extension = -1;
	private Rotation relativeExtractDir = Rotation.CLOCKWISE_180;

	public ExtractConveyor(BlockEntity tile)
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
		Transformation tMatrix = matrix.toTransformationMatrix();
		final double extend = getCurrentExtension();
		this.extension = extend;

		Function<Direction, TextureAtlasSprite> getCasingSprite = enumFacing -> enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

		Function<Vec3[], Vec3[]> vertexTransformer = vertices -> {
			if(extend==0)
				return vertices;
			Vec3[] ret = new Vec3[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vec3(vertices[i].x, vertices[i].y, vertices[i].z-extend);
			return ret;
		};
		Function<Vec3[], Vec3[]> casingTransformer = vertices -> {
			Vec3[] ret = new Vec3[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vec3(vertices[i].x, vertices[i].y-.25f, vertices[i].z-.625f-extend);
			return ret;
		};

		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.0625f, .375f, .625f), new Vec3(.1875f, 1f, 1f), matrix, getFacing(), casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.8125f, .375f, .625f), new Vec3(.9375f, 1f, 1f), matrix, getFacing(), casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.1875f, .875f, .625f), new Vec3(.8125f, 1f, 1f), matrix, getFacing(), casingTransformer, getCasingSprite, colour));

		if(getTile()!=null&&extend > 0)
		{
			TextureAtlasSprite tex_conveyor = ClientUtils.getSprite(isActive()?BasicConveyor.texture_on: BasicConveyor.texture_off);
			Function<Direction, TextureAtlasSprite> getExtensionSprite = enumFacing -> enumFacing.getAxis()==Axis.Y?null: enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

			Vec3[] vertices = {new Vec3(.0625f, 0, -extend), new Vec3(.0625f, 0, 0), new Vec3(.9375f, 0, 0), new Vec3(.9375f, 0, -extend)};
			baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, getFacing()), tex_conveyor, new double[]{15, extend*16, 1, 0}, colour, true));
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = Utils.withCoordinate(vertices[i], Axis.Y, .125);
			baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, getFacing()), tex_conveyor, new double[]{15, (1-extend)*16, 1, 16}, colour, false));
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.0625f, .25f, .625f), new Vec3(.9375f, .375f, .625f+extend), matrix, getFacing(), casingTransformer, getExtensionSprite, colour));
		}


		Vec3[] vertices = new Vec3[]{new Vec3(.8125f, .625f, .03125f), new Vec3(.8125f, .125f, .03125f), new Vec3(.1875f, .125f, .03125f), new Vec3(.1875f, .625f, .03125f)};
		baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, getFacing()), texture_assembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = Utils.withCoordinate(vertices[i], Axis.Z, .0625);
		baseModel.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(tMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, getFacing()), texture_assembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));

		for(int i = 0; i < 5; i++)
		{
			float off = i*.125f;
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.203125f+off, .1875f, .09375f), new Vec3(.296875f+off, .625f, .125f), matrix, getFacing(), vertexTransformer, (facing1) -> texture_curtain, colour));
		}

		super.modifyQuads(baseModel);

		return baseModel;
	}

	@Override
	public String getModelCacheKey()
	{
		String key = super.getModelCacheKey();
		key += "e"+this.getExtractDirection().ordinal();
		key += "ex"+getCurrentExtension();
		return key;
	}

	@Override
	public boolean renderWall(Direction facing, int wall)
	{
		Direction side = wall==0?facing.getCounterClockWise(): facing.getClockWise();
		return side!=this.getExtractDirection()&&super.renderWall(facing, wall);
	}

	private boolean extensionRecursionLock = false;
	private static final VoxelShape ALLOWED_MISSING_SHAPE = Shapes.joinUnoptimized(
			Shapes.box(1/16., 1/16., 1/16., 15/16., 15/16., 15/16.),
			Shapes.block(),
			BooleanOp.NOT_SAME
	);

	/**
	 * @return empty if the correct value can not be computed at this time. In this case, assume 0, but do not cache.
	 * Otherwise an optional of the correct extension length.
	 */
	private OptionalDouble getExtensionIntoBlock(BlockEntity tile)
	{
		if(tile==null||!tile.hasLevel()||extensionRecursionLock)
			return OptionalDouble.empty();
		extensionRecursionLock = true;
		double extend = 0;

		Level world = tile.getLevel();
		BlockPos neighbour = tile.getBlockPos().relative(this.getExtractDirection());
		if(!world.isEmptyBlock(neighbour))
		{
			BlockState connected = world.getBlockState(neighbour);
			BlockEntity connectedTile = world.getBlockEntity(neighbour);
			if(connectedTile!=null&&connectedTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getExtractDirection().getOpposite()).isPresent())
			{
				VoxelShape connectedShape = connected.getShape(world, neighbour);
				VoxelShape projected = connectedShape.getFaceShape(this.getExtractDirection().getOpposite());
				if(Shapes.joinIsNotEmpty(projected, ALLOWED_MISSING_SHAPE, BooleanOp.OR))
				{
					AABB aabb = connectedShape.bounds();
					switch(getExtractDirection())
					{
						case NORTH:
							extend = 1-aabb.maxZ;
							break;
						case SOUTH:
							extend = aabb.minZ;
							break;
						case WEST:
							extend = 1-aabb.maxX;
							break;
						case EAST:
							extend = aabb.minX;
							break;
					}
					if(extend > .25)
						extend = 0.25;
					double round = extend%.0625;
					if(round < extend)
						extend = round+.0625;
				}
			}
		}
		extensionRecursionLock = false;
		return OptionalDouble.of(extend);
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public boolean isTicking()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		if(!getTile().getLevel().isClientSide)
		{
			if(this.transferCooldown > 0)
			{
				this.transferCooldown--;
			}
			if(!isPowered()&&this.transferCooldown <= 0)
			{
				Level world = getTile().getLevel();
				BlockPos neighbour = getTile().getBlockPos().relative(this.getExtractDirection());
				if(!world.isEmptyBlock(neighbour))
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
										getTile().getBlockPos().getX()+.5,
										getTile().getBlockPos().getY()+.1875,
										getTile().getBlockPos().getZ()+.5, extractItem);
								entity.setDeltaMovement(Vec3.ZERO);
								world.addFreshEntity(entity);
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
	public boolean playerInteraction(Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		if(Utils.isHammer(heldItem)&&player.isShiftKeyDown())
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
			player.displayClientMessage(new TranslatableComponent(Lib.CHAT_INFO+"tickrate", this.transferTickrate), true);
			return true;
		}
		return false;
	}

	private double getCurrentExtension()
	{
		double extension;
		if(this.extension >= 0)
			extension = this.extension;
		else
		{
			OptionalDouble optValue = getExtensionIntoBlock(getTile());
			if(optValue.isPresent())
				extension = this.extension = optValue.getAsDouble();
			else
				extension = 0;
		}
		return extension;
	}

	@Override
	public VoxelShape getSelectionShape()
	{
		VoxelShape ret = conveyorBounds;
		double extension = getCurrentExtension();
		VoxelShape extensionShape = null;
		switch(getExtractDirection())
		{
			case NORTH:
				extensionShape = Shapes.box(.0625, .125, -extension, .9375, .75, .375-extension);
				break;
			case SOUTH:
				extensionShape = Shapes.box(.0625, .125, .625+extension, .9375, .75, 1+extension);
				break;
			case WEST:
				extensionShape = Shapes.box(-extension, .125, .0625, .375-extension, .75, .9375);
				break;
			case EAST:
				extensionShape = Shapes.box(.625+extension, .125, .0625, 1+extension, .75, .9375);
				break;
		}
		if(extensionShape!=null)
			ret = Shapes.join(ret, extensionShape, BooleanOp.OR);
		return ret;
	}

	@Override
	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = super.writeConveyorNBT();
		nbt.putInt("transferCooldown", transferCooldown);
		nbt.putInt("transferTickrate", transferTickrate);
		nbt.putInt("relativeExtractDir", relativeExtractDir.ordinal());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundTag nbt)
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
