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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.item.BuzzsawCallbacks.Key;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.SawbladeItem;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuzzsawCallbacks implements ItemCallback<Key>
{
	public static final BuzzsawCallbacks INSTANCE = new BuzzsawCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		var upgrades = BuzzsawItem.getUpgradesStatic(stack);
		final boolean hasQuiver = BuzzsawItem.hasQuiverUpgrade(stack);
		final int numBlades = hasQuiver?3: 1;
		List<ResourceLocation> bladeTextures = new ArrayList<>();
		for(int i = 0; i < numBlades; ++i)
		{
			ItemStack sawblade = BuzzsawItem.getSawblade(stack, i);
			if(sawblade.getItem() instanceof SawbladeItem sawbladeItem)
				bladeTextures.add(sawbladeItem.getSawbladeTexture());
			else
				bladeTextures.add(null);
		}
		return new Key(bladeTextures, hasQuiver, upgrades.has(UpgradeEffect.OILED));
	}

	@Override
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		if("blade".equals(material))
		{
			int spare = "upgrade_blades1".equals(group)?1: "upgrade_blades2".equals(group)?2: 0;
			ResourceLocation rl = key.bladeTexture.get(spare);
			if(rl!=null)
				return ClientUtils.getSprite(rl);
		}
		return null;
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if("body".equals(group))
			return true;
		if("blade".equals(group))
			return key.bladeTexture.get(0)!=null;

		if("upgrade_lube".equals(group))
			return key.oiled();
		if("upgrade_launcher".equals(group)||"upgrade_waterproof".equals(group))
			// TODO remove from model?
			return false;
		if("upgrade_blades0".equals(group))
			return key.hasQuiver();
		if("upgrade_blades1".equals(group))
			return key.hasQuiver()&&key.bladeTexture().get(1)!=null;
		if("upgrade_blades2".equals(group))
			return key.hasQuiver()&&key.bladeTexture().get(2)!=null;
		return true;
	}

	private static final List<List<String>> GROUP_BLADE = List.of(List.of("blade"));

	@Override
	public List<List<String>> getSpecialGroups(ItemStack stack, ItemDisplayContext transform, LivingEntity entity)
	{
		return GROUP_BLADE;
	}

	private static final Transformation MAT_FIXED = new Transformation(new Vector3f(0.60945f, 0, 0), null, null, null);

	@Nonnull
	@Override
	public Transformation getTransformForGroups(ItemStack stack, List<String> groups, ItemDisplayContext transform, LivingEntity entity, float partialTicks)
	{
		if(!DrillCallbacks.shouldRotate(Tools.BUZZSAW, entity, stack, transform))
			return MAT_FIXED;
		float ticksPerRotation = 10f;
		float angle = (entity.tickCount%ticksPerRotation+partialTicks)/ticksPerRotation*(float)(2*Math.PI);
		return new Transformation(
				new Vector3f(0.60945f, 0, 0),
				new Quaternionf().rotateXYZ(0, angle, 0),
				null, null);
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(Arrays.asList(null, null, null), false, false);
	}

	public record Key(List<ResourceLocation> bladeTexture, boolean hasQuiver, boolean oiled)
	{
	}
}
