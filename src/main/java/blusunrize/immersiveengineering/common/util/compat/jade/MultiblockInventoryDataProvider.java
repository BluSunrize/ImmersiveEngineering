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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.*;
import snownee.jade.util.JadeForgeUtils;

import java.util.ArrayList;
import java.util.List;

public class MultiblockInventoryDataProvider<T extends IMultiblockState> implements IServerExtensionProvider<IMultiblockBE<T>, ItemStack>, IClientExtensionProvider<ItemStack, ItemView>
{
	@Override
	public @Nullable List<ViewGroup<ItemStack>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel, IMultiblockBE<T> multiblockBE, boolean b)
	{
		final IMultiblockBEHelper<T> helper = multiblockBE.getHelper();
		List<ViewGroup<ItemStack>> list = new ArrayList<>();
		if(helper.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent())
		{
			helper.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(inv -> list.add(JadeForgeUtils.fromItemHandler(inv, 100, 0)));
		}
		else if(helper.getState() instanceof ProcessContext<?> state)
		{
			IItemHandler inventory = state.getInventory();
			return List.of(JadeForgeUtils.fromItemHandler(inventory, 100, 0));
		}
		else if(helper.getState() instanceof IFurnaceEnvironment<?> state)
		{
			IItemHandler inventory = state.getInventory();
			return List.of(JadeForgeUtils.fromItemHandler(inventory, 100, 0));
		}
		else if(helper.getState() instanceof SiloLogic.State state)
		{
			return List.of(new ViewGroup<>(List.of(new ItemStack(state.identStack.getItem(), state.storageAmount))));
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
