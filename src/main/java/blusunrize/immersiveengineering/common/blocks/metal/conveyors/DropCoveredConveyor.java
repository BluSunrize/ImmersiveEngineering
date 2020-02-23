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
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 17.02.2019
 */
public class DropCoveredConveyor extends DropConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "droppercovered");
	public ItemStack cover = ItemStack.EMPTY;

	public DropCoveredConveyor(TileEntity tile)
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
		ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)getTile());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		baseModel = super.modifyQuads(baseModel);
		CoveredConveyor.addCoverToQuads(baseModel, getTile(), getFacing(), () -> cover, ConveyorDirection.HORIZONTAL, new boolean[]{
				getTile()==null||this.renderWall(getFacing(), 0), getTile()==null||this.renderWall(getFacing(), 1)
		});
		return baseModel;
	}

	@Override
	public String getModelCacheKey()
	{
		String key = super.getModelCacheKey();
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName();
		return key;
	}


	@Override
	public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		return CoveredConveyor.handleCoverInteraction(getTile(), player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes()
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		list.add(topBox);
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes()
	{
		return Lists.newArrayList(FULL_BLOCK);
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
