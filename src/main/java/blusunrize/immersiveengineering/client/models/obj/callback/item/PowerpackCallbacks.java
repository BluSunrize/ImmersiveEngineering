/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.item.PowerpackCallbacks.Key;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class PowerpackCallbacks implements ItemCallback<Key>
{
	public static final PowerpackCallbacks INSTANCE = new PowerpackCallbacks();

	// If set to 2 will only render the banner. If set to 1 will render the banner_post in addition to other stuff
	// If set to 0, will omit the banner post.
	public static int THIRD_PERSON_PASS = 0;

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		return new Key(THIRD_PERSON_PASS);
	}

	@Override
	public boolean useAbsoluteUV(Key object, String material)
	{
		return "banner".equals(material);
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if("banner".equals(group) && THIRD_PERSON_PASS!=2)
			return false;
		if("banner_post".equals(group))
			return THIRD_PERSON_PASS==1;
		return true;
	}

	public record Key(int third_person_pass)
	{
	}
}
