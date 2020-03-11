/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 29.03.2017
 */
public class CoveredConveyor extends BasicConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "covered");
	public static ArrayList<Predicate<ItemStack>> validCoveyorCovers = new ArrayList<>();

	static
	{
		//TODO use blocks rather than itemstacks
		final ArrayList<ItemStack> scaffolds = Lists.newArrayList(new ItemStack(WoodenDecoration.treatedScaffolding));
		Stream.concat(
				MetalDecoration.aluScaffolding.values().stream(),
				MetalDecoration.steelScaffolding.values().stream()
		)
				.map(ItemStack::new)
				.forEach(scaffolds::add);

		validCoveyorCovers.add(input ->
		{
			if(input==null)
				return false;
			for(ItemStack stack : scaffolds)
				if(ItemStack.areItemsEqual(stack, input))
					return true;
			return false;
		});
		//TODO tag once one exists?
		validCoveyorCovers.add(input -> input.getItem()==Item.getItemFromBlock(Blocks.GLASS));
	}

	public ItemStack cover = ItemStack.EMPTY;

	public CoveredConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if(entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	@Override
	public void onItemDeployed(ItemEntity entity)
	{
		entity.setPickupDelay(10);
	}

	@Override
	public String getModelCacheKey()
	{
		String key = super.getModelCacheKey();
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName();
		return key;
	}

	private static ItemStack defaultCover;

	public static ItemStack getDefaultCover()
	{
		if(defaultCover==null)
			defaultCover = new ItemStack(MetalDecoration.steelScaffolding.get(
					MetalScaffoldingType.STANDARD));
		return defaultCover;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		addCoverToQuads(baseModel, getTile(), getFacing(), () -> this.cover, getConveyorDirection(), new boolean[]{
				getTile()==null||this.renderWall(getFacing(), 0), getTile()==null||this.renderWall(getFacing(), 1)
		});
		return baseModel;
	}

	static void addCoverToQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, Direction facing, Supplier<ItemStack> coverGet, ConveyorDirection conDir, boolean[] walls)
	{
		ItemStack cover = !coverGet.get().isEmpty()?coverGet.get(): getDefaultCover();
		Block b = Block.getBlockFromItem(cover.getItem());
		BlockState state = !cover.isEmpty()?b.getDefaultState(): Blocks.STONE.getDefaultState();
		IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
		if(model!=null)
		{
			TextureAtlasSprite sprite = model.getParticleTexture(EmptyModelData.INSTANCE);
			HashMap<Direction, TextureAtlasSprite> sprites = new HashMap<>();

			for(Direction f : Direction.VALUES)
				for(BakedQuad q : model.getQuads(state, f, Utils.RAND))
					if(q!=null&&q.getSprite()!=null)
						sprites.put(f, q.getSprite());
			for(BakedQuad q : model.getQuads(state, null, Utils.RAND))
				if(q!=null&&q.getSprite()!=null&&q.getFace()!=null)
					sprites.put(q.getFace(), q.getSprite());

			Function<Direction, TextureAtlasSprite> getSprite = f -> sprites.containsKey(f)?sprites.get(f): sprite;
			Function<Direction, TextureAtlasSprite> getSpriteHorizontal = f -> f.getAxis()==Axis.Y?null: sprites.containsKey(f)?sprites.get(f): sprite;

			float[] colour = {1, 1, 1, 1};
			Matrix4 matrix = new Matrix4(facing);

			Function<Vec3d[], Vec3d[]> vertexTransformer = conDir==ConveyorDirection.HORIZONTAL?vertices -> vertices: vertices -> {
				Vec3d[] ret = new Vec3d[vertices.length];
				for(int i = 0; i < ret.length; i++)
					ret[i] = new Vec3d(vertices[i].x, vertices[i].y+(vertices[i].z==(conDir==ConveyorDirection.UP?0: 1)?1: 0), vertices[i].z);
				return ret;
			};

			baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .75f, 0), new Vec3d(1, 1, 1), matrix, facing, vertexTransformer, getSprite, colour));
			if(walls[0])
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, 0), new Vec3d(.0625f, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, 0), new Vec3d(.0625f, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, .9375f), new Vec3d(.0625f, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}
			if(walls[1])
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, 0), new Vec3d(1, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, 0), new Vec3d(1, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, .9375f), new Vec3d(1, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}
		}
	}


	@Override
	public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		return handleCoverInteraction(getTile(), player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static boolean handleCoverInteraction(TileEntity tile, PlayerEntity player, Hand hand, ItemStack heldItem, Supplier<ItemStack> coverGet, Consumer<ItemStack> coverSet)
	{
		final ItemStack cover = coverGet.get();
		if(heldItem.isEmpty()&&player.isSneaking()&&!cover.isEmpty())
		{
			if(!tile.getWorld().isRemote&&tile.getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
			{
				ItemEntity entityitem = player.dropItem(cover.copy(), false);
				if(entityitem!=null)
					entityitem.setNoPickupDelay();
			}
			coverSet.accept(ItemStack.EMPTY);
			return true;
		}
		else if(!heldItem.isEmpty()&&!player.isSneaking())
			for(Predicate<ItemStack> func : validCoveyorCovers)
				if(func.test(heldItem))
				{
					if(!ItemHandlerHelper.canItemStacksStack(cover, heldItem))
					{
						if(!tile.getWorld().isRemote&&!cover.isEmpty()&&tile.getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
						{
							ItemEntity entityitem = player.dropItem(cover.copy(), false);
							if(entityitem!=null)
								entityitem.setNoPickupDelay();
						}
						coverSet.accept(Utils.copyStackWithAmount(heldItem, 1));
						heldItem.shrink(1);
						if(heldItem.getCount() <= 0)
							player.setHeldItem(hand, heldItem);
						return true;
					}
				}
		return false;
	}

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes()
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		if(getConveyorDirection()==ConveyorDirection.HORIZONTAL)
			list.add(topBox);
		else
		{
			boolean up = getConveyorDirection()==ConveyorDirection.UP;
			list.add(new AxisAlignedBB((getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 0, 1.75, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 0, (getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 1, 2, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 1));
			list.add(new AxisAlignedBB((getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 0, 1.25, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 0, (getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 1, 1.5, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 1));
		}
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes()
	{
		if(getConveyorDirection()==ConveyorDirection.HORIZONTAL)
			return Lists.newArrayList(VoxelShapes.fullCube().getBoundingBox());
		else
		{
			boolean up = getConveyorDirection()==ConveyorDirection.UP;
			return Lists.newArrayList(
					new AxisAlignedBB((getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 0, .5, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 0, (getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 1, 2, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 1),
					new AxisAlignedBB((getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 0, 0, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 0, (getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 1, 1.5, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 1));
		}
	}

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		if(cover!=null)
			nbt.put("cover", cover.write(new CompoundNBT()));
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		cover = ItemStack.read(nbt.getCompound("cover"));
	}
}