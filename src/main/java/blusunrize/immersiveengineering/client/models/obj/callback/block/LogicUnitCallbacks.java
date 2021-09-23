/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.blocks.wooden.LogicUnitBlockEntity;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nonnull;
import java.util.List;

public class LogicUnitCallbacks implements BlockCallback<LogicUnitCallbacks.Key>
{
	public static final LogicUnitCallbacks INSTANCE = new LogicUnitCallbacks();

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		RenderType renderType = MinecraftForgeClient.getRenderLayer();
		if(!(blockEntity instanceof LogicUnitBlockEntity logicUnit))
			return new Key(renderType, IntLists.EMPTY_LIST);
		IntList nonEmptySlots = new IntArrayList();
		NonNullList<ItemStack> inventory = logicUnit.getInventory();
		for(int i = 0; i < inventory.size(); i++)
			if(!inventory.get(i).isEmpty())
				nonEmptySlots.add(i);
		return new Key(renderType, nonEmptySlots);
	}

	private static final IEObjState visibilityTransparent = new IEObjState(VisibilityList.show("tubes"));

	@Override
	public IEObjState getIEOBJState(Key key)
	{
		if(key.renderType()==RenderType.translucent())
			return visibilityTransparent;
		List<String> parts = Lists.newArrayList("base");
		for(int i : key.presentBoards())
			parts.add("board_"+i);
		return new IEObjState(VisibilityList.show(parts));
	}

	//TODO test rendertype caching
	public record Key(RenderType renderType, IntList presentBoards)
	{
	}
}
