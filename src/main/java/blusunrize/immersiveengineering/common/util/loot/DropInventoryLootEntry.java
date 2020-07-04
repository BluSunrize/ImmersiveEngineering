package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class DropInventoryLootEntry extends StandaloneLootEntry
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "drop_inv");

	protected DropInventoryLootEntry(int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void func_216154_a(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.has(LootParameters.BLOCK_ENTITY))
		{
			TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
			if(te instanceof IGeneralMultiblock)
				te = (TileEntity)((IGeneralMultiblock)te).master();
			if(te instanceof IIEInventory&&((IIEInventory)te).getDroppedItems()!=null)
				((IIEInventory)te).getDroppedItems().forEach(output);
			else if(te!=null)
			{
				LazyOptional<IItemHandler> itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				itemHandler.ifPresent((h) ->
				{
					if(h instanceof IEInventoryHandler)
					{
						for(int i = 0; i < h.getSlots(); i++)
							if(!h.getStackInSlot(i).isEmpty())
							{
								output.accept(h.getStackInSlot(i));
								((IEInventoryHandler)h).setStackInSlot(i, ItemStack.EMPTY);
							}
					}
				});
			}
		}
	}

	public static StandaloneLootEntry.Builder<?> builder()
	{
		return builder(DropInventoryLootEntry::new);
	}

	@Nonnull
	@Override
	public LootPoolEntryType func_230420_a_()
	{
		return IELootFunctions.dropInventory;
	}

	public static class Serializer extends StandaloneLootEntry.Serializer<DropInventoryLootEntry>
	{
		@Nonnull
		@Override
		protected DropInventoryLootEntry func_212829_b_(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull ILootCondition[] conditions,
				@Nonnull ILootFunction[] functions
		)
		{
			return new DropInventoryLootEntry(weight, quality, conditions, functions);
		}
	}

}
