package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.StandaloneLootEntry;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MBOriginalBlockLootEntry extends StandaloneLootEntry
{
	protected MBOriginalBlockLootEntry(int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void func_216154_a(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.has(LootParameters.BLOCK_ENTITY))
		{
			TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
			if(te instanceof MultiblockPartTileEntity)
			{
				MultiblockPartTileEntity<?> multiblockTile = (MultiblockPartTileEntity<?>)te;
				Utils.getDrops(multiblockTile.getOriginalBlock(),
						new LootContext.Builder(context.getWorld())
								.withParameter(LootParameters.TOOL, context.get(LootParameters.TOOL))
								.withParameter(LootParameters.POSITION, context.get(LootParameters.POSITION))
				).forEach(output);
			}
		}
	}

	public static StandaloneLootEntry.Builder<?> builder()
	{
		return builder(MBOriginalBlockLootEntry::new);
	}

	public static class Serializer extends StandaloneLootEntry.Serializer<MBOriginalBlockLootEntry>
	{
		public Serializer()
		{
			super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblock_original_block"), MBOriginalBlockLootEntry.class);
		}

		@Nonnull
		@Override
		protected MBOriginalBlockLootEntry func_212829_b_(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull ILootCondition[] conditions,
				@Nonnull ILootFunction[] functions
		)
		{
			return new MBOriginalBlockLootEntry(weight, quality, conditions, functions);
		}
	}

}
