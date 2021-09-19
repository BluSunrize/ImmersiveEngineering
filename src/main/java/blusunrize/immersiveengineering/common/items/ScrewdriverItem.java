/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ITool;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import java.util.Set;

public class ScrewdriverItem extends IEBaseItem implements ITool
{
	public static final ToolType SCREWDRIVER_TOOL = ToolType.get(ImmersiveEngineering.MODID+"_screwdriver");

	public ScrewdriverItem()
	{
		super("screwdriver", new Properties());
	}

	@Nonnull
	@Override
	public Set<ToolType> getToolTypes(ItemStack stack)
	{
		return ImmutableSet.of(SCREWDRIVER_TOOL);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
	{
		return true;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}