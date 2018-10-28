package blusunrize.immersiveengineering.common.items.tools;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

import java.util.Set;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemToolBase extends ItemTool
{
	protected static final Set<Block> PICKAXE_EFFECTIVE = Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
	protected static final Set<Block> AXE_EFFECTIVE = Sets.newHashSet(Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE);
	protected static final Set<Block> SHOVEL_EFFECTIVE = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER);

	private final Set<String> toolClasses;
	private final String oreDict;

	protected ItemToolBase(ToolMaterial materialIn, String name, String toolclass, String oreDict, Set<Block> effectiveBlocksIn, float attackDamageIn, float attackSpeedIn)
	{
		super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);

		this.toolClasses = ImmutableSet.of(toolclass);
		this.oreDict = oreDict;
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, @javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player, @javax.annotation.Nullable IBlockState blockState)
	{
		int level = super.getHarvestLevel(stack, toolClass, player, blockState);
		if(level==-1&&toolClasses.contains(toolClass))
			return this.toolMaterial.getHarvestLevel();
		else
			return level;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return toolClasses;
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack)
	{
		if(this.oreDict!=null)
			return Utils.compareToOreName(stack, oreDict);
		return false;
	}
}
