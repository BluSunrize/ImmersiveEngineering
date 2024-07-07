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
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.client.models.obj.callback.item.PowerpackCallbacks.Key;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class PowerpackCallbacks implements ItemCallback<Key>
{
	public static final PowerpackCallbacks INSTANCE = new PowerpackCallbacks();

	// 0: Render base & upgrades
	// 1: Render base & upgrades & banner post
	// 2: Render banner for small (shield) textures
	// 3: Render banner for large textures
	public static int THIRD_PERSON_PASS = 0;

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		var upgrades = PowerpackItem.getUpgradesStatic(stack);
		return new Key(
				THIRD_PERSON_PASS,
				upgrades.has(UpgradeEffect.ANTENNA),
				upgrades.has(UpgradeEffect.INDUCTION),
				upgrades.has(UpgradeEffect.TESLA)
		);
	}

	@Override
	public boolean useAbsoluteUV(Key object, String material)
	{
		return "banner".equals(material)||"big_banner".equals(material);
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		// banners only render when specifically enabled
		if(key.third_person_pass==2||"banner".equals(group))
			return key.third_person_pass==2=="banner".equals(group);
		if(key.third_person_pass==3||"big_banner".equals(group))
			return key.third_person_pass==3=="big_banner".equals(group);
		// post is optional
		if("banner_post".equals(group))
			return key.third_person_pass==1;
		if("antenna".equals(group))
			return key.antenna;
		if("induction".equals(group)||"induction_tubes".equals(group))
			return key.induction;
		if("tesla_coil".equals(group))
			return key.tesla;
		// everything else is permitted
		return true;
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(0, false, false, false);
	}

	public record Key(int third_person_pass, boolean antenna, boolean induction, boolean tesla)
	{
	}
}
