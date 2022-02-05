/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules.StandardIECompatModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.function.Predicate;

public class CuriosCompatModule extends StandardIECompatModule
{
	@Override
	public void sendIMCs()
	{
		InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
				() -> SlotTypePreset.BACK.getMessageBuilder().build());
		InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
				() -> SlotTypePreset.HEAD.getMessageBuilder().build());
		PowerpackItem.POWERPACK_GETTER.addGetter(living -> getCuriosIfVisible(
				living, SlotTypePreset.BACK, stack -> stack.getItem() instanceof PowerpackItem
		));
		EarmuffsItem.EARMUFF_GETTERS.addGetter(living -> getCuriosIfVisible(
				living, SlotTypePreset.HEAD, stack -> stack.getItem() instanceof EarmuffsItem
		));
	}

	public static ItemStack getCuriosIfVisible(LivingEntity living, SlotTypePreset slot, Predicate<ItemStack> predicate)
	{
		LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(living);
		return optional.resolve()
				.flatMap(handler -> handler.getStacksHandler(slot.getIdentifier()))
				.filter(ICurioStacksHandler::isVisible)
				.map(stacksHandler -> {
					for(int i = 0; i < stacksHandler.getSlots(); i++)
						if(stacksHandler.getRenders().get(i))
						{
							ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
							if(predicate.test(stack))
								return stack;
						}
					return ItemStack.EMPTY;
				}).orElse(ItemStack.EMPTY);
	}

}