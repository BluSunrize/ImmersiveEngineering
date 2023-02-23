/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper.ItemEnergyStorage;
import blusunrize.immersiveengineering.common.util.SimpleCapProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class BlockItemCapacitor extends BlockItemIE
{
	private final CapacitorConfig configValues;

	public BlockItemCapacitor(Block b, CapacitorConfig configValues)
	{
		super(b, new Properties());
		this.configValues = configValues;
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
	{

		if(!stack.isEmpty())
			return new SimpleCapProvider<>(
					() -> ForgeCapabilities.ENERGY, new ItemEnergyStorage(stack, value -> configValues.storage.getAsInt())
			);
		else
			return super.initCapabilities(stack, nbt);
	}
}
