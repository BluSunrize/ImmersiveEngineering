/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.items.PowerpackItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class CuriosCompatModule extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public void sendIMCs()
	{
		InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
				() -> SlotTypePreset.BACK.getMessageBuilder().build());
	}

	public static ItemStack getPowerpack(LivingEntity living)
	{
		return getCuriosIfVisible(living, SlotTypePreset.BACK, stack -> stack.getItem() instanceof PowerpackItem);
	}

	public static ItemStack getCuriosIfVisible(LivingEntity living, SlotTypePreset slot, Predicate<ItemStack> predicate)
	{
		AtomicReference<ItemStack> ret = new AtomicReference<>(ItemStack.EMPTY);
		LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(living);
		optional.ifPresent(itemHandler -> {
			Optional<ICurioStacksHandler> stacksOptional = itemHandler.getStacksHandler(slot.getIdentifier());
			stacksOptional.ifPresent(stacksHandler -> {
				if(stacksHandler.isVisible())
				{
					for(int i = 0; i < stacksHandler.getSlots(); i++)
						if(stacksHandler.getRenders().get(i))
						{
							ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
							if(predicate.test(stack))
								ret.set(stack);
						}
				}
			});
		});
		return ret.get();
	}

}