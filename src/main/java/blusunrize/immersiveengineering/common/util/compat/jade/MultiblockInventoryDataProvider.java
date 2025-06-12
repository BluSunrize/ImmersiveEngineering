/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.IFurnaceEnvironment;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SiloLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.*;

import java.util.List;

public class MultiblockInventoryDataProvider<T extends IMultiblockState> implements IServerExtensionProvider<ItemStack>, IClientExtensionProvider<ItemStack, ItemView>
{

	public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor)
	{
		if(accessor.getTarget() instanceof IMultiblockBE<?> multiblockBE)
		{
			final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
			List<ItemStack> list = null;
//			if(helper.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent())
//			{
//				helper.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(inv -> list.add(JadeForgeUtils.fromItemHandler(inv, 100, 0)));
//			}
//			else
//
			if(helper.getState() instanceof ProcessContext<?> state)
				list = getFromInventory(state.getInventory());
			else if(helper.getState() instanceof IFurnaceEnvironment<?> state)
				list = getFromInventory(state.getInventory());
			else if(helper.getState() instanceof SiloLogic.State state)
				list = List.of(new ItemStack(state.identStack.getItem(), state.storageAmount));

			if(list!=null)
				return List.of(new ViewGroup<>(list));
		}
		return null;
	}

	private static List<ItemStack> getFromInventory(IItemHandlerModifiable inventory)
	{
		List<ItemStack> list = Lists.newArrayList();
		for(int iSlot = 0; iSlot < inventory.getSlots(); iSlot++)
		{
			ItemStack stack = inventory.getStackInSlot(iSlot);
			if(!stack.isEmpty())
			{
				stack = stack.copy();
//						CustomData customData = (CustomData)((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).update(NbtOps.INSTANCE, COOKING_TIME_CODEC, campfire.cookingTime[i] - campfire.cookingProgress[i]).getOrThrow();
//						stack.set(DataComponents.CUSTOM_DATA, customData);
				list.add(stack);
			}
		}
		return list;
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> list)
	{
		return ClientViewGroup.map(list, ItemView::new, null);
	}

	@Override
	public ResourceLocation getUid()
	{
		return ImmersiveEngineering.rl("multiblock_inventory");
	}
}
