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
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.client.models.obj.callback.item.RailgunCallbacks.Key;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class RailgunCallbacks implements ItemCallback<Key>
{
	public static RailgunCallbacks INSTANCE = new RailgunCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		var upgrades = RailgunItem.getUpgradesStatic(stack);
		return new Key(upgrades.has(UpgradeEffect.SCOPE), upgrades.get(UpgradeEffect.SPEED) > 0);
	}

	@Override
	public boolean shouldRenderGroup(Key stack, String group, RenderType layer)
	{
		if(group.equals("upgrade_scope"))
			return stack.scope();
		if(group.equals("upgrade_speed"))
			return stack.speed();
		if(group.equals("barrel_top"))
			return !stack.speed();
		return true;
	}

	@Override
	public void handlePerspective(Key key, LivingEntity holder, ItemDisplayContext cameraTransformType, PoseStack mat)
	{
		if(ZoomHandler.isZooming && (cameraTransformType==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||cameraTransformType==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND))
		{
			// hide the item in first person when zooming
			mat.scale(0f,0f,0f);
			return;
		}

		if(holder instanceof Fusilier fusilier&&(cameraTransformType==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND||cameraTransformType==ItemDisplayContext.THIRD_PERSON_LEFT_HAND))
		{
			switch(fusilier.getArmPose())
			{
				case CROSSBOW_HOLD ->
				{
					mat.mulPose(new Quaternionf().rotateXYZ(-0.174533f, -0.261799f, -0.698132f));
					mat.translate(.5, -2.5, .75);
				}
				case CELEBRATING ->
				{
					mat.translate(-4.5, -4, 2);
					mat.mulPose(new Quaternionf().rotateY(-1.65806f));
					mat.mulPose(new Quaternionf().rotateZ(-0.872665f));
					mat.mulPose(new Quaternionf().rotateX(0.349066f));
				}
				default ->
				{
					mat.mulPose(new Quaternionf().rotateXYZ(-0.139626f, 0, 1.91986f));
					mat.translate(4.75, 4, 0);
				}
			}
		}
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(false, false);
	}

	public record Key(boolean scope, boolean speed)
	{
	}
}
