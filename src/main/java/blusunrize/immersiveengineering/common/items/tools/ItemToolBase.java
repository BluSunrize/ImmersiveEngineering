package blusunrize.immersiveengineering.common.items.tools;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemToolBase extends ItemTool
{
	private final ImmutableSet<ToolType> toolClasses;
	private final ResourceLocation oreDict;

	protected ItemToolBase(IItemTier materialIn, String name, ToolType toolclass, ResourceLocation repairMaterial,
						   Set<Block> effectiveBlocksIn, float attackDamageIn, float attackSpeedIn)
	{
		super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn, new Properties().maxStackSize(1).group(ImmersiveEngineering.itemGroup));

		this.toolClasses = ImmutableSet.of(toolclass);
		this.oreDict = repairMaterial;
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
	{
		int level = super.getHarvestLevel(stack, tool, player, blockState);
		if(level==-1&&toolClasses.contains(tool))
			return getTier().getHarvestLevel();
		else
			return level;
	}

	@Override
	public Set<ToolType> getToolTypes(ItemStack stack)
	{
		return toolClasses;
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack)
	{
		if(this.oreDict!=null)
			return Utils.isInTag(stack, oreDict);
		return false;
	}
}
