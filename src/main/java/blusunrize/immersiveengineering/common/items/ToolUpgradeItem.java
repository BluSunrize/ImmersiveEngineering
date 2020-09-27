/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ToolUpgradeItem extends IEBaseItem implements IUpgrade
{
	private final ToolUpgrade type;

	public ToolUpgradeItem(ToolUpgrade type)
	{
		super("toolupgrade_"+type.name().toLowerCase(), new Properties().maxStackSize(1));
		this.type = type;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(
					I18n.format(Lib.DESC_FLAVOUR+getRegistryName().getPath()), 200);
			for(String s : flavour)
				list.add(new StringTextComponent(s));
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return type.stackSize;
	}

	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade)
	{
		return type.toolset;
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade)
	{
		BiPredicate<ItemStack, ItemStack> check = type.applyCheck;
		if(check!=null&&target.getItem() instanceof IUpgradeableTool)
			return check.test(target, upgrade);
		return true;
	}

	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, CompoundNBT modifications)
	{
		type.function.accept(upgrade, modifications);
	}


	public enum ToolUpgrade
	{
		DRILL_WATERPROOF(ImmutableSet.of("DRILL"), (upgrade, modifications) -> modifications.putBoolean("waterproof", true)),
		DRILL_LUBE(ImmutableSet.of("DRILL", "BUZZSAW"), (upgrade, modifications) -> modifications.putBoolean("oiled", true)),
		DRILL_DAMAGE(ImmutableSet.of("DRILL"), 3, (upgrade, modifications) -> {
			ItemNBTHelper.modifyFloat(modifications, "speed", upgrade.getCount()*2f);
			ItemNBTHelper.modifyInt(modifications, "damage", upgrade.getCount());
		}),
		DRILL_CAPACITY(ImmutableSet.of("DRILL", "CHEMTHROWER", "BUZZSAW"), 1, (target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).contains("multitank"), (upgrade, modifications) -> ItemNBTHelper.modifyInt(modifications, "capacity", 2*FluidAttributes.BUCKET_VOLUME)),
		REVOLVER_BAYONET(ImmutableSet.of("REVOLVER"), (upgrade, modifications) -> ItemNBTHelper.modifyFloat(modifications, "melee", 6f)),
		REVOLVER_MAGAZINE(ImmutableSet.of("REVOLVER"), 1, (target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).contains("bullets"), (upgrade, modifications) -> ItemNBTHelper.modifyInt(modifications, "bullets", 6)),
		REVOLVER_ELECTRO(ImmutableSet.of("REVOLVER"), (upgrade, modifications) -> modifications.putBoolean("electro", true)),
		CHEMTHROWER_FOCUS(ImmutableSet.of("CHEMTHROWER"), (upgrade, modifications) -> modifications.putBoolean("focus", true)),
		RAILGUN_SCOPE(ImmutableSet.of("RAILGUN"), (upgrade, modifications) -> modifications.putBoolean("scope", true)),
		RAILGUN_CAPACITORS(ImmutableSet.of("RAILGUN"), (upgrade, modifications) -> modifications.putFloat("speed", 1f)),
		SHIELD_FLASH(ImmutableSet.of("SHIELD"), (upgrade, modifications) -> modifications.putBoolean("flash", true)),
		SHIELD_SHOCK(ImmutableSet.of("SHIELD"), (upgrade, modifications) -> modifications.putBoolean("shock", true)),
		SHIELD_MAGNET(ImmutableSet.of("SHIELD"), (upgrade, modifications) -> modifications.putBoolean("magnet", true)),
		CHEMTHROWER_MULTITANK(ImmutableSet.of("CHEMTHROWER"), 1, (target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).contains("capacity"), (upgrade, modifications) -> modifications.putBoolean("multitank", true)),
		BUZZSAW_SPAREBLADES(ImmutableSet.of("BUZZSAW"), 1, (upgrade, modifications) -> modifications.putBoolean("spareblades", true));

		private ImmutableSet<String> toolset;
		private int stackSize = 1;
		private BiPredicate<ItemStack, ItemStack> applyCheck;
		private BiConsumer<ItemStack, CompoundNBT> function;

		ToolUpgrade(ImmutableSet<String> toolset, BiConsumer<ItemStack, CompoundNBT> function)
		{
			this(toolset, 1, function);
		}

		ToolUpgrade(ImmutableSet<String> toolset, int stackSize, BiConsumer<ItemStack, CompoundNBT> function)
		{
			this(toolset, stackSize, null, function);
		}

		ToolUpgrade(ImmutableSet<String> toolset, int stackSize, BiPredicate<ItemStack, ItemStack> applyCheck, BiConsumer<ItemStack, CompoundNBT> function)
		{
			this.toolset = toolset;
			this.stackSize = stackSize;
			this.applyCheck = applyCheck;
			this.function = function;
		}
	}
}
