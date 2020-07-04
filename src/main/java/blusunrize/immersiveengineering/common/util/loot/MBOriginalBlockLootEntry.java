package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
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

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MBOriginalBlockLootEntry extends StandaloneLootEntry
{
	public static ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "multiblock_original_block");

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

	@Override
	public LootPoolEntryType func_230420_a_()
	{
		return IELootFunctions.multiblockOrigBlock;
	}

	public static class Serializer extends StandaloneLootEntry.Serializer<MBOriginalBlockLootEntry>
	{
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
