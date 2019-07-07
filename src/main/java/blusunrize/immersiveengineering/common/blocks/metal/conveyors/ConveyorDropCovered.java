/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author BluSunrize - 17.02.2019
 */
public class ConveyorDropCovered extends ConveyorDrop
{
	public ItemStack cover = ItemStack.EMPTY;

	@Override
	public void onEntityCollision(TileEntity tile, Entity entity, Direction facing)
	{
		super.onEntityCollision(tile, entity, facing);
		if(entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	@Override
	public void onItemDeployed(TileEntity tile, ItemEntity entity, Direction facing)
	{
		entity.setPickupDelay(10);
		ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, Direction facing)
	{
		baseModel = super.modifyQuads(baseModel, tile, facing);
		ConveyorCovered.addCoverToQuads(baseModel, tile, facing, () -> cover, ConveyorDirection.HORIZONTAL, new boolean[]{
				tile==null||this.renderWall(tile, facing, 0), tile==null||this.renderWall(tile, facing, 1)
		});
		return baseModel;
	}

	@Override
	public String getModelCacheKey(TileEntity tile, Direction facing)
	{
		String key = super.getModelCacheKey(tile, facing);
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName()+cover.getMetadata();
		return key;
	}


	@Override
	public boolean playerInteraction(TileEntity tile, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(tile, player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		return ConveyorCovered.handleCoverInteraction(tile, player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, Direction facing)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		list.add(topBox);
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, Direction facing)
	{
		return Lists.newArrayList(Block.FULL_BLOCK_AABB);
	}

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		if(cover!=null)
			nbt.setTag("cover", cover.writeToNBT(new CompoundNBT()));
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		cover = new ItemStack(nbt.getCompound("cover"));
	}
}
